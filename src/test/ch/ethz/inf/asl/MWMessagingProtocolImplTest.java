package ch.ethz.inf.asl;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.sql.*;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class MWMessagingProtocolImplTest {

    private static final String SMALL = "small";
    private static final String INTEGRATION = "integration";

    private Connection mockedConnection;

    @BeforeMethod(groups = SMALL)
    public void setUp() throws SQLException {
        mockedConnection = mock(Connection.class);
        when(mockedConnection.prepareStatement(anyString())).thenReturn(mock(PreparedStatement.class));
        when(mockedConnection.prepareCall(anyString())).thenReturn(mock(CallableStatement.class));
    }


    @Test(groups = SMALL)
    public void testCreateQueueCallsStoredProcedure() throws SQLException {
        MWMessagingProtocolImpl protocol = new MWMessagingProtocolImpl(2, mockedConnection);
        protocol.createQueue();
//        verify(mockedConnection).prepareCall(CREATE_QUEUE_CALL);
    }

    @Test(groups = SMALL)
    public void testCreateQueueReturnsResult() throws SQLException {
        MWMessagingProtocolImpl protocol = new MWMessagingProtocolImpl(2, mockedConnection);
        protocol.createQueue();

    }
}
