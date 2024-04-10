package org.korolev.dens.blps_lab1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

@SpringBootApplication
@ServletComponentScan
public class BLPSLab1Application {

    public static void main(String[] args) {
        SpringApplication.run(BLPSLab1Application.class, args);
    }

}
