package gov.hhs.aspr.ms.gcm.plugins.regions.support;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import gov.hhs.aspr.ms.gcm.nucleus.Dimension;
import gov.hhs.aspr.ms.gcm.nucleus.DimensionContext;
import gov.hhs.aspr.ms.gcm.plugins.properties.support.PropertyError;
import gov.hhs.aspr.ms.gcm.plugins.regions.datamanagers.RegionsPluginData;
import gov.hhs.aspr.ms.util.errors.ContractException;

public class RegionPropertyDimension implements Dimension {
    private final Data data;

    private RegionPropertyDimension(Data data) {
        this.data = data;
    }

    private static class Data {
        private RegionId regionId;
        private RegionPropertyId regionPropertyId;
        private List<Object> values = new ArrayList<>();

        private Data() {
        }

        private Data(Data data) {
            regionPropertyId = data.regionPropertyId;
            regionId = data.regionId;
            values.addAll(data.values);
        }

        @Override
        public int hashCode() {
            return Objects.hash(regionId, regionPropertyId, values);
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
            return Objects.equals(regionId, other.regionId) && Objects.equals(regionPropertyId, other.regionPropertyId)
                    && Objects.equals(values, other.values);
        }

    }

    /**
     * Returns a new builder for RegionPropertyDimension
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder class for RegionPropertyDimension
     */
    public static class Builder {

        private Builder() {
        }

        private Data data = new Data();

        /**
         * Returns the RegionPropertyDimension from the collected data.
         * 
         * @throws ContractException
         *                           <ul>
         *                           <li>{@linkplain PropertyError#NULL_PROPERTY_ID} if
         *                           the region property id was not assigned</li>
         *                           <li>{@linkplain RegionError#NULL_REGION_ID} if the
         *                           RegionId was not assigned</li>
         *                           </ul>
         */
        public RegionPropertyDimension build() {
            validate();
            return new RegionPropertyDimension(data);
        }

        private void validate() {
            if (data.regionId == null) {
                throw new ContractException(RegionError.NULL_REGION_ID);
            }

            if (data.regionPropertyId == null) {
                throw new ContractException(PropertyError.NULL_PROPERTY_ID);
            }
        }

        /**
         * Sets the group id for the dimension. Defaults to null.
         * 
         * @throws ContractException
         *                           <ul>
         *                           <li>{@linkplain RegionError#NULL_REGION_ID} if the
         *                           regionId is null</li>
         *                           </ul>
         */
        public Builder setRegionId(RegionId RegionId) {
            validateRegionId(RegionId);
            data.regionId = RegionId;
            return this;
        }

        /**
         * Sets the region property for the dimension. Defaults to null.
         * 
         * @throws ContractException
         *                           <ul>
         *                           <li>{@linkplain PropertyError#NULL_PROPERTY_ID} if
         *                           the property id is null</li>
         *                           </ul>
         */
        public Builder setRegionPropertyId(RegionPropertyId RegionPropertyId) {
            validateRegionPropertyId(RegionPropertyId);
            data.regionPropertyId = RegionPropertyId;
            return this;
        }

        /**
         * Adds a value to the dimension.
         * 
         * @throws ContractException
         *                           <ul>
         *                           <li>{@linkplain PropertyError#NULL_PROPERTY_VALUE}
         *                           if the value is null</li>
         *                           </ul>
         */
        public Builder addValue(Object value) {
            validateValue(value);
            data.values.add(value);
            return this;
        }
    }

    @Override
    public List<String> getExperimentMetaData() {
        List<String> result = new ArrayList<>();
        result.add(data.regionPropertyId.toString());
        return result;
    }

    @Override
    public int levelCount() {
        return data.values.size();
    }

    @Override
    public List<String> executeLevel(DimensionContext dimensionContext, int level) {
        RegionsPluginData.Builder builder = dimensionContext.getPluginDataBuilder(RegionsPluginData.Builder.class);
        Object value = data.values.get(level);

        builder.setRegionPropertyValue(data.regionId, data.regionPropertyId, value);
        List<String> result = new ArrayList<>();
        result.add(value.toString());
        return result;
    }

    /**
     * Returns the group id for this dimension
     */
    public RegionId getRegionId() {
        return data.regionId;
    }

    /**
     * Returns the region property id for this dimension
     */
    public RegionPropertyId getRegionPropertyId() {
        return data.regionPropertyId;
    }

    /**
     * Returns the ordered list of region property values for this dimension
     */
    public List<Object> getValues() {
        return new ArrayList<>(data.values);
    }

    private static void validateValue(Object value) {
        if (value == null) {
            throw new ContractException(PropertyError.NULL_PROPERTY_VALUE);
        }
    }

    private static void validateRegionPropertyId(RegionPropertyId RegionPropertyId) {
        if (RegionPropertyId == null) {
            throw new ContractException(PropertyError.NULL_PROPERTY_ID);
        }
    }

    private static void validateRegionId(RegionId RegionId) {
        if (RegionId == null) {
            throw new ContractException(RegionError.NULL_REGION_ID);
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
        RegionPropertyDimension other = (RegionPropertyDimension) obj;
        return Objects.equals(data, other.data);
    }

}
