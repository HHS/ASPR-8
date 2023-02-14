package plugins.partitions.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import plugins.partitions.testsupport.attributes.support.AttributeLabeler;
import plugins.partitions.testsupport.attributes.support.TestAttributeId;
import util.annotations.UnitTestMethod;

public class AT_Partition {

	/**
	 * Tests {@linkplain Partition#builder()
	 */
	@Test
	@UnitTestMethod(target = Partition.class, name = "builder", args = {})
	public void testBuilder() {
		Partition partition = Partition.builder().build();
		assertNotNull(partition);
		assertFalse(partition.getFilter().isPresent());
		assertTrue(partition.getLabelers().isEmpty());
		assertTrue(partition.isDegenerate());
	}

	/**
	 * Tests {@linkplain Partition#getLabelers()
	 */
	@Test
	@UnitTestMethod(target = Partition.class, name = "getLabelers", args = {})
	public void testGetLabelers() {

		Set<Labeler> expectedLabelers = new LinkedHashSet<>();
		expectedLabelers.add(new AttributeLabeler(TestAttributeId.BOOLEAN_0, (v) -> new Object()));
		expectedLabelers.add(new AttributeLabeler(TestAttributeId.BOOLEAN_1, (v) -> new Object()));
		expectedLabelers.add(new AttributeLabeler(TestAttributeId.DOUBLE_0, (v) -> new Object()));

		Partition.Builder builder = Partition.builder();
		for (Labeler labeler : expectedLabelers) {
			builder.addLabeler(labeler);
		}

		Partition partition = builder.build();

		Set<Labeler> actualLabelers = partition.getLabelers();

		assertEquals(expectedLabelers, actualLabelers);

	}

	/**
	 * Tests {@linkplain Partition#getFilter()
	 */
	@Test
	@UnitTestMethod(target = Partition.class, name = "getFilter", args = {})
	public void testGetFilter() {

		Partition partition = Partition.builder().build();//
		assertFalse(partition.getFilter().isPresent());

		partition = Partition.builder().setFilter(Filter.allPeople()).build();//
		assertTrue(partition.getFilter().isPresent());

	}

	/**
	 * Tests {@linkplain Partition#isDegenerate()
	 */
	@Test
	@UnitTestMethod(target = Partition.class, name = "isDegenerate", args = {})
	public void testIsDegenerate() {

		Partition partition = Partition.builder().build();//
		assertTrue(partition.isDegenerate());

		partition = Partition.builder().addLabeler(new AttributeLabeler(TestAttributeId.BOOLEAN_0, (v) -> new Object())).build();
		assertFalse(partition.isDegenerate());
	}

	@Test
	@UnitTestMethod(target = Partition.class, name = "retainPersonKeys", args = {})
	public void testRetainPersonKeys() {
		Partition retainKeys = Partition.builder().setRetainPersonKeys(true).build();
		Partition dontRetainKeys = Partition.builder().setRetainPersonKeys(false).build();

		assertTrue(retainKeys.retainPersonKeys());
		assertFalse(dontRetainKeys.retainPersonKeys());
	}

	@Test
	@UnitTestMethod(target = Partition.Builder.class, name = "build", args = {})
	public void testBuild() {
		Partition partition = Partition.builder().build();
		assertNotNull(partition);
	}

	@Test
	@UnitTestMethod(target = Partition.Builder.class, name = "setFilter", args = { Filter.class })
	public void testSetFilter() {
		Partition.Builder builder = Partition.builder();
		Filter filter = Filter.allPeople();
		builder.setFilter(filter);

		Partition partition = builder.build();
		assertNotNull(partition);
		assertEquals(filter, partition.getFilter().get());
		assertTrue(!Partition.builder().build().getFilter().isPresent());
	}

	@Test
	@UnitTestMethod(target = Partition.Builder.class, name = "setRetainPersonKeys", args = { boolean.class })
	public void testSetRetainPersonKeys() {
		Partition retainKeys = Partition.builder().setRetainPersonKeys(true).build();
		Partition dontRetainKeys = Partition.builder().setRetainPersonKeys(false).build();

		assertTrue(retainKeys.retainPersonKeys());
		assertFalse(dontRetainKeys.retainPersonKeys());
	}

	@Test
	@UnitTestMethod(target = Partition.Builder.class, name = "addLabeler", args = { Labeler.class })
	public void testAddlabeler() {
		Set<Labeler> expectedLabelers = new LinkedHashSet<>();
		expectedLabelers.add(new AttributeLabeler(TestAttributeId.BOOLEAN_0, (v) -> new Object()));
		expectedLabelers.add(new AttributeLabeler(TestAttributeId.BOOLEAN_1, (v) -> new Object()));
		expectedLabelers.add(new AttributeLabeler(TestAttributeId.DOUBLE_0, (v) -> new Object()));

		Partition.Builder builder = Partition.builder();
		for (Labeler labeler : expectedLabelers) {
			builder.addLabeler(labeler);
		}

		Partition partition = builder.build();

		Set<Labeler> actualLabelers = partition.getLabelers();

		assertEquals(expectedLabelers, actualLabelers);
	}
}
