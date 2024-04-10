package org.korolev.dens.blps_lab1.repositories;

import org.korolev.dens.blps_lab1.entites.Client;
import org.korolev.dens.blps_lab1.entites.Rating;
import org.korolev.dens.blps_lab1.entites.Topic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RatingRepository extends JpaRepository<Rating, Integer> {

    Optional<Rating> findRatingByCreatorAndTopic(Client creator, Topic topic);

    List<Rating> findAllByTopic(Topic topic);

    @Modifying
    @Query(value = "update rating set rating = :rating where topic = :topicId and creator = :Cid", nativeQuery = true)
    void updateRatingByClientAndTopic(@Param("Cid") Integer CID, @Param("rating") Integer rating,
                              @Param("topicId") Integer topicId);

}