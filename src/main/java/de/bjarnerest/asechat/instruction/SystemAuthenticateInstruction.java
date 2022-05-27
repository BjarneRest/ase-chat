package de.bjarnerest.asechat.instruction;

import de.bjarnerest.asechat.helper.InstructionNameHelper;
import de.bjarnerest.asechat.model.Station;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class SystemAuthenticateInstruction extends BaseInstruction {

  private final String password;

  public SystemAuthenticateInstruction(Station origin) {
    this(origin, "");
  }

  public SystemAuthenticateInstruction(Station origin, String password) {
    super(origin);
    this.password = password;
  }

  @Contract("_, _ -> new")
  public static @NotNull SystemAuthenticateInstruction fromString(String stringRepresentation, Station origin)
      throws InstructionInvalidException {

    String[] split = splitInstruction(stringRepresentation);

    if (!split[0].equals(InstructionNameHelper.getNameForInstruction(SystemAuthenticateInstruction.class))) {
      throw new InstructionInvalidException();
    }

    // Try to parse payload
    String password = split.length == 2 ? split[1] : "";

    return new SystemAuthenticateInstruction(origin, password);

  }

  @Override
  public String toString() {
    String stringRepresentation = InstructionNameHelper.getNameForInstruction(this.getClass());
    if (!this.password.isEmpty()) {
      stringRepresentation += "=" + this.password;
    }

    return stringRepresentation;
  }

  public String getPassword() {
    return password;
  }

}
