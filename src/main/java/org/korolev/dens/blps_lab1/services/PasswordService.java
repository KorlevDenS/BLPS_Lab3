package org.korolev.dens.blps_lab1.services;

public interface PasswordService {

    String makeBCryptHash(String password);

    boolean checkIdentity(String rawPassword, String encodedPassword);

}
