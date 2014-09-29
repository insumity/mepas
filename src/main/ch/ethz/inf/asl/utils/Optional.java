package ch.ethz.inf.asl.utils;

import java.util.NoSuchElementException;
import java.util.Objects;

public class Optional<T> {

    private T value;

    private static final Optional<?> EMPTY = new Optional<>();

    private Optional(T value) {
        if (value == null) {
            throw new NullPointerException("value cannot be null");
        }
        this.value = value;
    }

    public Optional() {
        this.value = null;
    }

    public static<T> Optional<T> of(T value) {
        if (value == null) {
            throw new NullPointerException("Given value cannot be null");
        }
        return new Optional<>(value);
    }

    public static<T> Optional<T> empty() {
        Optional<T> t = (Optional<T>) EMPTY;
        return t;
    }

    public boolean isPresent() {
        return value != null;
    }

    public T get() {
        if (value == null) {
            throw new NoSuchElementException("There is no value present in this optional");
        }
        return value;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Optional) {
            Optional other = (Optional) obj;
            return Objects.equals(this.value, other.value);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
