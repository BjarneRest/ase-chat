package de.bjarnerest.asechat.instruction;


import de.bjarnerest.asechat.helper.InstructionNameHelper;
import de.bjarnerest.asechat.model.Station;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class SystemReadyInstruction extends BaseInstruction {


  public SystemReadyInstruction(Station origin) {
    super(origin);
  }

  @Contract("_, _ -> new")
  public static @NotNull SystemReadyInstruction fromString(@NotNull String stringRepresentation, Station origin)
      throws InstructionInvalidException {

    if (stringRepresentation.equals(InstructionNameHelper.getNameForInstruction(SystemReadyInstruction.class))) {
      return new SystemReadyInstruction(origin);
    }

    throw new InstructionInvalidException();

  }

  @Override
  public String toString() {
    return InstructionNameHelper.getNameForInstruction(this.getClass());
  }

}
