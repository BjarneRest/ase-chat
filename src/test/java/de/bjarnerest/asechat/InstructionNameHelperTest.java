package de.bjarnerest.asechat;

import static org.junit.jupiter.api.Assertions.*;

import de.bjarnerest.asechat.helper.InstructionNameHelper;
import de.bjarnerest.asechat.instruction.SystemAuthenticateInstruction;
import de.bjarnerest.asechat.instruction.SystemReadyInstruction;
import org.junit.jupiter.api.Test;

public class InstructionNameHelperTest {

  @Test
  void testWithSystemReadyInstruction() {

    assertEquals("system:ready", InstructionNameHelper.getNameForInstruction(SystemReadyInstruction.class));

  }

  @Test
  void testWithSystemAuthenticateInstruction() {

    assertEquals("system:authenticate", InstructionNameHelper.getNameForInstruction(SystemAuthenticateInstruction.class));

  }


}
