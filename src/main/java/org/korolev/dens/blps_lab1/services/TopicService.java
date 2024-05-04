package org.korolev.dens.blps_lab1.services;

import org.korolev.dens.blps_lab1.entites.*;
import org.korolev.dens.blps_lab1.repositories.ClientRepository;
import org.korolev.dens.blps_lab1.repositories.RatingRepository;
import org.korolev.dens.blps_lab1.repositories.SubscriptionRepository;
import org.korolev.dens.blps_lab1.repositories.TopicRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;

@Service
public class TopicService {

    private final TransactionTemplate transactionTemplate;
    private final ClientRepository clientRepository;
    private final TopicRepository topicRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final RatingRepository ratingRepository;


    public TopicService(PlatformTransactionManager platformTransactionManager, ClientRepository clientRepository,
                        TopicRepository topicRepository, SubscriptionRepository subscriptionRepository,
                        RatingRepository ratingRepository) {
        this.transactionTemplate = new TransactionTemplate(platformTransactionManager);
        this.clientRepository = clientRepository;
        this.topicRepository = topicRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.ratingRepository = ratingRepository;
        this.transactionTemplate.setIsolationLevel(TransactionDefinition.ISOLATION_SERIALIZABLE);

    }

    public ResponseEntity<?> subscribe(Integer topicId, String login) {
        Optional<Client> optionalClient = clientRepository.findByLogin(login);
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

    public ResponseEntity<?> unsubscribe(Integer topicId, String login) {
        return transactionTemplate.execute((TransactionCallback<ResponseEntity<?>>) status -> {
            try {
                Client client = clientRepository.findByLogin(login).orElseThrow(Exception::new);
                Topic topic = topicRepository.findById(topicId)
                        .orElseThrow(() -> new NoSuchElementException("Topic not found"));
                subscriptionRepository.findByClientAndTopic(client, topic)
                        .orElseThrow(() -> new NoSuchElementException("Subscription not found"));
                subscriptionRepository.deleteByClientAndTopic(client, topic);
            } catch (NoSuchElementException e) {
                status.setRollbackOnly();
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
            } catch (Exception e) {
                status.setRollbackOnly();
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Не удалось отписаться");
            }
            return ResponseEntity.status(HttpStatus.OK).body("Подписка успешно удалена");

        });
    }

    public ResponseEntity<?> update(Topic topic, String login) {
        return transactionTemplate.execute((TransactionCallback<ResponseEntity<?>>) status -> {
            try {
                Topic t = topicRepository.findById(topic.getId())
                        .orElseThrow(() -> new NoSuchElementException("Topic not found"));
                if (!(t.getOwner().getLogin().equals(login))) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body("Topic with id " + topic.getId() + " is not yours");
                }
                if (!Objects.equals(t.getTitle(), topic.getTitle())) {
                    topicRepository.updateTitle(t.getId(), topic.getTitle(), login);
                }
                if (!Objects.equals(t.getText(), topic.getText())) {
                    topicRepository.updateText(t.getId(), topic.getText(), login);
                }
            } catch (NoSuchElementException e) {
                status.setRollbackOnly();
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
            } catch (Exception e) {
                status.setRollbackOnly();
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Update failed");
            }
            return ResponseEntity.status(HttpStatus.OK).body("Topic " + topic.getId() + " has been updated");
        });
    }

    public ResponseEntity<?> rate(Rating rating, Integer topicId, String login) {
        return transactionTemplate.execute(status -> {
            try {
                Client client = clientRepository.findByLogin(login).orElseThrow(Exception::new);
                Topic topic = topicRepository.findById(topicId)
                        .orElseThrow(() -> new NoSuchElementException("Topic not found"));
                Optional<Rating> optionalRating = ratingRepository.findRatingByCreatorAndTopic(client, topic);
                if (optionalRating.isPresent()) {
                    ratingRepository.updateRatingByClientAndTopic(login, rating.getRating(), topicId);
                    return ResponseEntity.status(HttpStatus.OK).body("Оценка успешно обновлена");
                } else {
                    rating.setCreator(client);
                    rating.setTopic(topic);
                    Rating addedRating = ratingRepository.save(rating);
                    return ResponseEntity.ok(addedRating);
                }
            } catch (NoSuchElementException e) {
                status.setRollbackOnly();
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
            } catch (Exception e) {
                status.setRollbackOnly();
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Не удалось оценить тему");
            }
        });
    }

}
