package plugins.util.properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import annotations.UnitTest;
import annotations.UnitTestConstructor;
import annotations.UnitTestMethod;
import nucleus.Plugin;
import nucleus.SimulationContext;
import nucleus.testsupport.testplugin.TestActionSupport;
import nucleus.testsupport.testplugin.TestActorPlan;
import nucleus.testsupport.testplugin.TestPlugin;
import nucleus.testsupport.testplugin.TestPluginData;

@UnitTest(target = PropertyValueRecord.class)
public class AT_PropertyValueRecord {

	/**
	 * test for {@link PropertyValueRecord#getValue()}
	 */
	@Test
	@UnitTestMethod(name = "getValue", args = {})
	public void testGetValue() {
		
		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();

		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(345.6, (c) -> {
			PropertyValueRecord propertyValueRecord = new PropertyValueRecord(c);
			propertyValueRecord.setPropertyValue("cat");
			assertEquals("cat", propertyValueRecord.getValue());
		}));

		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(456.2, (c) -> {
			PropertyValueRecord propertyValueRecord = new PropertyValueRecord(c);
			propertyValueRecord.setPropertyValue("dog");
			assertEquals("dog", propertyValueRecord.getValue());

		}));

		TestPluginData testPluginData = pluginDataBuilder.build();
		Plugin plugin = TestPlugin.getPlugin(testPluginData);
		TestActionSupport.testConsumers(plugin);
	}

	/**
	 * test for {@link PropertyValueRecord#setPropertyValue(Object)}
	 */
	@Test
	@UnitTestMethod(name = "setPropertyValue", args = { Object.class })
	public void testSetPropertyValue() {

		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();

		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(345.6, (c) -> {
			PropertyValueRecord propertyValueRecord = new PropertyValueRecord(c);
			propertyValueRecord.setPropertyValue("cat");
			assertEquals("cat", propertyValueRecord.getValue());
		}));

		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(456.2, (c) -> {
			PropertyValueRecord propertyValueRecord = new PropertyValueRecord(c);
			propertyValueRecord.setPropertyValue("dog");
			assertEquals("dog", propertyValueRecord.getValue());

		}));

		TestPluginData testPluginData = pluginDataBuilder.build();
		Plugin plugin = TestPlugin.getPlugin(testPluginData);
		TestActionSupport.testConsumers(plugin);
	}

	@Test
	@UnitTestMethod(name = "getAssignmentTime", args = {})
	public void testGetAssignmentTime() {
		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();

		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(345.6, (c) -> {
			PropertyValueRecord propertyValueRecord = new PropertyValueRecord(c);
			propertyValueRecord.setPropertyValue("cat");
			assertEquals(c.getTime(), propertyValueRecord.getAssignmentTime(), 0);
		}));

		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(456.2, (c) -> {
			PropertyValueRecord propertyValueRecord = new PropertyValueRecord(c);
			propertyValueRecord.setPropertyValue("dog");
			assertEquals(c.getTime(), propertyValueRecord.getAssignmentTime(), 0);

		}));

		TestPluginData testPluginData = pluginDataBuilder.build();
		Plugin plugin = TestPlugin.getPlugin(testPluginData);
		TestActionSupport.testConsumers(plugin);

	}

	@Test
	@UnitTestConstructor(args = { SimulationContext.class })
	public void testConstructor() {
		TestActionSupport.testConsumer((c) -> {
			PropertyValueRecord propertyValueRecord = new PropertyValueRecord(c);
			assertNotNull(propertyValueRecord);
			assertEquals(0, propertyValueRecord.getAssignmentTime());
		});

	}

}