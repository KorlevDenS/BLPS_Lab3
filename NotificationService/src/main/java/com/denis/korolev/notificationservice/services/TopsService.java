package com.denis.korolev.notificationservice.services;

import com.denis.korolev.notificationservice.entities.Topic;
import com.denis.korolev.notificationservice.repositories.TopicRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TopsService {

    private final TopicRepository topicRepository;

    public TopsService(TopicRepository topicRepository) {
        this.topicRepository = topicRepository;
    }

    public List<Topic> getTopicsTopNByFame(Integer n) {
        return topicRepository.findTopNByFameDesc(n);
    }

    public List<Topic> getTopicsTopNByViews(Integer n) {
        return topicRepository.findTopNByViewsDesc(n);
    }
}
