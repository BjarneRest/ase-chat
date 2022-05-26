package de.bjarnerest.asechat.instruction;

import com.google.gson.stream.MalformedJsonException;
import de.bjarnerest.asechat.helper.InstructionNameHelper;
import de.bjarnerest.asechat.model.Message;
import de.bjarnerest.asechat.model.Station;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class ChatMessageEchoInstruction extends ChatMessageSendInstruction {

  public ChatMessageEchoInstruction(Station origin, @NotNull Message message) {
    super(origin, message);
  }

  @Override
  public String toString() {

    return InstructionNameHelper.getNameForInstruction(this.getClass())
    + "=" + this.message.toJson();
  }

  @Contract("_, _ -> new")
  public static @NotNull ChatMessageEchoInstruction fromString(String stringRepresentation, Station origin) throws InstructionInvalidException {

    String[] split = splitInstruction(stringRepresentation);
    if(!split[0].equals(InstructionNameHelper.getNameForInstruction(ChatMessageEchoInstruction.class))) {
      throw new InstructionInvalidException();
    }

    // Try to parse payload
    try {
      Message newMessage = Message.fromJson(split[1]);
      return new ChatMessageEchoInstruction(origin, newMessage);
    } catch (MalformedJsonException e) {
      throw new InstructionInvalidException();
    }

  }

}
