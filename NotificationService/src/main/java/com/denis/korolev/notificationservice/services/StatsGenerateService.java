package com.denis.korolev.notificationservice.services;

import com.denis.korolev.notificationservice.entities.Client;
import com.denis.korolev.notificationservice.entities.Topic;
import com.denis.korolev.notificationservice.repositories.ClientRepository;
import com.denis.korolev.notificationservice.repositories.TopicRepository;
import com.denis.korolev.notificationservice.responces.StatsMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class StatsGenerateService {

    private final ClientRepository clientRepository;
    private final TopicRepository topicRepository;

    public StatsGenerateService(ClientRepository clientRepository, TopicRepository topicRepository) {
        this.clientRepository = clientRepository;
        this.topicRepository = topicRepository;
    }

    @Transactional
    public void processMessage(String message) {
        ObjectMapper jsonMapper = new ObjectMapper();
        StatsMessage statsMessage;
        try {
            statsMessage = jsonMapper.readValue(message, StatsMessage.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        Client ownerClient = installClient(statsMessage.getTopicOwner());
        //Client producer = installClient(statsMessage.getProducer());

        Optional<Topic> optionalTopic = topicRepository.findById(statsMessage.getTopicId());
        Topic topic;
        if (optionalTopic.isEmpty()) {
            Topic newTopic = new Topic();
            newTopic.setId(statsMessage.getTopicId());
            newTopic.setOwner(ownerClient);
            newTopic.setViews(0);
            newTopic.setFame(0);
            topic = topicRepository.save(newTopic);
        } else {
            topic = optionalTopic.get();
        }

        switch (statsMessage.getProducerAction()) {
            case "watch" -> processWatch(topic, ownerClient);
            case "comment" -> processComment(topic, ownerClient, statsMessage.getProducer());
            case "rate" -> processRate(statsMessage);
            case "add" -> processAdd(topic, ownerClient);
            case "delete" -> processDelete(statsMessage);
            default -> System.out.println("Wrong message");
        }
    }

    private Client installClient(String login) {
        Optional<Client> optionalClient = clientRepository.findById(login);
        Client client;
        if (optionalClient.isEmpty()) {
            Client newClient = new Client();
            newClient.setLogin(login);
            newClient.setRating(1.0);
            client = clientRepository.save(newClient);
        } else {
            client = optionalClient.get();
        }
        return client;
    }

    private void processWatch(Topic topic, Client owner) {
        topicRepository.updateViewsById(topic.getId(), topic.getViews() + 1);
        topicRepository.updateFameById(topic.getId(), topic.getFame() + 1);
        clientRepository.updateRatingById(owner.getLogin(), owner.getRating() + 0.1);
    }

    private void processComment(Topic topic, Client owner, String producerLogin) {
        Client producer = installClient(producerLogin);
        clientRepository.updateRatingById(owner.getLogin(), owner.getRating() + 0.5);
        topicRepository.updateFameById(topic.getId(), topic.getFame() + 3);
        clientRepository.updateRatingById(producer.getLogin(), producer.getRating() + 1);
    }

    private void processRate(StatsMessage statsMessage) {

    }

    private void processAdd(Topic topic, Client owner) {
        topicRepository.updateFameById(topic.getId(), topic.getFame() + 10);
        clientRepository.updateRatingById(owner.getLogin(), owner.getRating() + 5);
    }

    private void processDelete(StatsMessage statsMessage) {

    }

}
