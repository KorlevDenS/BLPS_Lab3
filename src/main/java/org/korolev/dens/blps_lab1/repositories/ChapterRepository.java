package org.korolev.dens.blps_lab1.repositories;

import org.korolev.dens.blps_lab1.entites.Chapter;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChapterRepository extends JpaRepository<Chapter, Integer> {
}