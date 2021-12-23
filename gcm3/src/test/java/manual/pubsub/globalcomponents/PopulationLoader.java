package manual.pubsub.globalcomponents;

import org.apache.commons.math3.random.RandomGenerator;

import manual.demo.identifiers.GroupType;
import manual.pubsub.compartments.Compartment;
import manual.pubsub.regions.Region;
import plugins.gcm.agents.AbstractComponent;
import plugins.gcm.agents.Environment;
import plugins.groups.support.BulkGroupMembershipData;
import plugins.partitions.support.Filter;
import plugins.partitions.support.Partition;
import plugins.people.support.BulkPersonContructionData;
import plugins.people.support.PersonContructionData;
import plugins.regions.support.RegionLabeler;
import util.TimeElapser;

public class PopulationLoader extends AbstractComponent {

	private final Object PARTITION_KEY = "PARTITION_KEY";
	private final int POP_SIZE = 100_000;
	private BulkGroupMembershipData getBulkGroupMembershipData(Environment environment, boolean addMemberships) {

		
		RandomGenerator randomGenerator = environment.getRandomGenerator();
		BulkGroupMembershipData.Builder groupBuilder = BulkGroupMembershipData.builder();

		// aim for groupcount == 0.5 popsize

		int homeGroupCount = POP_SIZE / 4; // could be lower
		int schoolGroupCount = POP_SIZE / 100;
		int workGroupCount = POP_SIZE / 30; // could be lower

		for (int i = 0; i < homeGroupCount; i++) {
			groupBuilder.addGroup(GroupType.HOME);
		}

		for (int i = 0; i < schoolGroupCount; i++) {
			groupBuilder.addGroup(GroupType.SCHOOL);
		}

		for (int i = 0; i < workGroupCount; i++) {
			groupBuilder.addGroup(GroupType.WORK);
		}

		if (addMemberships) {
			for (int i = 0; i < POP_SIZE; i++) {
				if (homeGroupCount > 0) {
					int groupIndex = randomGenerator.nextInt(homeGroupCount);
					groupBuilder.addPersonToGroup(i, groupIndex);
				}
				if (schoolGroupCount > 0) {
					if (randomGenerator.nextDouble() < 0.2) {
						int groupIndex = randomGenerator.nextInt(schoolGroupCount);
						groupIndex += homeGroupCount;
						groupBuilder.addPersonToGroup(i, groupIndex);
					}
				}
				if (workGroupCount > 0) {
					if (randomGenerator.nextDouble() < 0.5) {
						int groupIndex = randomGenerator.nextInt(workGroupCount);
						groupIndex += homeGroupCount;
						groupIndex += schoolGroupCount;
						groupBuilder.addPersonToGroup(i, groupIndex);
					}
				}
			}
		}

		return groupBuilder.build();

	}

	@Override
	public void init(Environment environment) {
		boolean useBulkData = true;
		boolean addGroupData = false;
		boolean addMemberships = true;
		boolean usePartition = false;

		if (usePartition) {
			Partition partition = Partition.builder().setFilter(Filter.allPeople()).addLabeler(new RegionLabeler((r) -> r.equals(Region.REGION_1))).build();
			environment.addPartition(partition, PARTITION_KEY);
		}

		

		TimeElapser timeElapser = new TimeElapser();

		if (useBulkData) {
			BulkPersonContructionData.Builder builder = BulkPersonContructionData.builder();

			for (int i = 0; i < POP_SIZE; i++) {
				PersonContructionData personContructionData = PersonContructionData.builder().add(Region.REGION_1).add(Compartment.COMPARTMENT_1).build();
				builder.add(personContructionData);
			}

			if (addGroupData) {
				BulkGroupMembershipData bulkGroupMembershipData = getBulkGroupMembershipData(environment, addMemberships);
				builder.addAuxiliaryData(bulkGroupMembershipData);
			}
			BulkPersonContructionData bulkPersonContructionData = builder.build();
			environment.addBulkPeople(bulkPersonContructionData);
		} else {
			for (int i = 0; i < POP_SIZE; i++) {
				PersonContructionData personContructionData = PersonContructionData.builder().add(Region.REGION_1).add(Compartment.COMPARTMENT_1).build();
				environment.addPerson(personContructionData);
			}
		}
		double nanos = timeElapser.getElapsedNanoSeconds();
		nanos /= POP_SIZE;
		System.out.println("person addition =" + nanos + " nanoseconds per person");

	}

}
