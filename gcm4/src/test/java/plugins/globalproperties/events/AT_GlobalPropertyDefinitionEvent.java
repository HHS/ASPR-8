package plugins.globalproperties.events;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import plugins.globalproperties.support.GlobalPropertyId;
import plugins.globalproperties.support.SimpleGlobalPropertyId;
import plugins.util.properties.PropertyError;
import tools.annotations.UnitTestConstructor;
import util.errors.ContractException;

public class AT_GlobalPropertyDefinitionEvent {

	@Test
	@UnitTestConstructor(target = GlobalPropertyDefinitionEvent.class, args = { GlobalPropertyId.class, Object.class })
	public void testConstructor() {

		ContractException contractException = assertThrows(ContractException.class, () -> new GlobalPropertyDefinitionEvent(null, 7));
		assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());

		SimpleGlobalPropertyId goodId = new SimpleGlobalPropertyId(5);

		ContractException contractException2 = assertThrows(ContractException.class, () -> new GlobalPropertyDefinitionEvent(goodId, null));
		assertEquals(PropertyError.NULL_PROPERTY_VALUE, contractException2.getErrorType());

	}

}