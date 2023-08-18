package gov.hhs.aspr.ms.gcm.plugins.util.properties;

import java.util.Iterator;
import java.util.function.Supplier;

import gov.hhs.aspr.ms.gcm.plugins.util.properties.arraycontainers.IntValueContainer;
import gov.hhs.aspr.ms.gcm.plugins.util.properties.arraycontainers.IntValueContainer.IntValueType;
import util.errors.ContractException;

/**
 * Implementor of IndexedPropertyManager that compresses Byte, Short, Integer or
 * Long property values into a byte-based array data structure.
 */
public final class IntPropertyManager implements IndexedPropertyManager {

	/*
	 * A container, indexed by person id, that stores the various Boxed integral
	 * types values as bytes.
	 */
	private IntValueContainer intValueContainer;

	/*
	 * The particular IntValueType for this property manager as determined by the
	 * class type associated with the corresponding property definition.
	 */
	private IntValueType intValueType;

	/**
	 * Constructs this IntPropertyManager.
	 * 
	 * @throws util.errors.ContractException
	 *                           <ul>
	 *                           <li>{@linkplain PropertyError#NEGATIVE_INITIAL_SIZE}
	 *                           if the initial size is negative</li>
	 *                           <li>{@linkplain PropertyError#NULL_PROPERTY_DEFINITION}
	 *                           if the property definition is null</li>
	 *                           <li>{@linkplain PropertyError#PROPERTY_DEFINITION_IMPROPER_TYPE}
	 *                           if the property definition's type is not a Byte,
	 *                           Short, Integer or Long</li>
	 *                           </ul>
	 */
	public IntPropertyManager(PropertyDefinition propertyDefinition,
			Supplier<Iterator<Integer>> indexIteratorSupplier) {
		if (propertyDefinition == null) {
			throw new ContractException(PropertyError.NULL_PROPERTY_DEFINITION);
		}

		long longDefaultValue = 0L;
		if (propertyDefinition.getType() == Byte.class) {
			intValueType = IntValueType.BYTE;
		} else if (propertyDefinition.getType() == Short.class) {
			intValueType = IntValueType.SHORT;
		} else if (propertyDefinition.getType() == Integer.class) {
			intValueType = IntValueType.INT;
		} else if (propertyDefinition.getType() == Long.class) {
			intValueType = IntValueType.LONG;
		} else {
			throw new ContractException(PropertyError.PROPERTY_DEFINITION_IMPROPER_TYPE,
					"Requires a property definition type of Byte, Short, Integer or Long");
		}
		if (propertyDefinition.getDefaultValue().isPresent()) {
			Object defaultValue = propertyDefinition.getDefaultValue().get();

			switch (intValueType) {
			case BYTE:
				Byte b = (Byte) defaultValue;
				longDefaultValue = b.longValue();
				break;
			case INT:
				Integer i = (Integer) defaultValue;
				longDefaultValue = i.longValue();
				break;
			case LONG:
				Long l = (Long) defaultValue;
				longDefaultValue = l.longValue();
				break;
			case SHORT:
				Short s = (Short) defaultValue;
				longDefaultValue = s.longValue();
				break;
			default:
				throw new RuntimeException("unhandled type " + intValueType);
			}
		}

		intValueContainer = new IntValueContainer(longDefaultValue, indexIteratorSupplier);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T getPropertyValue(int id) {
		if (id < 0) {
			throw new ContractException(PropertyError.NEGATIVE_INDEX);
		}

		switch (intValueType) {
		case BYTE:
			Byte b = intValueContainer.getValueAsByte(id);
			return (T) b;
		case INT:
			Integer i = intValueContainer.getValueAsInt(id);
			return (T) i;
		case LONG:
			Long l = intValueContainer.getValueAsLong(id);
			return (T) l;
		case SHORT:
			Short s = intValueContainer.getValueAsShort(id);
			return (T) s;
		default:
			throw new RuntimeException("unhandled type");
		}
	}

	@Override
	public void setPropertyValue(int id, Object propertyValue) {
		if (id < 0) {
			throw new ContractException(PropertyError.NEGATIVE_INDEX);
		}

		switch (intValueType) {
		case BYTE:
			Byte b = (Byte) propertyValue;
			intValueContainer.setByteValue(id, b);
			break;
		case INT:
			Integer i = (Integer) propertyValue;
			intValueContainer.setIntValue(id, i);
			break;
		case LONG:
			Long l = (Long) propertyValue;
			intValueContainer.setLongValue(id, l);
			break;
		case SHORT:
			Short s = (Short) propertyValue;
			intValueContainer.setShortValue(id, s);
			break;
		default:
			throw new RuntimeException("unhandled type " + intValueType);
		}
	}

	@Override
	public void incrementCapacity(int count) {
		if (count < 0) {
			throw new ContractException(PropertyError.NEGATIVE_CAPACITY_INCREMENT);
		}
		intValueContainer.setCapacity(intValueContainer.getCapacity() + count);
	}

	@Override
	public void removeId(int id) {
		if (id < 0) {
			throw new ContractException(PropertyError.NEGATIVE_INDEX);
		}
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("IntPropertyManager [intValueContainer=");
		builder.append(intValueContainer);
		builder.append(", intValueType=");
		builder.append(intValueType);
		builder.append("]");
		return builder.toString();
	}

}
