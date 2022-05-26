package de.bjarnerest.asechat;

import static org.awaitility.Awaitility.await;

import de.bjarnerest.asechat.client.ChatRoomClient;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.net.InetAddress;
import java.net.Socket;

import static org.junit.jupiter.api.Assertions.*;

public class ChatRoomClientTest {

    private ChatRoomClient clientSubject;
    private final Socket fakeSocket = Mockito.mock(Socket.class);

    private PipedOutputStream mockInput;

    private BufferedReader mockOutputBuffered;

    private Thread clientThread;

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

    @BeforeEach
    void setUp() throws Exception {
        Mockito.reset(fakeSocket);
        prepareSubject();

        final PipedOutputStream pipedOutputStream = new PipedOutputStream();
        final PipedInputStream pipedInputStream = new PipedInputStream();
        Mockito.when(fakeSocket.getOutputStream()).thenReturn(pipedOutputStream);
        Mockito.when(fakeSocket.getInputStream()).thenReturn(pipedInputStream);

        final PipedInputStream mockOutput = new PipedInputStream();
        pipedOutputStream.connect(mockOutput);
        mockOutputBuffered = new BufferedReader(new InputStreamReader(mockOutput));

        mockInput = new PipedOutputStream();
        pipedInputStream.connect(mockInput);

        clientThread = startClient();
    }

    @AfterEach
    void tearDown() {
        clientThread.interrupt();
    }

    @Test
    void authenticationTest() throws Exception {

        mockInput.write("system:authenticate\n".getBytes(StandardCharsets.UTF_8));
        await().atMost(Duration.ofSeconds(2)).until(mockOutputBuffered::ready);
        String line = mockOutputBuffered.readLine();

        assertEquals("system:authenticate=password", line);

    }

}
