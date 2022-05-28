package de.bjarnerest.asechat.model;

import com.google.gson.Gson;

public class PngImage {

  private String fileName;
  private byte[] bytes;

  public String getFileName() {
    return fileName;
  }

  public byte[] getBytes() {
    return bytes;
  }

  public PngImage(String fileName, byte[] bytes) {
    this.fileName = fileName;
    this.bytes = bytes;
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
}
