package de.bjarnerest.asechat;

import static org.awaitility.Awaitility.await;

import de.bjarnerest.asechat.client.ChatRoomClient;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.net.InetAddress;
import java.net.Socket;

import static org.junit.jupiter.api.Assertions.*;

public class ChatRoomClientTest {

    private ChatRoomClient clientSubject;
    private final Socket fakeSocket = Mockito.mock(Socket.class);

    void prepareSubject() throws Exception {
        this.clientSubject = new ChatRoomClient(InetAddress.getByName("123.4.5.6"), 12345, "password", "username") {
            @Override
            protected Socket createSocket() {
                return fakeSocket;
            }
        };
    }

    private Thread startClient() {
        Thread clientThread = new Thread(() -> {
            try {
                clientSubject.connectToServer();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        clientThread.start();
        return clientThread;
    }

    @Test
    void authenticationTest() throws Exception {

        // Set up test environment
        Mockito.reset(fakeSocket);
        prepareSubject();

        // Fake i/o
        final PipedOutputStream pipedOutputStream = new PipedOutputStream();
        final PipedInputStream pipedInputStream = new PipedInputStream();
        Mockito.when(fakeSocket.getOutputStream()).thenReturn(pipedOutputStream);
        Mockito.when(fakeSocket.getInputStream()).thenReturn(pipedInputStream);

        final PipedInputStream mockOutput = new PipedInputStream();
        pipedOutputStream.connect(mockOutput);
        final BufferedReader mockOutputBuffered = new BufferedReader(new InputStreamReader(mockOutput));

        final PipedOutputStream mockInput = new PipedOutputStream();
        pipedInputStream.connect(mockInput);

        Thread clientThread = startClient();

        mockInput.write("system:authenticate\n".getBytes(StandardCharsets.UTF_8));
        await().atMost(Duration.ofSeconds(2)).until(mockOutputBuffered::ready);
        String line = mockOutputBuffered.readLine();

        assertEquals("system:authenticate=password", line);

        clientThread.interrupt();

    }

    private static class MockSocket {

        private final Socket socket;
        private final PipedInputStream inputStream;
        private final PipedOutputStream inputOfInputStream;

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

        public void awaitReady() {
            await().atMost(Duration.ofSeconds(2)).until(() -> getBufferedOutputOfOutputStream().ready());
        }

        public String readLine() throws IOException {
            return getBufferedOutputOfOutputStream().readLine();
        }

    }

}
