package org.korolev.dens.blps_lab1.controllers;

import org.korolev.dens.blps_lab1.entites.Client;
import org.korolev.dens.blps_lab1.entites.Notification;
import org.korolev.dens.blps_lab1.repositories.ClientRepository;
import org.korolev.dens.blps_lab1.repositories.NotificationRepository;
import org.korolev.dens.blps_lab1.requests.ClientLoginRequest;
import org.korolev.dens.blps_lab1.responces.JwtAuthenticationResponse;
import org.korolev.dens.blps_lab1.security.ClientService;
import org.korolev.dens.blps_lab1.services.JwtTokenService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/client")
public class ClientController {

    private final AuthenticationManager authenticationManager;
    private final ClientRepository clientRepository;
    private final JwtTokenService jwtTokenService;
    private final NotificationRepository notificationRepository;

    private final ClientService clientService;

    public ClientController(AuthenticationManager authenticationManager,
                            ClientRepository clientRepository, JwtTokenService jwtTokenService,
                            NotificationRepository notificationRepository, ClientService clientService) {
        this.clientService = clientService;
        this.clientRepository = clientRepository;
        this.jwtTokenService = jwtTokenService;
        this.notificationRepository = notificationRepository;
        this.authenticationManager = authenticationManager;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerClientN(@RequestBody @Validated(Client.New.class) Client client) {
        return clientService.addClient(client);
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginClient(@RequestBody ClientLoginRequest clientLoginRequest) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                clientLoginRequest.login(), clientLoginRequest.password()
        ));
        return ResponseEntity
                .ok(new JwtAuthenticationResponse(jwtTokenService.generateToken(clientLoginRequest.login())));
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/get/notifications")
    public ResponseEntity<?> getClientNotifications(@AuthenticationPrincipal UserDetails userDetails) {
        Optional<Client> optionalClient = clientRepository.findByLogin(userDetails.getUsername());
        if (optionalClient.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Пользователь не авторизован");
        } else {
            List<Notification> notifications = notificationRepository.findAllByRecipient(optionalClient.get());
            return ResponseEntity.ok(notifications);
        }
    }

}
