package com.letsbuy.identity.bootstrap;

import com.letsbuy.identity.domain.Role;
import com.letsbuy.identity.domain.RoleName;
import com.letsbuy.identity.persistence.RoleRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(0)
public class RoleInitializer implements ApplicationRunner {

    private final RoleRepository roles;

    public RoleInitializer(RoleRepository roles) {
        this.roles = roles;
    }

    @Override
    public void run(ApplicationArguments args) {
        ensure(RoleName.CUSTOMER, (short) 1);
        ensure(RoleName.ADMIN, (short) 2);
    }

    private void ensure(RoleName name, short id) {
        if (roles.findByName(name).isPresent()) {
            return;
        }
        Role role = new Role();
        role.setId(id);
        role.setName(name);
        roles.save(role);
    }
}
