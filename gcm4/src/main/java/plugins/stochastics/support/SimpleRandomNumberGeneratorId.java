package plugins.stochastics.support;

import util.errors.ContractException;

public class SimpleRandomNumberGeneratorId implements RandomNumberGeneratorId {
    private final Object value;

    public SimpleRandomNumberGeneratorId(Object value) {
        if (value == null) {
            throw new ContractException(StochasticsError.NULL_RANDOM_NUMBER_GENERATOR_ID);
        }
        this.value = value;
    }

    /**
     * Standard implementation
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((value == null) ? 0 : value.hashCode());
        return result;
    }

    /**
     * Two {@link SimpleGlobalPropertyId} instances are equal if and only if
     * their inputs are equal.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof SimpleRandomNumberGeneratorId)) {
            return false;
        }
        SimpleRandomNumberGeneratorId other = (SimpleRandomNumberGeneratorId) obj;
        if (value == null) {
            if (other.value != null) {
                return false;
            }
        } else if (!value.equals(other.value)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
