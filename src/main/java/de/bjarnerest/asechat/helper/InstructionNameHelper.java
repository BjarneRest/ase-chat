package de.bjarnerest.asechat.helper;

import de.bjarnerest.asechat.instruction.BaseInstruction;
import org.jetbrains.annotations.NotNull;

public class InstructionNameHelper {

  public static @NotNull String getNameForInstruction(@NotNull Class<? extends BaseInstruction> instructionClass) {
    StringBuilder instructionName = new StringBuilder();
    for (String w : instructionClass.getSimpleName().split("(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])")) {
      if (w.equalsIgnoreCase("instruction")) continue;
      if(instructionName.length() != 0)
        instructionName.append(':');
      instructionName.append(w.toLowerCase());
    }

    return instructionName.toString();

  }


}
