/*start code_ref=hello_world_short|code_cap=Building and executing an empty simulation.*/
package gov.hhs.aspr.ms.gcm.lessons;

import gov.hhs.aspr.ms.gcm.simulation.nucleus.Simulation;

public final class Example_1 {

	public static void main(String[] args) {
		Simulation.builder().build().execute();
	}
	/* end */

	public static void alternateMain(String[] args) {
		/*
		 * start code_ref=hello_world_long|code_cap=Building and executing an empty
		 * simulation broken out into discrete commands.
		 */
		Simulation.Builder builder = Simulation.builder();
		Simulation simulation = builder.build();
		simulation.execute();
		/* end */
	}
}
