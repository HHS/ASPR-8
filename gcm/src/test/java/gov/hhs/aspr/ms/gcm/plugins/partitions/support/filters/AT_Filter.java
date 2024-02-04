package gov.hhs.aspr.ms.gcm.plugins.partitions.support.filters;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.gcm.nucleus.Event;
import gov.hhs.aspr.ms.gcm.nucleus.testsupport.testplugin.TestSimulation;
import gov.hhs.aspr.ms.gcm.plugins.partitions.support.FilterSensitivity;
import gov.hhs.aspr.ms.gcm.plugins.partitions.support.PartitionError;
import gov.hhs.aspr.ms.gcm.plugins.partitions.support.PartitionsContext;
import gov.hhs.aspr.ms.gcm.plugins.partitions.testsupport.PartitionsTestPluginFactory;
import gov.hhs.aspr.ms.gcm.plugins.partitions.testsupport.PartitionsTestPluginFactory.Factory;
import gov.hhs.aspr.ms.gcm.plugins.partitions.testsupport.TestPartitionsContext;
import gov.hhs.aspr.ms.gcm.plugins.people.datamanagers.PeopleDataManager;
import gov.hhs.aspr.ms.gcm.plugins.people.support.PersonId;
import gov.hhs.aspr.ms.util.annotations.UnitTestMethod;
import gov.hhs.aspr.ms.util.errors.ContractException;

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
		public boolean evaluate(PartitionsContext partitionsContext, PersonId personId) {
			return false;
		}

		@Override
		public Set<FilterSensitivity<?>> getFilterSensitivities() {
			return new LinkedHashSet<>(filterSensitivities);
		}

		@Override
		public void validate(PartitionsContext partitionsContext) {
			// do nothing

		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((filterSensitivities == null) ? 0 : filterSensitivities.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (!(obj instanceof LocalFilter)) {
				return false;
			}
			LocalFilter other = (LocalFilter) obj;
			if (filterSensitivities == null) {
				if (other.filterSensitivities != null) {
					return false;
				}
			} else if (!filterSensitivities.equals(other.filterSensitivities)) {
				return false;
			}
			return true;
		}
		
		@Override
		public String toString() {
			return "LocalFilter[]";
		}		
	}

	private static Optional<PersonId> eventPredicate(PartitionsContext partitionsContext, Event event) {
		return Optional.of(new PersonId(4));
	}

	/**
	 * Tests {@link Filter#and(Filter)}
	 */
	@Test
	@UnitTestMethod(target = Filter.class, name = "and", args = { Filter.class })
	public void testAnd() {
		Factory factory = PartitionsTestPluginFactory.factory(100, 254308828477050611L, (c) -> {
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			/*
			 * Show that there are enough people in the simulation to make a
			 * valid test
			 */
			TestPartitionsContext testPartitionsContext = new TestPartitionsContext(c);
			
			assertEquals(100, peopleDataManager.getPopulationCount());

			// create the filters
			Filter filter = new TrueFilter().and(new TrueFilter());
			for (PersonId personId : peopleDataManager.getPeople()) {
				assertTrue(filter.evaluate(testPartitionsContext, personId));
			}

			filter = new TrueFilter().and(new FalseFilter());
			for (PersonId personId : peopleDataManager.getPeople()) {
				assertFalse(filter.evaluate(testPartitionsContext, personId));
			}

			filter = new FalseFilter().and(new TrueFilter());
			for (PersonId personId : peopleDataManager.getPeople()) {
				assertFalse(filter.evaluate(testPartitionsContext, personId));
			}

			filter = new FalseFilter().and(new FalseFilter());
			for (PersonId personId : peopleDataManager.getPeople()) {
				assertFalse(filter.evaluate(testPartitionsContext, personId));
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

			// if the filter is null
			ContractException contractException = assertThrows(ContractException.class, () -> new TrueFilter().and(null));
			assertEquals(PartitionError.NULL_FILTER, contractException.getErrorType());
		});
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

	}

	/**
	 * Tests {@link Filter#or(Filter)}
	 */
	@Test
	@UnitTestMethod(target = Filter.class, name = "or", args = { Filter.class })
	public void testOr() {
		Factory factory = PartitionsTestPluginFactory.factory(100, 921279696119043098L, (c) -> {

			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			TestPartitionsContext testPartitionsContext = new TestPartitionsContext(c);
			/*
			 * Show that there are enough people in the simulation to make a
			 * valid test
			 */
			assertEquals(100, peopleDataManager.getPopulationCount());

			// create the filters
			Filter filter = new TrueFilter().or(new TrueFilter());
			for (PersonId personId : peopleDataManager.getPeople()) {
				assertTrue(filter.evaluate(testPartitionsContext, personId));
			}

			filter = new TrueFilter().or(new FalseFilter());
			for (PersonId personId : peopleDataManager.getPeople()) {
				assertTrue(filter.evaluate(testPartitionsContext, personId));
			}

			filter = new FalseFilter().or(new TrueFilter());
			for (PersonId personId : peopleDataManager.getPeople()) {
				assertTrue(filter.evaluate(testPartitionsContext, personId));
			}

			filter = new FalseFilter().or(new FalseFilter());
			for (PersonId personId : peopleDataManager.getPeople()) {
				assertFalse(filter.evaluate(testPartitionsContext, personId));
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

			// if the filter is null

			ContractException contractException = assertThrows(ContractException.class, () -> new TrueFilter().or(null));
			assertEquals(PartitionError.NULL_FILTER, contractException.getErrorType());

		});
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

	}

	/**
	 * Tests {@link Filter#not()}
	 */
	@Test
	@UnitTestMethod(target = Filter.class, name = "not", args = {})
	public void testNot() {
		Factory factory = PartitionsTestPluginFactory.factory(100, 4038710674336002107L, (c) -> {
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			TestPartitionsContext testPartitionsContext = new TestPartitionsContext(c);
			/*
			 * Show that there are enough people in the simulation to make a
			 * valid test
			 */
			assertEquals(100, peopleDataManager.getPopulationCount());

			Filter filter = new TrueFilter().not();

			for (PersonId personId : peopleDataManager.getPeople()) {
				assertFalse(filter.evaluate(testPartitionsContext, personId));
			}

			filter = new FalseFilter().not();
			for (PersonId personId : peopleDataManager.getPeople()) {
				assertTrue(filter.evaluate(testPartitionsContext, personId));
			}

			assertEquals(filter.getFilterSensitivities().size(), 0);
		});
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
	}
}
