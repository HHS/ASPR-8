package plugins.partitions.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import plugins.compartments.support.CompartmentLabeler;
import plugins.groups.support.GroupLabeler;
import plugins.groups.support.GroupTypeCountMap;
import plugins.groups.support.GroupTypeId;
import plugins.regions.support.RegionLabeler;
import util.annotations.UnitTest;
import util.annotations.UnitTestMethod;

/**
 * Test class for {@link PartitionInfo}
 * 
 * @author Shawn Hatch
 *
 */
@UnitTest(target = Partition.class)
public class AT_Partition {

	private static Integer getGroupTypeCountLabel(GroupTypeCountMap groupTypeCountMap) {
		int result = 0;
		for (GroupTypeId groupTypeId : groupTypeCountMap.getGroupTypeIds()) {
			result += groupTypeCountMap.getGroupCount(groupTypeId);
		}
		return result;
	}

	/**
	 * Tests {@linkplain Partition#builder()
	 */
	@Test
	@UnitTestMethod(name = "builder", args = {})
	public void testBuilder() {
		Partition partition = Partition.builder().build();
		assertNotNull(partition);
		assertFalse(partition.getFilter().isPresent());
		assertTrue(partition.getLabelers().isEmpty());
		assertTrue(partition.isDegenerate());
	}

	/**
	 * Tests {@linkplain Partition#getLabelers()
	 */
	@Test
	@UnitTestMethod(name = "getLabelers", args = {})
	public void testGetLabelers() {

		Set<Labeler> expectedLabelers = new LinkedHashSet<>();
		expectedLabelers.add(new GroupLabeler(AT_Partition::getGroupTypeCountLabel));
		expectedLabelers.add(new CompartmentLabeler((c) -> ""));
		expectedLabelers.add(new RegionLabeler((r) -> 3));

		Partition.Builder builder = Partition.builder();
		for (Labeler labeler : expectedLabelers) {
			builder.addLabeler(labeler);
		}

		Partition partition = builder.build();

		Set<Labeler> actualLabelers = partition.getLabelers();

		assertEquals(expectedLabelers, actualLabelers);

	}

	/**
	 * Tests {@linkplain Partition#getFilter()
	 */
	@Test
	@UnitTestMethod(name = "getFilter", args = {})
	public void testGetFilter() {

		Partition partition = Partition.builder().build();//
		assertFalse(partition.getFilter().isPresent());

		partition = Partition.builder().setFilter(Filter.allPeople()).build();//
		assertTrue(partition.getFilter().isPresent());

	}

	/**
	 * Tests {@linkplain Partition#isDegenerate()
	 */
	@Test
	@UnitTestMethod(name = "isDegenerate", args = {})
	public void testIsDegenerate() {

		Partition partition = Partition.builder().build();//
		assertTrue(partition.isDegenerate());

		partition = Partition.builder().addLabeler(new CompartmentLabeler((c) -> "")).build();
		assertFalse(partition.isDegenerate());
	}

}
