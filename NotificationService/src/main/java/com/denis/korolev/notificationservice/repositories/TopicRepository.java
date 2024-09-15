package com.denis.korolev.notificationservice.repositories;

import com.denis.korolev.notificationservice.entities.Topic;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TopicRepository extends JpaRepository<Topic, Integer> {

}