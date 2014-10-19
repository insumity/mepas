import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;

public class ToBeRemoved {


    public static void main(String[] args) {
        Properties prop = new Properties();
        OutputStream output = null;

        try {

//            output = new FileOutputStream("src/main/resources/middleware.properties");
            FileInputStream input = new FileInputStream("src/main/resources/middleware.properties");

            prop.load(input);

            System.out.println(prop.getProperty("database"));
            System.out.println(prop.getProperty("dbUser"));
            System.out.println(prop.getProperty("dbpassword"));

//            // set the properties value
//            prop.setProperty("database", "localhost");
//            prop.setProperty("dbuser", "mkyong");
//            prop.setProperty("dbpassword", "password");
//
//            // save properties to project root folder
//            prop.store(output, null);

        } catch (IOException io) {
            io.printStackTrace();
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
    }
}
