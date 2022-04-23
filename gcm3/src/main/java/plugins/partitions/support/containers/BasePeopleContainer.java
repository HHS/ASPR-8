package plugins.partitions.support.containers;

import java.util.List;

import org.apache.commons.math3.random.RandomGenerator;

import nucleus.SimulationContext;
import plugins.people.datamanagers.PeopleDataManager;
import plugins.people.support.PersonId;

/**
 * Implementor of PeopleContainer that acts as a dynamic switching mechanism
 * between the two lower-level PeopleContainer implementors
 * 
 * @author Shawn Hatch
 */
public class BasePeopleContainer implements PeopleContainer {
	/*
	 * Enumeration for the two ways that people are stored in a partition
	 */
	private static enum PeopleContainerMode {
		INTSET, TREE_BIT_SET_SLOW;
	}

	private static final int TREE_BIT_SET_SLOW_THRESHOLD = 28;

	private static final int INT_SET_THRESHOLD = 33;

	private PeopleContainerMode mode;

	private final PeopleDataManager peopleDataManager;

	private PeopleContainer internalPeopleContainer;

	public BasePeopleContainer(SimulationContext simulationContext) {
		this.peopleDataManager = simulationContext.getDataManager(PeopleDataManager.class);
		mode = PeopleContainerMode.INTSET;
		internalPeopleContainer = new IntSetPeopleContainer();

	}

	/*
	 * Switches the internal container between BooleanPeopleContainer and
	 * SetPeopleContainer as needed whenever the appropriate threshold has been
	 * crossed. If the size of the container is less than 0.5% of the total
	 * world population, then the SetPeopleContainer should be chosen. If the
	 * size of the container is greater than 1% of the total world population,
	 * then the BooleanPeopleContainer should be chosen. By setting two separate
	 * thresholds, we avoid modality thrash.
	 */
	private void determineMode(int size) {

		switch (mode) {

		case TREE_BIT_SET_SLOW:
			if (size <= peopleDataManager.getProjectedPopulationCount() / INT_SET_THRESHOLD) {
				mode = PeopleContainerMode.INTSET;
				List<PersonId> people = internalPeopleContainer.getPeople();
				internalPeopleContainer = new IntSetPeopleContainer();
				for (PersonId personId : people) {
					/*
					 * We use unsafe add as it faster and we know that the
					 * person id cannot already be contained
					 */
					internalPeopleContainer.unsafeAdd(personId);
				}
			}
			break;
		case INTSET:
			if (size >= peopleDataManager.getProjectedPopulationCount() / TREE_BIT_SET_SLOW_THRESHOLD) {
				mode = PeopleContainerMode.TREE_BIT_SET_SLOW;
				List<PersonId> people = internalPeopleContainer.getPeople();
				internalPeopleContainer = new TreeBitSetPeopleContainer(peopleDataManager);
				for (PersonId personId : people) {
					// we use the safe add as it is faster
					internalPeopleContainer.safeAdd(personId);
				}
			}
			break;
		default:
			throw new RuntimeException("unhandled mode " + mode);
		}
	}

	@Override
	public List<PersonId> getPeople() {
		return internalPeopleContainer.getPeople();
	}

	@Override
	public boolean safeAdd(PersonId personId) {
		boolean result = internalPeopleContainer.safeAdd(personId);
		if (result) {
			determineMode(size());
		}
		return result;
	}

	@Override
	public boolean unsafeAdd(PersonId personId) {
		boolean result = internalPeopleContainer.unsafeAdd(personId);
		if (result) {
			determineMode(size());
		}
		return result;
	}

	@Override
	public boolean remove(PersonId personId) {
		boolean result = internalPeopleContainer.remove(personId);
		determineMode(size());
		return result;
	}

	@Override
	public int size() {
		return internalPeopleContainer.size();
	}

	@Override
	public boolean contains(PersonId personId) {
		return internalPeopleContainer.contains(personId);
	}

	/*
	 * Returns a randomly selected person if this container has any people.
	 * Returns null otherwise.
	 */
	@Override
	public PersonId getRandomPersonId(RandomGenerator randomGenerator) {
		return internalPeopleContainer.getRandomPersonId(randomGenerator);
	}

}