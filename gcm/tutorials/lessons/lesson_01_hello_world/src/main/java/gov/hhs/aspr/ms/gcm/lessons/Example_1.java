/*start code_ref=hello_world_short*/
package lesson;

import gov.hhs.aspr.ms.gcm.nucleus.Simulation;

public final class Example_1 {

	public static void main(String[] args) {
		Simulation.builder().build().execute();
	}
	/* end */

	public static void alternateMain(String[] args) {
		/* start code_ref=hello_world_long */
		Simulation.Builder builder = Simulation.builder();
		Simulation simulation = builder.build();
		simulation.execute();
		/* end */
	}
}
