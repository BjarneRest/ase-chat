package de.bjarnerest.asechat.helper;

import java.util.Random;

public class UserNameHelper {

    String username;
    private String capitalLetters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private String lowerLetters = "abcdefghijklmnopqrstuvwxyz";
    private String numbers = "0123456789";
    Random random = new Random();

    public UserNameHelper() {
        username = generateUsername();
    }

    private String generateUsername() {
        String generatedUsername = "";
        generatedUsername = generatedUsername + capitalLetters.charAt(random.nextInt(capitalLetters.length()));
        generatedUsername = generatedUsername + lowerLetters.charAt(random.nextInt(lowerLetters.length()));
        generatedUsername = generatedUsername + lowerLetters.charAt(random.nextInt(lowerLetters.length()));
        generatedUsername = generatedUsername + numbers.charAt(random.nextInt(numbers.length()));
        generatedUsername = generatedUsername + numbers.charAt(random.nextInt(numbers.length()));
        return generatedUsername;
    }

}
