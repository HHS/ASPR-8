package plugins.partitions.datamanagers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import nucleus.PluginData;
import util.annotations.UnitTestMethod;

public class AT_PartitionsPluginData {

	@Test
	@UnitTestMethod(target = PartitionsPluginData.class, name = "builder", args = {})
	public void testBuilder() {
		assertNotNull(PartitionsPluginData.builder());
	}

	@Test
	@UnitTestMethod(target = PartitionsPluginData.class, name = "getCloneBuilder", args = {})
	public void testGetCloneBuilder() {
		PartitionsPluginData p1 = PartitionsPluginData.builder().setRunContinuitySupport(true).build();
		PluginData p2 = p1.getCloneBuilder().build();
		assertEquals(p1, p2);

		p1 = PartitionsPluginData.builder().setRunContinuitySupport(false).build();
		p2 = p1.getCloneBuilder().build();
		assertEquals(p1, p2);
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
		assertEquals(p1.hashCode(), p4.hashCode());
		assertEquals(p1.hashCode(), p5.hashCode());
		assertEquals(p2.hashCode(), p3.hashCode());

		// hash codes are reasonably distributed
		assertNotEquals(p1.hashCode(), p2.hashCode());

	}

	@Test
	@UnitTestMethod(target = PartitionsPluginData.class, name = "equals", args = { Object.class })
	public void testEquals() {

		PartitionsPluginData p1 = PartitionsPluginData.builder().build();
		PartitionsPluginData p2 = PartitionsPluginData.builder().setRunContinuitySupport(true).build();
		PartitionsPluginData p3 = PartitionsPluginData.builder().setRunContinuitySupport(true).build();
		PartitionsPluginData p4 = PartitionsPluginData.builder().setRunContinuitySupport(false).build();
		PartitionsPluginData p5 = PartitionsPluginData.builder().setRunContinuitySupport(false).build();

		// null
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

		// symmetry and transitivity
		assertEquals(p1, p4);
		assertEquals(p4, p1);

		assertEquals(p1, p5);
		assertEquals(p5, p1);

		assertEquals(p4, p5);
		assertEquals(p5, p4);

		assertEquals(p2, p3);
		assertEquals(p3, p2);

		// non-equality
		assertNotEquals(p1, p2);
		assertNotEquals(p1, p3);
		assertNotEquals(p4, p2);
		assertNotEquals(p4, p3);

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
	@UnitTestMethod(target = PartitionsPluginData.Builder.class, name = "supportsRunContinuity", args = {})
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
	@UnitTestMethod(target = PartitionsPluginData.Builder.class, name = "toString", args = {})
	public void testToString() {
		PartitionsPluginData p = PartitionsPluginData.builder().build();
		assertEquals("PartitionsPluginData [data=Data [supportRunContinuity=false]]",p.toString());
		
		p = PartitionsPluginData.builder().setRunContinuitySupport(false).build();
		assertEquals("PartitionsPluginData [data=Data [supportRunContinuity=false]]",p.toString());
		
		p = PartitionsPluginData.builder().setRunContinuitySupport(true).build();
		assertEquals("PartitionsPluginData [data=Data [supportRunContinuity=true]]",p.toString());

		
	}

}
