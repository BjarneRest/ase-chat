package de.bjarnerest.asechat.instruction;

import de.bjarnerest.asechat.helper.InstructionNameHelper;
import de.bjarnerest.asechat.model.AnsiColor;
import de.bjarnerest.asechat.model.Station;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class ChatChangeColorInstruction extends BaseInstruction {

  private final @NotNull AnsiColor color;

  public ChatChangeColorInstruction(Station origin, @NotNull AnsiColor color) {
    super(origin);
    this.color = color;
  }

  @SuppressWarnings("unused")
  @Contract("_, _ -> new")
  public static @NotNull ChatChangeColorInstruction fromString(String stringRepresentation, Station origin)
      throws InstructionInvalidException {

    String[] split = splitInstruction(stringRepresentation);

    if (!split[0].equals(InstructionNameHelper.getNameForInstruction(ChatChangeColorInstruction.class))) {
      throw new InstructionInvalidException();
    }

    // Try to parse payload
    return new ChatChangeColorInstruction(origin, AnsiColor.valueOf(split[1]));

  }

  public @NotNull AnsiColor getColor() {
    return color;
  }

  @Override
  public String toString() {

    return InstructionNameHelper.getNameForInstruction(this.getClass())
        + "=" + this.color;
  }

}
