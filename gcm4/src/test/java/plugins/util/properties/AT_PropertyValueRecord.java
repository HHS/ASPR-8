package plugins.util.properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import nucleus.SimulationContext;
import nucleus.testsupport.testplugin.TestActorPlan;
import nucleus.testsupport.testplugin.TestPluginData;
import nucleus.testsupport.testplugin.TestPluginFactory;
import nucleus.testsupport.testplugin.TestSimulation;
import tools.annotations.UnitTestConstructor;
import tools.annotations.UnitTestMethod;

public class AT_PropertyValueRecord {

	/**
	 * test for {@link PropertyValueRecord#getValue()}
	 */
	@Test
	@UnitTestMethod(target = PropertyValueRecord.class, name = "getValue", args = {})
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
		TestSimulation.executeSimulation(TestPluginFactory.factory(testPluginData).getPlugins());
	}

	/**
	 * test for {@link PropertyValueRecord#setPropertyValue(Object)}
	 */
	@Test
	@UnitTestMethod(target = PropertyValueRecord.class, name = "setPropertyValue", args = { Object.class })
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
		TestSimulation.executeSimulation(TestPluginFactory.factory(testPluginData).getPlugins());
	}

	@Test
	@UnitTestMethod(target = PropertyValueRecord.class, name = "getAssignmentTime", args = {})
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
		TestSimulation.executeSimulation(TestPluginFactory.factory(testPluginData).getPlugins());

	}

	@Test
	@UnitTestConstructor(target = PropertyValueRecord.class, args = { SimulationContext.class })
	public void testConstructor() {
		TestSimulation.executeSimulation(TestPluginFactory.factory((c) -> {
			PropertyValueRecord propertyValueRecord = new PropertyValueRecord(c);
			assertNotNull(propertyValueRecord);
			assertEquals(0, propertyValueRecord.getAssignmentTime());
		}).getPlugins());

	}

}