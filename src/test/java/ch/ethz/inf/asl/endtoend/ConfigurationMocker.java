package ch.ethz.inf.asl.endtoend;

import ch.ethz.inf.asl.utils.ConfigurationReader;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * This class contains two methods to mock the read configurations of middleware and clients so they can be used
 * by tests without creating properties files.
 */
public class ConfigurationMocker {

    public static ConfigurationReader mockMiddlewareConfiguration(String databaseHost, String databasePortNumber, String databaseName,
                                                          String databaseUsername, String databasePassword,
                                                          String threadPoolSize, String connectionPoolSize,
                                                          String dataSourceName, String middlewarePortNumber) {
        ConfigurationReader mockedConfiguration = mock(ConfigurationReader.class);

        when(mockedConfiguration.getProperty("databaseHost")).thenReturn(databaseHost);
        when(mockedConfiguration.getProperty("databasePortNumber")).thenReturn(databasePortNumber);
        when(mockedConfiguration.getProperty("databaseName")).thenReturn(databaseName);
        when(mockedConfiguration.getProperty("databaseUsername")).thenReturn(databaseUsername);
        when(mockedConfiguration.getProperty("databasePassword")).thenReturn(databasePassword);

        when(mockedConfiguration.getProperty("threadPoolSize")).thenReturn(threadPoolSize);
        when(mockedConfiguration.getProperty("connectionPoolSize")).thenReturn(connectionPoolSize);
        when(mockedConfiguration.getProperty("dataSourceName")).thenReturn(dataSourceName);
        when(mockedConfiguration.getProperty("middlewarePortNumber")).thenReturn(middlewarePortNumber);

        return mockedConfiguration;
    }

    public static ConfigurationReader mockClientConfiguration(String middlewareHost, String middlewarePortNumber, String numberOfClients, String totalClients,
                                                      String totalQueues, String messageSize, String startingId, String runningTimeInSeconds) {
        ConfigurationReader mockedConfiguration = mock(ConfigurationReader.class);

        when(mockedConfiguration.getProperty("middlewareHost")).thenReturn(middlewareHost);
        when(mockedConfiguration.getProperty("middlewarePortNumber")).thenReturn(middlewarePortNumber);

        when(mockedConfiguration.getProperty("numberOfClients")).thenReturn(numberOfClients);
        when(mockedConfiguration.getProperty("totalClients")).thenReturn(totalClients);
        when(mockedConfiguration.getProperty("totalQueues")).thenReturn(totalQueues);
        when(mockedConfiguration.getProperty("messageSize")).thenReturn(messageSize);

        when(mockedConfiguration.getProperty("startingId")).thenReturn(startingId);

        when(mockedConfiguration.getProperty("runningTimeInSeconds")).thenReturn(runningTimeInSeconds);

        return mockedConfiguration;
    }
}
