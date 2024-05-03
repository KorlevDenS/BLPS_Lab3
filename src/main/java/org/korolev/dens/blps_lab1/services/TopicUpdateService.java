package org.korolev.dens.blps_lab1.services;

import org.korolev.dens.blps_lab1.entites.Topic;

public interface TopicUpdateService {

    void updateTopic(Topic originalTopic, Topic updatedTopic, String login);

}
