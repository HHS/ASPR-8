package plugins.partitions.support.containers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import nucleus.SimulationContext;
import plugins.partitions.support.PartitionError;
import plugins.people.datamanagers.PeopleDataManager;
import plugins.people.support.PersonId;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;
import util.errors.ContractException;


public class AT_TreeBitSetPeopleContainer {

	
	private PeopleContainer getPeopleContainer(SimulationContext context) {
		return new TreeBitSetPeopleContainer(context.getDataManager(PeopleDataManager.class));
	}
	
	@Test
	@UnitTestConstructor(target = TreeBitSetPeopleContainer.class,args = {PeopleDataManager.class})
	public void testConstructor() {
		ContractException contractException = assertThrows(ContractException.class,()->new TreeBitSetPeopleContainer(null));
		assertEquals(PartitionError.NULL_PERSON_DATA_VIEW, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = TreeBitSetPeopleContainer.class,name = "getPeople", args= {})
	public void testGetPeople(){
		PeopleContainerTester.testGetPeople(this::getPeopleContainer,647365023411331008L);
	}
	
	@Test
	@UnitTestMethod(target = TreeBitSetPeopleContainer.class,name = "safeAdd", args= {PersonId.class})
	public void testSafeAdd(){
		PeopleContainerTester.testSafeAdd(this::getPeopleContainer,5811617482064210099L);
	}


	@Test
	@UnitTestMethod(target = TreeBitSetPeopleContainer.class,name = "unsafeAdd", args= {PersonId.class})
	public void testUnsafeAdd(){
		PeopleContainerTester.testUnsafeAdd(this::getPeopleContainer,3895584565521026498L);
	}

	@Test
	@UnitTestMethod(target = TreeBitSetPeopleContainer.class,name = "remove", args= {PersonId.class})
	public void testRemove(){
		PeopleContainerTester.testRemove(this::getPeopleContainer,6552795405656249759L);
	}

	@Test
	@UnitTestMethod(target = TreeBitSetPeopleContainer.class,name = "size", args= {})
	public void testSize(){
		PeopleContainerTester.testSize(this::getPeopleContainer,4996900201269028809L);
	}

	@Test
	@UnitTestMethod(target = TreeBitSetPeopleContainer.class,name = "contains", args= {PersonId.class})
	public void testContains(){
		PeopleContainerTester.testContains(this::getPeopleContainer,3310387654745518349L);
	}

	@Test
	@UnitTestMethod(target = TreeBitSetPeopleContainer.class,name = "getRandomPersonId", args= {RandomGenerator.class})
	public void testGetRandomPersonId(){
		PeopleContainerTester.testGetRandomPersonId(this::getPeopleContainer,4940840474171134819L);
	}

}