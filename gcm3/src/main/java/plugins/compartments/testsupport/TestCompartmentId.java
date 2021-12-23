package plugins.compartments.testsupport;

import java.util.Arrays;

import org.apache.commons.math3.random.RandomGenerator;

import plugins.compartments.support.CompartmentId;
import plugins.compartments.support.CompartmentPropertyId;

/**
 * Enumeration that provides {@linkplain CompartmentId} values as an enum. Each
 * compartment id has a predefined set of associated
 * {@linkplain CompartmentPropertyId} values. No property definitions are
 * provided.
 */
public enum TestCompartmentId implements CompartmentId {
	COMPARTMENT_1("Compartment_Property_1_1", "Compartment_Property_1_2", "Compartment_Property_1_3"),
	COMPARTMENT_2("Compartment_Property_2_1", "Compartment_Property_2_2"),
	COMPARTMENT_3("Compartment_Property_3_1", "Compartment_Property_3_2", "Compartment_Property_3_3", "Compartment_Property_3_4"),
	COMPARTMENT_4("Compartment_Property_4_1"),
	COMPARTMENT_5("Compartment_Property_4_1", "Compartment_Property_4_2", "Compartment_Property_4_3");

	private static class TestCompartmentPropertyId implements CompartmentPropertyId {
		private final String id;

		private TestCompartmentPropertyId(String id) {
			this.id = id;
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("TestCompartmentPropertyId [id=");
			builder.append(id);
			builder.append("]");
			return builder.toString();
		}

	}

	private final CompartmentPropertyId[] compartmentPropertyIds;

	private TestCompartmentId(String... strings) {
		compartmentPropertyIds = new CompartmentPropertyId[strings.length];
		for (int i = 0; i < strings.length; i++) {
			CompartmentPropertyId compartmentPropertyId = new TestCompartmentPropertyId(strings[i]);
			compartmentPropertyIds[i] = compartmentPropertyId;
		}
	}

	public CompartmentPropertyId[] getCompartmentPropertyIds() {
		return Arrays.copyOf(compartmentPropertyIds, compartmentPropertyIds.length);
	}

	public int getCompartmentPropertyCount() {
		return compartmentPropertyIds.length;
	}

	public CompartmentPropertyId getCompartmentPropertyId(int index) {
		return compartmentPropertyIds[index];
	}

	/**
	 * Returns a randomly chosen member of this enum
	 */
	public static TestCompartmentId getRandomCompartmentId(final RandomGenerator randomGenerator) {
		return TestCompartmentId.values()[randomGenerator.nextInt(TestCompartmentId.values().length)];
	}

	/**
	 * Returns the number of TestCompartmentId members in this enum
	 */
	public static int size() {
		return values().length;
	}

	private TestCompartmentId next;

	/**
	 * Returns the next member of this enum in the declared order with
	 * wrap-around
	 */
	public TestCompartmentId next() {
		if (next == null) {
			next = TestCompartmentId.values()[(ordinal() + 1) % TestCompartmentId.values().length];
		}
		return next;
	}

	/**
	 * Returns a new {@link CompartmentId} instance that is not a member of this
	 * enum.
	 */
	public static CompartmentId getUnknownCompartmentId() {
		return new CompartmentId() {
		};
	}

	/**
	 * Returns a new {@link CompartmentPropertyId} instance that is not a
	 * property of any member of this enum.
	 */
	public static CompartmentPropertyId getUnknownCompartmentPropertyId() {
		return new CompartmentPropertyId() {
		};
	}
}
