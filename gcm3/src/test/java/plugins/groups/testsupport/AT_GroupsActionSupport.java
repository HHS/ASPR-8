package plugins.groups.testsupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.function.Consumer;

import org.junit.jupiter.api.Test;

import nucleus.testsupport.testplugin.TestError;
import nucleus.testsupport.testplugin.TestPlugin;
import nucleus.testsupport.testplugin.TestPluginData;
import nucleus.util.ContractException;
import plugins.groups.GroupDataManager;
import plugins.groups.support.GroupId;
import plugins.people.PersonDataManager;
import tools.annotations.UnitTestMethod;
import util.wrappers.MutableBoolean;

//@UnitTest(target = GroupsActionSupport.class)
public class AT_GroupsActionSupport {

	@Test
	@UnitTestMethod(name = "testConsumer", args = { int.class, double.class, double.class, long.class, Consumer.class })
	public void testConsumer() {
		MutableBoolean executed = new MutableBoolean();
		GroupsActionSupport.testConsumer(100, 3, 5, 234L, (c) -> {

			// show that there are 100 people
			PersonDataManager personDataManager = c.getDataManager(PersonDataManager.class).get();
			assertEquals(100, personDataManager.getPopulationCount());

			// show that there are 60 groups
			GroupDataManager groupDataManager = c.getDataManager(GroupDataManager.class).get();
			assertEquals(60, groupDataManager.getGroupIds().size());

			// show that there are 300 group memberships
			int membershipCount = 0;
			for (GroupId groupId : groupDataManager.getGroupIds()) {
				membershipCount += groupDataManager.getPersonCountForGroup(groupId);

			}
			assertEquals(300, membershipCount);

			// show that the group properties exist
			for (TestGroupPropertyId testGroupPropertyId : TestGroupPropertyId.values()) {
				assertTrue(groupDataManager.getGroupPropertyExists(testGroupPropertyId.getTestGroupTypeId(), testGroupPropertyId));
			}

			executed.setValue(true);
		});
		assertTrue(executed.getValue());
	}

	@Test
	@UnitTestMethod(name = "testConsumers", args = { int.class, double.class, double.class, long.class, Consumer.class })
	public void testConsumers() {
		ContractException contractException = assertThrows(ContractException.class,
				() -> GroupsActionSupport.testConsumers(100, 3, 5, 234L, TestPlugin.getTestPlugin(TestPluginData.builder().build())));
		
		assertEquals(TestError.TEST_EXECUTION_FAILURE, contractException.getErrorType());
	}

}
