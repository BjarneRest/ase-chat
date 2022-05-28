package de.bjarnerest.asechat.helper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.bjarnerest.asechat.helper.InstructionNameHelper;
import de.bjarnerest.asechat.instruction.ChatLeaveInstruction;
import de.bjarnerest.asechat.instruction.InstructionInvalidException;
import de.bjarnerest.asechat.instruction.SystemAuthenticateInstruction;
import de.bjarnerest.asechat.instruction.SystemErrorInstruction;
import de.bjarnerest.asechat.instruction.SystemReadyInstruction;
import de.bjarnerest.asechat.model.Station;
import org.junit.jupiter.api.Test;

public class InstructionNameHelperTest {

  @Test
  void testClassToName() {

    assertEquals("system:ready", InstructionNameHelper.getNameForInstruction(SystemReadyInstruction.class));
    assertEquals("system:authenticate", InstructionNameHelper.getNameForInstruction(SystemAuthenticateInstruction.class));

  }

  @Test
  void testNameToClass() throws InstructionInvalidException {

    assertEquals(ChatLeaveInstruction.class, InstructionNameHelper.parseInstruction("chat:leave", Station.CLIENT).getClass());
    assertEquals(
        SystemReadyInstruction.class,
        InstructionNameHelper.parseInstruction("system:ready", Station.SERVER).getClass()
    );
    assertEquals(
        SystemErrorInstruction.class,
        InstructionNameHelper.parseInstruction("system:error=parsing", Station.SERVER).getClass()
    );

  }


}
