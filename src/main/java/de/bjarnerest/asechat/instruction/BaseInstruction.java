package de.bjarnerest.asechat.instruction;

import de.bjarnerest.asechat.model.Station;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public abstract class BaseInstruction {

  public final Station origin;

  protected BaseInstruction(Station origin) {
    this.origin = origin;
  }

  public static String[] splitInstruction(String stringRepresentation) {
    return stringRepresentation.split("=", 2);
  }

  @SuppressWarnings("unused")
  @Contract("_, _ -> fail")
  public static @NotNull BaseInstruction fromString(@NotNull String stringRepresentation, Station origin)
      throws InstructionInvalidException {

    throw new InstructionInvalidException();

  }

  public abstract String toString();

  public Station getOrigin() {
    return origin;
  }

}
