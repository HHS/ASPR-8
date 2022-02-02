package plugins.compartments.testsupport;

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
	COMPARTMENT_1,
	COMPARTMENT_2,
	COMPARTMENT_3,
	COMPARTMENT_4,
	COMPARTMENT_5;
	
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

	
}
