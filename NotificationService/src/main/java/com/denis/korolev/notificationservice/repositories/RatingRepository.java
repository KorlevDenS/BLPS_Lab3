package com.denis.korolev.notificationservice.repositories;

import com.denis.korolev.notificationservice.entities.Client;
import com.denis.korolev.notificationservice.entities.Rating;
import com.denis.korolev.notificationservice.entities.Topic;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RatingRepository extends JpaRepository<Rating, Integer> {

    List<Rating> findAllByTopic(Topic topic);

    Optional<Rating> findByCreatorAndTopic(Client creator, Topic topic);
}