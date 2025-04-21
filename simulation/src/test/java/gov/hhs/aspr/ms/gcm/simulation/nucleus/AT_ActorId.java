package gov.hhs.aspr.ms.gcm.simulation.nucleus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.util.annotations.UnitTestConstructor;
import gov.hhs.aspr.ms.util.annotations.UnitTestMethod;
import gov.hhs.aspr.ms.util.random.RandomGeneratorProvider;

public final class AT_ActorId {

	@UnitTestConstructor(target = ActorId.class, args = { int.class })
	@Test
	public void testConstructor() {
		for (int i = 0; i < 100; i++) {
			assertEquals(i, new ActorId(i).getValue());
		}
	}

	@UnitTestMethod(target = ActorId.class, name = "getValue", args = {})
	@Test
	public void testGetValue() {
		for (int i = 0; i < 100; i++) {
			assertEquals(i, new ActorId(i).getValue());
		}
	}

	@UnitTestMethod(target = ActorId.class, name = "toString", args = {})
	@Test
	public void testToString() {
		for (int i = 0; i < 100; i++) {
			assertEquals("ActorId [id=" + i + "]", new ActorId(i).toString());
		}
	}

	@UnitTestMethod(target = ActorId.class, name = "hashCode", args = {})
	@Test
	public void testHashCode() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(2653490908465183354L);

		// equal objects have equal hash codes
		for (int i = 0; i < 30; i++) {
			long seed = randomGenerator.nextLong();
			ActorId actorId1 = getRandomActorId(seed);
			ActorId actorId2 = getRandomActorId(seed);

			assertEquals(actorId1, actorId2);
			assertEquals(actorId1.hashCode(), actorId2.hashCode());
		}

		// hash codes are reasonably distributed
		Set<Integer> hashCodes = new LinkedHashSet<>();
		for (int i = 0; i < 100; i++) {
			ActorId actorId = getRandomActorId(randomGenerator.nextLong());
			hashCodes.add(actorId.hashCode());
		}

		assertEquals(100, hashCodes.size());
	}

	@UnitTestMethod(target = ActorId.class, name = "equals", args = { Object.class })
	@Test
	public void testEquals() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(8980821418373346870L);

		// never equal to another type
		for (int i = 0; i < 30; i++) {
			ActorId actorId = getRandomActorId(randomGenerator.nextLong());
			assertFalse(actorId.equals(new Object()));
		}

		// never equal to null
		for (int i = 0; i < 30; i++) {
			ActorId actorId = getRandomActorId(randomGenerator.nextLong());
			assertFalse(actorId.equals(null));
		}

		// reflexive
		for (int i = 0; i < 30; i++) {
			ActorId actorId = getRandomActorId(randomGenerator.nextLong());
			assertTrue(actorId.equals(actorId));
		}

		// symmetric, transitive, consistent
		for (int i = 0; i < 30; i++) {
			long seed = randomGenerator.nextLong();
			ActorId actorId1 = getRandomActorId(seed);
			ActorId actorId2 = getRandomActorId(seed);
			assertFalse(actorId1 == actorId2);
			for (int j = 0; j < 10; j++) {
				assertTrue(actorId1.equals(actorId2));
				assertTrue(actorId2.equals(actorId1));
			}
		}

		// different inputs yield unequal actorIds
		Set<ActorId> set = new LinkedHashSet<>();
		for (int i = 0; i < 100; i++) {
			ActorId actorId = getRandomActorId(randomGenerator.nextLong());
			set.add(actorId);
		}
		assertEquals(100, set.size());
	}

	private ActorId getRandomActorId(long seed) {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(seed);
		return new ActorId(randomGenerator.nextInt(Integer.MAX_VALUE));
	}
}
