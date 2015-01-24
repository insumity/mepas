package ch.ethz.inf.asl.utils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import static ch.ethz.inf.asl.utils.Verifier.hasText;

/**
 * This class can be used to read a Java properties file.
 */
public class ConfigurationReader {

    private Properties properties;

    /**
     * Constructs a ConfigurationReader given the path of the configuration file.
     * @param configurationFilePath path of the properties-configuration file
     */
    public ConfigurationReader(String configurationFilePath) {
        hasText(configurationFilePath, "Given configurationFilePath cannot be null or empty!");

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

    /**
     * Given the key, retrieves the corresponding value of this key.
     * @param key key of the value we are looking for
     * @return the value of the key
     */
    public String getProperty(String key) {
        return properties.getProperty(key);
    }
}
