package de.bjarnerest.asechat.instruction;

import de.bjarnerest.asechat.helper.InstructionNameHelper;
import de.bjarnerest.asechat.model.Station;

public class SystemAuthenticateInstruction extends BaseInstruction {

  private final String password;

  public SystemAuthenticateInstruction(Station origin) {
    this(origin, "");
  }

  public SystemAuthenticateInstruction(Station origin, String password) {
    super(origin);
    this.password = password;
  }

  @Override
  public String toString() {
    String stringRepresentation = InstructionNameHelper.getNameForInstruction(this.getClass());
    if(!this.password.isEmpty()) {
      stringRepresentation += "=" + this.password;
    }

    return stringRepresentation;
  }
}
