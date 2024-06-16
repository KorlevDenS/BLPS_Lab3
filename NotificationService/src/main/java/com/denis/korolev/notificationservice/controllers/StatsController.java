package com.denis.korolev.notificationservice.controllers;

import com.denis.korolev.notificationservice.entities.Client;
import com.denis.korolev.notificationservice.entities.Topic;
import com.denis.korolev.notificationservice.repositories.ClientRepository;
import com.denis.korolev.notificationservice.repositories.TopicRepository;
import com.denis.korolev.notificationservice.services.TopsService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/stats")
public class StatsController {

    private final ClientRepository clientRepository;
    private final TopicRepository topicRepository;
    private final TopsService topsService;

    public StatsController(ClientRepository clientRepository, TopicRepository topicRepository, TopsService topsService) {
        this.clientRepository = clientRepository;
        this.topicRepository = topicRepository;
        this.topsService = topsService;
    }

    @GetMapping("/get/topics")
    public List<Topic> getTopicsStats() {
        return topicRepository.findAll();
    }

    @GetMapping("/get/clients")
    public List<Client> getClientsStats() {
        return clientRepository.findAll();
    }

    @GetMapping("/get/topics/fame/top/{n}")
    public List<Topic> getTopByFame(@PathVariable Integer n) {
        return topsService.getTopicsTopNByFame(n);
    }

    @GetMapping("/get/topics/views/top/{n}")
    public List<Topic> getTopByViews(@PathVariable Integer n) {
        return topsService.getTopicsTopNByViews(n);
    }

    @GetMapping("/get/topic/{topicId}")
    public ResponseEntity<?> getTopicStats(@PathVariable Integer topicId) {
        Optional<Topic> optionalTopic = topicRepository.findById(topicId);
        if (optionalTopic.isPresent()) {
            return ResponseEntity.ok(optionalTopic.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No topic with id " + topicId);
        }
    }

    @GetMapping("/get/client/{clientLogin}")
    public ResponseEntity<?> getClientStats(@PathVariable String clientLogin) {
        Optional<Client> optionalClient = clientRepository.findById(clientLogin);
        if (optionalClient.isPresent()) {
            return ResponseEntity.ok(optionalClient.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No client with login " + clientLogin);
        }
    }

}
