package ch.ethz.inf.asl.utils;

import java.io.Serializable;
import java.util.NoSuchElementException;
import java.util.Objects;

// Optional shouldn't be serializble TODO FIXME
public class Optional<T> implements Serializable {

    private T value;

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
            Optional<T> other = (Optional<T>) obj;
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
