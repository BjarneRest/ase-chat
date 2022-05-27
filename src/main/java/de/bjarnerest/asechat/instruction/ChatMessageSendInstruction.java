package de.bjarnerest.asechat.instruction;

import com.google.gson.stream.MalformedJsonException;
import de.bjarnerest.asechat.helper.InstructionNameHelper;
import de.bjarnerest.asechat.model.Message;
import de.bjarnerest.asechat.model.Station;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class ChatMessageSendInstruction extends BaseInstruction {

  protected final @NotNull Message message;

  public ChatMessageSendInstruction(Station origin, @NotNull Message message) {
    super(origin);
    this.message = message;
  }

  @SuppressWarnings("unused")
  @Contract("_, _ -> new")
  public static @NotNull ChatMessageSendInstruction fromString(String stringRepresentation, Station origin)
      throws InstructionInvalidException {

    String[] split = splitInstruction(stringRepresentation);
    if (!split[0].equals(InstructionNameHelper.getNameForInstruction(ChatMessageSendInstruction.class))) {
      throw new InstructionInvalidException();
    }

    // Try to parse payload
    try {
      Message newMessage = Message.fromJson(split[1]);
      return new ChatMessageSendInstruction(origin, newMessage);
    } catch (MalformedJsonException e) {
      throw new InstructionInvalidException();
    }

  }

  public @NotNull Message getMessage() {
    return message;
  }

  @Override
  public String toString() {

    return InstructionNameHelper.getNameForInstruction(this.getClass())
        + "=" + this.message.toJson();
  }

}
