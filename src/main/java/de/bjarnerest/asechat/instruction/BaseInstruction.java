package de.bjarnerest.asechat.instruction;

import de.bjarnerest.asechat.model.Station;

public abstract class BaseInstruction {

  public final Station origin;

  protected BaseInstruction(Station origin) {
    this.origin = origin;
  }

  public static String[] splitInstruction(String stringRepresentation) {
    return stringRepresentation.split("=", 2);
  }

  public abstract String toString();

}
