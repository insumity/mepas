package ch.ethz.inf.asl.utils;

import java.util.NoSuchElementException;
import java.util.Objects;

public class Optional<T> {

    private T value;

    private Optional(T value) {
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
        return new Optional<>();
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

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
