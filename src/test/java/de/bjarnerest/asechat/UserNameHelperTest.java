package de.bjarnerest.asechat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.bjarnerest.asechat.helper.UserNameHelper;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

public class UserNameHelperTest {

  @Test
  void testUserNameGeneratorProducesUniqueNames() {

    final Set<String> usernameSet = new HashSet<>();

    for (int i = 0; i < 100; i++) {
      usernameSet.add(UserNameHelper.generateUsername());
    }

    // When duplicates have occurred, the set size would not increase
    assertEquals(100, usernameSet.size());

  }

  @Test
  void testUserNameNotEmpty() {

    assertFalse(UserNameHelper.generateUsername().isEmpty());

  }

  @Test
  void testUserNameBeginning() {

    assertTrue(UserNameHelper.generateUsername().startsWith("User_"));

  }

}
