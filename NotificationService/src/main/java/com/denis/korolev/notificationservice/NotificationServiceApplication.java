package com.denis.korolev.notificationservice;

import com.denis.korolev.notificationservice.services.MessagesService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
@ServletComponentScan
public class NotificationServiceApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(NotificationServiceApplication.class, args);
        MessagesService messagesService = context.getBean(MessagesService.class);
        messagesService.startListener();
    }

}
