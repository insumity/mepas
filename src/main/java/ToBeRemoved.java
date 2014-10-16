import java.lang.reflect.InvocationTargetException;
class Foo {

    public Foo(int x) {
        System.err.println(x);
    }
}

public class ToBeRemoved {


    public static void main(String[] args) {
        try {
            Foo.class.getDeclaredConstructor(int.class).newInstance(134);
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        System.out.println(String.format("client%03d", 891));
        System.out.println(String.format("foo%s", null));
    }
}
