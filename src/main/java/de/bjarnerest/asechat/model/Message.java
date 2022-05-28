package de.bjarnerest.asechat.model;

import com.google.gson.Gson;
import com.google.gson.stream.MalformedJsonException;
import java.util.Objects;
import java.util.UUID;

public class Message {

  private final UUID id = UUID.randomUUID();
  private String messageText;
  private User messageSender;

  public Message(String messageText, User messageSender) {

    this.messageText = messageText;
    this.messageSender = messageSender;

  }

  public static Message fromJson(String json) throws MalformedJsonException {

    return new Gson().fromJson(json, Message.class);

  }

  public UUID getId() {
    return id;
  }

  public String getMessageText() {

    return messageText;

  }

  public void setMessageText(String messageText) {

    this.messageText = messageText;

  }

  public User getMessageSender() {

    return messageSender;

  }

  public void setMessageSender(User messageSender) {

    this.messageSender = messageSender;

  }

  @Override
  public boolean equals(Object o) {

    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    Message message = (Message) o;
    return Objects.equals(messageText, message.messageText) && Objects.equals(messageSender, message.messageSender);

  }

  @Override
  public int hashCode() {

    return Objects.hash(messageText, messageSender);

  }

  @Override
  public String toString() {

    return "Message{" +
        "messageText='" + messageText + '\'' +
        ", messageSenderName='" + messageSender + '\'' +
        '}';

  }

  public String toJson() {

    return new Gson().toJson(this);

  }

}
