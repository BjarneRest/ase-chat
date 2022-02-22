package de.bjarnerest.asechat;

import de.bjarnerest.asechat.model.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ChatRoomServerTest {

    private final ServerSocket serverSocket = Mockito.mock(ServerSocket.class);
    private ChatRoomServer subject;

    @BeforeEach
    void setUp() {
        Mockito.reset(this.serverSocket);
    }

    void prepareSubject(InetAddress host, int port) {
        this.subject = new ChatRoomServer(host, port) {
            @Override
            protected void createSocket() throws IOException {
                this.serverSocket = ChatRoomServerTest.this.serverSocket;
            }
        };
    }

    @Test
    void serverMessagingTest() throws IOException {
        this.prepareSubject(InetAddress.getByName("127.0.0.1"), 50501);

        // Mock client socket
        final MockSocket mockSocket1 = new MockSocket();
        final MockSocket mockSocket2 = new MockSocket();
        Mockito.when(this.serverSocket.accept()).thenReturn(mockSocket1.getSocket(), mockSocket2.getSocket());
        this.subject.setTestModeMaxClients(2);
        Thread serverThread = new Thread(() -> {
            try {
                this.subject.startServer();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        serverThread.start();


        // Send message from client 1
        Message message1 = new Message("Hello World", "Max");
        mockSocket1.writeLine("chat:message:send=" + message1.toJson());

        // Send message from client 2
        Message message2 = new Message("Greetings!", "Moritz");
        mockSocket2.writeLine("chat:message:send=" + message2.toJson());

        // Send corrupted message from client 2
        mockSocket2.writeLine("chat:message:send={\"mess");

        // Receive message 1+2
        // Collect server messages
        List<String> receivedMessages1 = mockSocket1.getBufferedOutputOfOutputStream().lines().limit(2).collect(Collectors.toList());
        List<String> receivedMessages2 = mockSocket2.getBufferedOutputOfOutputStream().lines().limit(3).collect(Collectors.toList());


        assertEquals("system:ready", receivedMessages1.get(0));
        assertEquals("system:ready", receivedMessages2.get(0));

        assertEquals("chat:message:publish=" + message1.toJson(), receivedMessages2.get(1));
        assertEquals("chat:message:publish=" + message2.toJson(), receivedMessages1.get(1));

        assertEquals("system:error:parsing", receivedMessages2.get(2));

    }

    private static class MockSocket {

        private final Socket socket;
        private final PipedInputStream inputStream;
        private final PipedOutputStream inputOfInputStream;
        //private final BufferedReader bufferedReader;
        //private final BufferedOutputStream outputStream;

        private final PipedOutputStream outputStream;
        private final PipedInputStream outputOfOutputStream;
        private final BufferedReader bufferedOutputOfOutputStream;

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
            Mockito.when(this.socket.getOutputStream()).thenReturn(this.outputStream);
            Mockito.when(this.socket.getInputStream()).thenReturn(this.inputStream);
        }

        public void writeLine(String line) throws IOException {
            this.inputOfInputStream.write((line + "\n").getBytes(StandardCharsets.UTF_8));
        }

        public Socket getSocket() {
            return socket;
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
    }

}