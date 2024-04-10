package org.korolev.dens.blps_lab1.repositories;

import org.korolev.dens.blps_lab1.entites.Client;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClientRepository extends JpaRepository<Client, Integer> {

    Optional<Client> findByLogin(String login);
}