import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Properties;
import java.util.Random;
import java.util.Set;

public class ToBeRemoved {


    public static void main(String[] args) {
        Properties prop = new Properties();
        OutputStream output = null;

        Set<Long> set = new HashSet<Long>();

        for (int i = 0; i < 1000000; ++i) {
            Random r = new Random();
            long s = r.nextLong();
            long s1 = r.nextLong();
            long s2 = r.nextLong();
            long s3 = r.nextLong();
            long s4 = r.nextLong();
            long s5 = r.nextLong();
            long s6 = r.nextLong();

            if (set.contains(s)) {
                System.err.println("DIE!!!!!" + s);
            }
            if (set.contains(s1)) {
                System.err.println("DIE!!!!!" + s1);
            }            if (set.contains(s2)) {
                System.err.println("DIE!!!!!" + s2);
            }            if (set.contains(s3)) {
                System.err.println("DIE!!!!!" + s3);
            }            if (set.contains(s4)) {
                System.err.println("DIE!!!!!" + s4);
            }            if (set.contains(s5)) {
                System.err.println("DIE!!!!!" + s5);
            }
            if (set.contains(s6)) {
                System.err.println("DIE!!!!!" + s6);
            }

            set.add(s);
            set.add(s1);
            set.add(s2);
            set.add(s3);
            set.add(s4);
            set.add(s5);
            set.add(s6);
        }

        System.exit(1);

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
