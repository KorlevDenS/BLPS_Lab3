package org.korolev.dens.blps_lab1.repositories;

import org.korolev.dens.blps_lab1.entites.Permission;
import org.korolev.dens.blps_lab1.entites.PermissionId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PermissionRepository extends JpaRepository<Permission, PermissionId> {
}