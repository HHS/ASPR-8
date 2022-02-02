package manual;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import nucleus.AgentContext;
import nucleus.Simulation;
import nucleus.testsupport.actionplugin.ActionAgent;
import nucleus.testsupport.actionplugin.ActionPlugin;
import nucleus.testsupport.actionplugin.AgentActionPlan;
import plugins.compartments.CompartmentPlugin;
import plugins.compartments.initialdata.CompartmentInitialData;
import plugins.compartments.support.CompartmentId;
import plugins.compartments.testsupport.TestCompartmentId;
import plugins.components.ComponentPlugin;
import plugins.partitions.PartitionsPlugin;
import plugins.partitions.datacontainers.PartitionDataView;
import plugins.partitions.events.PartitionAdditionEvent;
import plugins.partitions.events.PartitionRemovalEvent;
import plugins.partitions.support.Equality;
import plugins.partitions.support.Partition;
import plugins.partitions.support.PartitionSampler;
import plugins.people.PeoplePlugin;
import plugins.people.datacontainers.PersonDataView;
import plugins.people.events.mutation.PersonCreationEvent;
import plugins.people.initialdata.PeopleInitialData;
import plugins.people.support.PersonContructionData;
import plugins.people.support.PersonId;
import plugins.personproperties.PersonPropertiesPlugin;
import plugins.personproperties.datacontainers.PersonPropertyDataView;
import plugins.personproperties.events.mutation.PersonPropertyValueAssignmentEvent;
import plugins.personproperties.initialdata.PersonPropertyInitialData;
import plugins.personproperties.support.PersonPropertyId;
import plugins.personproperties.support.PropertyFilter;
import plugins.personproperties.testsupport.TestPersonPropertyId;
import plugins.properties.PropertiesPlugin;
import plugins.properties.support.PropertyDefinition;
import plugins.regions.RegionPlugin;
import plugins.regions.initialdata.RegionInitialData;
import plugins.regions.support.RegionId;
import plugins.regions.testsupport.TestRegionId;
import plugins.reports.ReportPlugin;
import plugins.reports.initialdata.ReportsInitialData;
import plugins.stochastics.StochasticsDataView;
import plugins.stochastics.StochasticsPlugin;
import util.MutableInteger;
@Tag("manual")
public final class PartitionBugTest {

	@Test
	public void test() {
		RegionId regionId = TestRegionId.REGION_1;
		// add a region that does nothing
		RegionInitialData.Builder regionInitialDataBuilder = RegionInitialData.builder();
		regionInitialDataBuilder.setRegionComponentInitialBehaviorSupplier(regionId, () -> {
			return (c) -> {
			};
		});
		RegionInitialData regionInitialData = regionInitialDataBuilder.build();
		RegionPlugin regionPlugin = new RegionPlugin(regionInitialData);

		// add a compartment and link it to the action plugin
		CompartmentId compartmentId = TestCompartmentId.COMPARTMENT_1;
		CompartmentInitialData.Builder compartmentInitialDataBuilder = CompartmentInitialData.builder();
		compartmentInitialDataBuilder.setCompartmentInitialBehaviorSupplier(compartmentId, () -> {
			return new ActionAgent(compartmentId)::init;
		});
		CompartmentInitialData compartmentInitialData = compartmentInitialDataBuilder.build();
		CompartmentPlugin compartmentPlugin = new CompartmentPlugin(compartmentInitialData);

		// define a boolean person property, defaulted to false
		PersonPropertyId propertyId = TestPersonPropertyId.PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK;

		PersonPropertiesPlugin personPropertiesPlugin = new PersonPropertiesPlugin(//
				PersonPropertyInitialData	.builder()//
											.definePersonProperty(propertyId, //
													PropertyDefinition	.builder()//
																		.setDefaultValue(false)//
																		.setType(Boolean.class)//
																		.build())//
											.build());//

		ActionPlugin.Builder actionPluginBuilder = ActionPlugin.builder();

		// create key2 for the partitions
		Object key_1 = new Object();
		Object key_2 = new Object();

		/*
		 * Have the compartment make some people and initialize the partition
		 * filter on those people whose property value is false
		 */
		actionPluginBuilder.addAgentActionPlan(compartmentId, new AgentActionPlan(0, (c) -> {
			// create 10 people
			for (int i = 0; i < 10; i++) {
				PersonContructionData personContructionData = PersonContructionData.builder().add(TestCompartmentId.COMPARTMENT_1).add(TestRegionId.REGION_1).build();
				c.resolveEvent(new PersonCreationEvent(personContructionData));
			}

			// add a partition that is only a filter on the property
			PropertyFilter propertyFilter = new PropertyFilter(propertyId, Equality.EQUAL, false);
			c.resolveEvent(new PartitionAdditionEvent(Partition.builder().setFilter(propertyFilter).build(), key_1));

			// add a second partition
			c.resolveEvent(new PartitionAdditionEvent(Partition.builder().setFilter(propertyFilter).build(), key_2));

		}));

		/*
		 * schedule the removal of the second partition for some time before the
		 * end of the updates to the person property
		 */
		actionPluginBuilder.addAgentActionPlan(compartmentId, new AgentActionPlan(500, (c) -> {
			c.resolveEvent(new PartitionRemovalEvent(key_2));
		}));

		// cycle people in and out based on the property being changed
		actionPluginBuilder.addAgentActionPlan(compartmentId, new AgentActionPlan(1, (c) -> {
			RandomGenerator randomGenerator = c.getDataView(StochasticsDataView.class).get().getRandomGenerator();
			List<PersonId> people = c.getDataView(PersonDataView.class).get().getPeople();
			PersonPropertyDataView personPropertyDataView = c.getDataView(PersonPropertyDataView.class).get();
			PartitionDataView partitionDataView = c.getDataView(PartitionDataView.class).get();
			// create a plan that will be executed multiple times
			Consumer<AgentContext> plan = (c2) -> {
				// pick a rando and switch their property value
				PersonId personId = people.get(randomGenerator.nextInt(people.size()));
				Boolean value = personPropertyDataView.getPersonPropertyValue(personId, propertyId);
				c.resolveEvent(new PersonPropertyValueAssignmentEvent(personId, propertyId, !value));
				boolean personContainedInPartition = partitionDataView.contains(personId, key_1);
				assertEquals(value, personContainedInPartition);
			};

			// schedule the plan 1000 times
			for (int i = 0; i < 1000; i++) {
				double planTime = i + 10;
				c.addPlan(plan, planTime);
			}
		}));

		/*
		 * Create a counter to record the number of times the partition was
		 * empty to show that the following test is valid.
		 */

		MutableInteger emptyPartitionCounter = new MutableInteger();

		/*
		 * schedule multiple samples from the partition and show that the
		 * returned people all have the property value of false
		 */
		actionPluginBuilder.addAgentActionPlan(compartmentId, new AgentActionPlan(1, (c) -> {
			PersonPropertyDataView personPropertyDataView = c.getDataView(PersonPropertyDataView.class).get();
			PartitionDataView partitionDataView = c.getDataView(PartitionDataView.class).get();
			PartitionSampler partitionSampler = PartitionSampler.builder().build();

			// create a plan that will be executed multiple times
			Consumer<AgentContext> plan = (c2) -> {
				Optional<PersonId> optional = partitionDataView.samplePartition(key_1, partitionSampler);
				if (optional.isPresent()) {
					PersonId personId = optional.get();

					Boolean value = personPropertyDataView.getPersonPropertyValue(personId, propertyId);
					assertFalse(value);
				} else {
					emptyPartitionCounter.increment();
				}
			};

			// schedule the plan 1000 times
			for (int i = 0; i < 1000; i++) {
				double planTime = i + 15;
				c.addPlan(plan, planTime);
			}
		}));

		// build the action plugin
		ActionPlugin actionPlugin = actionPluginBuilder.build();

		// build and execute the engine
		Simulation	.builder()//
				.addPlugin(RegionPlugin.PLUGIN_ID,regionPlugin::init)//
				.addPlugin(ReportPlugin.PLUGIN_ID,new ReportPlugin(ReportsInitialData.builder().build())::init)//
				.addPlugin(CompartmentPlugin.PLUGIN_ID,compartmentPlugin::init)//
				.addPlugin(PropertiesPlugin.PLUGIN_ID,new PropertiesPlugin()::init)//
				.addPlugin(StochasticsPlugin.PLUGIN_ID, StochasticsPlugin.builder().setSeed(34234234234L).build()::init)
				.addPlugin(PeoplePlugin.PLUGIN_ID,new PeoplePlugin(PeopleInitialData.builder().build())::init)//
				.addPlugin(ComponentPlugin.PLUGIN_ID,new ComponentPlugin()::init)//
				.addPlugin(PartitionsPlugin.PLUGIN_ID,new PartitionsPlugin()::init)//
				.addPlugin(ActionPlugin.PLUGIN_ID,actionPlugin::init)//
				.addPlugin(PersonPropertiesPlugin.PLUGIN_ID,personPropertiesPlugin::init)//
				.build().execute();//

		// show that all actions executed
		assertTrue(actionPlugin.allActionsExecuted());

		// show that the sampling test was not encountering too many empty
		// partitions
		assertTrue(emptyPartitionCounter.getValue() < 100);
	}

}
