package ch.ethz.inf.asl;

import org.testng.annotations.Test;

import java.util.regex.Pattern;

public class FooTest {

    class Foo {
        public Foo() {
            throw new IllegalArgumentException("This is (sparta), cool he?");
        }
    }

    /* problems with multilne expection messages regular exceptions
    comments here: http://stackoverflow.com/questions/3222649/any-character-including-newline-java-regex
     */
    @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = ".* is \\(sparta\\).*")
    public void testFooBar() {
        System.err.println(Pattern.DOTALL);
        new Foo();
    }
}
