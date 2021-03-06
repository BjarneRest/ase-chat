package de.bjarnerest.asechat.server;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import de.bjarnerest.asechat.helper.HashingHelper;
import de.bjarnerest.asechat.helper.InstructionNameHelper;
import de.bjarnerest.asechat.instruction.BaseInstruction;
import de.bjarnerest.asechat.instruction.ChatInfoInstruction;
import de.bjarnerest.asechat.instruction.ChatLeaveInstruction;
import de.bjarnerest.asechat.instruction.InstructionInvalidException;
import de.bjarnerest.asechat.model.Message;
import de.bjarnerest.asechat.model.Station;
import de.bjarnerest.asechat.model.User;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class ChatRoomServerTest {

  private final ServerSocket serverSocket = Mockito.mock(ServerSocket.class);
  private ChatRoomServer subject;

  @BeforeEach
  void setUp() {
    Mockito.reset(this.serverSocket);
  }

  void prepareSubject(InetAddress host, @SuppressWarnings("SameParameterValue") int port) {
    prepareSubject(host, port, "");
  }

  void prepareSubject(InetAddress host, int port, String password) {
    this.subject = new ChatRoomServer(host, port, password) {
      @Override
      protected void createSocket() {
        this.serverSocket = ChatRoomServerTest.this.serverSocket;
      }
    };
  }

  private @NotNull Thread startServer() {
    Thread serverThread = new Thread(() -> {
      try {
        this.subject.startServer();
      }
      catch (IOException e) {
        e.printStackTrace();
      }
    });
    serverThread.start();
    return serverThread;
  }

  private void introduce(MockSocket mockSocket, User user) throws IOException {

    mockSocket.awaitReady();
    assertEquals("change:user", mockSocket.readLine());

    mockSocket.writeLine("change:user=" + user.toJson());
    mockSocket.awaitReady();
    assertEquals("change:user=" + user.toJson(), mockSocket.readLine());

  }

  @Test
  void serverMessagingTest() throws IOException {
    this.prepareSubject(InetAddress.getByName("127.0.0.1"), 50501);

    // Mock client socket
    final MockSocket mockSocket1 = new MockSocket();
    final MockSocket mockSocket2 = new MockSocket();
    final User fakeUser1 = new User("Max");
    final User fakeUser2 = new User("Moritz");
    Mockito.when(this.serverSocket.accept())
        .thenReturn(mockSocket1.getSocket(), mockSocket2.getSocket());
    this.subject.setTestModeMaxClients(2);
    Thread serverThread = this.startServer();

    // Check for change:user
    introduce(mockSocket1, fakeUser1);
    introduce(mockSocket2, fakeUser2);

    // Check for system:ready
    mockSocket1.awaitReady();
    assertEquals("system:ready", mockSocket1.readLine());

    mockSocket2.awaitReady();
    assertEquals("system:ready", mockSocket2.readLine());

    // Send message from client 1
    Message message1 = new Message("Hello World", fakeUser1);
    mockSocket1.writeLine("chat:message:send=" + message1.toJson());
    mockSocket1.awaitReady();
    assertEquals("chat:message:echo=" + message1.toJson(), mockSocket1.readLine());
    mockSocket2.awaitReady();
    assertEquals("chat:message:send=" + message1.toJson(), mockSocket2.readLine());

    // Send message from client 2
    Message message2 = new Message("Greetings!", fakeUser2);
    mockSocket2.writeLine("chat:message:send=" + message2.toJson());
    mockSocket1.awaitReady();
    assertEquals("chat:message:send=" + message2.toJson(), mockSocket1.readLine());
    mockSocket2.awaitReady();
    assertEquals("chat:message:echo=" + message2.toJson(), mockSocket2.readLine());

    // Check for remains
    assertFalse(mockSocket1.getBufferedOutputOfOutputStream().ready());
    assertFalse(mockSocket2.getBufferedOutputOfOutputStream().ready());

    serverThread.interrupt();

  }

  @Test
  void serverPasswordProtectionTest() throws IOException {

    String hashedPassword = HashingHelper.hashSha512WithSalt("securePasswordTest");
    this.prepareSubject(InetAddress.getByName("1.2.3.4"), 12345, hashedPassword);

    MockSocket mockSocket1 = new MockSocket();
    MockSocket mockSocket2 = new MockSocket();
    final User fakeUser1 = new User("Max");
    final User fakeUser2 = new User("catlover_9000");

    Mockito.when(this.serverSocket.accept())
        .thenReturn(mockSocket1.getSocket(), mockSocket2.getSocket());
    this.subject.setTestModeMaxClients(2);
    Thread serverThread = this.startServer();

    // Should ask for authentication at begin for both clients
    mockSocket1.awaitReady();
    assertEquals("system:authenticate", mockSocket1.readLine());

    mockSocket2.awaitReady();
    assertEquals("system:authenticate", mockSocket2.readLine());

    // Try to authenticate without password
    mockSocket1.writeLine("system:authenticate");
    mockSocket1.awaitReady();
    assertEquals("system:authenticate", mockSocket1.readLine());

    mockSocket1.writeLine("system:authenticate=");
    mockSocket1.awaitReady();
    assertEquals("system:authenticate", mockSocket1.readLine());

    // Try to authenticate client 2 with correct password
    mockSocket2.writeLine("system:authenticate=securePasswordTest");

    introduce(mockSocket2, fakeUser2);

    mockSocket2.awaitReady();
    assertEquals("system:ready", mockSocket2.readLine());

    // Publish message
    Message dummyMsg1 = new Message("hw", fakeUser2);
    mockSocket2.writeLine("chat:message:send=" + dummyMsg1.toJson());
    mockSocket2.awaitReady();
    assertEquals("chat:message:echo=" + dummyMsg1.toJson(), mockSocket2.readLine());

    // Login client 1
    mockSocket1.writeLine("system:authenticate=securePasswordTest");

    introduce(mockSocket1, fakeUser1);

    mockSocket1.awaitReady();
    assertEquals("system:ready", mockSocket1.readLine());

    // Publish message
    Message dummyMsg2 = new Message("I like cats.", fakeUser2);
    mockSocket2.writeLine("chat:message:send=" + dummyMsg2.toJson());

    mockSocket2.awaitReady();
    assertEquals("chat:message:echo=" + dummyMsg2.toJson(), mockSocket2.readLine());

    mockSocket1.awaitReady();
    assertEquals("chat:message:send=" + dummyMsg2.toJson(), mockSocket1.readLine());

    // Check for remains
    assertFalse(mockSocket1.getBufferedOutputOfOutputStream().ready());
    assertFalse(mockSocket2.getBufferedOutputOfOutputStream().ready());

    serverThread.interrupt();

  }

  @Test
  void testLeave() throws IOException {

    this.prepareSubject(InetAddress.getByName("127.0.0.1"), 50501);

    // Mock client socket
    final MockSocket mockSocket1 = new MockSocket();
    Mockito.when(this.serverSocket.accept())
        .thenReturn(mockSocket1.getSocket());
    this.subject.setTestModeMaxClients(1);
    Thread serverThread = this.startServer();

    ChatLeaveInstruction leaveInstruction = new ChatLeaveInstruction(Station.CLIENT);
    mockSocket1.writeLine(leaveInstruction.toString());
    mockSocket1.awaitReady();

    serverThread.interrupt();

  }

  @Test
  void testInfo() throws IOException, InstructionInvalidException {

    this.prepareSubject(InetAddress.getByName("127.0.0.1"), 50501);

    // Mock client socket
    final MockSocket mockSocket1 = new MockSocket();
    final MockSocket mockSocket2 = new MockSocket();
    final User fakeUser1 = new User("Hans");
    final User fakeUser2 = new User("Hilde");
    Mockito.when(this.serverSocket.accept())
        .thenReturn(mockSocket1.getSocket(), mockSocket2.getSocket());
    this.subject.setTestModeMaxClients(2);
    Thread serverThread = this.startServer();

    introduce(mockSocket1, fakeUser1);
    introduce(mockSocket2, fakeUser2);

    // system:ready
    mockSocket1.awaitReady();
    mockSocket1.readLine();


    ChatInfoInstruction chatInfoInstructionClient = new ChatInfoInstruction(Station.CLIENT);
    mockSocket1.writeLine(chatInfoInstructionClient.toString());
    mockSocket1.awaitReady();


    String line = mockSocket1.readLine();
    BaseInstruction instruction = InstructionNameHelper.parseInstruction(line, Station.SERVER);
    assertInstanceOf(ChatInfoInstruction.class, instruction);

    ChatInfoInstruction chatInfoInstructionServer = (ChatInfoInstruction) instruction;
    assertEquals(2, chatInfoInstructionServer.getConnectedClientsAmount());

    serverThread.interrupt();

  }

  private static class MockSocket {

    private final Socket socket;
    private final PipedInputStream inputStream;
    private final PipedOutputStream inputOfInputStream;
    private final PipedOutputStream outputStream;
    //private final BufferedReader bufferedReader;
    //private final BufferedOutputStream outputStream;
    private final PipedInputStream outputOfOutputStream;
    private final BufferedReader bufferedOutputOfOutputStream;
    private boolean closed = false;

    public MockSocket() throws IOException {
      this.socket = Mockito.mock(Socket.class);
      this.inputStream = new PipedInputStream();
      this.inputOfInputStream = new PipedOutputStream();
      this.inputStream.connect(this.inputOfInputStream);
      //this.bufferedReader = new BufferedReader(new InputStreamReader(this.inputStream));
      //this.outputStream = new BufferedOutputStream(new PipedOutputStream());
      this.outputStream = new PipedOutputStream();
      this.outputOfOutputStream = new PipedInputStream();
      this.outputOfOutputStream.connect(this.outputStream);
      this.bufferedOutputOfOutputStream = new BufferedReader(new InputStreamReader(this.outputOfOutputStream));
      Mockito.when(this.socket.getOutputStream())
          .thenReturn(this.outputStream);
      Mockito.when(this.socket.getInputStream())
          .thenReturn(this.inputStream);
      Mockito.when(this.socket.isClosed())
          .thenAnswer(invocation -> {
              closed = true;
              return null;
      });
    }

    public void writeLine(String line) throws IOException {
      this.inputOfInputStream.write((line + "\n").getBytes(StandardCharsets.UTF_8));
    }

    public Socket getSocket() {
      return socket;
    }

    public boolean isClosed() {
      return closed;
    }

    public PipedInputStream getInputStream() {
      return inputStream;
    }

    public PipedOutputStream getInputOfInputStream() {
      return inputOfInputStream;
    }

    public PipedOutputStream getOutputStream() {
      return outputStream;
    }

    public PipedInputStream getOutputOfOutputStream() {
      return outputOfOutputStream;
    }

    public BufferedReader getBufferedOutputOfOutputStream() {
      return bufferedOutputOfOutputStream;
    }

    public void awaitReady() {
      await()
          .atMost(Duration.ofSeconds(2))
          .until(() -> getBufferedOutputOfOutputStream().ready());
    }

    public String readLine() throws IOException {
      return getBufferedOutputOfOutputStream().readLine();
    }

  }

}