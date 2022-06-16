package plugins.util.properties;

import plugins.people.support.PersonId;
import plugins.personproperties.support.PersonPropertyId;
import plugins.personproperties.testsupport.TestPersonPropertyId;
import plugins.util.properties.PropertyDefinitionInitialization.Builder;

public class Junk {
	public static void main(String[] args) {
		Builder<PersonPropertyId, PersonId> builder = new PropertyDefinitionInitialization.Builder<PersonPropertyId, PersonId>();
		PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Integer.class).setDefaultValue(23).build();
		builder.setPropertyDefinition(propertyDefinition);
		builder.setPropertyId(TestPersonPropertyId.PERSON_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK);
		builder.addPropertyValue(new PersonId(45), 67);
		//builder.addPropertyValue(new PersonId(5), "asdf");
		PropertyDefinitionInitialization<PersonPropertyId, PersonId> propertyDefinitionInitialization = builder.build();
		System.out.println(propertyDefinitionInitialization.getPropertyDefinition());
		System.out.println(propertyDefinitionInitialization.getPropertyId());
		System.out.println(propertyDefinitionInitialization.getPropertyValues());

	}
}
