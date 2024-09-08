package com.denis.korolev.notificationservice.services;

import com.denis.korolev.notificationservice.entities.Client;
import com.denis.korolev.notificationservice.entities.Rating;
import com.denis.korolev.notificationservice.entities.Topic;
import com.denis.korolev.notificationservice.repositories.ClientRepository;
import com.denis.korolev.notificationservice.repositories.RatingRepository;
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
    private final RatingRepository ratingRepository;

    public StatsGenerateService(ClientRepository clientRepository, TopicRepository topicRepository, RatingRepository ratingRepository) {
        this.clientRepository = clientRepository;
        this.topicRepository = topicRepository;
        this.ratingRepository = ratingRepository;
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
            newTopic.setTemporal_views(0);
            newTopic.setTemporal_fame(10.0);
            newTopic.setTemporal_comments(0);
            newTopic.setFame(10.0);
            topic = topicRepository.save(newTopic);
        } else {
            topic = optionalTopic.get();
        }

        switch (statsMessage.getProducerAction()) {
            case "watch" -> processWatch(topic, ownerClient);
            case "comment" -> processComment(topic, ownerClient, statsMessage.getProducer());
            case "1", "2", "3", "4", "5", "6", "7", "8", "9", "10" -> processRate(topic, ownerClient,
                    statsMessage.getProducer(), statsMessage.getProducerAction());
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
//        topicRepository.updateViewsById(topic.getId(), topic.getViews() + 1);
//        topicRepository.updateFameById(topic.getId(), topic.getFame() + 1);
//        clientRepository.updateRatingById(owner.getLogin(), owner.getRating() + 0.1);
        topic.setViews(topic.getViews() + 1);
        topic.setTemporal_views(topic.getTemporal_views() + 1);
        topicRepository.save(topic);
    }

    private void processComment(Topic topic, Client owner, String producerLogin) {
        Client producer = installClient(producerLogin);
        clientRepository.updateRatingById(owner.getLogin(), owner.getRating() + 0.5);
        //topicRepository.updateFameById(topic.getId(), topic.getFame() + 3);
        clientRepository.updateRatingById(producer.getLogin(), producer.getRating() + 1);
    }

    private void processRate(Topic topic, Client owner, String producerLogin, String strRating) {
        Client producer = installClient(producerLogin);
        Rating rating;
        Optional<Rating> oldRating = ratingRepository.findByCreatorAndTopic(producer, topic);
        rating = oldRating.orElseGet(Rating::new);
        rating.setCreator(producer);
        rating.setTopic(topic);
        rating.setRating(Integer.parseInt(strRating));
        ratingRepository.save(rating);
    }

    private void processAdd(Topic topic, Client owner) {
        //topicRepository.updateFameById(topic.getId(), topic.getFame() + 10);
        clientRepository.updateRatingById(owner.getLogin(), owner.getRating() + 5);
    }

    private void processDelete(StatsMessage statsMessage) {

    }

}
