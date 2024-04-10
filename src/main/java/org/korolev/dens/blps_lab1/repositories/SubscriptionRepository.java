package org.korolev.dens.blps_lab1.repositories;

import org.korolev.dens.blps_lab1.entites.Client;
import org.korolev.dens.blps_lab1.entites.Subscription;
import org.korolev.dens.blps_lab1.entites.SubscriptionId;
import org.korolev.dens.blps_lab1.entites.Topic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import java.util.List;
import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, SubscriptionId> {

    Optional<Subscription> findByClientAndTopic(Client client, Topic topic);

    @Modifying
    void deleteByClientAndTopic(Client client, Topic topic);

    List<Subscription> findAllByTopic(Topic topic);

}