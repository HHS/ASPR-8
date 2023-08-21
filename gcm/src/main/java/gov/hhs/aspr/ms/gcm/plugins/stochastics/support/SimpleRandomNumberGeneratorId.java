package gov.hhs.aspr.ms.gcm.plugins.stochastics.support;

import gov.hhs.aspr.ms.gcm.plugins.globalproperties.support.SimpleGlobalPropertyId;
import util.errors.ContractException;

public class SimpleRandomNumberGeneratorId implements RandomNumberGeneratorId {
    private final Object value;

    /**
     * @throws ContractException
     *                           <li>
     *                           {@linkplain StochasticsError#NULL_RANDOM_NUMBER_GENERATOR_ID}
     *                           if the value is null</li>
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
     * Two {@link SimpleGlobalPropertyId} instances are equal if and only if their
     * inputs are equal.
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

    /**
     * Returns the string representation of the generator's input
     */
    @Override
    public String toString() {
        return value.toString();
    }
}
