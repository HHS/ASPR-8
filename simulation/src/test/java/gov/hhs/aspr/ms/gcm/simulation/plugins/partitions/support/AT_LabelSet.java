package gov.hhs.aspr.ms.gcm.simulation.plugins.partitions.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.util.annotations.UnitTestMethod;
import gov.hhs.aspr.ms.util.errors.ContractException;
import gov.hhs.aspr.ms.util.random.RandomGeneratorProvider;


public class AT_LabelSet {

	private static enum Dimension {
		DIM_1, DIM_2, DIM_3, DIM_4, DIM_5;
	}

	/**
	 * Tests {@linkplain LabelSet#builder()
	 */
	@Test
	@UnitTestMethod(target = LabelSet.class, name = "builder", args = {})
	public void testBuilder() {
		assertNotNull(LabelSet.builder());
	}

	/**
	 * Tests {@linkplain LabelSet#isEmpty()
	 */
	@Test
	@UnitTestMethod(target = LabelSet.class, name = "isEmpty", args = {})
	public void testIsEmpty() {

		LabelSet labelSet = LabelSet.builder().build();
		assertTrue(labelSet.isEmpty());

		labelSet = LabelSet.builder().setLabel(Dimension.DIM_1, "compartment label").build();
		assertFalse(labelSet.isEmpty());

		labelSet = LabelSet.builder().setLabel(Dimension.DIM_2, "group label").build();
		assertFalse(labelSet.isEmpty());

		labelSet = LabelSet.builder().setLabel(Dimension.DIM_3, "region label").build();
		assertFalse(labelSet.isEmpty());

		labelSet = LabelSet.builder().setLabel(Dimension.DIM_4, "resource label").build();
		assertFalse(labelSet.isEmpty());

		labelSet = LabelSet.builder().setLabel(Dimension.DIM_5, "property label").build();
		assertFalse(labelSet.isEmpty());

	}

	@Test
	@UnitTestMethod(target = LabelSet.class, name = "getLabel", args = { Object.class })
	public void testGetLabel() {
		Object expectedCompartmentLabel = "Compartment Label";
		LabelSet labelSet = LabelSet.builder().setLabel(Dimension.DIM_1, expectedCompartmentLabel).build();
		Optional<Object> optionalLabel = labelSet.getLabel(Dimension.DIM_1);
		assertTrue(optionalLabel.isPresent());
		Object actualCompartmentLabel = optionalLabel.get();
		assertEquals(expectedCompartmentLabel, actualCompartmentLabel);

	}

	/**
	 * Tests {@linkplain LabelSet#getDimensions()
	 */
	@Test
	@UnitTestMethod(target = LabelSet.class, name = "getDimensions", args = {})
	public void testGetDimensions() {
		// getDimensions()
		LabelSet.Builder builder = LabelSet.builder();
		Set<Object> expectedDimensions = new LinkedHashSet<>();
		for (int i = 0; i < 10; i++) {
			expectedDimensions.add(i);
			builder.setLabel(i, Integer.toString(i));
		}
		LabelSet labelSet = builder.build();
		Set<Object> actualDimensions = labelSet.getDimensions();
		assertEquals(expectedDimensions, actualDimensions);
	}

	/**
	 * Tests {@linkplain LabelSet#equals(Object)
	 */
	@Test
	@UnitTestMethod(target = LabelSet.class, name = "equals", args = { Object.class })
	public void testEquals() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(8980821418377306870L);

		// never equal to another type
		for (int i = 0; i < 30; i++) {
			LabelSet labelSet = getRandomLabelSet(randomGenerator.nextLong());
			assertFalse(labelSet.equals(new Object()));
		}

		// never equal to null
		for (int i = 0; i < 30; i++) {
			LabelSet labelSet = getRandomLabelSet(randomGenerator.nextLong());
			assertFalse(labelSet.equals(null));
		}

		// reflexive
		for (int i = 0; i < 30; i++) {
			LabelSet labelSet = getRandomLabelSet(randomGenerator.nextLong());
			assertTrue(labelSet.equals(labelSet));
		}

		// symmetric, transitive, consistent
		for (int i = 0; i < 30; i++) {
			long seed = randomGenerator.nextLong();
			LabelSet labelSet1 = getRandomLabelSet(seed);
			LabelSet labelSet2 = getRandomLabelSet(seed);
			assertFalse(labelSet1 == labelSet2);
			for (int j = 0; j < 10; j++) {
				assertTrue(labelSet1.equals(labelSet2));
				assertTrue(labelSet2.equals(labelSet1));
			}
		}

		// different inputs yield unequal labelSets
		Set<LabelSet> set = new LinkedHashSet<>();
		for (int i = 0; i < 100; i++) {
			LabelSet labelSet = getRandomLabelSet(randomGenerator.nextLong());
			set.add(labelSet);
		}
		assertEquals(100, set.size());
	}

	/**
	 * Tests {@linkplain LabelSet#hashCode()
	 */
	@Test
	@UnitTestMethod(target = LabelSet.class, name = "hashCode", args = {})
	public void testHashCode() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(2653091508465183354L);

		// equal objects have equal hash codes
		for (int i = 0; i < 30; i++) {
			long seed = randomGenerator.nextLong();
			LabelSet labelSet1 = getRandomLabelSet(seed);
			LabelSet labelSet2 = getRandomLabelSet(seed);

			assertEquals(labelSet1, labelSet2);
			assertEquals(labelSet1.hashCode(), labelSet2.hashCode());
		}

		// hash codes are reasonably distributed
		Set<Integer> hashCodes = new LinkedHashSet<>();
		for (int i = 0; i < 100; i++) {
			LabelSet labelSet = getRandomLabelSet(randomGenerator.nextLong());
			hashCodes.add(labelSet.hashCode());
		}

		assertEquals(100, hashCodes.size());
	}

	@Test
	@UnitTestMethod(target = LabelSet.class, name = "toString", args = {})
	public void testToString() {
		LabelSet labelSet = LabelSet.builder().setLabel(Dimension.DIM_1, "compartment label").build();

		String expectedString = "LabelSet [labels={DIM_1=compartment label}]";
		assertNotNull(labelSet);
		assertEquals(expectedString, labelSet.toString());
	}

	@Test
	@UnitTestMethod(target = LabelSet.Builder.class, name = "build", args = {})
	public void testBuild() {
		LabelSet labelSet = LabelSet.builder().setLabel(Dimension.DIM_1, "compartment label").build();
		assertNotNull(labelSet);
	}

	@Test
	@UnitTestMethod(target = LabelSet.Builder.class, name = "setLabel", args = { Object.class, Object.class })
	public void testSetLabel() {
		String expectedLabel1 = "expected label 1";
		String expectedLabel2 = "expected label 2";

		LabelSet labelSet = LabelSet.builder().setLabel(Dimension.DIM_1, expectedLabel1).setLabel(Dimension.DIM_2, expectedLabel2).build();
		assertEquals(expectedLabel1, labelSet.getLabel(Dimension.DIM_1).get());
		assertEquals(expectedLabel2, labelSet.getLabel(Dimension.DIM_2).get());

		// precondition test: if the label is null
		ContractException contractException = assertThrows(ContractException.class, () -> LabelSet.builder().setLabel(null, expectedLabel1));
		assertEquals(PartitionError.NULL_PARTITION_LABEL_DIMENSION, contractException.getErrorType());

		// precondition test: if the dimension is null
		contractException = assertThrows(ContractException.class, () -> LabelSet.builder().setLabel(Dimension.DIM_1, null));
		assertEquals(PartitionError.NULL_PARTITION_LABEL, contractException.getErrorType());

	}

	private LabelSet getRandomLabelSet(long seed) {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(seed);
		LabelSet.Builder builder = LabelSet.builder();
		
		List<Dimension> dimensions = Arrays.asList(Dimension.values());
		Random random = new Random(randomGenerator.nextLong());
		Collections.shuffle(dimensions, random);

		int n = randomGenerator.nextInt(dimensions.size()) + 1;
		for (int i = 0; i < n; i++) {
			Dimension randomDimension = dimensions.get(i);
			String randomLabel = "Label" + randomGenerator.nextInt();
			builder.setLabel(randomDimension, randomLabel);
		}

		return builder.build();
	}
}
