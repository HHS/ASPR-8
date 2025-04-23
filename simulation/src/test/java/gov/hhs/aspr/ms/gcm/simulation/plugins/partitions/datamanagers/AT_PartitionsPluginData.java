package gov.hhs.aspr.ms.gcm.simulation.plugins.partitions.datamanagers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.gcm.simulation.nucleus.PluginData;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.StandardVersioning;
import gov.hhs.aspr.ms.util.annotations.UnitTestMethod;

public class AT_PartitionsPluginData {

	@Test
	@UnitTestMethod(target = PartitionsPluginData.class, name = "builder", args = {})
	public void testBuilder() {
		assertNotNull(PartitionsPluginData.builder());
	}

	@Test
	@UnitTestMethod(target = PartitionsPluginData.class, name = "toBuilder", args = {})
	public void testToBuilder() {
		PartitionsPluginData p1 = PartitionsPluginData.builder().setRunContinuitySupport(true).build();
		PluginData p2 = p1.toBuilder().build();
		assertEquals(p1, p2);
		
		

		p1 = PartitionsPluginData.builder().setRunContinuitySupport(false).build();
		p2 = p1.toBuilder().build();
		assertEquals(p1, p2);
		
		
		// show that the returned clone builder will build an identical instance if no
		// mutations are made
		PartitionsPluginData.Builder cloneBuilder = p1.toBuilder();
		assertNotNull(cloneBuilder);
		assertEquals(p1, cloneBuilder.build());

		// show that the clone builder builds a distinct instance if any mutation is
		// made

		// defineGlobalProperty
		cloneBuilder = p1.toBuilder();
		cloneBuilder.setRunContinuitySupport(true);
		assertNotEquals(p1, cloneBuilder.build());

	}

	@Test
	@UnitTestMethod(target = PartitionsPluginData.class, name = "getVersion", args = {})
	public void testGetVersion() {
		PartitionsPluginData pluginData = PartitionsPluginData.builder().setRunContinuitySupport(true).build();
		assertEquals(StandardVersioning.VERSION, pluginData.getVersion());
	}

	@Test
	@UnitTestMethod(target = PartitionsPluginData.class, name = "checkVersionSupported", args = { String.class })
	public void testCheckVersionSupported() {
		List<String> versions = Arrays.asList(StandardVersioning.VERSION);

		for (String version : versions) {
			assertTrue(PartitionsPluginData.checkVersionSupported(version));
			assertFalse(PartitionsPluginData.checkVersionSupported(version + "badVersion"));
			assertFalse(PartitionsPluginData.checkVersionSupported("badVersion"));
			assertFalse(PartitionsPluginData.checkVersionSupported(version + "0"));
			assertFalse(PartitionsPluginData.checkVersionSupported(version + ".0.0"));
		}
	}

	@Test
	@UnitTestMethod(target = PartitionsPluginData.class, name = "hashCode", args = {})
	public void testHashCode() {
		PartitionsPluginData p1 = PartitionsPluginData.builder().build();
		PartitionsPluginData p2 = PartitionsPluginData.builder().setRunContinuitySupport(true).build();
		PartitionsPluginData p3 = PartitionsPluginData.builder().setRunContinuitySupport(true).build();
		PartitionsPluginData p4 = PartitionsPluginData.builder().setRunContinuitySupport(false).build();
		PartitionsPluginData p5 = PartitionsPluginData.builder().setRunContinuitySupport(false).build();

		// equal objects have equal hash codes
		assertEquals(p1, p4);
		assertEquals(p1.hashCode(), p4.hashCode());

		assertEquals(p1, p5);
		assertEquals(p1.hashCode(), p5.hashCode());

		assertEquals(p4, p5);
		assertEquals(p4.hashCode(), p5.hashCode());
		
		assertEquals(p2, p3);
		assertEquals(p2.hashCode(), p3.hashCode());

		// hash codes are reasonably distributed
		assertNotEquals(p1.hashCode(), p2.hashCode());
		assertNotEquals(p1.hashCode(), p3.hashCode());
		assertNotEquals(p4.hashCode(), p2.hashCode());
		assertNotEquals(p4.hashCode(), p3.hashCode());
		assertNotEquals(p5.hashCode(), p2.hashCode());
		assertNotEquals(p5.hashCode(), p3.hashCode());
	}

	@Test
	@UnitTestMethod(target = PartitionsPluginData.class, name = "equals", args = { Object.class })
	public void testEquals() {

		PartitionsPluginData p1 = PartitionsPluginData.builder().build();
		PartitionsPluginData p2 = PartitionsPluginData.builder().setRunContinuitySupport(true).build();
		PartitionsPluginData p3 = PartitionsPluginData.builder().setRunContinuitySupport(true).build();
		PartitionsPluginData p4 = PartitionsPluginData.builder().setRunContinuitySupport(false).build();
		PartitionsPluginData p5 = PartitionsPluginData.builder().setRunContinuitySupport(false).build();

		// never equal to another type
		assertFalse(p1.equals(new Object()));
		assertFalse(p2.equals(new Object()));
		assertFalse(p3.equals(new Object()));
		assertFalse(p4.equals(new Object()));
		assertFalse(p5.equals(new Object()));

		// never equal to null
		assertNotEquals(p1, null);
		assertNotEquals(p2, null);
		assertNotEquals(p3, null);
		assertNotEquals(p4, null);
		assertNotEquals(p5, null);

		// reflexive
		assertEquals(p1, p1);
		assertEquals(p2, p2);
		assertEquals(p3, p3);
		assertEquals(p4, p4);
		assertEquals(p5, p5);

		// symmetric, transitive, consistent
		for (int j = 0; j < 10; j++) {
			assertFalse(p1 == p4);
			assertEquals(p1, p4);
			assertEquals(p4, p1);
	
			assertFalse(p1 == p5);
			assertEquals(p1, p5);
			assertEquals(p5, p1);
	
			assertFalse(p4 == p5);
			assertEquals(p4, p5);
			assertEquals(p5, p4);
	
			assertFalse(p2 == p3);
			assertEquals(p2, p3);
			assertEquals(p3, p2);
		}

		// different inputs yield unequal plugin datas
		assertNotEquals(p1, p2);
		assertNotEquals(p1, p3);
		assertNotEquals(p4, p2);
		assertNotEquals(p4, p3);
		assertNotEquals(p5, p2);
		assertNotEquals(p5, p3);
	}

	@Test
	@UnitTestMethod(target = PartitionsPluginData.Builder.class, name = "build", args = {})
	public void testBuild() {

		PartitionsPluginData partitionsPluginData = PartitionsPluginData.builder().build();
		assertNotNull(partitionsPluginData);

		partitionsPluginData = PartitionsPluginData.builder().setRunContinuitySupport(true).build();
		assertNotNull(partitionsPluginData);

		partitionsPluginData = PartitionsPluginData.builder().setRunContinuitySupport(false).build();
		assertNotNull(partitionsPluginData);

	}

	@Test
	@UnitTestMethod(target = PartitionsPluginData.class, name = "supportsRunContinuity", args = {})
	public void testSupportsRunContinuity() {
		PartitionsPluginData p = PartitionsPluginData.builder().setRunContinuitySupport(true).build();
		assertTrue(p.supportsRunContinuity());

		p = PartitionsPluginData.builder().setRunContinuitySupport(false).build();
		assertFalse(p.supportsRunContinuity());

		p = PartitionsPluginData.builder().build();
		assertFalse(p.supportsRunContinuity());

	}

	@Test
	@UnitTestMethod(target = PartitionsPluginData.Builder.class, name = "setRunContinuitySupport", args = { boolean.class })
	public void testSetRunContinuitySupport() {

		PartitionsPluginData p = PartitionsPluginData.builder().setRunContinuitySupport(true).build();
		assertTrue(p.supportsRunContinuity());

		p = PartitionsPluginData.builder().setRunContinuitySupport(false).build();
		assertFalse(p.supportsRunContinuity());
	}

	@Test
	@UnitTestMethod(target = PartitionsPluginData.class, name = "toString", args = {})
	public void testToString() {
		PartitionsPluginData p = PartitionsPluginData.builder().build();
		assertEquals("PartitionsPluginData [data=Data [supportRunContinuity=false]]",p.toString());
		
		p = PartitionsPluginData.builder().setRunContinuitySupport(false).build();
		assertEquals("PartitionsPluginData [data=Data [supportRunContinuity=false]]",p.toString());
		
		p = PartitionsPluginData.builder().setRunContinuitySupport(true).build();
		assertEquals("PartitionsPluginData [data=Data [supportRunContinuity=true]]",p.toString());

		
	}

}
