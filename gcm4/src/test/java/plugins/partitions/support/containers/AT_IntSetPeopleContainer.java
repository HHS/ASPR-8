package plugins.partitions.support.containers;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import nucleus.SimulationContext;
import plugins.people.support.PersonId;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestConstructor;
import tools.annotations.UnitTestMethod;


@UnitTest(target = IntSetPeopleContainer.class)
public class AT_IntSetPeopleContainer {

	
	private PeopleContainer getPeopleContainer(SimulationContext context) {
		return new IntSetPeopleContainer();
	}
	
	@Test
	@UnitTestConstructor(args = {})
	public void testConstructor() {		
		assertNotNull(new IntSetPeopleContainer());
	}

	@Test
	@UnitTestMethod(name = "getPeople", args= {})
	public void testGetPeople(){
		PeopleContainerTester.testGetPeople(this::getPeopleContainer,2057487963804869808L);
	}
	
	@Test
	@UnitTestMethod(name = "safeAdd", args= {PersonId.class})
	public void testSafeAdd(){
		PeopleContainerTester.testSafeAdd(this::getPeopleContainer,8929313356256516845L);
	}


	@Test
	@UnitTestMethod(name = "unsafeAdd", args= {PersonId.class})
	public void testUnsafeAdd(){
		PeopleContainerTester.testUnsafeAdd(this::getPeopleContainer,7154495188896202332L);
	}

	@Test
	@UnitTestMethod(name = "remove", args= {PersonId.class})
	public void testRemove(){
		PeopleContainerTester.testRemove(this::getPeopleContainer,3751615599692356108L);
	}

	@Test
	@UnitTestMethod(name = "size", args= {})
	public void testSize(){
		PeopleContainerTester.testSize(this::getPeopleContainer,7891778848164062625L);
	}

	@Test
	@UnitTestMethod(name = "contains", args= {PersonId.class})
	public void testContains(){
		PeopleContainerTester.testContains(this::getPeopleContainer,604075687669743780L);
	}

	@Test
	@UnitTestMethod(name = "getRandomPersonId", args= {RandomGenerator.class})
	public void testGetRandomPersonId(){
		PeopleContainerTester.testGetRandomPersonId(this::getPeopleContainer,2124533986436224378L);
	}

}