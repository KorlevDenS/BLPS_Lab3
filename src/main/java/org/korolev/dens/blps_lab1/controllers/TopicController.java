package org.korolev.dens.blps_lab1.controllers;

import jakarta.annotation.Nullable;
import org.korolev.dens.blps_lab1.entites.*;
import org.korolev.dens.blps_lab1.repositories.*;
import org.korolev.dens.blps_lab1.requests.StatsMessage;
import org.korolev.dens.blps_lab1.services.MessageProducer;
import org.korolev.dens.blps_lab1.services.TopicService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

@RestController
@RequestMapping("/topic")
public class TopicController {

    private final TopicRepository topicRepository;
    private final ChapterRepository chapterRepository;
    private final RatingRepository ratingRepository;
    private final TopicService topicService;

    private final MessageProducer messageProducer;


    public TopicController(TopicRepository topicRepository,
                           ChapterRepository chapterRepository, RatingRepository ratingRepository,
                           TopicService topicService, MessageProducer messageProducer) {
        this.topicRepository = topicRepository;
        this.chapterRepository = chapterRepository;
        this.ratingRepository = ratingRepository;
        this.topicService = topicService;
        this.messageProducer = messageProducer;
    }

    @PostMapping("/multi")
    public void getMultipart(@RequestParam("a1") String a1,
                             @RequestParam("a2") String a2,
                             @RequestParam("a3") String a3,
                             @Nullable @RequestParam("a4") MultipartFile a4) {
        System.out.println(a1 + " " + a2 + " " + a3);
        System.out.println(System.getenv("PHOTO_STORAGE"));
        if (a4 == null) System.out.println("NULL IMG");
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
        messageProducer.sendMessage(new StatsMessage(
                optionalTopic.get().getId(), "", "watch", optionalTopic.get().getOwner().getLogin()));
        return ResponseEntity.ok(optionalTopic.get());
    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/add/{chapterId}")
    public ResponseEntity<?> addTopic(@RequestParam("title") String title, @RequestParam("text") String text,
                                      @PathVariable Integer chapterId,
                                      @AuthenticationPrincipal UserDetails userDetails,
                                      @RequestParam("img1") @Nullable MultipartFile img1,
                                      @RequestParam("img2") @Nullable MultipartFile img2,
                                      @RequestParam("img3") @Nullable MultipartFile img3) {
        Topic topic = new Topic();
        topic.setTitle(title);
        topic.setText(text);
        return topicService.add(topic, chapterId, userDetails.getUsername(), img1, img2, img3);
    }

    @PreAuthorize("hasRole('MODER')")
    @DeleteMapping("/delete/{topicId}")
    public ResponseEntity<?> deleteTopic(@PathVariable Integer topicId) {
        return topicService.delete(topicId);
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
