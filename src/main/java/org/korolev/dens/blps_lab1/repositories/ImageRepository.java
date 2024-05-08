package org.korolev.dens.blps_lab1.repositories;

import org.korolev.dens.blps_lab1.entites.Image;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImageRepository extends JpaRepository<Image, Integer> {
}