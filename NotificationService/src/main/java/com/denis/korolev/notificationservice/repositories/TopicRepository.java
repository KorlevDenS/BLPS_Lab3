package com.denis.korolev.notificationservice.repositories;

import com.denis.korolev.notificationservice.entities.Topic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TopicRepository extends JpaRepository<Topic, Integer> {

    @Modifying
    @Query("UPDATE Topic t SET t.views = :newVal WHERE t.id = :topicId")
    void updateViewsById(@Param("topicId") Integer topicId, @Param("newVal") Integer newVal);

    @Modifying
    @Query("UPDATE Topic t SET t.fame = :newVal WHERE t.id = :topicId")
    void updateFameById(@Param("topicId") Integer topicId, @Param("newVal") Integer newVal);

    @Query("SELECT t FROM Topic t ORDER BY t.fame DESC LIMIT :N")
    List<Topic> findTopNByFameDesc(@Param("N") Integer n);

    @Query("SELECT t FROM Topic t ORDER BY t.views DESC LIMIT :N")
    List<Topic> findTopNByViewsDesc(@Param("N") Integer n);

}