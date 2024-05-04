package org.korolev.dens.blps_lab1.controllers;

import org.korolev.dens.blps_lab1.entites.*;
import org.korolev.dens.blps_lab1.repositories.*;
import org.korolev.dens.blps_lab1.services.TopicService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/topic")
public class TopicController {

    private final TopicRepository topicRepository;
    private final ClientRepository clientRepository;
    private final ChapterRepository chapterRepository;
    private final RatingRepository ratingRepository;

    private final TopicService topicService;


    public TopicController(TopicRepository topicRepository, ClientRepository clientRepository,
                           ChapterRepository chapterRepository, RatingRepository ratingRepository,
                           TopicService topicService) {
        this.topicRepository = topicRepository;
        this.clientRepository = clientRepository;
        this.chapterRepository = chapterRepository;
        this.ratingRepository = ratingRepository;
        this.topicService = topicService;
    }

    @GetMapping("/get/all/by/chapter/{chapterId}")
    public ResponseEntity<?> getAllTopicsByChapter(@PathVariable Integer chapterId) {
        if (chapterRepository.findById(chapterId).isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No chapter with id " + chapterId);
        }
        return ResponseEntity.ok(topicRepository.getAllByChapter(chapterId));
    }

    @GetMapping("get/by/id/{topicId}")
    public ResponseEntity<?> getTopicById(@PathVariable Integer topicId) {
        Optional<Topic> optionalTopic = topicRepository.findById(topicId);
        if (optionalTopic.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No topic with id " + topicId);
        }
        return ResponseEntity.ok(optionalTopic.get());
    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/add/{chapterId}")
    public ResponseEntity<?> addTopic(@RequestBody Topic topic, @PathVariable Integer chapterId,
                                      @AuthenticationPrincipal UserDetails userDetails) {
        Optional<Chapter> optionalChapter = chapterRepository.findById(chapterId);
        Optional<Client> optionalClient = clientRepository.findByLogin(userDetails.getUsername());
        if (optionalClient.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Пользователь не авторизован");
        } else if (optionalChapter.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No chapter with id " + chapterId);
        }
        topic.setOwner(optionalClient.get());
        topic.setChapter(optionalChapter.get());
        Topic addedTopic = topicRepository.save(topic);
        return ResponseEntity.ok(addedTopic);
    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/update")
    public ResponseEntity<?> updateTopic(@Validated @RequestBody Topic topic,
                                         @AuthenticationPrincipal UserDetails userDetails) {
        return topicService.update(topic, userDetails.getUsername());
    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/add/rating/{topicId}")
    public ResponseEntity<?> addRating(@RequestBody @Validated Rating rating, @PathVariable Integer topicId,
                                       @AuthenticationPrincipal UserDetails userDetails) {
        return topicService.rate(rating, topicId, userDetails.getUsername());
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/get/rating/{topicId}")
    public ResponseEntity<?> getRatingOfClients(@PathVariable Integer topicId) {
        Optional<Topic> optionalTopic = topicRepository.findById(topicId);
        if (optionalTopic.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Оцениваемая тема не существует");
        }
        return ResponseEntity.ok(ratingRepository.findAllByTopic(optionalTopic.get()));
    }

    @PreAuthorize("hasRole('USER')")
    @DeleteMapping("/delete/subscription/{topicId}")
    public ResponseEntity<?> deleteSubscription(@AuthenticationPrincipal UserDetails userDetails,
                                                @PathVariable Integer topicId) {
        return topicService.unsubscribe(topicId, userDetails.getUsername());
    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/add/subscription/{topicId}")
    public ResponseEntity<?> addSubscription(@AuthenticationPrincipal UserDetails userDetails,
                                             @PathVariable Integer topicId) {
        return topicService.subscribe(topicId, userDetails.getUsername());
    }

}
