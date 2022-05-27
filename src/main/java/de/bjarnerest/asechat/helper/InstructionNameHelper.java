package de.bjarnerest.asechat.helper;

import de.bjarnerest.asechat.instruction.BaseInstruction;
import de.bjarnerest.asechat.instruction.InstructionInvalidException;
import de.bjarnerest.asechat.model.Station;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.reflections.Reflections;

public class InstructionNameHelper {

  public static @NotNull String getNameForInstruction(@NotNull Class<? extends BaseInstruction> instructionClass) {
    StringBuilder instructionName = new StringBuilder();
    for (String w : instructionClass.getSimpleName().split("(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])")) {
      if (w.equalsIgnoreCase("instruction")) {
        continue;
      }
      if (instructionName.length() != 0) {
        instructionName.append(':');
      }
      instructionName.append(w.toLowerCase());
    }

    return instructionName.toString();

  }

  public static BaseInstruction parseInstruction(String stringRepresentation, Station origin)
      throws InstructionInvalidException {

    Reflections reflections = new Reflections(BaseInstruction.class.getPackageName());
    Set<Class<? extends BaseInstruction>> subClasses = reflections.getSubTypesOf(BaseInstruction.class);

    final List<Class<? extends BaseInstruction>> resultClass = new ArrayList<>();

    String searchedName = BaseInstruction.splitInstruction(stringRepresentation)[0];

    subClasses.stream()
        .filter(aClass -> getNameForInstruction(aClass).equals(searchedName))
        .findFirst()
        .ifPresent(resultClass::add);

    if (resultClass.isEmpty()) {
      throw new InstructionInvalidException("No class found");
    }

    Class<? extends BaseInstruction> instructionClass = resultClass.get(0);
    try {
      Object[] args = {stringRepresentation, origin};
      Class<?>[] argTypes = {String.class, Station.class};
      Object instance = instructionClass.getMethod("fromString", argTypes).invoke(null, args);
      return (BaseInstruction) instance;
    } catch (Exception e) {
      throw new InstructionInvalidException(e);
    }


  }


}
