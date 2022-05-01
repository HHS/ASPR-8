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

	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {
		private Data data = new Data();

		private Builder() {

		}

		/**
		 * Returns the {@linkplain AttributesPluginData} from the collected data
		 */
		public AttributesPluginData build() {
			try {
				return new AttributesPluginData(data);
			} finally {
				data = new Data();
			}
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
		// TODO Auto-generated method stub
		return null;
	}

}
