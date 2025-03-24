package gov.hhs.aspr.ms.gcm.simulation.plugins.people.support.containers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.gcm.simulation.plugins.people.datamanagers.PeopleDataManager;
import gov.hhs.aspr.ms.gcm.simulation.plugins.people.support.PersonError;
import gov.hhs.aspr.ms.gcm.simulation.plugins.people.support.PersonId;
import gov.hhs.aspr.ms.util.annotations.UnitTestConstructor;
import gov.hhs.aspr.ms.util.annotations.UnitTestField;
import gov.hhs.aspr.ms.util.annotations.UnitTestMethod;
import gov.hhs.aspr.ms.util.errors.ContractException;

public class AT_IntSetPeopleContainer2 {

	@Test
	@UnitTestField(target = IntSetPeopleContainer2.class,name = "DEFAULT_TARGET_DEPTH")
	public void testDefaultTargetDepth() {
		assertEquals(80, IntSetPeopleContainer2.DEFAULT_TARGET_DEPTH);
	}

	private PeopleContainer getPeopleContainer(PeopleDataManager peopleDataManager) {
		return new IntSetPeopleContainer2(peopleDataManager);
	}

	@Test
	@UnitTestConstructor(target = IntSetPeopleContainer2.class, args = {})
	public void testConstructor() {
		ContractException contractException = assertThrows(ContractException.class,()->new IntSetPeopleContainer2(null));
		assertEquals(PersonError.NULL_PEOPLE_DATA_MANAGER, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = IntSetPeopleContainer2.class, name = "getPeople", args = {})
	public void testGetPeople() {
		PeopleContainerTester.testGetPeople(this::getPeopleContainer, 2057487963804869808L);
	}

	@Test
	@UnitTestMethod(target = IntSetPeopleContainer2.class, name = "safeAdd", args = { PersonId.class })
	public void testSafeAdd() {
		PeopleContainerTester.testSafeAdd(this::getPeopleContainer, 8929313356256516845L);
	}

	@Test
	@UnitTestMethod(target = IntSetPeopleContainer2.class, name = "unsafeAdd", args = { PersonId.class })
	public void testUnsafeAdd() {
		PeopleContainerTester.testUnsafeAdd(this::getPeopleContainer, 7154495188896202332L);
	}

	@Test
	@UnitTestMethod(target = IntSetPeopleContainer2.class, name = "remove", args = { PersonId.class })
	public void testRemove() {
		PeopleContainerTester.testRemove(this::getPeopleContainer, 3751615599692356108L);
	}

	@Test
	@UnitTestMethod(target = IntSetPeopleContainer2.class, name = "size", args = {})
	public void testSize() {
		PeopleContainerTester.testSize(this::getPeopleContainer, 7891778848164062625L);
	}

	@Test
	@UnitTestMethod(target = IntSetPeopleContainer2.class, name = "contains", args = { PersonId.class })
	public void testContains() {
		PeopleContainerTester.testContains(this::getPeopleContainer, 604075687669743780L);
	}

	@Test
	@UnitTestMethod(target = IntSetPeopleContainer2.class, name = "getRandomPersonId", args = { RandomGenerator.class })
	public void testGetRandomPersonId() {
		PeopleContainerTester.testGetRandomPersonId(this::getPeopleContainer, 2124533986436224378L);
	}

}