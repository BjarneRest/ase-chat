package de.bjarnerest.asechat.helper;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.logging.Logger;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class HashingHelper {

  private static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

  public static @NotNull String hashSha512WithSalt(@NotNull String subject) {

    SecureRandom random = new SecureRandom();
    byte[] salt = new byte[16];
    random.nextBytes(salt);
    byte[] byteArray = mergeByteArrays(salt, subject.getBytes(StandardCharsets.UTF_8));

    return bytesToHex(salt) + ":" + bytesToHex(hashSha512(byteArray));

  }

  public static boolean verifySha512WithSalt(@NotNull String subject, @NotNull String hashedRepresentation) {

      String[] splitString = hashedRepresentation.split(":");
      if(splitString.length != 2) {
        return false;
      }
      String saltRepresentation = splitString[0];
      String hashRepresentation = splitString[1];

      byte[] saltBytes = hexStringToByteArray(saltRepresentation);

      byte[] realHash = hashSha512(mergeByteArrays(saltBytes, subject.getBytes(StandardCharsets.UTF_8)));
      String realHashString = bytesToHex(realHash);

      return realHashString.equals(hashRepresentation);

  }

  private static byte[] hashSha512(byte[] subject) {

    byte[] hash = new byte[0];

    try {
      MessageDigest md = MessageDigest.getInstance("SHA-512");

      hash = md.digest(subject);

    } catch (NoSuchAlgorithmException e) {
      logger.severe(e.toString());
    }

    return hash;

  }

  private static byte @NotNull [] mergeByteArrays(byte @NotNull [] a, byte @NotNull [] b) {

    ArrayList<Byte> bytesList = new ArrayList<>();
    // Add a
    for (byte value : a) {
      bytesList.add(value);
    }
    // Add b
    for (byte value : b) {
      bytesList.add(value);
    }

    // Convert to array
    byte[] byteArray = new byte[bytesList.size()];
    for (int i = 0; i < bytesList.size(); i++) {
      byteArray[i] = bytesList.get(i);
    }

    return byteArray;

  }

  private static final byte[] HEX_ARRAY = "0123456789ABCDEF".getBytes(StandardCharsets.US_ASCII);

  @Contract(value = "_ -> new", pure = true)
  private static @NotNull String bytesToHex(byte @NotNull [] bytes) {
    byte[] hexChars = new byte[bytes.length * 2];
    for (int j = 0; j < bytes.length; j++) {
      int v = bytes[j] & 0xFF;
      hexChars[j * 2] = HEX_ARRAY[v >>> 4];
      hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
    }
    return new String(hexChars, StandardCharsets.UTF_8);
  }

  private static byte @NotNull [] hexStringToByteArray(@NotNull String s) {
    int len = s.length();
    byte[] data = new byte[len / 2];
    for (int i = 0; i < len; i += 2) {
      data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
          + Character.digit(s.charAt(i+1), 16));
    }
    return data;
  }


}
