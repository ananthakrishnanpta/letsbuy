package com.letsbuy.catalog.bootstrap;

import com.letsbuy.catalog.domain.Category;
import com.letsbuy.catalog.domain.Product;
import com.letsbuy.catalog.domain.ProductStatus;
import com.letsbuy.catalog.persistence.CategoryRepository;
import com.letsbuy.catalog.persistence.ProductRepository;
import com.letsbuy.common.text.Slugs;
import java.math.BigDecimal;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Profile("local")
@Order(2)
@ConditionalOnProperty(prefix = "app.seed", name = "catalog", havingValue = "true")
public class CatalogSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(CatalogSeeder.class);

    private final CategoryRepository categories;
    private final ProductRepository products;

    public CatalogSeeder(CategoryRepository categories, ProductRepository products) {
        this.categories = categories;
        this.products = products;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (products.count() > 0) {
            return;
        }
        Category kitchen = category("Kitchen", "Tools and vessels for everyday cooking.");
        Category home = category("Home", "Pieces that make a room feel finished.");
        Category outdoors = category("Outdoors", "For walks, picnics, and long weekends.");
        List<SeedProduct> seeds = List.of(
                new SeedProduct(kitchen, "LB-KETTLE", "Stoneware kettle", "A 1.2 litre kettle with a cork lid and a spout that pours cleanly.", "2490.00", 24),
                new SeedProduct(kitchen, "LB-BOARD", "Walnut chopping board", "End-grain walnut, finished with food-safe oil. 30 by 20 centimetres.", "1890.00", 40),
                new SeedProduct(kitchen, "LB-LINEN", "Linen tea towel, set of 2", "Washed oatmeal linen. Gets softer every time it is dried in the sun.", "890.00", 60),
                new SeedProduct(home, "LB-LAMP", "Paper shade lamp", "A rice-paper shade on a turned beech base. Warm light, no glare.", "3290.00", 15),
                new SeedProduct(home, "LB-THROW", "Handloom cotton throw", "Indigo and natural stripes, woven on a narrow loom. 130 by 180 centimetres.", "2790.00", 18),
                new SeedProduct(home, "LB-VASE", "Speckled clay vase", "A short vase for a few stems. Unglazed foot, satin glaze on the body.", "1490.00", 22),
                new SeedProduct(outdoors, "LB-BOTTLE", "Steel water bottle", "600 millilitres, powder-coated moss green. Keeps water cold for a commute.", "1290.00", 50),
                new SeedProduct(outdoors, "LB-BLANKET", "Picnic blanket", "Waxed cotton on one side, brushed cotton on the other. Folds into its own strap.", "2190.00", 20)
        );
        for (SeedProduct seed : seeds) {
            Product product = new Product();
            product.setCategory(seed.category());
            product.setSku(seed.sku());
            product.setName(seed.name());
            product.setSlug(Slugs.from(seed.name()));
            product.setDescription(seed.description());
            product.setPriceMinor(new BigDecimal(seed.price()).movePointRight(2).longValueExact());
            product.setCurrency("INR");
            product.setStockQuantity(seed.stock());
            product.setStatus(ProductStatus.ACTIVE);
            products.save(product);
        }
        log.info("Seeded catalog with {} products", seeds.size());
    }

    private Category category(String name, String description) {
        return categories.findBySlug(Slugs.from(name)).orElseGet(() -> {
            Category category = new Category();
            category.setName(name);
            category.setSlug(Slugs.from(name));
            category.setDescription(description);
            return categories.save(category);
        });
    }

    private record SeedProduct(Category category, String sku, String name, String description, String price, int stock) {
    }
}
