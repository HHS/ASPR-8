package gov.hhs.aspr.ms.gcm.nucleus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.util.annotations.UnitTestMethod;
import gov.hhs.aspr.ms.util.errors.ContractException;
import gov.hhs.aspr.ms.util.random.RandomGeneratorProvider;

public class AT_PlanQueueData {
	class TestPlanData1 implements PlanData {
	}

	@Test
	@UnitTestMethod(target = PlanQueueData.Builder.class, name = "build", args = {})
	public void testBuild() {
		assertNotNull(PlanQueueData.builder().setPlanData(new TestPlanData1()).setPlanner(Planner.ACTOR).build());

		// preconditions:
		// PlanData is null
		ContractException contractException = assertThrows(ContractException.class, () -> {
			PlanQueueData.builder().setPlanner(Planner.ACTOR).build();
		});

		assertEquals(NucleusError.NULL_PLAN_DATA, contractException.getErrorType());

		// Planner is null
		contractException = assertThrows(ContractException.class, () -> {
			PlanQueueData.builder().setPlanData(new TestPlanData1()).build();
		});

		assertEquals(NucleusError.NULL_PLANNER, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = PlanQueueData.Builder.class, name = "setTime", args = { double.class })
	public void testSetTime() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(594478369345316212L);

		for (int i = 0; i < 10; i++) {
			double time = randomGenerator.nextDouble() * 100;
			PlanQueueData planQueueData = PlanQueueData.builder().setPlanData(new TestPlanData1())
					.setPlanner(Planner.ACTOR).setTime(time).build();

			assertEquals(time, planQueueData.getTime());
		}

		PlanQueueData planQueueData = PlanQueueData.builder().setPlanData(new TestPlanData1()).setPlanner(Planner.ACTOR)
				.build();

		assertEquals(0, planQueueData.getTime());
	}

	@Test
	@UnitTestMethod(target = PlanQueueData.Builder.class, name = "setActive", args = { boolean.class })
	public void testSetActive() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(2212750783748698797L);

		for (int i = 0; i < 10; i++) {
			boolean active = randomGenerator.nextBoolean();
			PlanQueueData planQueueData = PlanQueueData.builder().setPlanData(new TestPlanData1())
					.setPlanner(Planner.ACTOR).setActive(active).build();

			assertEquals(active, planQueueData.isActive());
		}

		PlanQueueData planQueueData = PlanQueueData.builder().setPlanData(new TestPlanData1()).setPlanner(Planner.ACTOR)
				.build();

		assertTrue(planQueueData.isActive());
	}

	@Test
	@UnitTestMethod(target = PlanQueueData.Builder.class, name = "setKey", args = { Object.class })
	public void testSetKey() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(6278569817385464648L);

		for (int i = 0; i < 10; i++) {
			String key = "TestKey" + (i * randomGenerator.nextInt(100) + 1) + i % 2;
			PlanQueueData planQueueData = PlanQueueData.builder().setPlanData(new TestPlanData1())
					.setPlanner(Planner.ACTOR).setKey(key).build();

			assertEquals(key, planQueueData.getKey());
		}

		PlanQueueData planQueueData = PlanQueueData.builder().setPlanData(new TestPlanData1()).setPlanner(Planner.ACTOR)
				.build();

		assertTrue(planQueueData.getKey() == null);
	}

	@Test
	@UnitTestMethod(target = PlanQueueData.Builder.class, name = "setPlanData", args = { PlanData.class })
	public void testSetPlanData() {
		for (int i = 0; i < 10; i++) {
			TestPlanData1 testPlanData1 = new TestPlanData1();
			PlanQueueData planQueueData = PlanQueueData.builder().setPlanData(testPlanData1).setPlanner(Planner.ACTOR)
					.build();

			assertEquals(testPlanData1, planQueueData.getPlanData());
		}
	}

	@Test
	@UnitTestMethod(target = PlanQueueData.Builder.class, name = "setPlanner", args = { Planner.class })
	public void testSetPlanner() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(5508769779925956678L);

		for (int i = 0; i < 10; i++) {
			Planner planner = Planner.values()[randomGenerator.nextInt(3)];

			PlanQueueData planQueueData = PlanQueueData.builder().setPlanData(new TestPlanData1()).setPlanner(planner)
					.build();

			assertEquals(planner, planQueueData.getPlanner());
		}
	}

	@Test
	@UnitTestMethod(target = PlanQueueData.Builder.class, name = "setPlannerId", args = { int.class })
	public void testSetPlannerId() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(594478369345316212L);

		for (int i = 0; i < 10; i++) {
			int plannerId = randomGenerator.nextInt(100);
			PlanQueueData planQueueData = PlanQueueData.builder().setPlanData(new TestPlanData1())
					.setPlanner(Planner.ACTOR).setPlannerId(plannerId).build();

			assertEquals(plannerId, planQueueData.getPlannerId());
		}

		PlanQueueData planQueueData = PlanQueueData.builder().setPlanData(new TestPlanData1()).setPlanner(Planner.ACTOR)
				.build();

		assertEquals(0, planQueueData.getPlannerId());
	}

	@Test
	@UnitTestMethod(target = PlanQueueData.Builder.class, name = "setArrivalId", args = { long.class })
	public void testSetArrivalId() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(5508769779925956678L);

		for (int i = 0; i < 10; i++) {
			long arrivalId = randomGenerator.nextLong();
			PlanQueueData planQueueData = PlanQueueData.builder().setPlanData(new TestPlanData1())
					.setPlanner(Planner.ACTOR).setArrivalId(arrivalId).build();

			assertEquals(arrivalId, planQueueData.getArrivalId());
		}

		PlanQueueData planQueueData = PlanQueueData.builder().setPlanData(new TestPlanData1()).setPlanner(Planner.ACTOR)
				.build();

		assertEquals(0, planQueueData.getArrivalId());
	}

	@Test
	@UnitTestMethod(target = PlanQueueData.class, name = "builder", args = {})
	public void testBuilder() {
		assertNotNull(PlanQueueData.builder());
	}

	@Test
	@UnitTestMethod(target = PlanQueueData.class, name = "getArrivalId", args = {})
	public void testGetArrivalId() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(5508769779925956678L);

		for (int i = 0; i < 10; i++) {
			long arrivalId = randomGenerator.nextLong();
			PlanQueueData planQueueData = PlanQueueData.builder().setPlanData(new TestPlanData1())
					.setPlanner(Planner.ACTOR).setArrivalId(arrivalId).build();

			assertEquals(arrivalId, planQueueData.getArrivalId());
		}

		PlanQueueData planQueueData = PlanQueueData.builder().setPlanData(new TestPlanData1()).setPlanner(Planner.ACTOR)
				.build();

		assertEquals(0, planQueueData.getArrivalId());
	}

	@Test
	@UnitTestMethod(target = PlanQueueData.class, name = "getKey", args = {})
	public void testGetKey() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(6278569817385464648L);

		for (int i = 0; i < 10; i++) {
			String key = "TestKey" + (i * randomGenerator.nextInt(100) + 1) + i % 2;
			PlanQueueData planQueueData = PlanQueueData.builder().setPlanData(new TestPlanData1())
					.setPlanner(Planner.ACTOR).setKey(key).build();

			assertEquals(key, planQueueData.getKey());
		}

		PlanQueueData planQueueData = PlanQueueData.builder().setPlanData(new TestPlanData1()).setPlanner(Planner.ACTOR)
				.build();

		assertTrue(planQueueData.getKey() == null);
	}

	@Test
	@UnitTestMethod(target = PlanQueueData.class, name = "getPlanData", args = {})
	public void testGetPlanData() {
		for (int i = 0; i < 10; i++) {
			TestPlanData1 testPlanData1 = new TestPlanData1();
			PlanQueueData planQueueData = PlanQueueData.builder().setPlanData(testPlanData1).setPlanner(Planner.ACTOR)
					.build();

			assertEquals(testPlanData1, planQueueData.getPlanData());
		}
	}

	@Test
	@UnitTestMethod(target = PlanQueueData.class, name = "getPlanner", args = {})
	public void testGetPlanner() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(5508769779925956678L);

		for (int i = 0; i < 10; i++) {
			Planner planner = Planner.values()[randomGenerator.nextInt(3)];

			PlanQueueData planQueueData = PlanQueueData.builder().setPlanData(new TestPlanData1()).setPlanner(planner)
					.build();

			assertEquals(planner, planQueueData.getPlanner());
		}
	}

	@Test
	@UnitTestMethod(target = PlanQueueData.class, name = "getPlannerId", args = {})
	public void testGetPlannerId() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(594478369345316212L);

		for (int i = 0; i < 10; i++) {
			int plannerId = randomGenerator.nextInt(100);
			PlanQueueData planQueueData = PlanQueueData.builder().setPlanData(new TestPlanData1())
					.setPlanner(Planner.ACTOR).setPlannerId(plannerId).build();

			assertEquals(plannerId, planQueueData.getPlannerId());
		}

		PlanQueueData planQueueData = PlanQueueData.builder().setPlanData(new TestPlanData1()).setPlanner(Planner.ACTOR)
				.build();

		assertEquals(0, planQueueData.getPlannerId());
	}

	@Test
	@UnitTestMethod(target = PlanQueueData.class, name = "getTime", args = {})
	public void testGetTime() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(594478369345316212L);

		for (int i = 0; i < 10; i++) {
			double time = randomGenerator.nextDouble() * 100;
			PlanQueueData planQueueData = PlanQueueData.builder().setPlanData(new TestPlanData1())
					.setPlanner(Planner.ACTOR).setTime(time).build();

			assertEquals(time, planQueueData.getTime());
		}

		PlanQueueData planQueueData = PlanQueueData.builder().setPlanData(new TestPlanData1()).setPlanner(Planner.ACTOR)
				.build();

		assertEquals(0, planQueueData.getTime());
	}

	@Test
	@UnitTestMethod(target = PlanQueueData.class, name = "isActive", args = {})
	public void testIsActive() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(2212750783748698797L);

		for (int i = 0; i < 10; i++) {
			boolean active = randomGenerator.nextBoolean();
			PlanQueueData planQueueData = PlanQueueData.builder().setPlanData(new TestPlanData1())
					.setPlanner(Planner.ACTOR).setActive(active).build();

			assertEquals(active, planQueueData.isActive());
		}

		PlanQueueData planQueueData = PlanQueueData.builder().setPlanData(new TestPlanData1()).setPlanner(Planner.ACTOR)
				.build();

		assertTrue(planQueueData.isActive());
	}

	private static class TestPlanData implements PlanData {
		private final int index;

		public TestPlanData(int index) {
			this.index = index;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + index;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (!(obj instanceof TestPlanData)) {
				return false;
			}
			TestPlanData other = (TestPlanData) obj;
			if (index != other.index) {
				return false;
			}
			return true;
		}

	}

	private PlanQueueData getRandomPlanQueueData(long seed) {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(seed);
		return PlanQueueData.builder()//
				.setActive(randomGenerator.nextBoolean())//
				.setArrivalId(randomGenerator.nextLong())//
				.setKey(Integer.toString(randomGenerator.nextInt()))//
				.setPlanData(new TestPlanData(randomGenerator.nextInt()))
				.setPlanner(Planner.values()[randomGenerator.nextInt(Planner.values().length)])//
				.setPlannerId(randomGenerator.nextInt())//
				.setTime(randomGenerator.nextDouble())//
				.build();
	}

	@Test
	@UnitTestMethod(target = PlanQueueData.class, name = "hashCode", args = {})
	public void testHashCode() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(3433493259546771854L);

		// show equal objects have equal hash codes
		for (int i = 0; i < 30; i++) {
			long seed = randomGenerator.nextLong();

			PlanQueueData pqd1 = getRandomPlanQueueData(seed);
			PlanQueueData pqd2 = getRandomPlanQueueData(seed);

			assertEquals(pqd1, pqd2);
			assertEquals(pqd1.hashCode(), pqd2.hashCode());
		}

		// show hash codes are reasonably distributed
		Set<Integer> hashCodes = new LinkedHashSet<>();
		for (int i = 0; i < 100; i++) {
			long seed = randomGenerator.nextLong();

			PlanQueueData pqd = getRandomPlanQueueData(seed);
			hashCodes.add(pqd.hashCode());
		}

		assertTrue(hashCodes.size() > 95);

	}

	@Test
	@UnitTestMethod(target = PlanQueueData.class, name = "equals", args = {Object.class})
	public void testEquals() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(4135063462491592422L);

		// show not equal to null
		for (int i = 0; i < 30; i++) {
			PlanQueueData pqd = getRandomPlanQueueData(randomGenerator.nextLong());
			assertFalse(pqd.equals(null));
		}

		// reflexive
		for (int i = 0; i < 30; i++) {
			PlanQueueData pqd = getRandomPlanQueueData(randomGenerator.nextLong());
			assertTrue(pqd.equals(pqd));
		}

		// symmetric/transitive and consistent
		for (int i = 0; i < 30; i++) {
			long seed = randomGenerator.nextLong();
			PlanQueueData pqd1 = getRandomPlanQueueData(seed);
			PlanQueueData pqd2 = getRandomPlanQueueData(seed);
			for (int j = 0; j < 10; j++) {
				assertTrue(pqd1.equals(pqd2));
				assertTrue(pqd2.equals(pqd1));
			}
		}
		
		//show that different inputs lead to non-equality
		for (int i = 0; i < 30; i++) {
			
			PlanQueueData pqd1 = getRandomPlanQueueData(randomGenerator.nextLong());
			PlanQueueData pqd2 = getRandomPlanQueueData(randomGenerator.nextLong());
			
			//VERY low probability they are equal 
			assertNotEquals(pqd1,pqd2);
		}

	}
}
