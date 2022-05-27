package de.bjarnerest.asechat;

import static org.awaitility.Awaitility.await;

import de.bjarnerest.asechat.client.ChatRoomClient;
import de.bjarnerest.asechat.helper.InstructionNameHelper;
import de.bjarnerest.asechat.instruction.BaseInstruction;
import de.bjarnerest.asechat.instruction.ChatLeaveInstruction;
import de.bjarnerest.asechat.instruction.ChatMessageSendInstruction;
import de.bjarnerest.asechat.instruction.InstructionInvalidException;
import de.bjarnerest.asechat.model.Message;
import de.bjarnerest.asechat.model.Station;
import de.bjarnerest.asechat.model.User;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
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

    private PipedInputStream fakeUserInput;

    private PipedOutputStream fakeUserOutput;

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

        // Network input
        final PipedOutputStream pipedOutputStream = new PipedOutputStream();
        final PipedInputStream pipedInputStream = new PipedInputStream();
        Mockito.when(fakeSocket.getOutputStream()).thenReturn(pipedOutputStream);
        Mockito.when(fakeSocket.getInputStream()).thenReturn(pipedInputStream);

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

    @Test
    void authenticationTest() throws Exception {

        mockInput.write("system:authenticate\n".getBytes(StandardCharsets.UTF_8));
        await().atMost(Duration.ofSeconds(2)).until(mockOutputBuffered::ready);
        String line = mockOutputBuffered.readLine();

        assertEquals("system:authenticate=password", line);

    }

    @Test
    void userMessageTest() throws Exception {

        mockInput.write("system:ready\n".getBytes(StandardCharsets.UTF_8));

        // Greeting message
        await().atMost(Duration.ofSeconds(2)).until(mockOutputBuffered::ready);

        String line = mockOutputBuffered.readLine();
        BaseInstruction instruction = InstructionNameHelper.parseInstruction(line, Station.CLIENT);
        assertInstanceOf(ChatMessageSendInstruction.class, instruction);

        ChatMessageSendInstruction chatMessageSendInstruction = (ChatMessageSendInstruction) instruction;
        assertEquals("Hello Welt. Hier ist username", chatMessageSendInstruction.getMessage().getMessageText());
        assertEquals("username", chatMessageSendInstruction.getMessage().getMessageSender().getUsername());




        fakeUserOutput.write("Hi!\n".getBytes(StandardCharsets.UTF_8));
        await().atMost(Duration.ofSeconds(2)).until(mockOutputBuffered::ready);

        String line2 = mockOutputBuffered.readLine();
        BaseInstruction instruction2 = InstructionNameHelper.parseInstruction(line2, Station.CLIENT);
        assertInstanceOf(ChatMessageSendInstruction.class, instruction2);

        ChatMessageSendInstruction chatMessageSendInstruction2 = (ChatMessageSendInstruction) instruction2;
        assertEquals("Hi!", chatMessageSendInstruction2.getMessage().getMessageText());
        assertEquals("username", chatMessageSendInstruction2.getMessage().getMessageSender().getUsername());


    }

    @Test
    void testLeave() throws IOException, InstructionInvalidException {

        mockInput.write("system:ready\n".getBytes(StandardCharsets.UTF_8));

        // Greeting message
        await().atMost(Duration.ofSeconds(2)).until(mockOutputBuffered::ready);

        String line = mockOutputBuffered.readLine();
        BaseInstruction instruction = InstructionNameHelper.parseInstruction(line, Station.CLIENT);
        assertInstanceOf(ChatMessageSendInstruction.class, instruction);

        ChatMessageSendInstruction chatMessageSendInstruction = (ChatMessageSendInstruction) instruction;
        assertEquals("Hello Welt. Hier ist username", chatMessageSendInstruction.getMessage().getMessageText());
        assertEquals("username", chatMessageSendInstruction.getMessage().getMessageSender().getUsername());

        fakeUserOutput.write("/leave\n".getBytes(StandardCharsets.UTF_8));
        await().atMost(Duration.ofSeconds(2)).until(mockOutputBuffered::ready);

        String line2 = mockOutputBuffered.readLine();
        BaseInstruction instruction2 = InstructionNameHelper.parseInstruction(line2, Station.CLIENT);
        assertInstanceOf(ChatLeaveInstruction.class, instruction2);

    }

}
