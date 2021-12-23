package plugins.personproperties.support;

public class PersonPropertyInitialization {
	private final PersonPropertyId personPropertyId;
	private final Object value;

	public PersonPropertyInitialization(PersonPropertyId personPropertyId, Object value) {
		super();
		this.personPropertyId = personPropertyId;
		this.value = value;
	}

	public PersonPropertyId getPersonPropertyId() {
		return personPropertyId;
	}

	public Object getValue() {
		return value;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("PersonPropertyAssignment [personPropertyId=");
		builder.append(personPropertyId);
		builder.append(", value=");
		builder.append(value);
		builder.append("]");
		return builder.toString();
	}

}
