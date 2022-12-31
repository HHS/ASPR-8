package plugins.groups.testsupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.function.Consumer;

import org.junit.jupiter.api.Test;

import nucleus.Plugin;
import nucleus.testsupport.testplugin.TestError;
import nucleus.testsupport.testplugin.TestPlugin;
import nucleus.testsupport.testplugin.TestPluginData;
import plugins.groups.datamanagers.GroupsDataManager;
import plugins.groups.support.GroupId;
import plugins.people.datamanagers.PeopleDataManager;
import tools.annotations.UnitTestMethod;
import util.errors.ContractException;
import util.wrappers.MutableBoolean;

public class AT_GroupsActionSupport {

	@Test
	@UnitTestMethod(target = GroupsActionSupport.class, name = "testConsumer", args = { int.class, double.class, double.class, long.class, Consumer.class })
	public void testTestConsumer() {
		MutableBoolean executed = new MutableBoolean();
		GroupsActionSupport.testConsumer(100, 3, 5, 3765548905828391577L, (c) -> {

			// show that there are 100 people
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			assertEquals(100, peopleDataManager.getPopulationCount());

			// show that there are 60 groups
			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
			assertEquals(60, groupsDataManager.getGroupIds().size());

			// show that there are 300 group memberships
			int membershipCount = 0;
			for (GroupId groupId : groupsDataManager.getGroupIds()) {
				membershipCount += groupsDataManager.getPersonCountForGroup(groupId);

			}
			assertEquals(300, membershipCount);

			// show that the group properties exist
			for (TestGroupPropertyId testGroupPropertyId : TestGroupPropertyId.values()) {
				assertTrue(groupsDataManager.getGroupPropertyExists(testGroupPropertyId.getTestGroupTypeId(), testGroupPropertyId));
			}

			executed.setValue(true);
		});
		assertTrue(executed.getValue());
	}

	@Test
	@UnitTestMethod(target = GroupsActionSupport.class, name = "testConsumers", args = { int.class, double.class, double.class, long.class, Plugin.class })
	public void testTestConsumers() {
		ContractException contractException = assertThrows(ContractException.class,
				() -> GroupsActionSupport.testConsumers(100, 3, 5, 5220283745188783777L, TestPlugin.getTestPlugin(TestPluginData.builder().build())));

		assertEquals(TestError.TEST_EXECUTION_FAILURE, contractException.getErrorType());
	}

}
