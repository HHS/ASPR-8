package gov.hhs.aspr.ms.gcm.simulation.plugins.stochastics.support;

import java.util.Objects;

import gov.hhs.aspr.ms.util.errors.ContractException;

public class SimpleRandomNumberGeneratorId implements RandomNumberGeneratorId {
    private final Object value;

    /**
     * @throws ContractException {@linkplain StochasticsError#NULL_RANDOM_NUMBER_GENERATOR_ID}
     *                           if the value is null
     */
    public SimpleRandomNumberGeneratorId(Object value) {
        if (value == null) {
            throw new ContractException(StochasticsError.NULL_RANDOM_NUMBER_GENERATOR_ID);
        }
        this.value = value;
    }

    public Object getValue() {
        return this.value;
    }

	/**
     * Standard implementation consistent with the {@link #equals(Object)} method
     */
    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    /**
     * Two {@link SimpleRandomNumberGeneratorId} instances are equal if and only if
     * their inputs are equal.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        SimpleRandomNumberGeneratorId other = (SimpleRandomNumberGeneratorId) obj;
        return Objects.equals(value, other.value);
    }

    /**
     * Returns the string representation of the generator's input
     */
    @Override
    public String toString() {
        return value.toString();
    }
}
