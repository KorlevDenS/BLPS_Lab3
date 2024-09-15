package com.denis.korolev.notificationservice.repositories;

import com.denis.korolev.notificationservice.entities.Client;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClientRepository extends JpaRepository<Client, String> {

}