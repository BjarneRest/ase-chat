package de.bjarnerest.asechat.model;

import com.google.gson.Gson;

public class PngImage {

  private String fileName;
  private byte[] bytes;

  private User sender;

  public String getFileName() {
    return fileName;
  }

  public byte[] getBytes() {
    return bytes;
  }

  public PngImage(String fileName, byte[] bytes, User sender) {
    this.fileName = fileName;
    this.bytes = bytes;
    this.sender = sender;
  }

  public static PngImage fromJson(String json) {
    return new Gson().fromJson(json, PngImage.class);
  }

  public String toJson() {
    return new Gson().toJson(this);
  }

  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  public void setBytes(byte[] bytes) {
    this.bytes = bytes;
  }

  public User getSender() {
    return sender;
  }

  public void setSender(User sender) {
    this.sender = sender;
  }
}
