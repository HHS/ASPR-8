package plugins.personproperties.support;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import nucleus.Dimension;
import nucleus.DimensionContext;
import plugins.personproperties.PersonPropertiesPluginData;
import plugins.util.properties.PropertyDefinition;
import plugins.util.properties.PropertyError;
import util.errors.ContractException;

/**
 * Dimension implementation for setting a person property to a list of values in
 * a person properties plugin data.
 */
public class PersonPropertyDimension implements Dimension {

    private static class Data {
        private PersonPropertyId personPropertyId;
        private boolean trackTimes = false;
        private List<Object> values = new ArrayList<>();

        private Data() {
        }

        private Data(Data data) {
            personPropertyId = data.personPropertyId;
            trackTimes = data.trackTimes;
            values.addAll(data.values);
        }

        @Override
        public int hashCode() {
            return Objects.hash(personPropertyId, values, trackTimes);
        }

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
            Data other = (Data) obj;
            return Objects.equals(personPropertyId, other.personPropertyId)
                    && Objects.equals(values, other.values)
                    && trackTimes == other.trackTimes;
        }

    }

    /**
     * Returns a new builder for PersonPropertyDimension
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder class for PersonPropertyDimension
     *
     */
    public static class Builder {

        private Data data = new Data();

        /**
         * Returns the PersonPropertyDimension from the collected data.
         * 
         * @throws ContractException
         *                           <ul>
         *                           <li>{@linkplain PropertyError#NULL_PROPERTY_ID} if
         *                           the person property id was not assigned
         * 
         */
        public PersonPropertyDimension build() {
            validate();
            return new PersonPropertyDimension(new Data(data));
        }

        private void validate() {
            if (data.personPropertyId == null) {
                throw new ContractException(PropertyError.NULL_PROPERTY_ID);
            }
        }

        /**
         * Sets the time tracking policy for this dimension. defaults to false
         */
        public Builder setTrackTimes(boolean trackTimes) {
            data.trackTimes = trackTimes;
            return this;
        }

        /**
         * Sets the person property for the dimension. Defaults to null.
         * 
         * @throws ContractException
         *                           <ul>
         *                           <li>{@linkplain PropertyError#NULL_PROPERTY_ID} if
         *                           the id is null
         * 
         */
        public Builder setPersonPropertyId(PersonPropertyId personPropertyId) {
            validatePersonPropertyId(personPropertyId);
            data.personPropertyId = personPropertyId;
            return this;
        }

        /**
         * Adds a value to the dimension.
         * 
         * @throws ContractException
         *                           <ul>
         *                           <li>{@linkplain PropertyError#NULL_PROPERTY_VALUE}
         *                           if the value is null
         * 
         */
        public Builder addValue(Object value) {
            validateValue(value);
            data.values.add(value);
            return this;
        }
    }

    private final Data data;

    private PersonPropertyDimension(Data data) {
        this.data = data;
    }

    @Override
    public List<String> getExperimentMetaData() {
        List<String> result = new ArrayList<>();
        result.add(data.personPropertyId.toString());
        return result;
    }

    @Override
    public int levelCount() {
        return data.values.size();
    }

    @Override
    public List<String> executeLevel(DimensionContext dimensionContext, int level) {
        PersonPropertiesPluginData personPropertiesPluginData = dimensionContext
                .getPluginData(PersonPropertiesPluginData.class);
        PersonPropertiesPluginData.Builder builder = dimensionContext
                .getPluginDataBuilder(PersonPropertiesPluginData.Builder.class);
        Object value = data.values.get(level);

        PropertyDefinition existingPropDef = personPropertiesPluginData
                .getPersonPropertyDefinition(data.personPropertyId);
        double existingTime = personPropertiesPluginData.getPropertyDefinitionTime(data.personPropertyId);

        PropertyDefinition newPropDef = PropertyDefinition.builder()
                .setDefaultValue(value)
                .setPropertyValueMutability(existingPropDef.propertyValuesAreMutable())
                .setType(existingPropDef.getType())
                .build();

        builder.definePersonProperty(data.personPropertyId, newPropDef, existingTime, data.trackTimes);

        List<String> result = new ArrayList<>();
        result.add(value.toString());
        return result;
    }

    /**
     * Returns the time tracking policy for this dimension
     */
    public boolean getTrackTimes() {
        return data.trackTimes;
    }

    /**
     * Returns the person property id for this dimension
     */
    public PersonPropertyId getPersonPropertyId() {
        return data.personPropertyId;
    }

    /**
     * Returns the ordered list of person property values for this dimension
     */
    public List<Object> getValues() {
        return new ArrayList<>(data.values);
    }

    private static void validateValue(Object value) {
        if (value == null) {
            throw new ContractException(PropertyError.NULL_PROPERTY_VALUE);
        }
    }

    private static void validatePersonPropertyId(PersonPropertyId personPropertyId) {
        if (personPropertyId == null) {
            throw new ContractException(PropertyError.NULL_PROPERTY_ID);
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(data);
    }

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
        PersonPropertyDimension other = (PersonPropertyDimension) obj;
        return Objects.equals(data, other.data);
    }

}
