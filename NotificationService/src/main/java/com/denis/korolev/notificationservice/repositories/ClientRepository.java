package com.denis.korolev.notificationservice.repositories;

import com.denis.korolev.notificationservice.entities.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ClientRepository extends JpaRepository<Client, String> {

    @Modifying
    @Query("UPDATE Client c SET c.rating = :newVal WHERE c.login = :clientId")
    void updateRatingById(@Param("clientId") String clientId, @Param("newVal") Double newVal);

}