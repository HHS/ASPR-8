package gov.hhs.aspr.ms.gcm.simulation.nucleus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import gov.hhs.aspr.ms.util.errors.ContractException;
import gov.hhs.aspr.ms.util.wrappers.MutableInteger;

public final class FunctionalDimensionData extends DimensionData {

    /**
     * A builder class for FunctionalDimensionData
     */
    public static class Builder {
        private Data data;

        private Builder(Data data) {
            this.data = data;
        }

        /**
         * Returns a FunctionalDimensionData from the collected data
         * 
         * @throws ContractException
         *                           <ul>
         *                           <li>{@linkplain NucleusError#DUPLICATE_DIMENSION_LEVEL_NAME}
         *                           if there are duplicate dimension level names</li>
         *                           </ul>
         */
        public FunctionalDimensionData build() {
            if (!data.locked) {
                validateData();
            }
            ensureImmutability();
            return new FunctionalDimensionData(data);
        }

        /**
         * Adds an experiment-level string meta datum value that describes the
         * corresponding scenario-level meta data returned by the individual levels of
         * this dimension.
         * 
         * @throws ContractException
         *                           <ul>
         *                           <li>{@linkplain NucleusError#NULL_META_DATA}
         *                           if the meta datum value is null</li>
         *                           </ul>
         */
        public Builder addMetaDatum(String idValue) {
            if (idValue == null) {
                throw new ContractException(NucleusError.NULL_META_DATA);
            }
            ensureDataMutability();
            data.metaData.add(idValue);
            return this;
        }

        /**
         * Adds a level function to the dimension. Each such function consumes a
         * DimensionContext of PluginDataBuilders and returns a list of scenario-level
         * meta data that describes the changes performed on the PluginDataBuilders. The
         * list of meta data is aligned to the experiment level meta data contained in
         * the dimension and must contain the same number of elements.
         * 
         * @throws ContractException
         *                           <ul>
         *                           <li>{@linkplain NucleusError#NULL_DIMENSION_LEVEL_NAME}
         *                           if the levelName is null or an empty string</li>
         *                           <li>{@linkplain NucleusError#NULL_FUNCTION}
         *                           if the level function is null</li>
         *                           </ul>
         */
        public Builder addValue(String levelName, Function<DimensionContext, List<String>> memberGenerator) {

            if (levelName == null || levelName.equals("")) {
                throw new ContractException(NucleusError.NULL_DIMENSION_LEVEL_NAME);
            }

            if (memberGenerator == null) {
                throw new ContractException(NucleusError.NULL_FUNCTION);
            }

            ensureDataMutability();

            int level = data.levelNames.indexOf(levelName);

            if (level == -1) {
                data.levelNames.add(levelName);
                level = data.levelNames.size() - 1;
                data.values.add(null);
            }

            data.values.set(level, memberGenerator);

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
        private final List<Function<DimensionContext, List<String>>> values = new ArrayList<>();
        private final List<String> metaData = new ArrayList<>();
        private boolean locked;

        private Data() {
        }

        private Data(Data data) {
            levelNames.addAll(data.levelNames);
            values.addAll(data.values);
            metaData.addAll(data.metaData);
            locked = data.locked;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((levelNames == null) ? 0 : levelNames.hashCode());
            result = prime * result + ((values == null) ? 0 : values.hashCode());
            result = prime * result + ((metaData == null) ? 0 : metaData.hashCode());
            return result;
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
            if (levelNames == null) {
                if (other.levelNames != null)
                    return false;
            } else if (!levelNames.equals(other.levelNames))
                return false;
            if (values == null) {
                if (other.values != null)
                    return false;
            } else if (!values.equals(other.values))
                return false;
            if (metaData == null) {
                if (other.metaData != null)
                    return false;
            } else if (!metaData.equals(other.metaData))
                return false;
            return true;
        }

        @Override
        public String toString() {
            return "Data [levelNames=" + levelNames + ", values=" + values + ", metaData=" + metaData + "]";
        }
    }

    /**
     * Returns a builder class for FunctionalDimensionData
     */
    public static Builder builder() {
        return new Builder(new Data());
    }

    private final Data data;

    private FunctionalDimensionData(Data data) {
        super(data.levelNames);
        this.data = data;
    }

    public List<String> getMetaData() {
        return Collections.unmodifiableList(data.metaData);
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
    public Function<DimensionContext, List<String>> getValue(int level) {
        if (level < 0 || level > this.getLevelCount()) {
            throw new ContractException(NucleusError.INVALID_DIMENSION_LEVEL, level);
        }

        Function<DimensionContext, List<String>> value = data.values.get(level);
        return value;
    }

    /**
     * Returns the ordered list of functional property values for this dimension
     */
    public List<Function<DimensionContext, List<String>>> getValues() {
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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((data == null) ? 0 : data.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        FunctionalDimensionData other = (FunctionalDimensionData) obj;
        if (data == null) {
            if (other.data != null)
                return false;
        } else if (!data.equals(other.data))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "FunctionalDimensionData [data=" + data + "]";
    }

    /**
     * Returns a new builder instance that is pre-filled with the current state of
     * this instance.
     */
    public Builder toBuilder() {
        return new Builder(data);
    }
}
