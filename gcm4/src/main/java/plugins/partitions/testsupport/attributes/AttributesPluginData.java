package plugins.partitions.testsupport.attributes;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import net.jcip.annotations.Immutable;
import nucleus.PluginData;
import nucleus.PluginDataBuilder;
import plugins.partitions.testsupport.attributes.support.AttributeDefinition;
import plugins.partitions.testsupport.attributes.support.AttributeError;
import plugins.partitions.testsupport.attributes.support.AttributeId;
import util.errors.ContractException;

@Immutable
public class AttributesPluginData implements PluginData {

	private static class Data {
		private Map<AttributeId, AttributeDefinition> attributeDefinitions = new LinkedHashMap<>();
		private boolean locked;

		public Data() {
		}

		public Data(Data data) {
			locked = data.locked;
			attributeDefinitions.putAll(data.attributeDefinitions);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((attributeDefinitions == null) ? 0 : attributeDefinitions.hashCode());
			result = prime * result + (locked ? 1231 : 1237);
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (!(obj instanceof Data)) {
				return false;
			}
			Data other = (Data) obj;
			if (attributeDefinitions == null) {
				if (other.attributeDefinitions != null) {
					return false;
				}
			} else if (!attributeDefinitions.equals(other.attributeDefinitions)) {
				return false;
			}
			if (locked != other.locked) {
				return false;
			}
			return true;
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("Data [attributeDefinitions=");
			builder.append(attributeDefinitions);
			builder.append(", locked=");
			builder.append(locked);
			builder.append("]");
			return builder.toString();
		}
		
		
	}

	public static Builder builder() {
		return new Builder(new Data());
	}

	public static class Builder implements PluginDataBuilder {
		private Data data = new Data();

		private Builder(Data data) {
			this.data = data;
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

		/**
		 * Returns the {@linkplain AttributesPluginData} from the collected data
		 */
		public AttributesPluginData build() {
			ensureImmutability();
			return new AttributesPluginData(new Data(data));
		}

		/**
		 * Adds an attribute definition.
		 *
		 * @throws ContractException
		 *             <li>{@linkplain AttributeError#NULL_ATTRIBUTE_ID} if the
		 *             attribute id is null</li>
		 *             <li>{@linkplain AttributeError#NULL_ATTRIBUTE_DEFINITION}
		 *             if the attribute definition is null</li>
		 *             <li>{@linkplain AttributeError#DUPLICATE_ATTRIBUTE_DEFINITION}
		 *             if the attribute id was previously added</li>
		 */
		public Builder defineAttribute(final AttributeId attributeId, final AttributeDefinition attributeDefinition) {
			ensureDataMutability();
			validateAttributeIdNotNull(attributeId);
			validateAttributeDefinitionNotNull(attributeDefinition);
			validateAttributeIsNotDefined(data, attributeId);
			data.attributeDefinitions.put(attributeId, attributeDefinition);
			return this;
		}

	}

	private static void validateAttributeIsNotDefined(final Data data, final AttributeId attributeId) {
		AttributeDefinition attributeDefinition = data.attributeDefinitions.get(attributeId);
		if (attributeDefinition != null) {
			throw new ContractException(AttributeError.DUPLICATE_ATTRIBUTE_DEFINITION, attributeId);
		}
	}

	private static void validateAttributeDefinitionNotNull(AttributeDefinition attributeDefinition) {
		if (attributeDefinition == null) {
			throw new ContractException(AttributeError.NULL_ATTRIBUTE_DEFINITION);
		}
	}

	private static void validateAttributeIdNotNull(AttributeId attributeId) {
		if (attributeId == null) {
			throw new ContractException(AttributeError.NULL_ATTRIBUTE_ID);
		}
	}

	private final Data data;

	private AttributesPluginData(Data data) {
		this.data = data;
	}

	/**
	 * Returns the attribute definition for the given attribute id
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain AttributeError#NULL_ATTRIBUTE_ID} if the
	 *             attribute id is null</li>
	 *             <li>{@linkplain AttributeError#UNKNOWN_ATTRIBUTE_ID} if the
	 *             attribute id is unknown</li>
	 */
	public AttributeDefinition getAttributeDefinition(final AttributeId attributeId) {
		validateAttributeIdNotNull(attributeId);
		AttributeDefinition attributeDefinition = data.attributeDefinitions.get(attributeId);
		if (attributeDefinition == null) {
			throw new ContractException(AttributeError.UNKNOWN_ATTRIBUTE_ID, attributeId);
		}
		return attributeDefinition;
	}

	/**
	 * Returns the attribute ids
	 *
	 */
	@SuppressWarnings("unchecked")
	public <T extends AttributeId> Set<T> getAttributeIds() {
		Set<T> result = new LinkedHashSet<>();
		for (AttributeId attributeId : data.attributeDefinitions.keySet()) {
			result.add((T) attributeId);
		}
		return result;
	}

	@Override
	public PluginDataBuilder getCloneBuilder() {

		return new Builder(data);
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
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof AttributesPluginData)) {
			return false;
		}
		AttributesPluginData other = (AttributesPluginData) obj;
		if (data == null) {
			if (other.data != null) {
				return false;
			}
		} else if (!data.equals(other.data)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder2 = new StringBuilder();
		builder2.append("AttributesPluginData [data=");
		builder2.append(data);
		builder2.append("]");
		return builder2.toString();
	}
	
	

}
