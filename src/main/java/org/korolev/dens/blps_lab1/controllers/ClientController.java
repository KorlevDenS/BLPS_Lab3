package org.korolev.dens.blps_lab1.controllers;

import org.korolev.dens.blps_lab1.entites.Client;
import org.korolev.dens.blps_lab1.entites.Notification;
import org.korolev.dens.blps_lab1.repositories.ClientRepository;
import org.korolev.dens.blps_lab1.repositories.NotificationRepository;
import org.korolev.dens.blps_lab1.requests.ClientLoginRequest;
import org.korolev.dens.blps_lab1.responces.JwtAuthenticationResponse;
import org.korolev.dens.blps_lab1.services.JwtTokenService;
import org.korolev.dens.blps_lab1.services.PasswordService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/client")
public class ClientController {

    private final PasswordService passwordService;
    private final ClientRepository clientRepository;
    private final JwtTokenService jwtTokenService;
    private final NotificationRepository notificationRepository;

    public ClientController(PasswordService passwordService,
                            ClientRepository clientRepository, JwtTokenService jwtTokenService,
                            NotificationRepository notificationRepository) {
        this.passwordService = passwordService;
        this.clientRepository = clientRepository;
        this.jwtTokenService = jwtTokenService;
        this.notificationRepository = notificationRepository;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerClient(@RequestBody @Validated(Client.New.class) Client client) {
        client.setPassword(passwordService.makeBCryptHash(client.getPassword()));
        Client savedClient;
        savedClient = clientRepository.save(client);
        String jwt = jwtTokenService.generateToken(savedClient.getId(), "futureRole");
        return ResponseEntity.ok(new JwtAuthenticationResponse(jwt));
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginClient(@RequestBody ClientLoginRequest clientLoginRequest) {
        Optional<Client> optionalClient = clientRepository.findByLogin(clientLoginRequest.login());
        if (optionalClient.isPresent()) {
            Client client = optionalClient.get();
            if (passwordService.checkIdentity(clientLoginRequest.password(), client.getPassword())) {
                String jwt = jwtTokenService.generateToken(client.getId(), "futureRole");
                return ResponseEntity.ok(new JwtAuthenticationResponse(jwt));
            }
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Неверное имя пользователя или пароль");
    }

    @GetMapping("/get/notifications")
    public ResponseEntity<?> getClientNotifications(@RequestAttribute(name = "Cid") Integer CID) {
        Optional<Client> optionalClient = clientRepository.findById(CID);
        if (optionalClient.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Пользователь не авторизован");
        } else {
            List<Notification> notifications = notificationRepository.findAllByRecipient(optionalClient.get());
            return ResponseEntity.ok(notifications);
        }
    }

}
