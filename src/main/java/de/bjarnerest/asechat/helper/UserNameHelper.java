package de.bjarnerest.asechat.helper;

import java.util.Random;
import org.jetbrains.annotations.NotNull;

public class UserNameHelper {

  private static final String capitalLetters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
  private static final String lowerLetters = "abcdefghijklmnopqrstuvwxyz";
  private static final String numbers = "0123456789";
  private static final Random random = new Random();


  public static @NotNull String generateUsername() {
    @SuppressWarnings("StringBufferReplaceableByString")
    StringBuilder out = new StringBuilder(10);
    out.append("User_");

    // 1 capital letter
    out.append(capitalLetters.charAt(random.nextInt(capitalLetters.length())));

    // 2 lower letters
    out.append(lowerLetters.charAt(random.nextInt(lowerLetters.length())));
    out.append(lowerLetters.charAt(random.nextInt(lowerLetters.length())));

    // 2 numbers
    out.append(numbers.charAt(random.nextInt(numbers.length())));
    out.append(numbers.charAt(random.nextInt(numbers.length())));

    return out.toString();
  }

}
