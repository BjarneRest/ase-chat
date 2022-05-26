package de.bjarnerest.asechat.instruction;

import de.bjarnerest.asechat.helper.InstructionNameHelper;
import de.bjarnerest.asechat.model.Station;

public abstract class BaseInstruction {

  public final Station origin;

  protected BaseInstruction(Station origin) {
    this.origin = origin;
  }

  public abstract String toString();

  protected static String[] splitInstruction(String stringRepresentation) {
    return stringRepresentation.split("=", 1);
  }

}
