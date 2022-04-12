package plugins.partitions.support.containers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import nucleus.SimulationContext;
import nucleus.util.ContractException;
import plugins.partitions.support.PartitionError;
import plugins.people.PersonDataManager;
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
	@UnitTestConstructor(args = {PersonDataManager.class})
	public void testConstructor() {
		ContractException contractException = assertThrows(ContractException.class,()->new TreeBitSetPeopleContainer(null));
		assertEquals(PartitionError.NULL_PERSON_DATA_VIEW, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(name = "getPeople", args= {})
	public void testGetPeople(){
		PeopleContainerTester.testGetPeople(this::getPeopleContainer,473484353360778267L);
	}
	
	@Test
	@UnitTestMethod(name = "safeAdd", args= {PersonId.class})
	public void testSafeAdd(){
		PeopleContainerTester.testSafeAdd(this::getPeopleContainer,1067260810269284703L);
	}


	@Test
	@UnitTestMethod(name = "unsafeAdd", args= {PersonId.class})
	public void testUnsafeAdd(){
		PeopleContainerTester.testUnsafeAdd(this::getPeopleContainer,2640497434656632684L);
	}

	@Test
	@UnitTestMethod(name = "remove", args= {PersonId.class})
	public void testRemove(){
		PeopleContainerTester.testRemove(this::getPeopleContainer,1461315035567239819L);
	}

	@Test
	@UnitTestMethod(name = "size", args= {})
	public void testSize(){
		PeopleContainerTester.testSize(this::getPeopleContainer,5880341220076297803L);
	}

	@Test
	@UnitTestMethod(name = "", args= {PersonId.class})
	public void testContains(){
		PeopleContainerTester.testContains(this::getPeopleContainer,6865277196728541573L);
	}

	@Test
	@UnitTestMethod(name = "getRandomPersonId", args= {RandomGenerator.class})
	public void testGetRandomPersonId(){
		PeopleContainerTester.testGetRandomPersonId(this::getPeopleContainer,1976658500916036734L);
	}

}