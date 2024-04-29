package org.korolev.dens.blps_lab1.controllers;

import org.korolev.dens.blps_lab1.entites.*;
import org.korolev.dens.blps_lab1.repositories.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/comment")
public class CommentController {

    private final TopicRepository topicRepository;
    private final ClientRepository clientRepository;
    private final CommentRepository commentRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final NotificationRepository notificationRepository;

    public CommentController(TopicRepository topicRepository,
                             ClientRepository clientRepository,
                             CommentRepository commentRepository,
                             SubscriptionRepository subscriptionRepository,
                             NotificationRepository notificationRepository) {
        this.topicRepository = topicRepository;
        this.clientRepository = clientRepository;
        this.commentRepository = commentRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.notificationRepository = notificationRepository;
    }

    @GetMapping("/get/all/by/topic/{topicId}")
    public List<Comment> getAllByTopic(@PathVariable Integer topicId) {
        return commentRepository.getAllByTopic(topicId);
    }

    @PostMapping("/add/{topicId}/{quoteId}")
    public ResponseEntity<?> addComment(@RequestBody Comment comment, @PathVariable Integer topicId,
                                        @PathVariable Integer quoteId, @RequestAttribute(name = "Cid") Integer CID) {
        if (quoteId > 0) {
            Optional<Comment> optionalComment = commentRepository.findById(quoteId);
            if (optionalComment.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Комментарий для цитирования не существует");
            }
            comment.setQuote(optionalComment.get());
        }
        Optional<Topic> optionalTopic = topicRepository.findById(topicId);
        Optional<Client> optionalClient = clientRepository.findById(CID);
        if (optionalClient.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Пользователь не авторизован");
        } else if (optionalTopic.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Тема не существует");
        }
        comment.setCommentator(optionalClient.get());
        comment.setTopic(optionalTopic.get());
        Comment addedComment = commentRepository.save(comment);

        List<Subscription> subscriptions = subscriptionRepository.findAllByTopic(optionalTopic.get());
        for (Subscription subscription : subscriptions) {
            Notification notification = new Notification();
            notification.setTopic(optionalTopic.get());
            notification.setInitiator(optionalClient.get());
            notification.setRecipient(subscription.getClient());
            notification.setDescription("Пользователь " + optionalClient.get().getLogin()
                    + " добавил комментарий к теме " + optionalTopic.get().getTitle());
            notificationRepository.save(notification);
        }
        return ResponseEntity.ok(addedComment);
    }

}
