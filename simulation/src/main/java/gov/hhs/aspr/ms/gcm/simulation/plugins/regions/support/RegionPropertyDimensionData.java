package gov.hhs.aspr.ms.gcm.simulation.plugins.regions.support;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import gov.hhs.aspr.ms.gcm.simulation.nucleus.DimensionData;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.NucleusError;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.StandardVersioning;
import gov.hhs.aspr.ms.gcm.simulation.plugins.properties.support.PropertyError;
import gov.hhs.aspr.ms.util.errors.ContractException;
import gov.hhs.aspr.ms.util.wrappers.MutableInteger;

public final class RegionPropertyDimensionData extends DimensionData {

    /**
     * Builder class for RegionPropertyDimensionData
     */
    public static class Builder {
        private Data data;

        private Builder(Data data) {
            this.data = data;
        }

        /**
         * Returns the RegionPropertyDimensionData from the collected data.
         * 
         * @throws ContractException
         *                           <ul>
         *                           <li>{@linkplain NucleusError#DUPLICATE_DIMENSION_LEVEL_NAME}
         *                           if there are duplicate dimension level names</li>
         *                           <li>{@linkplain PropertyError#NULL_PROPERTY_ID} if
         *                           the region property id was not assigned</li>
         *                           <li>{@linkplain RegionError#NULL_REGION_ID} if the
         *                           RegionId was not assigned</li>
         *                           </ul>
         */
        public RegionPropertyDimensionData build() {
            if (!data.locked) {
                validateData();
            }
            ensureImmutability();
            return new RegionPropertyDimensionData(data);
        }

        /**
         * Sets the region id for the dimension. Defaults to null.
         * 
         * @throws ContractException
         *                           <ul>
         *                           <li>{@linkplain RegionError#NULL_REGION_ID} if the
         *                           regionId is null</li>
         *                           </ul>
         */
        public Builder setRegionId(RegionId regionId) {
            if (regionId == null) {
                throw new ContractException(RegionError.NULL_REGION_ID);
            }
            ensureDataMutability();
            data.regionId = regionId;
            return this;
        }

        /**
         * Sets the region property id for the dimension. Defaults to null.
         * 
         * @throws ContractException
         *                           <ul>
         *                           <li>{@linkplain PropertyError#NULL_PROPERTY_ID} if
         *                           the region property id is null</li>
         *                           </ul>
         */
        public Builder setRegionPropertyId(RegionPropertyId regionPropertyId) {
            if (regionPropertyId == null) {
                throw new ContractException(PropertyError.NULL_PROPERTY_ID);
            }
            ensureDataMutability();
            data.regionPropertyId = regionPropertyId;
            return this;
        }

        /**
         * Adds a value to the dimension.
         * 
         * @throws ContractException
         *                           <ul>
         *                           <li>{@linkplain NucleusError#NULL_DIMENSION_LEVEL_NAME}
         *                           if the levelName is null or an empty string</li>
         *                           <li>{@linkplain PropertyError#NULL_PROPERTY_VALUE}
         *                           if the value is null</li>
         *                           </ul>
         */
        public Builder addValue(String levelName, Object value) {

            if (levelName == null || levelName.equals("")) {
                throw new ContractException(NucleusError.NULL_DIMENSION_LEVEL_NAME);
            }

            if (value == null) {
                throw new ContractException(PropertyError.NULL_PROPERTY_VALUE);
            }

            ensureDataMutability();

            int level = data.levelNames.indexOf(levelName);

            if (level == -1) {
                data.levelNames.add(levelName);
                level = data.levelNames.size() - 1;
                data.values.add(null);
            }

            data.values.set(level, value);

            return this;
        }

        /*
         * This is for testing the duplicate level name precondition. Because the add
         * method tolerates duplicate level names by finding the index of the level
         * name, this error will never be thrown normally. So this method is purely to
         * facilitate showing that on the off chance something went very wrong, the
         * error gets thrown properly.
         */
        Builder _addLevelName(String levelName) {
            data.levelNames.add(levelName);
            return this;
        }

        private void ensureDataMutability() {
            if (data.locked) {
                data = new Data(data);
                data.locked = false;
            }
        }

        private void ensureImmutability() {
            if (!data.locked) {
                data.locked = true;
            }
        }

        private void validateData() {
            if (data.regionId == null) {
                throw new ContractException(RegionError.NULL_REGION_ID);
            }
            if (data.regionPropertyId == null) {
                throw new ContractException(PropertyError.NULL_PROPERTY_ID);
            }

            Map<String, MutableInteger> levelNamesMap = new LinkedHashMap<>();

            for (String levelName : data.levelNames) {
                MutableInteger integer = levelNamesMap.get(levelName);

                if (integer == null) {
                    integer = new MutableInteger();
                    levelNamesMap.put(levelName, integer);
                }

                integer.increment();
            }

            for (MutableInteger integer : levelNamesMap.values()) {
                if (integer.getValue() > 1) {
                    throw new ContractException(NucleusError.DUPLICATE_DIMENSION_LEVEL_NAME);
                }
            }
            /*
             * Nothing to validate. The plugin data that will receive the
             * dimension values will perform the validation.
             */
        }
    }

    private static class Data {
        private final List<String> levelNames = new ArrayList<>();
        private final List<Object> values = new ArrayList<>();
        private RegionId regionId;
        private RegionPropertyId regionPropertyId;
        private boolean locked;

        private Data() {
        }

        private Data(Data data) {
            levelNames.addAll(data.levelNames);
            values.addAll(data.values);
            regionId = data.regionId;
            regionPropertyId = data.regionPropertyId;
            locked = data.locked;
        }

		/**
    	 * Standard implementation consistent with the {@link #equals(Object)} method
    	 */
        @Override
        public int hashCode() {
            return Objects.hash(levelNames, values, regionId, regionPropertyId);
        }

        /**
    	 * Two {@link Data} instances are equal if and only if
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
            Data other = (Data) obj;
            return Objects.equals(levelNames, other.levelNames)
                    && Objects.equals(values, other.values)
                    && Objects.equals(regionId, other.regionId)
                    && Objects.equals(regionPropertyId, other.regionPropertyId);
        }

        @Override
        public String toString() {
            return "Data [levelNames=" + levelNames + ", values=" + values + ", regionId=" + regionId
                    + ", regionPropertyId=" + regionPropertyId + "]";
        }
    }

    /**
     * Returns a new builder for RegionPropertyDimensionData
     */
    public static Builder builder() {
        return new Builder(new Data());
    }

    private final Data data;

    private RegionPropertyDimensionData(Data data) {
        super(data.levelNames);
        this.data = data;
    }

    /**
     * Returns the region id for this dimension
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
     * Returns the value for the given level
     * 
     * @throws ContractException
     *                           <ul>
     *                           <li>{@linkplain NucleusError#INVALID_DIMENSION_LEVEL}
     *                           if the level is not valid</li>
     *                           </ul>
     */
    public Object getValue(int level) {
        if (level < 0 || level > this.getLevelCount()) {
            throw new ContractException(NucleusError.INVALID_DIMENSION_LEVEL, level);
        }

        Object value = data.values.get(level);
        return value;
    }

    /**
     * Returns the ordered list of region property values for this dimension
     */
    public List<Object> getValues() {
        return Collections.unmodifiableList(data.values);
    }

    /**
     * Returns the current version of this Simulation Plugin, which is equal to the
     * version of the GCM Simulation
     */
    public String getVersion() {
        return StandardVersioning.VERSION;
    }

    /**
     * Given a version string, returns whether the version is a supported version or
     * not.
     */
    public static boolean checkVersionSupported(String version) {
        return StandardVersioning.checkVersionSupported(version);
    }

    /**
     * Standard implementation consistent with the {@link #equals(Object)} method
     */
    @Override
    public int hashCode() {
        return Objects.hash(data);
    }

    /**
     * Two {@link RegionPropertyDimensionData} instances are equal if and only if
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
        RegionPropertyDimensionData other = (RegionPropertyDimensionData) obj;
        return Objects.equals(data, other.data);
    }

    @Override
    public String toString() {
        return "RegionPropertyDimensionData [data=" + data + "]";
    }

    /**
     * Returns a new builder instance that is pre-filled with the current state of
     * this instance.
     */
    public Builder toBuilder() {
        return new Builder(data);
    }

}
