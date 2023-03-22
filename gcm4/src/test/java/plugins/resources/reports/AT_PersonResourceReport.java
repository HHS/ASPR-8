package plugins.resources.reports;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

import nucleus.Plugin;
import nucleus.ReportContext;
import nucleus.testsupport.testplugin.TestActorPlan;
import nucleus.testsupport.testplugin.TestPluginData;
import nucleus.testsupport.testplugin.TestSimulation;
import plugins.resources.testsupport.ResourcesTestPluginFactory;
import util.annotations.UnitTag;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

public class AT_PersonResourceReport {

	@Test
	@UnitTestConstructor(target = PersonResourceReport.class, args = { PersonResourceReportPluginData.class })
	public void testConstructor() {
		/*
		 * Nothing to test. The PersonResourceReportPluginData guarantees that
		 * the report lable and report period will not be null. The super
		 * constructor of the PersonResourceReport prevents the use of a
		 * ContractException for null PersonResourceReportPluginData instances.
		 */
		assertThrows(NullPointerException.class, () -> new PersonResourceReport(null));

	}

	@Test
	@UnitTestMethod(target = PersonResourceReport.class, name = "init", args = { ReportContext.class }, tags = UnitTag.INCOMPLETE)
	public void testInit() {
		
		//excluded properties
		//included properties
		//new non existing
		
//		reportContext.subscribe(PersonAdditionEvent.class, this::handlePersonAdditionEvent);
//		reportContext.subscribe(PersonImminentRemovalEvent.class, this::handlePersonImminentRemovalEvent);
//		reportContext.subscribe(PersonRegionUpdateEvent.class, this::handlePersonRegionUpdateEvent);
//		reportContext.subscribe(RegionAdditionEvent.class, this::handleRegionAdditionEvent);
//		reportContext.subscribe(PersonResourceUpdateEvent.class, this::handlePersonResourceUpdateEvent);
//		reportContext.subscribe(ResourceIdAdditionEvent.class, this::handleResourceIdAdditionEvent);
		
		
		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();
		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(0,(c)->{}));
		TestPluginData testPluginData = pluginDataBuilder.build();
		
		List<Plugin> plugins = ResourcesTestPluginFactory//
				.factory(5, 5884216992159063226L, testPluginData)//				
				.getPlugins();
		
		TestSimulation.executeSimulation(plugins);
	}

}