package gov.hhs.aspr.ms.gcm.plugins.partitions.testsupport;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.function.Function;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.gcm.plugins.partitions.testsupport.attributes.support.AttributeId;
import gov.hhs.aspr.ms.util.annotations.UnitTestConstructor;

public class AT_FunctionalAttributeLabeler {
	private static enum LocalAttributeId implements AttributeId {
		LOCAL_ID
	}

	@Test
	@UnitTestConstructor(target = FunctionalAttributeLabeler.class, args = { AttributeId.class, Function.class })
	public void testFunctionalAttributeLabeler() {
		FunctionalAttributeLabeler functionalAttributeLabeler = new FunctionalAttributeLabeler(
				LocalAttributeId.LOCAL_ID, (a) -> "valid value");
		
		assertEquals(LocalAttributeId.LOCAL_ID, functionalAttributeLabeler.getId());

	}

}
