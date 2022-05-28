package de.bjarnerest.asechat.model;

import com.google.gson.Gson;

public class User {

  private String username;
  private AnsiColor color;

  public User(String username) {
    this.username = username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public AnsiColor getColor() {
    return color;
  }

  public void setColor(AnsiColor color) {
    this.color = color;
  }

  public User(String username, AnsiColor color) {


    this.username = username;
    this.color = color;
  }

  public static User fromJson(String json) {
    return new Gson().fromJson(json, User.class);
  }

  public String getUsername() {
    return username;
  }

  public String toJson() {
    return new Gson().toJson(this);
  }

}
