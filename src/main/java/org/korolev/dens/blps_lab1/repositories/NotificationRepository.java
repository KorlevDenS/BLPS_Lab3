package org.korolev.dens.blps_lab1.repositories;

import org.korolev.dens.blps_lab1.entites.Client;
import org.korolev.dens.blps_lab1.entites.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.sound.midi.Receiver;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Integer> {

    List<Notification> findAllByRecipient(Client recipient);
}