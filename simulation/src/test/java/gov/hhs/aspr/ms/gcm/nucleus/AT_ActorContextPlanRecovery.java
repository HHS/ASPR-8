package gov.hhs.aspr.ms.gcm.nucleus;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.util.annotations.UnitTestMethod;

/**
 * Unit tests designed to demonstrate that recovered plans (plans that have
 * non-negative arrival ids) are processed before new plans and in the correct
 * order to support run continuity.
 */
public class AT_ActorContextPlanRecovery {

	private static class SpecialActor implements Consumer<ActorContext> {
		private final int id;

		public SpecialActor(int id) {
			this.id = id;
		}

		@Override
		public void accept(ActorContext actorContext) {
			switch (id) {
			case 0:
				actorContext.addPlan(new ActorPlan(2, true, 5L, c -> c.releaseOutput(1)));
				actorContext.addPlan(new ActorPlan(7, true, 14L, c -> c.releaseOutput(4)));
				actorContext.addPlan(new ActorPlan(14, true, 12L, c -> c.releaseOutput(9)));
				actorContext.addPlan(new ActorPlan(2, true, -1L, c -> c.releaseOutput(2)));
				actorContext.addPlan(new ActorPlan(7, true, -1L, c -> c.releaseOutput(6)));
				actorContext.addPlan(new ActorPlan(14, true, -1L, c -> c.releaseOutput(10)));

				break;
			case 1:
				actorContext.addPlan(new ActorPlan(2, true, 4L, c -> c.releaseOutput(0)));
				actorContext.addPlan(new ActorPlan(7, true, 16L, c -> c.releaseOutput(5)));
				actorContext.addPlan(new ActorPlan(14, true, 11L, c -> c.releaseOutput(8)));
				actorContext.addPlan(new ActorPlan(2, true, -1L, c -> c.releaseOutput(3)));
				actorContext.addPlan(new ActorPlan(7, true, -1L, c -> c.releaseOutput(7)));
				actorContext.addPlan(new ActorPlan(14, true, -1L, c -> c.releaseOutput(11)));

				break;
			default:
				throw new RuntimeException("unhandled id = " + id);
			}
		}

	}

	@Test
	@UnitTestMethod(target = ActorContext.class, name = "addPlan", args = { ActorPlan.class })
	public void testPlanRecovery() {
		Plugin plugin = Plugin.builder()//
				.setPluginId(new SimplePluginId("plugin"))//
				.setInitializer((c) -> {
					c.addActor(new SpecialActor(0));
					c.addActor(new SpecialActor(1));
				}).build();
		
		
		List<Integer> actualOutput = new ArrayList<>();

		Simulation.builder()//
				.addPlugin(plugin)//
				.setOutputConsumer(output->actualOutput.add((int)output))//
				.build()//
				.execute();
		
		List<Integer> expectedOutput = new ArrayList<>();
		for(int i = 0;i<12;i++) {
			expectedOutput.add(i);
		}
		
		assertEquals(expectedOutput, actualOutput);
		

	}

}
