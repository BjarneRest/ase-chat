package de.bjarnerest.asechat.instruction;

import de.bjarnerest.asechat.helper.InstructionNameHelper;
import de.bjarnerest.asechat.model.Station;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class ChatInfoInstruction extends BaseInstruction {

  private final int connectedClientsAmount;

  public ChatInfoInstruction(Station origin) {
    this(origin, -1);
  }

  public ChatInfoInstruction(Station origin, int connectedClientsAmount) {
    super(origin);
    this.connectedClientsAmount = connectedClientsAmount;
  }

  @SuppressWarnings("unused")
  @Contract("_, _ -> new")
  public static @NotNull ChatInfoInstruction fromString(String stringRepresentation, Station origin)
      throws InstructionInvalidException {

    String[] split = splitInstruction(stringRepresentation);

    if (!split[0].equals(InstructionNameHelper.getNameForInstruction(ChatInfoInstruction.class))) {
      throw new InstructionInvalidException();
    }

    // Try to parse payload
    String connectedClientsStr = split.length == 2 ? split[1] : "-1";

    return new ChatInfoInstruction(origin, Integer.parseInt(connectedClientsStr));

  }

  @Override
  public String toString() {
    String stringRepresentation = InstructionNameHelper.getNameForInstruction(this.getClass());
    if (this.connectedClientsAmount != -1) {
      stringRepresentation += "=" + this.connectedClientsAmount;
    }

    return stringRepresentation;
  }

  public int getConnectedClientsAmount() {
    return connectedClientsAmount;
  }
}
