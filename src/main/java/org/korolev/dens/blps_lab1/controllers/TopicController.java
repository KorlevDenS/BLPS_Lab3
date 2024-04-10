package org.korolev.dens.blps_lab1.controllers;

import jakarta.transaction.Transactional;
import org.korolev.dens.blps_lab1.entites.*;
import org.korolev.dens.blps_lab1.repositories.*;
import org.korolev.dens.blps_lab1.services.TopicUpdateService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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
    public List<Topic> getAllTopicsByChapter(@PathVariable Integer chapterId) {
        return topicRepository.getAllByChapter(chapterId);
    }

    @GetMapping("get/by/id/{topicId}")
    public ResponseEntity<?> getTopicById(@PathVariable Integer topicId) {
        Optional<Topic> optionalTopic = topicRepository.findById(topicId);
        if (optionalTopic.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Такой темы не существует");
        }
        return ResponseEntity.ok(optionalTopic.get());
    }

    @PostMapping("/add/{chapterId}")
    public ResponseEntity<?> addTopic(@RequestBody Topic topic, @RequestAttribute(name = "Cid") Integer CID,
                                      @PathVariable Integer chapterId) {
        Optional<Chapter> optionalChapter = chapterRepository.findById(chapterId);
        Optional<Client> optionalClient = clientRepository.findById(CID);
        if (optionalClient.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Пользователь не авторизован");
        } else if (optionalChapter.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Раздел для темы не существует");
        }
        topic.setOwner(optionalClient.get());
        topic.setChapter(optionalChapter.get());
        topicRepository.save(topic);
        return ResponseEntity.status(HttpStatus.OK).body("Тема успешно добавлена");
    }

    @PostMapping("/update")
    public ResponseEntity<?> updateTopic(@RequestBody List<Topic> topics, @RequestAttribute(name = "Cid") Integer CID) {
        topicUpdateService.updateTopic(topics.get(0), topics.get(1), CID);
        return ResponseEntity.status(HttpStatus.OK).body("Тема успешно обновлена");
    }

    @Transactional
    @PostMapping("/add/rating/{topicId}")
    public ResponseEntity<?> addRating(@RequestBody Rating rating, @RequestAttribute(name = "Cid") Integer CID,
                                       @PathVariable Integer topicId) {
        Optional<Client> optionalClient = clientRepository.findById(CID);
        Optional<Topic> optionalTopic = topicRepository.findById(topicId);
        if (optionalClient.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Пользователь не авторизован");
        } else if (optionalTopic.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Оцениваемая тема не существует");
        }
        Optional<Rating> optionalRating =
                ratingRepository.findRatingByCreatorAndTopic(optionalClient.get(), optionalTopic.get());
        if (optionalRating.isPresent()) {
            ratingRepository.updateRatingByClientAndTopic(CID, rating.getRating(), topicId);
            return ResponseEntity.status(HttpStatus.OK).body("Оценка успешно обновлена");
        } else {
            rating.setCreator(optionalClient.get());
            rating.setTopic(optionalTopic.get());
            ratingRepository.save(rating);
            return ResponseEntity.status(HttpStatus.OK).body("Оценка успешно добавлена");
        }
    }

    @GetMapping("/get/rating/{topicId}")
    public ResponseEntity<?> getRatingOfClients(@PathVariable Integer topicId) {
        Optional<Topic> optionalTopic = topicRepository.findById(topicId);
        if (optionalTopic.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Оцениваемая тема не существует");
        }
        return ResponseEntity.ok(ratingRepository.findAllByTopic(optionalTopic.get()));
    }

    @DeleteMapping("/delete/subscription/{topicId}")
    @Transactional
    public ResponseEntity<?> deleteSubscription(@RequestAttribute(name = "Cid") Integer CID,
                                                @PathVariable Integer topicId) {
        Optional<Client> optionalClient = clientRepository.findById(CID);
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

    @PostMapping("/add/subscription/{topicId}")
    @Transactional
    public ResponseEntity<?> addSubscription(@RequestAttribute(name = "Cid") Integer CID,
                                             @PathVariable Integer topicId) {
        Optional<Client> optionalClient = clientRepository.findById(CID);
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
            subscriptionId.setClient(CID);
            subscriptionId.setTopic(topicId);
            subscription.setId(subscriptionId);
            subscription.setClient(optionalClient.get());
            subscription.setTopic(optionalTopic.get());
            subscriptionRepository.save(subscription);
            return ResponseEntity.status(HttpStatus.OK).body("Подписка успешно оформлена");
        }
    }

}
