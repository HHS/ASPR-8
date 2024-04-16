package gov.hhs.aspr.ms.gcm.simulation.plugins.partitions.support.containers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.gcm.simulation.plugins.partitions.support.PartitionsContext;
import gov.hhs.aspr.ms.gcm.simulation.plugins.people.support.PersonId;
import gov.hhs.aspr.ms.util.annotations.UnitTestConstructor;
import gov.hhs.aspr.ms.util.annotations.UnitTestField;
import gov.hhs.aspr.ms.util.annotations.UnitTestMethod;

public class AT_IntSetPeopleContainer {

	@Test
	@UnitTestField(target = IntSetPeopleContainer.class,name = "DEFAULT_TARGET_DEPTH")
	public void testDefaultTargetDepth() {
		assertEquals(80, IntSetPeopleContainer.DEFAULT_TARGET_DEPTH);
	}

	private PeopleContainer getPeopleContainer(PartitionsContext context) {
		return new IntSetPeopleContainer();
	}

	@Test
	@UnitTestConstructor(target = IntSetPeopleContainer.class, args = {})
	public void testConstructor() {
		assertNotNull(new IntSetPeopleContainer());
	}

	@Test
	@UnitTestMethod(target = IntSetPeopleContainer.class, name = "getPeople", args = {})
	public void testGetPeople() {
		PeopleContainerTester.testGetPeople(this::getPeopleContainer, 2057487963804869808L);
	}

	@Test
	@UnitTestMethod(target = IntSetPeopleContainer.class, name = "safeAdd", args = { PersonId.class })
	public void testSafeAdd() {
		PeopleContainerTester.testSafeAdd(this::getPeopleContainer, 8929313356256516845L);
	}

	@Test
	@UnitTestMethod(target = IntSetPeopleContainer.class, name = "unsafeAdd", args = { PersonId.class })
	public void testUnsafeAdd() {
		PeopleContainerTester.testUnsafeAdd(this::getPeopleContainer, 7154495188896202332L);
	}

	@Test
	@UnitTestMethod(target = IntSetPeopleContainer.class, name = "remove", args = { PersonId.class })
	public void testRemove() {
		PeopleContainerTester.testRemove(this::getPeopleContainer, 3751615599692356108L);
	}

	@Test
	@UnitTestMethod(target = IntSetPeopleContainer.class, name = "size", args = {})
	public void testSize() {
		PeopleContainerTester.testSize(this::getPeopleContainer, 7891778848164062625L);
	}

	@Test
	@UnitTestMethod(target = IntSetPeopleContainer.class, name = "contains", args = { PersonId.class })
	public void testContains() {
		PeopleContainerTester.testContains(this::getPeopleContainer, 604075687669743780L);
	}

	@Test
	@UnitTestMethod(target = IntSetPeopleContainer.class, name = "getRandomPersonId", args = { RandomGenerator.class })
	public void testGetRandomPersonId() {
		PeopleContainerTester.testGetRandomPersonId(this::getPeopleContainer, 2124533986436224378L);
	}

}