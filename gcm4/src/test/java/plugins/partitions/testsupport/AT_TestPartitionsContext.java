package plugins.partitions.testsupport;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;

import nucleus.ActorContext;
import nucleus.testsupport.testplugin.TestActorPlan;
import nucleus.testsupport.testplugin.TestPluginData;
import nucleus.testsupport.testplugin.TestSimulation;
import plugins.partitions.datamanagers.PartitionsDataManager;
import plugins.partitions.testsupport.PartitionsTestPluginFactory.Factory;
import plugins.partitions.testsupport.attributes.AttributesDataManager;
import plugins.people.datamanagers.PeopleDataManager;
import plugins.stochastics.datamanagers.StochasticsDataManager;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

public class AT_TestPartitionsContext {

	@Test
	@UnitTestConstructor(target = TestPartitionsContext.class, args = { ActorContext.class })
	public void testTestPartitionsContext() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = TestPartitionsContext.class, name = "getDataManager", args = { Class.class })
	public void testGetDataManager() {

		Factory factory = PartitionsTestPluginFactory.factory(0, 9139618710983560173L, (c) -> {

			// establish data managers
			AttributesDataManager attributesDataManager = c.getDataManager(AttributesDataManager.class);
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			PartitionsDataManager partitionsDataManager = c.getDataManager(PartitionsDataManager.class);
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);

			// create a TestPartitionsContext from the ActorContext
			TestPartitionsContext testPartitionsContext = new TestPartitionsContext(c);

			// show that the partitions context is a pass through to the actor context
			assertEquals(attributesDataManager, testPartitionsContext.getDataManager(AttributesDataManager.class));
			assertEquals(peopleDataManager, testPartitionsContext.getDataManager(PeopleDataManager.class));
			assertEquals(partitionsDataManager, testPartitionsContext.getDataManager(PartitionsDataManager.class));
			assertEquals(stochasticsDataManager, testPartitionsContext.getDataManager(StochasticsDataManager.class));

		});
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
	}

	@Test
	@UnitTestMethod(target = TestPartitionsContext.class, name = "getTime", args = {})
	public void testGetTime() {
		TestPluginData.Builder testPluginDataBuilder = TestPluginData.builder();

		//show that the TestPartitionsContext returns the correct time
		IntStream.range(0, 5).forEach((i) -> {
			testPluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(i, (c) -> {
				TestPartitionsContext testPartitionsContext = new TestPartitionsContext(c);
				assertEquals(c.getTime(), testPartitionsContext.getTime());

			}));
		});
		
		TestPluginData testPluginData = testPluginDataBuilder.build();
		

		Factory factory = PartitionsTestPluginFactory.factory(0, 2094974625949946320L, testPluginData);
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
	}

}
