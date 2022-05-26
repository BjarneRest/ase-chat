package de.bjarnerest.asechat;

import de.bjarnerest.asechat.client.ChatRoomClient;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.net.InetAddress;
import java.net.Socket;

public class ChatRoomClientTest {

    private ChatRoomClient clientSubject;
    private final Socket fakeSocket = Mockito.mock(Socket.class);

    void prepareSubject() throws Exception {
        this.clientSubject = new ChatRoomClient(InetAddress.getByName("123.4.5.6"), 12345, "password", "username") {
            @Override
            protected Socket createSocket() throws Exception {
                return fakeSocket;
            }
        };
    }

    private Thread startClient() {
        Thread clientThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    clientSubject.connectToServer();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        clientThread.start();
        return clientThread;
    }

    @Test
    void authenticationTest() throws Exception {
        prepareSubject();
        Mockito.reset(fakeSocket);

    }

}
