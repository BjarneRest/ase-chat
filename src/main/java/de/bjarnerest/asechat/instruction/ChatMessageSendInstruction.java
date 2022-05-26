package de.bjarnerest.asechat.instruction;

import de.bjarnerest.asechat.model.Station;

public class ChatMessageSendInstruction extends BaseInstruction {

  protected ChatMessageSendInstruction(Station origin) {
    super(origin);
  }

  @Override
  public String toString() {
    return null;
  }
}
