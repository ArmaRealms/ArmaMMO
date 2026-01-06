package com.gmail.nossr50.util.random;

public record ProbabilityImpl(double value) implements Probability {

    /**
     * Create a probability from a static value. A value of 0 represents a 0% chance of success, A
     * value of 1 represents a 100% chance of success. A value of 0.5 represents a 50% chance of
     * success. A value of 0.01 represents a 1% chance of success. And so on.
     *
     * @param value the value of the probability between 0 and 100
     * @throws IllegalArgumentException if the value is negative
     */
    public ProbabilityImpl {
        if (value < 0) {
            throw new IndexOutOfBoundsException("Value should never be negative for Probability!" +
                    " This suggests a coding mistake, contact the devs!");
        }
    }
}
