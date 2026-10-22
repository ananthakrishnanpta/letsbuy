package com.letsbuy.identity.bootstrap;

import com.letsbuy.config.AppProperties;
import com.letsbuy.identity.domain.RoleName;
import com.letsbuy.identity.domain.User;
import com.letsbuy.identity.domain.UserStatus;
import com.letsbuy.identity.persistence.RoleRepository;
import com.letsbuy.identity.persistence.UserRepository;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Order(1)
@ConditionalOnProperty(prefix = "app.seed", name = "enabled", havingValue = "true")
public class AdminSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminSeeder.class);

    private final AppProperties properties;
    private final UserRepository users;
    private final RoleRepository roles;
    private final PasswordEncoder passwordEncoder;

    public AdminSeeder(AppProperties properties, UserRepository users, RoleRepository roles, PasswordEncoder passwordEncoder) {
        this.properties = properties;
        this.users = users;
        this.roles = roles;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        String email = properties.seed().adminEmail();
        String password = properties.seed().adminPassword();
        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            return;
        }
        String normalized = email.trim().toLowerCase(java.util.Locale.ROOT);
        if (users.existsByEmail(normalized)) {
            return;
        }
        User admin = new User();
        admin.setEmail(normalized);
        admin.setPasswordHash(passwordEncoder.encode(password));
        admin.setFirstName("Letsbuy");
        admin.setLastName("Admin");
        admin.setStatus(UserStatus.ACTIVE);
        admin.setEmailVerifiedAt(Instant.now());
        roles.findByName(RoleName.ADMIN).ifPresent(admin.getRoles()::add);
        roles.findByName(RoleName.CUSTOMER).ifPresent(admin.getRoles()::add);
        users.save(admin);
        log.info("Seeded admin user {}", normalized);
    }
}
