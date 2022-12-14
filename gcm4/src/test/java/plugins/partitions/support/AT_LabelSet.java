package plugins.partitions.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;

import tools.annotations.UnitTest;
import tools.annotations.UnitTestMethod;

/**
 * Test class for {@link LabelSetInfo}
 * 
 * @author Shawn Hatch
 *
 */
@UnitTest(target = LabelSet.class)

public class AT_LabelSet {

	private static enum Dimension {
		DIM_1, DIM_2, DIM_3, DIM_4, DIM_5;
	}

	/**
	 * Tests {@linkplain LabelSet#builder()
	 */
	@Test
	@UnitTestMethod(name = "builder", args = {})
	public void testBuilder() {
		assertNotNull(LabelSet.builder());
	}

	/**
	 * Tests {@linkplain LabelSet#isEmpty()
	 */
	@Test
	@UnitTestMethod(name = "isEmpty", args = {})
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
	@UnitTestMethod(name = "getLabel", args = {Object.class})
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
	@UnitTestMethod(name = "getDimensions", args = {})
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
	@UnitTestMethod(name = "equals", args = { Object.class })
	public void testEquals() {
		LabelSet labelSet1 = LabelSet.builder().setLabel(Dimension.DIM_1, "compartment label").build();
		LabelSet labelSet2 = LabelSet.builder().setLabel(Dimension.DIM_1, "compartment label").build();
		LabelSet labelSet3 = LabelSet.builder().setLabel(Dimension.DIM_1, "compartment label2").build();

		assertFalse(labelSet1 == labelSet2);
		assertTrue(labelSet1.equals(labelSet1));
		assertTrue(labelSet1.equals(labelSet2));
		assertTrue(labelSet2.equals(labelSet1));
		assertFalse(labelSet1.equals(labelSet3));

	}

	/**
	 * Tests {@linkplain LabelSet#hashCode()
	 */
	@Test
	@UnitTestMethod(name = "hashCode", args = {})
	public void testHashCode() {

		LabelSet labelSet1 = LabelSet.builder().setLabel(Dimension.DIM_1, "compartment label").build();
		LabelSet labelSet2 = LabelSet.builder().setLabel(Dimension.DIM_1, "compartment label").build();

		assertFalse(labelSet1 == labelSet2);
		assertEquals(labelSet1, labelSet2);
		assertEquals(labelSet1.hashCode(), labelSet2.hashCode());
	}

	public void testToString() {

	}

	public void testBuild() {

	}

	public void testSetLabel() {
		
	}
}
