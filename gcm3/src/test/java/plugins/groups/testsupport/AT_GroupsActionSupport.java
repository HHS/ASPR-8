package plugins.groups.testsupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.function.Consumer;

import org.junit.jupiter.api.Test;

import nucleus.testsupport.actionplugin.ActionError;
import nucleus.testsupport.actionplugin.ActionPluginInitializer;
import plugins.groups.datacontainers.PersonGroupDataView;
import plugins.groups.support.GroupId;
import plugins.people.datacontainers.PersonDataView;
import util.ContractException;
import util.MutableBoolean;
import util.annotations.UnitTestMethod;

//@UnitTest(target = GroupsActionSupport.class)
public class AT_GroupsActionSupport {

	@Test
	@UnitTestMethod(name = "testConsumer", args = { int.class, double.class, double.class, long.class, Consumer.class })
	public void testConsumer() {
		MutableBoolean executed = new MutableBoolean();
		GroupsActionSupport.testConsumer(100, 3, 5, 234L, (c)->{
			
			//show that there are 100 people
			PersonDataView personDataView = c.getDataView(PersonDataView.class).get();
			assertEquals(100,personDataView.getPopulationCount());
			
			//show that there are 60 groups
			PersonGroupDataView personGroupDataView = c.getDataView(PersonGroupDataView.class).get();
			assertEquals(60,personGroupDataView.getGroupIds().size());
			
			//show that there are 300 group memberships
			int membershipCount = 0;
			for(GroupId groupId : personGroupDataView.getGroupIds()) {
				membershipCount +=
				personGroupDataView.getPersonCountForGroup(groupId);
				
			}
			assertEquals(300,membershipCount);
			
			//show that the group properties exist
			for(TestGroupPropertyId testGroupPropertyId : TestGroupPropertyId.values()) {
				assertTrue(personGroupDataView.getGroupPropertyExists(testGroupPropertyId.getTestGroupTypeId(), testGroupPropertyId));
			}			
			
			executed.setValue(true);
		});
		assertTrue(executed.getValue());
	}

	@Test
	@UnitTestMethod(name = "testConsumers", args = { int.class, double.class, double.class, long.class, Consumer.class })
	public void testConsumers() {
		ContractException contractException = assertThrows(ContractException.class,()->
		GroupsActionSupport.testConsumers(100, 3, 5, 234L,  ActionPluginInitializer.builder().build()));
		assertEquals(ActionError.ACTION_EXECUTION_FAILURE, contractException.getErrorType());
	}

}
