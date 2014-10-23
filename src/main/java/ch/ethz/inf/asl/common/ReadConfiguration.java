package ch.ethz.inf.asl.common;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import static ch.ethz.inf.asl.utils.Verifier.notNull;

public class ReadConfiguration {

    private Properties properties;

    public ReadConfiguration(String configurationFilePath) {
        notNull(configurationFilePath, "Given configurationFilePath cannot be null!");

        properties = new Properties();
        FileInputStream input;
        try {
            input = new FileInputStream(configurationFilePath);
            properties.load(input);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getProperty(String key) {
        return properties.getProperty(key);
    }
}
