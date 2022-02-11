package plugins.resources.events.mutation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import nucleus.Event;
import plugins.people.support.PersonId;
import plugins.resources.support.ResourceId;
import plugins.resources.testsupport.TestResourceId;
import util.annotations.UnitTest;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;


@UnitTest(target = ResourceTransferFromPersonEvent.class)
public final class AT_ResourceTransferFromPersonEvent implements Event {

	

	
	@Test
	@UnitTestConstructor(args = { ResourceId.class, PersonId.class, long.class })
	public void testConstructor() {
		//nothing to test
	}
	
	@Test
	@UnitTestMethod(name = "getResourceId", args = {})
	public void testGetResourceId() {
		ResourceId resourceId = TestResourceId.RESOURCE_4;
		PersonId personId = new PersonId(55634);
		long amount = 126234L;
		ResourceTransferFromPersonEvent resourceTransferFromPersonEvent = new ResourceTransferFromPersonEvent(resourceId, personId, amount);
		assertEquals(resourceId,resourceTransferFromPersonEvent.getResourceId());
	}

	
	@Test
	@UnitTestMethod(name = "getPersonId", args = {})
	public void testGetPersonId() {
		ResourceId resourceId = TestResourceId.RESOURCE_4;
		PersonId personId = new PersonId(55634);
		long amount = 126234L;
		ResourceTransferFromPersonEvent resourceTransferFromPersonEvent = new ResourceTransferFromPersonEvent(resourceId, personId, amount);
		assertEquals(personId,resourceTransferFromPersonEvent.getPersonId());
	}

	
	@Test
	@UnitTestMethod(name = "getAmount", args = {})
	public void testGetAmount() {
		ResourceId resourceId = TestResourceId.RESOURCE_4;
		PersonId personId = new PersonId(55634);
		long amount = 126234L;
		ResourceTransferFromPersonEvent resourceTransferFromPersonEvent = new ResourceTransferFromPersonEvent(resourceId, personId, amount);
		assertEquals(amount,resourceTransferFromPersonEvent.getAmount());
	}

	@Test
	@UnitTestMethod(name = "getPrimaryKeyValue", args = {})
	public void testGetPrimaryKeyValue() {
		ResourceId resourceId = TestResourceId.RESOURCE_4;
		PersonId personId = new PersonId(55634);
		long amount = 126234L;
		ResourceTransferFromPersonEvent resourceTransferFromPersonEvent = new ResourceTransferFromPersonEvent(resourceId, personId, amount);
		assertEquals(ResourceTransferFromPersonEvent.class,resourceTransferFromPersonEvent.getPrimaryKeyValue());
	}
}
