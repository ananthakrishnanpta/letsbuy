package com.letsbuy.identity.persistence;

import com.letsbuy.identity.domain.Role;
import com.letsbuy.identity.domain.RoleName;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Short> {

    Optional<Role> findByName(RoleName name);
}
