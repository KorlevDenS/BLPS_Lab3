package org.korolev.dens.blps_lab1.controllers;

import jakarta.transaction.Transactional;
import org.korolev.dens.blps_lab1.entites.*;
import org.korolev.dens.blps_lab1.repositories.*;
import org.korolev.dens.blps_lab1.services.TopicUpdateService;
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
    private final TopicUpdateService topicUpdateService;
    private final RatingRepository ratingRepository;
    private final SubscriptionRepository subscriptionRepository;

    public TopicController(TopicRepository topicRepository,
                           ClientRepository clientRepository,
                           ChapterRepository chapterRepository, TopicUpdateService topicUpdateService,
                           RatingRepository ratingRepository,
                           SubscriptionRepository subscriptionRepository) {
        this.topicRepository = topicRepository;
        this.clientRepository = clientRepository;
        this.chapterRepository = chapterRepository;
        this.topicUpdateService = topicUpdateService;
        this.ratingRepository = ratingRepository;
        this.subscriptionRepository = subscriptionRepository;
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
        Optional<Topic> optionalTopic = topicRepository.findById(topic.getId());
        if (optionalTopic.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No topic with id " + topic.getId());
        }
        if (!(optionalTopic.get().getOwner().getLogin().equals(userDetails.getUsername()))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Topic with id " + topic.getId() + " is not yours");
        }
        topicUpdateService.updateTopic(optionalTopic.get(), topic, userDetails.getUsername());
        Optional<Topic> optionalNewTopic = topicRepository.findById(topic.getId());
        if (optionalNewTopic.isEmpty()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Conflict while updating topic " + topic.getId());
        }
        return ResponseEntity.status(HttpStatus.OK).body("Topic " + topic.getId() + " has been updated");
    }

    @Transactional
    @PreAuthorize("hasRole('USER')")
    @PostMapping("/add/rating/{topicId}")
    public ResponseEntity<?> addRating(@RequestBody @Validated Rating rating, @PathVariable Integer topicId,
                                       @AuthenticationPrincipal UserDetails userDetails) {
        Optional<Client> optionalClient = clientRepository.findByLogin(userDetails.getUsername());
        Optional<Topic> optionalTopic = topicRepository.findById(topicId);
        if (optionalClient.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Пользователь не авторизован");
        } else if (optionalTopic.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Оцениваемая тема не существует");
        }
        Optional<Rating> optionalRating =
                ratingRepository.findRatingByCreatorAndTopic(optionalClient.get(), optionalTopic.get());
        if (optionalRating.isPresent()) {
            ratingRepository.updateRatingByClientAndTopic(userDetails.getUsername(), rating.getRating(), topicId);
            return ResponseEntity.status(HttpStatus.OK).body("Оценка успешно обновлена");
        } else {
            rating.setCreator(optionalClient.get());
            rating.setTopic(optionalTopic.get());
            Rating addedRating = ratingRepository.save(rating);
            return ResponseEntity.ok(addedRating);
        }
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
    @Transactional
    public ResponseEntity<?> deleteSubscription(@AuthenticationPrincipal UserDetails userDetails,
                                                @PathVariable Integer topicId) {
        Optional<Client> optionalClient = clientRepository.findByLogin(userDetails.getUsername());
        Optional<Topic> optionalTopic = topicRepository.findById(topicId);
        if (optionalClient.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Пользователь не авторизован");
        } else if (optionalTopic.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Такой темы с подпиской не существует");
        }
        Optional<Subscription> optionalSubscription =
                subscriptionRepository.findByClientAndTopic(optionalClient.get(), optionalTopic.get());
        if (optionalSubscription.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Подписка не существует");
        } else {
            subscriptionRepository.deleteByClientAndTopic(optionalClient.get(), optionalTopic.get());
            return ResponseEntity.status(HttpStatus.OK).body("Подписка успешно удалена");
        }
    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/add/subscription/{topicId}")
    public ResponseEntity<?> addSubscription(@AuthenticationPrincipal UserDetails userDetails,
                                             @PathVariable Integer topicId) {
        Optional<Client> optionalClient = clientRepository.findByLogin(userDetails.getUsername());
        Optional<Topic> optionalTopic = topicRepository.findById(topicId);
        if (optionalClient.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Пользователь не авторизован");
        } else if (optionalTopic.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Тема для подписки не существует");
        }
        Optional<Subscription> optionalSubscription =
                subscriptionRepository.findByClientAndTopic(optionalClient.get(), optionalTopic.get());
        if (optionalSubscription.isPresent()) {
            return ResponseEntity.status(HttpStatus.ALREADY_REPORTED).body("Подписка на эту тему уже оформлена");
        } else {
            Subscription subscription = new Subscription();
            SubscriptionId subscriptionId = new SubscriptionId();
            subscriptionId.setClient(optionalClient.get().getId());
            subscriptionId.setTopic(topicId);
            subscription.setId(subscriptionId);
            subscription.setClient(optionalClient.get());
            subscription.setTopic(optionalTopic.get());
            Subscription addedSubscription = subscriptionRepository.save(subscription);
            return ResponseEntity.ok(addedSubscription);
        }
    }

}
