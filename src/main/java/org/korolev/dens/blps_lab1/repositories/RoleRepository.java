package org.korolev.dens.blps_lab1.repositories;

import org.korolev.dens.blps_lab1.entites.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Integer> {

    Role findByName(String name);

}