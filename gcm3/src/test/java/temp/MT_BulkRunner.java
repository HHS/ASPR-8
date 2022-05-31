package temp;

import util.time.StopwatchManager;
import util.time.Watch;

public class MT_BulkRunner {

	public static void main(String[] args) {

		StopwatchManager.start(Watch.TOTAL);
		MT_Bulk	.builder()//
				.setSeed(2227922522174256634L)//

				//people
				.setLoadPeopleInPlugins(false)//
				.setPopulationSize(10_000_000)//

				//person properties
				.setUsePersonProperties(true)//
				.setPersonPropertyCount(25)//
				.setInitializedPersonPropertyCount(5)//

				
				//groups
				.setUseGroups(true)//
				.setUseGroupProperties(true)//
				.setWorkplaceSize(25)//
				.setSchoolSize(300)//
				.setHouseholdSize(5)//
				.setSchoolAgeProportion(0.15)//
				.setActiveWorkerProportion(0.8)//
				
				//regions
				.setUseRegions(true)//				
				.setRegionCount(74_000)//
				.setRegionPropertyCount(5)//
				.setInitializedRegionPropertyCount(2)//

				//resources
				.setUseResources(true)//

				.build()//
				.execute();
		StopwatchManager.stop(Watch.TOTAL);
		StopwatchManager.report();
		
		//System.out.println("Remove Stopwatch related classes");

	}
}
