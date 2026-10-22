package com.letsbuy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.letsbuy.catalog.domain.Category;
import com.letsbuy.catalog.domain.Product;
import com.letsbuy.catalog.domain.ProductStatus;
import com.letsbuy.catalog.persistence.CategoryRepository;
import com.letsbuy.catalog.persistence.ProductRepository;
import com.letsbuy.identity.domain.User;
import com.letsbuy.identity.domain.UserStatus;
import com.letsbuy.identity.persistence.UserRepository;
import com.letsbuy.support.TestEmailSender;
import jakarta.servlet.http.Cookie;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CommerceFlowTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    CategoryRepository categories;

    @Autowired
    ProductRepository products;

    @Autowired
    UserRepository users;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    TestEmailSender email;

    @Test
    void registerLoginRefreshAndRejectsReusedRefreshToken() throws Exception {
        String body = """
                {"email":"aisha@example.com","password":"correct-horse-1","firstName":"Aisha","lastName":"Khan"}
                """;
        MvcResult registered = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.user.roles[0]").value("CUSTOMER"))
                .andReturn();
        Cookie refresh = registered.getResponse().getCookie("letsbuy_refresh");
        assertThat(refresh).isNotNull();
        assertThat(refresh.isHttpOnly()).isTrue();

        String access = com.jayway.jsonpath.JsonPath.read(registered.getResponse().getContentAsString(), "$.data.accessToken");
        mockMvc.perform(get("/api/v1/auth/me").header("Authorization", "Bearer " + access))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value("aisha@example.com"));

        MvcResult rotated = mockMvc.perform(post("/api/v1/auth/refresh")
                        .cookie(refresh)
                        .header("X-Letsbuy-Request", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andReturn();
        Cookie next = rotated.getResponse().getCookie("letsbuy_refresh");
        assertThat(next.getValue()).isNotEqualTo(refresh.getValue());

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .cookie(refresh)
                        .header("X-Letsbuy-Request", "1"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .cookie(next)
                        .header("X-Letsbuy-Request", "1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void locksAccountAfterRepeatedFailures() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"lock@example.com","password":"correct-horse-1","firstName":"L","lastName":"K"}
                                """))
                .andExpect(status().isCreated());
        String bad = """
                {"email":"lock@example.com","password":"wrong-password-1"}
                """;
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON).content(bad))
                    .andExpect(status().isUnauthorized());
        }
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"lock@example.com","password":"correct-horse-1"}
                                """))
                .andExpect(status().isLocked())
                .andExpect(jsonPath("$.code").value("ACCOUNT_LOCKED"));
    }

    @Test
    void resetsPasswordWithoutRevealingUnknownEmails() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"reset@example.com","password":"correct-horse-1","firstName":"R","lastName":"S"}
                                """))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/v1/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"missing@example.com\"}"))
                .andExpect(status().isNoContent());
        mockMvc.perform(post("/api/v1/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"reset@example.com\"}"))
                .andExpect(status().isNoContent());
        String token = email.resetTokens.getLast();
        mockMvc.perform(post("/api/v1/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"" + token + "\",\"password\":\"new-password-2\"}"))
                .andExpect(status().isNoContent());
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"reset@example.com","password":"correct-horse-1"}
                                """))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"reset@example.com","password":"new-password-2"}
                                """))
                .andExpect(status().isOk());
    }

    @Test
    void checkoutDecrementsStockAndCustomerCannotCallAdmin() throws Exception {
        Category category = new Category();
        category.setName("Kitchen");
        category.setSlug("kitchen-" + UUID.randomUUID().toString().substring(0, 8));
        categories.save(category);
        Product product = new Product();
        product.setCategory(category);
        product.setSku("SKU-" + UUID.randomUUID().toString().substring(0, 8));
        product.setName("Test kettle");
        product.setSlug("test-kettle-" + UUID.randomUUID().toString().substring(0, 8));
        product.setDescription("A kettle used by the checkout test.");
        product.setPriceMinor(50_000);
        product.setCurrency("INR");
        product.setStockQuantity(3);
        product.setStatus(ProductStatus.ACTIVE);
        products.save(product);

        MvcResult registered = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"buyer@example.com","password":"correct-horse-1","firstName":"B","lastName":"Y"}
                                """))
                .andExpect(status().isCreated())
                .andReturn();
        String access = com.jayway.jsonpath.JsonPath.read(registered.getResponse().getContentAsString(), "$.data.accessToken");

        mockMvc.perform(post("/api/v1/admin/products")
                        .header("Authorization", "Bearer " + access)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/v1/cart/items")
                        .header("Authorization", "Bearer " + access)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":\"" + product.getId() + "\",\"quantity\":2}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.subtotalMinor").value(100_000));

        MvcResult address = mockMvc.perform(post("/api/v1/addresses")
                        .header("Authorization", "Bearer " + access)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fullName":"B Y","line1":"12 Lake Road","city":"Pune","region":"MH","postalCode":"411001","countryCode":"IN","phone":"9999999999","defaultAddress":true}
                                """))
                .andExpect(status().isCreated())
                .andReturn();
        String addressId = com.jayway.jsonpath.JsonPath.read(address.getResponse().getContentAsString(), "$.data.id");

        mockMvc.perform(post("/api/v1/checkout")
                        .header("Authorization", "Bearer " + access)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"addressId\":\"" + addressId + "\",\"paymentMethod\":\"COD\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.status").value("PLACED"))
                .andExpect(jsonPath("$.data.shippingMinor").value(0))
                .andExpect(jsonPath("$.data.paymentStatus").value("PENDING"))
                .andExpect(jsonPath("$.data.items[0].quantity").value(2));

        assertThat(products.findById(product.getId()).orElseThrow().getStockQuantity()).isEqualTo(1);
        mockMvc.perform(get("/api/v1/cart").header("Authorization", "Bearer " + access))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items.length()").value(0));
    }

    @Test
    void verifiesAPendingAccount() throws Exception {
        User user = new User();
        user.setEmail("pending@example.com");
        user.setPasswordHash(passwordEncoder.encode("correct-horse-1"));
        user.setFirstName("P");
        user.setLastName("Q");
        user.setStatus(UserStatus.PENDING_VERIFICATION);
        users.save(user);
        mockMvc.perform(post("/api/v1/auth/resend-verification")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"pending@example.com\"}"))
                .andExpect(status().isNoContent());
        String token = email.verificationTokens.getLast();
        mockMvc.perform(post("/api/v1/auth/verify-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"" + token + "\"}"))
                .andExpect(status().isNoContent());
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"pending@example.com","password":"correct-horse-1"}
                                """))
                .andExpect(status().isOk());
    }
}
