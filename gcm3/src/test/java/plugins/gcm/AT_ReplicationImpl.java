package plugins.gcm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import plugins.gcm.experiment.Replication;
import plugins.gcm.experiment.ReplicationId;
import plugins.gcm.experiment.ReplicationImpl;
import util.annotations.UnitTest;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

/**
 * Test class for {@link ReplicationImpl}
 * 
 * @author Shawn Hatch
 *
 */
@UnitTest(target = ReplicationImpl.class)
public class AT_ReplicationImpl {

	/**
	 * Tests {@link ReplicationImpl#getSeed()}
	 */
	@Test
	@UnitTestMethod(name = "getSeed", args = {})
	public void testGetSeed() {
		int[] ids = { 17, 2344, -12352356, 776785768, 2, 0 };
		long[] seeds = { 340983453345L, 34563455L, 97735136456L, 36694567567L, 234333456L, 8876867225L };

		for (int i = 0; i < ids.length; i++) {
			int id = ids[i];
			long seed = seeds[i];
			ReplicationId replicationId = new ReplicationId(id);
			Replication replication = new ReplicationImpl(replicationId, seed);
			assertEquals(seed, replication.getSeed().longValue());
		}

	}

	/**
	 * Tests {@link ReplicationImpl#ReplicationImpl(ReplicationId, Long)}
	 */
	@Test
	@UnitTestConstructor(args = { ReplicationId.class, Long.class })
	public void testConstructor() {

		ReplicationId replicationId = new ReplicationId(45);
		Long seed = 34624564564L;
		ReplicationImpl replicationImpl = new ReplicationImpl(replicationId, seed);

		assertNotNull(replicationImpl);
		assertEquals(replicationId, replicationImpl.getId());
		assertEquals(seed, replicationImpl.getSeed());

	}

	/**
	 * Tests {@link ReplicationImpl#getId()}
	 */
	@Test
	@UnitTestMethod(name = "getId", args = {})
	public void testGetId() {
		int[] ids = { 17, 2344, -12352356, 776785768, 2, 0 };
		long[] seeds = { 340983453345L, 34563455L, 97735136456L, 36694567567L, 234333456L, 8876867225L };

		for (int i = 0; i < ids.length; i++) {
			int id = ids[i];
			long seed = seeds[i];
			ReplicationId replicationId = new ReplicationId(id);
			Replication replication = new ReplicationImpl(replicationId, seed);
			assertEquals(replicationId, replication.getId());
		}

	}

}
