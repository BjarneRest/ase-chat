package de.bjarnerest.asechat.instruction;


import de.bjarnerest.asechat.helper.InstructionNameHelper;
import de.bjarnerest.asechat.model.Station;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class ChatLeaveInstruction extends BaseInstruction {


  public ChatLeaveInstruction(Station origin) {
    super(origin);
  }

  @Contract("_, _ -> new")
  public static @NotNull ChatLeaveInstruction fromString(@NotNull String stringRepresentation, Station origin)
      throws InstructionInvalidException {

    if (stringRepresentation.equals(InstructionNameHelper.getNameForInstruction(ChatLeaveInstruction.class))) {
      return new ChatLeaveInstruction(origin);
    }

    throw new InstructionInvalidException();

  }

  @Override
  public String toString() {
    return InstructionNameHelper.getNameForInstruction(this.getClass());
  }

}

