package ch.ethz.inf.asl.client;

import ch.ethz.inf.asl.common.request.Request;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import static ch.ethz.inf.asl.testutils.TestConstants.SMALL;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

// TODO
public class ClientMessagingProtocolImplTest {

    @Test(groups = SMALL, expectedExceptions = NullPointerException.class)
    public void testClientMessagingWithNulllSocket() throws IOException {
        new ClientMessagingProtocolImpl(null, 23, false);
    }

//    @Test(groups = SMALL)
//    public void testSayHello() throws IOException {
//        Socket mockedSocket = mock(Socket.class);
//        OutputStream outputStream = mockedSocket.getOutputStream();
//        InputStream inputStream = mockedSocket.getInputStream();
//
//        ClientMessagingProtocolImpl protocol = new ClientMessagingProtocolImpl(mockedSocket, 1, false);
//        protocol.sayHello("some client");
//
////        verify(outputStream).write();
//    }
}
