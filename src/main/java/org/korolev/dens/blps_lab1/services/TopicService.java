package org.korolev.dens.blps_lab1.services;

import jakarta.annotation.Nullable;
import org.korolev.dens.blps_lab1.entites.*;
import org.korolev.dens.blps_lab1.repositories.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.*;

@Service
public class TopicService {

    private final TransactionTemplate transactionTemplate;
    private final ClientRepository clientRepository;
    private final TopicRepository topicRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final RatingRepository ratingRepository;
    private final ChapterRepository chapterRepository;
    private final ImageRepository imageRepository;


    public TopicService(PlatformTransactionManager platformTransactionManager, ClientRepository clientRepository,
                        TopicRepository topicRepository, SubscriptionRepository subscriptionRepository,
                        RatingRepository ratingRepository, ChapterRepository chapterRepository, ImageRepository imageRepository) {
        this.transactionTemplate = new TransactionTemplate(platformTransactionManager);
        this.clientRepository = clientRepository;
        this.topicRepository = topicRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.ratingRepository = ratingRepository;
        this.chapterRepository = chapterRepository;
        this.imageRepository = imageRepository;
        this.transactionTemplate.setIsolationLevel(TransactionDefinition.ISOLATION_SERIALIZABLE);

    }

    public ResponseEntity<?> add(Topic topic, Integer chapterId, String login, @Nullable MultipartFile img1,
                                 @Nullable MultipartFile img2, @Nullable MultipartFile img3) {
        return transactionTemplate.execute((TransactionCallback<ResponseEntity<?>>) status -> {
            Optional<Chapter> optionalChapter = chapterRepository.findById(chapterId);
            Optional<Client> optionalClient = clientRepository.findByLogin(login);
            if (optionalClient.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Пользователь не авторизован");
            } else if (optionalChapter.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No chapter with id " + chapterId);
            }
            topic.setOwner(optionalClient.get());
            topic.setChapter(optionalChapter.get());
            Topic addedTopic = topicRepository.save(topic);

            List<String> imgLinks = new ArrayList<>();
            try {
                if (img1 != null) {
                    imgLinks.add(uploadImage(img1, 1, addedTopic.getId()).toString());
                }
                if (img2 != null) {
                    imgLinks.add(uploadImage(img2, 2, addedTopic.getId()).toString());
                }
                if (img3 != null) {
                    imgLinks.add(uploadImage(img3, 3, addedTopic.getId()).toString());
                }
            } catch (IOException e) {
                status.setRollbackOnly();
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("Unable to save topic because we cannot upload the image");
            }
            for (String imgLink : imgLinks) {
                Image image = new Image();
                image.setTopic(addedTopic);
                image.setLink(imgLink);
                image.setCreated(LocalDate.now());
                imageRepository.save(image);
            }
            return ResponseEntity.ok("DOWNLOAD COMPLETE");
        });
    }

    private Path uploadImage(MultipartFile img, int i, int id) throws IOException {
        String storage = System.getenv("PHOTO_STORAGE") + "//";
        Path imagePath = Paths.get(storage  + "t" + id + "_i" + i + img.getOriginalFilename());
        return Files.write(imagePath, img.getBytes());
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
