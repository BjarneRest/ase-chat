package de.bjarnerest.asechat.instruction;

import de.bjarnerest.asechat.helper.InstructionNameHelper;
import de.bjarnerest.asechat.model.Station;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class SystemErrorInstruction extends BaseInstruction {

  private final @NotNull String cause;

  public SystemErrorInstruction(Station origin, @NotNull String cause) {
    super(origin);
    this.cause = cause;
  }

  @Contract("_, _ -> new")
  public static @NotNull SystemErrorInstruction fromString(String stringRepresentation, Station origin)
      throws InstructionInvalidException {

    String[] split = splitInstruction(stringRepresentation);

    if (!split[0].equals(InstructionNameHelper.getNameForInstruction(SystemErrorInstruction.class))) {
      throw new InstructionInvalidException();
    }

    // Try to parse payload
    return new SystemErrorInstruction(origin, split[1]);

  }

  public @NotNull String getCause() {
    return cause;
  }

  @Override
  public String toString() {

    return InstructionNameHelper.getNameForInstruction(this.getClass())
        + "=" + this.cause;
  }

}
