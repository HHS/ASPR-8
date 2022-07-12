package plugins.util.p2;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import util.random.RandomGeneratorProvider;
import util.time.TimeElapser;

public class TestProperty {

	private static final Property<Integer> PROPERTY_A = new Property<>(0);
	private static final Property<Boolean> PROPERTY_B = new Property<>(false);

	@Test
	public void test() {
		PropertyContainer propertyContainer = new PropertyContainer();

		propertyContainer.set(PROPERTY_A, 1, 1);
		propertyContainer.set(PROPERTY_B, 1, true);

		assertEquals(0, propertyContainer.get(PROPERTY_A, 0));
		assertEquals(false, propertyContainer.get(PROPERTY_B, 0));
		assertEquals(1, propertyContainer.get(PROPERTY_A, 1));
		assertEquals(true, propertyContainer.get(PROPERTY_B, 1));
	}

	@SuppressWarnings("unused")
	private static Object getRandomPropertyValue(final RandomGenerator randomGenerator) {

		int index = randomGenerator.nextInt(5);
		switch (index) {
		case 0:
			return randomGenerator.nextBoolean();
		case 1:
			return randomGenerator.nextDouble();
		case 2:
			return randomGenerator.nextFloat();
		case 3:
			return randomGenerator.nextInt();
		case 4:
			return randomGenerator.nextLong();
		default:
			throw new RuntimeException("unknown index " + index);
		}
	}

	@Test
	public void testCast() {
//		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(2930571381466931440L);
//
//		PropertyContainer propertyContainer = new PropertyContainer();
//
//		List<Property<?>> properties = new ArrayList<>();
//		for (int i = 0; i < 1_000; i++) {
//			properties.add(new Property<>(getRandomPropertyValue(randomGenerator)));
//
//		}

		// for(Property<?> property : properties) {
		// for(int i = 0;i<1000;i++) {
		// propertyContainer.set(property, i, property.defaultValue());
		// }
		// }

	}

	@Test
	public void testKeySpeed() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(2930571381466931440L);

		PropertyContainer propertyContainer = new PropertyContainer();

		List<Property<Integer>> properties = new ArrayList<>();
		for (int i = 0; i < 1_000; i++) {
			properties.add(new Property<>(randomGenerator.nextInt()));
		}

		for (Property<Integer> property : properties) {
			for (int i = 0; i < 1000; i++) {
				propertyContainer.set(property, i, randomGenerator.nextInt());
			}
		}

		@SuppressWarnings("unused")
		TimeElapser timeElapser = new TimeElapser();
		for (Property<Integer> property : properties) {
			for (int i = 0; i < 1000; i++) {
				propertyContainer.get(property, i);
			}
		}
		
//		System.out.println(timeElapser.getElapsedMilliSeconds());

	}
	
	@Test
	public void testKeySpeed2() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(2930571381466931440L);

		PropertyContainer propertyContainer = new PropertyContainer();

		List<Property<Integer>> properties = new ArrayList<>();
		for (int i = 0; i < 1_000; i++) {
			properties.add(new Property<>(randomGenerator.nextInt()));
		}

		for (Property<Integer> property : properties) {
			for (int i = 0; i < 1000; i++) {
				propertyContainer.set(property, i, randomGenerator.nextInt());
			}
		}

		@SuppressWarnings("unused")
		TimeElapser timeElapser = new TimeElapser();
		for (Property<Integer> property : properties) {
			for (int i = 0; i < 1000; i++) {
				propertyContainer.get(property, i);
			}
		}
		
//		System.out.println(timeElapser.getElapsedMilliSeconds());

	}

}
