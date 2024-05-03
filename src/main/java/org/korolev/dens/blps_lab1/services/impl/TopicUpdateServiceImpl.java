package org.korolev.dens.blps_lab1.services.impl;

import jakarta.transaction.Transactional;
import org.korolev.dens.blps_lab1.entites.Topic;
import org.korolev.dens.blps_lab1.repositories.TopicRepository;
import org.korolev.dens.blps_lab1.services.TopicUpdateService;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class TopicUpdateServiceImpl implements TopicUpdateService {

    private final TopicRepository topicRepository;

    public TopicUpdateServiceImpl(TopicRepository topicRepository) {
        this.topicRepository = topicRepository;
    }

    @Transactional
    @Override
    public void updateTopic(Topic originalTopic, Topic updatedTopic, String login) {
        if (!Objects.equals(originalTopic.getTitle(), updatedTopic.getTitle())) {
            topicRepository.updateTitle(originalTopic.getId(), updatedTopic.getTitle(), login);
        }
        if (!Objects.equals(originalTopic.getText(), updatedTopic.getText())) {
            topicRepository.updateText(originalTopic.getId(), updatedTopic.getText(), login);
        }
    }

}
