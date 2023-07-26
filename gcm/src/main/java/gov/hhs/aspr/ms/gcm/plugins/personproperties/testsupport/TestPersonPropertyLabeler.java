package gov.hhs.aspr.ms.gcm.plugins.personproperties.testsupport;

import gov.hhs.aspr.ms.gcm.plugins.partitions.support.Equality;
import gov.hhs.aspr.ms.gcm.plugins.personproperties.support.PersonPropertyId;
import gov.hhs.aspr.ms.gcm.plugins.personproperties.support.PersonPropertyLabeler;

/**
 * A person property labeler that labels people as either true or false based on
 * an Equality based comparison with an integer value and the specified value.
 * 
 * Should only be used with properties that are comparable to integers when the
 * equality type is not Equality.EQUAL or Equality.NOT_EQUAL.
 */
public class TestPersonPropertyLabeler extends PersonPropertyLabeler {

	private final Equality equality;

	private final Integer value;

	public int getValue() {
		return value;
	}

	public Equality getEquality() {
		return equality;
	}

	public TestPersonPropertyLabeler(PersonPropertyId personPropertyId, Equality equality, int value) {
		super(personPropertyId);
		this.equality = equality;
		this.value = value;
	}

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected Object getLabelFromValue(Object value) {
		if (equality.equals(Equality.EQUAL)) {
			return this.value.equals(value);
		} else if (equality.equals(Equality.NOT_EQUAL)) {
			return !this.value.equals(value);
		} else {
			Comparable comparableAttributeValue = (Comparable) value;
			int evaluation = comparableAttributeValue.compareTo(this.value);
			return equality.isCompatibleComparisonValue(evaluation);
		}
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("TestPersonPropertyLabeler [equality=");
		builder.append(equality);
		builder.append(", value=");
		builder.append(value);
		builder.append(", personPropertyId=");
		builder.append(getPersonPropertyId());
		builder.append("]");
		return builder.toString();
	}
	
	

}
