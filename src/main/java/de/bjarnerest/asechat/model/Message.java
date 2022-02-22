package de.bjarnerest.asechat.model;

import com.google.gson.Gson;
import com.google.gson.stream.MalformedJsonException;

import java.util.Objects;

public class Message {

    private String messageText;
    private String messageSenderName;

    public Message(String messageText, String messageSenderName) {
        this.messageText = messageText;
        this.messageSenderName = messageSenderName;
    }

    public static Message fromJson(String json) throws MalformedJsonException {
        return new Gson().fromJson(json, Message.class);
    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public String getMessageSenderName() {
        return messageSenderName;
    }

    public void setMessageSenderName(String messageSenderName) {
        this.messageSenderName = messageSenderName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Message message = (Message) o;
        return Objects.equals(messageText, message.messageText) && Objects.equals(messageSenderName, message.messageSenderName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(messageText, messageSenderName);
    }

    @Override
    public String toString() {
        return "Message{" +
                "messageText='" + messageText + '\'' +
                ", messageSenderName='" + messageSenderName + '\'' +
                '}';
    }

    public String toJson() {
        return new Gson().toJson(this);
    }

}
