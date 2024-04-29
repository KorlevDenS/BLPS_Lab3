package org.korolev.dens.blps_lab1.controllers;

import org.korolev.dens.blps_lab1.entites.Chapter;
import org.korolev.dens.blps_lab1.entites.Client;
import org.korolev.dens.blps_lab1.repositories.ChapterRepository;
import org.korolev.dens.blps_lab1.repositories.ClientRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/chapter")
public class ChapterController {

    private final ClientRepository clientRepository;
    private final ChapterRepository chapterRepository;

    public ChapterController(ClientRepository clientRepository,
                             ChapterRepository chapterRepository) {
        this.clientRepository = clientRepository;
        this.chapterRepository = chapterRepository;
    }

    @PostMapping("/add")
    public ResponseEntity<?> addChapter(@RequestBody Chapter chapter, @RequestAttribute(name = "Cid") Integer CID) {
        Optional<Client> optionalClient = clientRepository.findById(CID);
        if (optionalClient.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Пользователь не авторизован");
        }
        chapter.setCreator(optionalClient.get());
        Chapter addedChapter = chapterRepository.save(chapter);
        return ResponseEntity.ok(addedChapter);
    }

    @GetMapping("/get/all")
    public List<Chapter> getAllChapters() {
        return chapterRepository.findAll();
    }

}
