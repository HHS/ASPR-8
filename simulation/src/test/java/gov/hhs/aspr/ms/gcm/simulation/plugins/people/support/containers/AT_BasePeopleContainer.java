package gov.hhs.aspr.ms.gcm.simulation.plugins.people.support.containers;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.gcm.simulation.plugins.people.datamanagers.PeopleDataManager;
import gov.hhs.aspr.ms.gcm.simulation.plugins.people.support.PersonId;
import gov.hhs.aspr.ms.util.annotations.UnitTestConstructor;
import gov.hhs.aspr.ms.util.annotations.UnitTestMethod;

public class AT_BasePeopleContainer {

	private PeopleContainer getPeopleContainer(PeopleDataManager peopleDataManager) {
		return new BasePeopleContainer(peopleDataManager,false);
	}

	@Test
	@UnitTestConstructor(target = BasePeopleContainer.class, args = { PeopleDataManager.class, boolean.class})
	public void testConstructor() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = BasePeopleContainer.class, name = "getPeople", args = {})
	public void testGetPeople() {
		PeopleContainerTester.testGetPeople(this::getPeopleContainer, 473484353360778267L);
	}

	@Test
	@UnitTestMethod(target = BasePeopleContainer.class, name = "safeAdd", args = { PersonId.class })
	public void testSafeAdd() {
		PeopleContainerTester.testSafeAdd(this::getPeopleContainer, 1067260810269284703L);
	}

	@Test
	@UnitTestMethod(target = BasePeopleContainer.class, name = "unsafeAdd", args = { PersonId.class })
	public void testUnsafeAdd() {
		PeopleContainerTester.testUnsafeAdd(this::getPeopleContainer, 2640497434656632684L);
	}

	@Test
	@UnitTestMethod(target = BasePeopleContainer.class, name = "remove", args = { PersonId.class })
	public void testRemove() {
		PeopleContainerTester.testRemove(this::getPeopleContainer, 1461315035567239819L);
	}

	@Test
	@UnitTestMethod(target = BasePeopleContainer.class, name = "size", args = {})
	public void testSize() {
		PeopleContainerTester.testSize(this::getPeopleContainer, 5880341220076297803L);
	}

	@Test
	@UnitTestMethod(target = BasePeopleContainer.class, name = "contains", args = { PersonId.class })
	public void testContains() {
		PeopleContainerTester.testContains(this::getPeopleContainer, 6865277196728541573L);
	}

	@Test
	@UnitTestMethod(target = BasePeopleContainer.class, name = "getRandomPersonId", args = { RandomGenerator.class })
	public void testGetRandomPersonId() {
		PeopleContainerTester.testGetRandomPersonId(this::getPeopleContainer, 1976658500916036734L);
	}

}