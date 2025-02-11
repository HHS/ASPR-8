package gov.hhs.aspr.ms.gcm.simulation.nucleus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.util.annotations.UnitTestMethod;
import gov.hhs.aspr.ms.util.errors.ContractException;
import gov.hhs.aspr.ms.util.random.RandomGeneratorProvider;

public class AT_SimulationState {


	@Test
	@UnitTestMethod(target = SimulationState.Builder.class, name = "build", args = {})
	public void testBuild() {
		assertNotNull(SimulationState.builder().build());
	}

	@Test
	@UnitTestMethod(target = SimulationState.Builder.class, name = "setStartTime", args = { double.class })
	public void testSetStartTime() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(6278569817385464648L);

		for (int i = 0; i < 10; i++) {
			double startTime = randomGenerator.nextDouble() * 10;
			SimulationState simulationState = SimulationState.builder().setStartTime(startTime).build();

			assertEquals(startTime, simulationState.getStartTime());
		}
	}

	@Test
	@UnitTestMethod(target = SimulationState.Builder.class, name = "setBaseDate", args = { LocalDate.class })
	public void testSetBaseDate() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(196036555746621355L);

		for (int i = 0; i < 10; i++) {
			int month = randomGenerator.nextInt(12) + 1;
			int day;
			int year = 2023 + randomGenerator.nextInt(2);

			if (month == 2) {
				day = randomGenerator.nextInt(28) + 1;
			} else if (month == 9 || month == 4 || month == 6 || month == 11) {
				day = randomGenerator.nextInt(30) + 1;
			} else {
				day = randomGenerator.nextInt(31) + 1;
			}

			LocalDate localDate = LocalDate.of(year, month, day);
			SimulationState simulationState = SimulationState.builder().setBaseDate(localDate).build();

			assertEquals(localDate, simulationState.getBaseDate());
		}

		// precondition:
		// LocalDate is null
		ContractException contractException = assertThrows(ContractException.class, () -> {
			SimulationState.builder().setBaseDate(null);
		});

		assertEquals(NucleusError.NULL_BASE_DATE, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = SimulationState.class, name = "builder", args = {})
	public void testBuilder() {
		assertNotNull(SimulationState.builder());
	}

	@Test
	@UnitTestMethod(target = SimulationState.class, name = "getStartTime", args = {})
	public void testGetStartTime() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(5508769779925956678L);

		for (int i = 0; i < 10; i++) {
			double startTime = randomGenerator.nextDouble() * 10;
			SimulationState simulationState = SimulationState.builder().setStartTime(startTime).build();

			assertEquals(startTime, simulationState.getStartTime());
		}
	}

	@Test
	@UnitTestMethod(target = SimulationState.class, name = "getBaseDate", args = {})
	public void testGetBaseDate() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(1840591792412124210L);

		for (int i = 0; i < 10; i++) {
			int month = randomGenerator.nextInt(12) + 1;
			int day;
			int year = 2023 + randomGenerator.nextInt(2);

			if (month == 2) {
				day = randomGenerator.nextInt(28) + 1;
			} else if (month == 9 || month == 4 || month == 6 || month == 11) {
				day = randomGenerator.nextInt(30) + 1;
			} else {
				day = randomGenerator.nextInt(31) + 1;
			}

			LocalDate localDate = LocalDate.of(year, month, day);
			SimulationState simulationState = SimulationState.builder().setBaseDate(localDate).build();

			assertEquals(localDate, simulationState.getBaseDate());
		}
	}

	private SimulationState getRandomSimulationState(long seed) {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(seed);
		return SimulationState.builder()//
				.setBaseDate(LocalDate.of(randomGenerator.nextInt(20) + 2000, randomGenerator.nextInt(12) + 1,
						randomGenerator.nextInt(28) + 1))
				.setStartTime(randomGenerator.nextDouble() * 100)
				.setStartTime(randomGenerator.nextDouble()*100)
				.build();
	}

	@Test
	@UnitTestMethod(target = SimulationState.class, name = "getVersion", args = {})
	public void testGetVersion() {
		SimulationState groupsPluginData = getRandomSimulationState(0);
		assertEquals(StandardVersioning.VERSION, groupsPluginData.getVersion());
	}

	@Test
	@UnitTestMethod(target = SimulationState.class, name = "checkVersionSupported", args = { String.class })
	public void testCheckVersionSupported() {
		List<String> versions = Arrays.asList(StandardVersioning.VERSION);

		for (String version : versions) {
			assertTrue(SimulationState.checkVersionSupported(version));
			assertFalse(SimulationState.checkVersionSupported(version + "badVersion"));
			assertFalse(SimulationState.checkVersionSupported("badVersion"));
			assertFalse(SimulationState.checkVersionSupported(version + "0"));
			assertFalse(SimulationState.checkVersionSupported(version + ".0.0"));
		}
	}

	@Test
	@UnitTestMethod(target = SimulationState.class, name = "equals", args = { Object.class })
	public void testEquals() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(1814317894811919552L);

		// show not equal to null
		for (int i = 0; i < 30; i++) {
			SimulationState simulationState = getRandomSimulationState(randomGenerator.nextLong());
			assertFalse(simulationState.equals(null));
		}

		// show reflexivity
		for (int i = 0; i < 30; i++) {
			SimulationState simulationState = getRandomSimulationState(randomGenerator.nextLong());
			assertTrue(simulationState.equals(simulationState));
		}
		
		// show symmetry/transitivity
		for (int i = 0; i < 30; i++) {
			long seed = randomGenerator.nextLong();
			
			SimulationState simulationState1 = getRandomSimulationState(seed);
			SimulationState simulationState2 = getRandomSimulationState(seed);
			assertTrue(simulationState1.equals(simulationState2));
			assertTrue(simulationState2.equals(simulationState1));
		}
		
		//show different inputs cause non-equality
		for (int i = 0; i < 30; i++) {
			SimulationState simulationState1 = getRandomSimulationState(randomGenerator.nextLong());
			SimulationState simulationState2 = getRandomSimulationState(randomGenerator.nextLong());
			assertNotEquals(simulationState1,simulationState2);
		}

	}

	@Test
	@UnitTestMethod(target = SimulationState.class, name = "hashCode", args = {})
	public void testHashCode() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(3169333378872001748L);
		
		//show equal objects have equal hash codes
		for (int i = 0; i < 30; i++) {
			long seed = randomGenerator.nextLong();			
			SimulationState simulationState1 = getRandomSimulationState(seed);
			SimulationState simulationState2 = getRandomSimulationState(seed);
			assertEquals(simulationState1,simulationState2);
			assertEquals(simulationState1.hashCode(),simulationState2.hashCode());
		}

	}
}
