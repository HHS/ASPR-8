package plugins.partitions.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;

import nucleus.Event;
import nucleus.SimulationContext;
import plugins.partitions.testsupport.PartitionsActionSupport;
import plugins.people.datamanagers.PeopleDataManager;
import plugins.people.support.PersonId;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestMethod;
import util.errors.ContractException;


@UnitTest(target = Filter.class)
public class AT_Filter {
	
	private static class LocalFilter extends Filter {

		private final Set<FilterSensitivity<?>> filterSensitivities = new LinkedHashSet<>();

		@SafeVarargs
		public LocalFilter(FilterSensitivity<Event>... filterSensitivities) {
			for (FilterSensitivity<Event> filterSensitivity : filterSensitivities) {
				this.filterSensitivities.add(filterSensitivity);
			}
		}

		@Override
		public boolean evaluate(SimulationContext context, PersonId personId) {
			return false;
		}

		@Override
		public Set<FilterSensitivity<?>> getFilterSensitivities() {
			return new LinkedHashSet<>(filterSensitivities);
		}

		@Override
		public void validate(SimulationContext context) {
			// do nothing

		}
	}

	private static Optional<PersonId> eventPredicate(SimulationContext context, Event event) {
		return Optional.of(new PersonId(4));
	}

	public void testConstructor() {

	}

	public void testValidate() {

	}

	public void testEvaluate() {

	}

	public void testGetFilterSensitivities() {
		
	}
	/**
	 * Tests {@link Filter#and(Filter)}
	 */
	@Test
	@UnitTestMethod(name = "and", args = { Filter.class })
	public void testAnd() {
		PartitionsActionSupport.testConsumer(100, 254308828477050611L, (c) -> {
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			/*
			 * Show that there are enough people in the simulation to make a
			 * valid test
			 */
			assertEquals(100,peopleDataManager.getPopulationCount());

			// create the filters
			Filter filter = Filter.allPeople().and(Filter.allPeople());
			for (PersonId personId : peopleDataManager.getPeople()) {
				assertTrue(filter.evaluate(c, personId));
			}

			filter = Filter.allPeople().and(Filter.noPeople());
			for (PersonId personId : peopleDataManager.getPeople()) {
				assertFalse(filter.evaluate(c, personId));
			}

			filter = Filter.noPeople().and(Filter.allPeople());
			for (PersonId personId : peopleDataManager.getPeople()) {
				assertFalse(filter.evaluate(c, personId));
			}

			filter = Filter.noPeople().and(Filter.noPeople());
			for (PersonId personId : peopleDataManager.getPeople()) {
				assertFalse(filter.evaluate(c, personId));
			}

			FilterSensitivity<Event> fsA = new FilterSensitivity<>(Event.class, AT_Filter::eventPredicate);
			FilterSensitivity<Event> fsB = new FilterSensitivity<>(Event.class, AT_Filter::eventPredicate);
			FilterSensitivity<Event> fsC = new FilterSensitivity<>(Event.class, AT_Filter::eventPredicate);
			FilterSensitivity<Event> fsD = new FilterSensitivity<>(Event.class, AT_Filter::eventPredicate);
			filter = new LocalFilter(fsA, fsB, fsC).and(new LocalFilter(fsA, fsD));

			Set<FilterSensitivity<?>> expectedFilterSensitivities = new LinkedHashSet<>();
			expectedFilterSensitivities.add(fsA);
			expectedFilterSensitivities.add(fsB);
			expectedFilterSensitivities.add(fsC);
			expectedFilterSensitivities.add(fsD);
			Set<FilterSensitivity<?>> actualFilterSensitivities = filter.getFilterSensitivities();
			assertEquals(expectedFilterSensitivities, actualFilterSensitivities);
			
			// precondition tests
			
			//if the filter is null
			ContractException contractException = assertThrows(ContractException.class, () -> Filter.allPeople().and(null));
			assertEquals(PartitionError.NULL_FILTER, contractException.getErrorType());
		});
		
	}

	/**
	 * Tests {@link Filter#or(Filter)}
	 */
	@Test
	@UnitTestMethod(name = "or", args = { Filter.class })
	public void testOr() {
		PartitionsActionSupport.testConsumer(100, 921279696119043098L, (c) -> {
			
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			
			/*
			 * Show that there are enough people in the simulation to make a
			 * valid test
			 */
			assertEquals(100,peopleDataManager.getPopulationCount());

			// create the filters
			Filter filter = Filter.allPeople().or(Filter.allPeople());
			for (PersonId personId : peopleDataManager.getPeople()) {
				assertTrue(filter.evaluate(c, personId));
			}

			filter = Filter.allPeople().or(Filter.noPeople());
			for (PersonId personId : peopleDataManager.getPeople()) {
				assertTrue(filter.evaluate(c, personId));
			}

			filter = Filter.noPeople().or(Filter.allPeople());
			for (PersonId personId : peopleDataManager.getPeople()) {
				assertTrue(filter.evaluate(c, personId));
			}

			filter = Filter.noPeople().or(Filter.noPeople());
			for (PersonId personId : peopleDataManager.getPeople()) {
				assertFalse(filter.evaluate(c, personId));
			}

			FilterSensitivity<Event> fsA = new FilterSensitivity<>(Event.class, AT_Filter::eventPredicate);
			FilterSensitivity<Event> fsB = new FilterSensitivity<>(Event.class, AT_Filter::eventPredicate);
			FilterSensitivity<Event> fsC = new FilterSensitivity<>(Event.class, AT_Filter::eventPredicate);
			FilterSensitivity<Event> fsD = new FilterSensitivity<>(Event.class, AT_Filter::eventPredicate);
			filter = new LocalFilter(fsA, fsB, fsC).or(new LocalFilter(fsA, fsD));

			Set<FilterSensitivity<?>> expectedFilterSensitivities = new LinkedHashSet<>();
			expectedFilterSensitivities.add(fsA);
			expectedFilterSensitivities.add(fsB);
			expectedFilterSensitivities.add(fsC);
			expectedFilterSensitivities.add(fsD);
			Set<FilterSensitivity<?>> actualFilterSensitivities = filter.getFilterSensitivities();
			assertEquals(expectedFilterSensitivities, actualFilterSensitivities);
			
			// precondition test
			
			//if the filter is null

			ContractException contractException = assertThrows(ContractException.class, () -> Filter.allPeople().or(null));
			assertEquals(PartitionError.NULL_FILTER, contractException.getErrorType());
			
		});
		
	}

	/**
	 * Tests {@link Filter#negate()}
	 */
	@Test
	@UnitTestMethod(name = "negate", args = {})
	public void testNegate() {
		PartitionsActionSupport.testConsumer(100, 4038710674336002107L, (c) -> {
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			/*
			 * Show that there are enough people in the simulation to make a
			 * valid test
			 */
			assertEquals(100,peopleDataManager.getPopulationCount());

			Filter filter = Filter.allPeople().negate();

			for (PersonId personId : peopleDataManager.getPeople()) {
				assertFalse(filter.evaluate(c, personId));
			}

			filter = Filter.noPeople().negate();
			for (PersonId personId : peopleDataManager.getPeople()) {
				assertTrue(filter.evaluate(c, personId));
			}

			assertEquals(filter.getFilterSensitivities().size(), 0);
		});
	}


	/**
	 * Tests {@link Filter#allPeople()}
	 */
	@Test
	@UnitTestMethod(name = "allPeople", args = {})
	public void testAllPeople() {
		PartitionsActionSupport.testConsumer(30, 847391904888351863L, (c) -> {
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			// show that the test is valid
			assertTrue(peopleDataManager.getPopulationCount() > 0);

			final Filter filter = Filter.allPeople();

			for (PersonId personId : peopleDataManager.getPeople()) {
				assertTrue(filter.evaluate(c, personId));
			}
			assertEquals(filter.getFilterSensitivities().size(), 0);
		});

	}

	/**
	 * Tests {@link Filter#noPeople()}
	 */
	@Test
	@UnitTestMethod(name = "noPeople", args = {})
	public void testNoPeople() {
		PartitionsActionSupport.testConsumer(100, 6400633994679307999L, (c) -> {
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			assertEquals(100,peopleDataManager.getPopulationCount());

			final Filter filter = Filter.noPeople();

			for (PersonId personId : peopleDataManager.getPeople()) {
				assertFalse(filter.evaluate(c, personId));
			}

			assertEquals(filter.getFilterSensitivities().size(), 0);
		});
		
	}
}
