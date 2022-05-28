package de.bjarnerest.asechat.model;

import java.util.Arrays;

public enum AnsiColor {
  RESET("\u001B[0m"),
  BLACK("\u001B[30m"),
  RED("\u001B[31m"),
  GREEN("\u001B[32m"),
  YELLOW("\u001B[33m"),
  BLUE("\u001B[34m"),
  PURPLE("\u001B[35m"),
  CYAN("\u001B[36m"),
  WHITE("\u001B[37m");

  public static final AnsiColor[] colors = Arrays.copyOfRange(AnsiColor.values(), 1, AnsiColor.values().length);
  public final String code;

  AnsiColor(String code) {
    this.code = code;
  }
}
