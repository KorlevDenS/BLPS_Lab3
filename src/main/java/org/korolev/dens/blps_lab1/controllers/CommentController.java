package org.korolev.dens.blps_lab1.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.korolev.dens.blps_lab1.configuration.RabbitConfig;
import org.korolev.dens.blps_lab1.entites.*;
import org.korolev.dens.blps_lab1.repositories.*;
import org.korolev.dens.blps_lab1.requests.StatsMessage;
import org.korolev.dens.blps_lab1.services.CommentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/comment")
public class CommentController {

    private final TopicRepository topicRepository;
    private final CommentRepository commentRepository;
    private final CommentService commentService;

    private final RabbitConfig.MQTTGateway mqttGateway;


    public CommentController(TopicRepository topicRepository, CommentRepository commentRepository,
                             CommentService commentService, RabbitConfig.MQTTGateway mqttGateway) {
        this.topicRepository = topicRepository;
        this.commentRepository = commentRepository;
        this.commentService = commentService;
        this.mqttGateway = mqttGateway;
    }

    @GetMapping("/test/message")
    public ResponseEntity<?> testMessage() {
        ObjectMapper jsonMapper = new ObjectMapper();
        StatsMessage messageObj = new StatsMessage(1, "user1", "comment", "user0");
        try {
            mqttGateway.sendToMqtt(jsonMapper.writeValueAsString(messageObj));
        } catch (JsonProcessingException e) {
            return ResponseEntity.ok("Bad");
        }
        return ResponseEntity.ok("Good");
    }

    @GetMapping("/get/all/by/topic/{topicId}")
    public ResponseEntity<?> getAllByTopic(@PathVariable Integer topicId) {
        if (topicRepository.findById(topicId).isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Topic with id " + topicId + " not found");
        }
        return ResponseEntity.ok(commentRepository.getAllByTopic(topicId));
    }

    @PreAuthorize("hasRole('MODER')")
    @DeleteMapping("/delete/{commentId}")
    public ResponseEntity<?> deleteComment(@PathVariable Integer commentId) {
        Optional<Comment> optionalComment = commentRepository.findById(commentId);
        if (optionalComment.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Comment with id " + commentId + " not found");
        }
        commentRepository.deleteById(commentId);
        return ResponseEntity.ok("Comment with id " + commentId + " deleted");
    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/add/{topicId}/{quoteId}")
    public ResponseEntity<?> addComment(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Integer topicId,
                                        @PathVariable Integer quoteId, @RequestBody Comment comment) {
        return commentService.comment(userDetails.getUsername(), topicId, quoteId, comment);
    }

}
