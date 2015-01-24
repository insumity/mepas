package ch.ethz.inf.asl.utils;

import java.util.Objects;

/**
 * Class that corresponds to a general pair of two objects.
 * @param <T> type of first object in the pair
 * @param <V> type of second object in the pair
 */
public class Pair<T, V> {

    private T first;
    private V second;

    /**
     * Constructs an empty pair. Both its first and second elements will contain null.
     */
    public Pair() {
    }

    /**
     * Constructs a pair based on the given parameters.
     * @param first value of the pair
     * @param second value of the pair
     */
    public Pair(T first, V second) {
        this.first = first;
        this.second = second;
    }

    /**
     * Retrieves the first value of the pair.
     * @return first value
     */
    public T getFirst() {
        return first;
    }

    /**
     * Retrieves the second value of the pair.
     * @return second value
     */
    public V getSecond() {
        return second;
    }

    /**
     * Changes the first value of the pair.
     * @param first value to be set as first value of the pair
     */
    public void setFirst(T first) {
        this.first = first;
    }

    /**
     * Changes the second value of the pair.
     * @param second value to be set as second value of the pair
     */
    public void setSecond(V second) {
        this.second = second;
    }

    @Override
    public String toString() {
        return "(" + first + ", " + second + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Pair) {
            Pair other = (Pair) obj;
            return Objects.equals(this.first, other.first)
                    && Objects.equals(this.second, other.second);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(first, second);
    }
}
