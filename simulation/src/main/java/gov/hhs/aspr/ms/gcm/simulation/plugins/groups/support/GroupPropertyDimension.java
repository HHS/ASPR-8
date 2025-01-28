package gov.hhs.aspr.ms.gcm.simulation.plugins.groups.support;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import gov.hhs.aspr.ms.gcm.simulation.nucleus.Dimension;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.DimensionContext;
import gov.hhs.aspr.ms.gcm.simulation.plugins.groups.datamanagers.GroupsPluginData;
import gov.hhs.aspr.ms.gcm.simulation.plugins.properties.support.PropertyError;
import gov.hhs.aspr.ms.util.errors.ContractException;

/**
 * Dimension implementation for setting a group property to a list of values in
 * a groups plugin data.
 */
public class GroupPropertyDimension implements Dimension {
    private final Data data;

    private GroupPropertyDimension(Data data) {
        this.data = data;
    }

    private static class Data {
        private GroupId groupId;
        private GroupPropertyId groupPropertyId;
        private List<Object> values = new ArrayList<>();
        private boolean locked;

        private Data() {
        }

        private Data(Data data) {
            groupPropertyId = data.groupPropertyId;
            groupId = data.groupId;
            values.addAll(data.values);
            locked = data.locked;
        }

        @Override
        public int hashCode() {
            return Objects.hash(groupId, groupPropertyId, values);
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
            return Objects.equals(groupId, other.groupId) && Objects.equals(groupPropertyId, other.groupPropertyId)
                    && Objects.equals(values, other.values);
        }

    }

    /**
     * Returns a new builder for GroupPropertyDimension
     */
    public static Builder builder() {
        return new Builder(new Data());
    }

    /**
     * Builder class for GroupPropertyDimension
     */
    public static class Builder {

        private Data data;

        private Builder(Data data) {
            this.data = data;
        }

        /**
         * Returns the GroupPropertyDimension from the collected data.
         * 
         * @throws ContractException
         *                           <ul>
         *                           <li>{@linkplain PropertyError#NULL_PROPERTY_ID} if
         *                           the group property id was not assigned</li>
         *                           <li>{@linkplain GroupError#NULL_GROUP_ID} if the
         *                           groupId was not assigned</li>
         *                           </ul>
         */
        public GroupPropertyDimension build() {
            if (!data.locked) {
                validate();
            }
            ensureImmutability();
            return new GroupPropertyDimension(data);
        }

        private void validate() {
            if (data.groupId == null) {
                throw new ContractException(GroupError.NULL_GROUP_ID);
            }

            if (data.groupPropertyId == null) {
                throw new ContractException(PropertyError.NULL_PROPERTY_ID);
            }
        }

        /**
         * Sets the group id for the dimension. Defaults to null.
         * 
         * @throws ContractException {@linkplain GroupError#NULL_GROUP_ID} if the
         *                           groupId is null
         */
        public Builder setGroupId(GroupId groupId) {
            ensureDataMutability();
            validateGroupId(groupId);
            data.groupId = groupId;
            return this;
        }

        /**
         * Sets the group property for the dimension. Defaults to null.
         * 
         * @throws ContractException {@linkplain PropertyError#NULL_PROPERTY_ID} if the
         *                           property id is null
         */
        public Builder setGroupPropertyId(GroupPropertyId groupPropertyId) {
            ensureDataMutability();
            validateGroupPropertyId(groupPropertyId);
            data.groupPropertyId = groupPropertyId;
            return this;
        }

        /**
         * Adds a value to the dimension.
         * 
         * @throws ContractException {@linkplain PropertyError#NULL_PROPERTY_VALUE} if
         *                           the value is null
         */
        public Builder addValue(Object value) {
            ensureDataMutability();
            validateValue(value);
            data.values.add(value);
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
    }

    @Override
    public List<String> getExperimentMetaData() {
        List<String> result = new ArrayList<>();
        result.add(data.groupPropertyId.toString());
        return result;
    }

    @Override
    public int levelCount() {
        return data.values.size();
    }

    @Override
    public List<String> executeLevel(DimensionContext dimensionContext, int level) {
        GroupsPluginData.Builder builder = dimensionContext.getPluginDataBuilder(GroupsPluginData.Builder.class);
        Object value = data.values.get(level);
        builder.setGroupPropertyValue(data.groupId, data.groupPropertyId, value);
        List<String> result = new ArrayList<>();
        result.add(value.toString());
        return result;
    }

    /**
     * Returns the group id for this dimension
     */
    public GroupId getGroupId() {
        return data.groupId;
    }

    /**
     * Returns the group property id for this dimension
     */
    public GroupPropertyId getGroupPropertyId() {
        return data.groupPropertyId;
    }

    /**
     * Returns the ordered list of group property values for this dimension
     */
    public List<Object> getValues() {
        return new ArrayList<>(data.values);
    }

    private static void validateValue(Object value) {
        if (value == null) {
            throw new ContractException(PropertyError.NULL_PROPERTY_VALUE);
        }
    }

    private static void validateGroupPropertyId(GroupPropertyId groupPropertyId) {
        if (groupPropertyId == null) {
            throw new ContractException(PropertyError.NULL_PROPERTY_ID);
        }
    }

    private static void validateGroupId(GroupId groupId) {
        if (groupId == null) {
            throw new ContractException(GroupError.NULL_GROUP_ID);
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
        GroupPropertyDimension other = (GroupPropertyDimension) obj;
        return Objects.equals(data, other.data);
    }

    public Builder toBuilder() {
		return new Builder(data);
	}
}
