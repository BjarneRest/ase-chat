package de.bjarnerest.asechat.client;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import de.bjarnerest.asechat.client.ChatRoomClient;
import de.bjarnerest.asechat.helper.InstructionNameHelper;
import de.bjarnerest.asechat.instruction.BaseInstruction;
import de.bjarnerest.asechat.instruction.ChangeUserInstruction;
import de.bjarnerest.asechat.instruction.ChatChangeColorInstruction;
import de.bjarnerest.asechat.instruction.ChatInfoInstruction;
import de.bjarnerest.asechat.instruction.ChatLeaveInstruction;
import de.bjarnerest.asechat.instruction.ChatMessageSendInstruction;
import de.bjarnerest.asechat.instruction.InstructionInvalidException;
import de.bjarnerest.asechat.model.AnsiColor;
import de.bjarnerest.asechat.model.Message;
import de.bjarnerest.asechat.model.Station;
import de.bjarnerest.asechat.model.User;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class ChatRoomClientTest {

  private final Socket fakeSocket = Mockito.mock(Socket.class);
  private ChatRoomClient clientSubject;
  private PipedInputStream fakeUserInput;

  private PipedOutputStream fakeUserOutput;

  private PipedInputStream fakeUserScreen;

  private PipedOutputStream mockInput;

  private BufferedReader mockOutputBuffered;

  private Thread clientThread;

  void prepareSubject() throws Exception {
    this.clientSubject = new ChatRoomClient(InetAddress.getByName("123.4.5.6"), 12345, "password", "username") {
      @Override
      protected Socket createSocket() {
        return fakeSocket;
      }

      @Override
      protected InputStream getUserInputStream() {
        return fakeUserInput;
      }

      @Override
      protected PrintStream getUserOutputStream() {
        PipedOutputStream pis = new PipedOutputStream();
        fakeUserScreen = new PipedInputStream();
        try {
          fakeUserScreen.connect(pis);
        }
        catch (IOException e) {
          throw new RuntimeException(e);
        }
        return new PrintStream(pis);
      }
    };
  }

  private Thread startClient() {
    Thread clientThread = new Thread(() -> {
      try {
        clientSubject.connectToServer();
      }
      catch (Exception e) {
        e.printStackTrace();
      }
    });
    clientThread.start();
    return clientThread;
  }

  @BeforeEach
  void setUp() throws Exception {
    Mockito.reset(fakeSocket);

    // Network input
    final PipedOutputStream pipedOutputStream = new PipedOutputStream();
    final PipedInputStream pipedInputStream = new PipedInputStream();
    Mockito.when(fakeSocket.getOutputStream())
        .thenReturn(pipedOutputStream);
    Mockito.when(fakeSocket.getInputStream())
        .thenReturn(pipedInputStream);

    final PipedInputStream mockOutput = new PipedInputStream();
    pipedOutputStream.connect(mockOutput);
    mockOutputBuffered = new BufferedReader(new InputStreamReader(mockOutput));

    mockInput = new PipedOutputStream();
    pipedInputStream.connect(mockInput);

    // User input
    fakeUserInput = new PipedInputStream();
    fakeUserOutput = new PipedOutputStream();
    fakeUserOutput.connect(fakeUserInput);

    prepareSubject();
    clientThread = startClient();
  }

  @AfterEach
  void tearDown() {
    clientThread.interrupt();
  }

  private void catchGreeting() throws IOException, InstructionInvalidException {

    mockInput.write("system:ready\n".getBytes(StandardCharsets.UTF_8));

    // Greeting message
    await()
        .atMost(Duration.ofSeconds(2))
        .until(mockOutputBuffered::ready);

    String line = mockOutputBuffered.readLine();
    BaseInstruction instruction = InstructionNameHelper.parseInstruction(line, Station.CLIENT);
    assertInstanceOf(ChatMessageSendInstruction.class, instruction);

    ChatMessageSendInstruction chatMessageSendInstruction = (ChatMessageSendInstruction) instruction;
    assertEquals("Hallo Welt. Hier ist username", chatMessageSendInstruction.getMessage().getMessageText());
    assertEquals("username", chatMessageSendInstruction.getMessage().getMessageSender().getUsername());


  }

  private void catchInfo() throws IOException, InstructionInvalidException {

    // Greeting message
    await()
        .atMost(Duration.ofSeconds(2))
        .until(mockOutputBuffered::ready);

    String line = mockOutputBuffered.readLine();
    BaseInstruction instruction = InstructionNameHelper.parseInstruction(line, Station.CLIENT);
    assertInstanceOf(ChatInfoInstruction.class, instruction);

    ChatInfoInstruction chatInfoInstruction = (ChatInfoInstruction) instruction;
    assertEquals(-1, chatInfoInstruction.getConnectedClientsAmount());

  }

  @Test
  void authenticationTest() throws Exception {

    mockInput.write("system:authenticate\n".getBytes(StandardCharsets.UTF_8));
    await()
        .atMost(Duration.ofSeconds(2))
        .until(mockOutputBuffered::ready);
    String line = mockOutputBuffered.readLine();

    assertEquals("system:authenticate=password", line);

  }

  @Test
  void userMessageTest() throws Exception {

    catchGreeting();
    catchInfo();

    fakeUserOutput.write("Hi!\n".getBytes(StandardCharsets.UTF_8));
    await()
        .atMost(Duration.ofSeconds(2))
        .until(mockOutputBuffered::ready);

    String line2 = mockOutputBuffered.readLine();
    BaseInstruction instruction2 = InstructionNameHelper.parseInstruction(line2, Station.CLIENT);
    assertInstanceOf(ChatMessageSendInstruction.class, instruction2);

    ChatMessageSendInstruction chatMessageSendInstruction2 = (ChatMessageSendInstruction) instruction2;
    assertEquals("Hi!", chatMessageSendInstruction2.getMessage().getMessageText());
    assertEquals("username", chatMessageSendInstruction2.getMessage().getMessageSender().getUsername());


  }

  @Test
  void testLeave() throws IOException, InstructionInvalidException {

    catchGreeting();
    catchInfo();

    fakeUserOutput.write("/leave\n".getBytes(StandardCharsets.UTF_8));
    await()
        .atMost(Duration.ofSeconds(2))
        .until(mockOutputBuffered::ready);

    String line2 = mockOutputBuffered.readLine();
    BaseInstruction instruction2 = InstructionNameHelper.parseInstruction(line2, Station.CLIENT);
    assertInstanceOf(ChatLeaveInstruction.class, instruction2);

  }

  @Test
  void testMessageReceive() throws IOException, InstructionInvalidException {

    catchGreeting();

    User dummyUser = new User("Heinz");
    Message dummyMessage = new Message("What a beautiful day!", dummyUser);
    ChatMessageSendInstruction chatMessageSendInstruction = new ChatMessageSendInstruction(Station.SERVER, dummyMessage);

    // Flush input
    while (fakeUserScreen.available() > 0) {
      fakeUserScreen.read();
    }

    mockInput
        .write((chatMessageSendInstruction + "\n").getBytes(StandardCharsets.UTF_8));

    await()
        .atMost(Duration.ofSeconds(2))
        .until(() -> fakeUserScreen.available() > 0);

    String expected = "\n"
        + dummyUser.getColor().code
        + dummyUser.getUsername()
        + AnsiColor.RESET.code
        + ": "
        + dummyMessage.getMessageText();

    byte[] received = new byte[expected.length()];
    fakeUserScreen.read(received);

    String readString = new String(received);

    assertEquals(expected, readString);


  }

  @Test
  void testUsernameColor() throws IOException, InstructionInvalidException {

    catchGreeting();

    final AnsiColor color = AnsiColor.BLUE;
    User dummyUser = new User("Heinz", color);
    Message dummyMessage = new Message("What a beautiful day!", dummyUser);
    ChatMessageSendInstruction chatMessageSendInstruction = new ChatMessageSendInstruction(Station.SERVER, dummyMessage);

    // Flush input
    while (fakeUserScreen.available() > 0) {
      fakeUserScreen.read();
    }

    mockInput
        .write((chatMessageSendInstruction + "\n").getBytes(StandardCharsets.UTF_8));

    await()
        .atMost(Duration.ofSeconds(2))
        .until(() -> fakeUserScreen.available() > 0);

    String expected = "\n"
        + color.code
        + dummyUser.getUsername()
        + AnsiColor.RESET.code
        + ": "
        + dummyMessage.getMessageText();

    byte[] received = new byte[expected.length()];
    fakeUserScreen.read(received);

    String readString = new String(received);

    assertEquals(expected, readString);

  }

  @Test
  void testSetMessageColor() throws IOException, InstructionInvalidException {

    catchGreeting();
    catchInfo();

    fakeUserOutput.write("/color message cyan\n".getBytes(StandardCharsets.UTF_8));
    await()
        .atMost(Duration.ofSeconds(2))
        .until(mockOutputBuffered::ready);

    String line = mockOutputBuffered.readLine();
    BaseInstruction instruction = InstructionNameHelper.parseInstruction(line, Station.CLIENT);
    assertInstanceOf(ChatChangeColorInstruction.class, instruction);

    ChatChangeColorInstruction chatChangeColorInstruction = (ChatChangeColorInstruction) instruction;
    assertEquals(AnsiColor.CYAN, chatChangeColorInstruction.getColor());

  }

  @Test
  void testInfoRequest() throws IOException, InstructionInvalidException {

    catchGreeting();
    catchInfo();

    fakeUserOutput.write("/info\n".getBytes(StandardCharsets.UTF_8));
    catchInfo();

  }

  @Test
  void testChangeUser() throws IOException, InstructionInvalidException {

    catchGreeting();
    catchInfo();

    fakeUserOutput.write("/username Peter\n".getBytes(StandardCharsets.UTF_8));

    await()
        .atMost(Duration.ofSeconds(2))
        .until(mockOutputBuffered::ready);

    String line = mockOutputBuffered.readLine();
    BaseInstruction instruction = InstructionNameHelper.parseInstruction(line, Station.CLIENT);
    assertInstanceOf(ChangeUserInstruction.class, instruction);

    ChangeUserInstruction changeUserInstruction = (ChangeUserInstruction) instruction;
    assert changeUserInstruction.getUser() != null;
    assertEquals("Peter", changeUserInstruction.getUser().getUsername());


  }

}
