
package lessons.lesson_01;

import nucleus.Simulation;

public final class Example_1 {

	public static void main(String[] args) {
		Simulation.builder().build().execute();
	}	
	
	public static void alternateMain(String[] args) {
		Simulation.Builder builder = Simulation.builder();
		Simulation simulation = builder.build();
		simulation.execute();
	}
}


