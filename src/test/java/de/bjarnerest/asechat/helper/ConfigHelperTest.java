package de.bjarnerest.asechat.helper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URL;
import org.apache.commons.configuration2.Configuration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ConfigHelperTest {

  private ConfigHelper subject;

  @BeforeEach
  void setUp() {
    subject = ConfigHelper.forceNewInstance();
  }

  @Test
  void testDefaultConfig() {
    subject.initTemp();
    Configuration config = subject.getConfig();

    assertEquals(25531, config.getInt("server.port"));
    assertEquals("0.0.0.0", config.getString("server.host"));
    assertEquals("", config.getString("server.password"));

    assertEquals(25531, config.getInt("client.port"));
    assertEquals("127.0.0.1", config.getString("client.host"));
    assertEquals("", config.getString("client.password"));
    assertFalse(config.getString("client.username").isEmpty());

  }

  @Test
  void testDummyConfig() {
    // Get resource
    ClassLoader classLoader = getClass().getClassLoader();
    URL resource = classLoader.getResource("dummy.properties");
    assert resource != null;

    subject.init(resource.getPath());
    Configuration config = subject.getConfig();

    assertEquals(12345, config.getInt("server.port"));
    assertEquals("1.2.3.4", config.getString("server.host"));
    assertEquals("secret", config.getString("server.password"));

    assertEquals(54321, config.getInt("client.port"));
    assertEquals("127.0.0.2", config.getString("client.host"));
    assertEquals("pass123", config.getString("client.password"));
    assertEquals("Eddie", config.getString("client.username"));

  }

  @Test
  void readyCheck() {

    assertFalse(subject.isReady());
    subject.initTemp();
    assertTrue(subject.isReady());

  }

}
