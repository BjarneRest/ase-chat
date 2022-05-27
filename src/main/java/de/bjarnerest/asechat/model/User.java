package de.bjarnerest.asechat.model;

import com.google.gson.Gson;

public class User {

  private final String username;

  public User(String username) {
    this.username = username;
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
