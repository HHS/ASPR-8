package plugins.partitions.support;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import net.jcip.annotations.Immutable;
import net.jcip.annotations.NotThreadSafe;

/**
 * A {@linkplain Partition} is the general description of a partitioning of the
 * people contained in the simulation. It is composed of a filter and various
 * functions that map values associated with each person to labels. The space
 * formed by the labels forms the formal partition. Partitions significantly
 * reduce the runtime required to sample/select people from the simulation who
 * meet some set of criteria.
 * 
 * This class functions as a description of a population partition and is
 * immutable. However, the implementation of the partition within the simulation
 * is dynamic and the contents(people) of the partition remain consistent with
 * the filter and label mapping functions as people change their properties,
 * group associations, resources, regions and compartments.
 * 
 * Partitions are built by the modeler via the supplied Builder class.
 * Partitions may be added and removed from the simulation and are identified by
 * a unique identifier. Only the Component that adds a partition may remove that
 * partition.
 * 
 * The filter: The role of the filter supplied to the partition is simply to
 * determine which people will be included in the partition's cells. If no
 * filter is supplied, the resulting partition will include all people. For
 * example, suppose there is a Boolean person property IS_VACCINTATED. If the
 * filter is [IS_VACCINATED EQUALS == FALSE] then only people who had not been
 * vaccinated would be included in the cells of the partition.
 * 
 * The labeling functions: The labeling functions serve to group people into the
 * cells of the partition. For example, suppose their are numerous regions in
 * the simulation with each representing a single census tract. The modeler may
 * wish to group these regions by state and would thus provide a function that
 * accepts a region and returns a state name as the label. Labels are not
 * required to be of any particular type or even to be of the same type for any
 * particular function. In this example, the modeler could create an enumeration
 * composed of values for each of the states and territories(ALABAMA, ALASKA,
 * ...) and would thus return the associated enumeration member as the label
 * value.
 * 
 * The inclusion of multiple labeling functions serves to refine the cells of
 * the partition. For example, suppose that there is a person property for the
 * (Integer) age of each person. The modeler may want to group people under age
 * 30 as "YOUNG" and those older as "OLD". Combined with the previous region
 * labeling function, the partition will have cells such as [UTAH,OLD],
 * [OHIO,YOUNG], etc.
 * 
 * Thus to retrieve or randomly sample from the simulation those people who are
 * unvaccinated, live in Maryland, and are below the age of 30 the modeler
 * queries the environment with the the id of the partition and the label values
 * [MARYLAND,YOUNG].
 * 
 * When implementing a supplied labeling function, the modeler must take some
 * care to only consider the inputs of the function and to guarantee the
 * stability of the return value. For example, in the age labeling function
 * above, the function will receive an Integer (such as 35) and must always
 * return the same label(OLD) in every invocation of the function. Without this
 * the partition will not function correctly.
 * 
 * @author Shawn Hatch
 *
 */
@Immutable
public final class Partition {

	/**
	 * Returns a new Builder instance
	 */
	public static Builder builder() {
		return new Builder();
	}

	@NotThreadSafe
	/**
	 * Standard builder class for partitions. All inputs are optional.
	 * 
	 * @author Shawn Hatch
	 *
	 */
	public static class Builder {

		private Scaffold scaffold = new Scaffold();

		private Builder() {
		}

		/**
		 * Returns the {@linkplain Partition} formed from the inputs collected
		 * by this builder and resets the state of the builder to empty.
		 */
		public Partition build() {
			try {
				return new Partition(scaffold);
			} finally {
				scaffold = new Scaffold();
			}
		}

		/**
		 * Adds a labeler.
		 */
		public Builder addLabeler(Labeler labeler) {
			scaffold.labelers.add(labeler);
			return this;
		}

		/**
		 * Sets the filter for the {@linkplain Partition}. If no filter is
		 * provided, a default filter that accepts all people is used instead.
		 */
		public Builder setFilter(Filter filter) {
			scaffold.filter = filter;
			return this;
		}

		/**
		 * Set the retention policy for derived partition cell keys for people
		 */
		public Builder setRetainPersonKeys(boolean retainPersonKeys) {
			scaffold.retainPersonKeys = retainPersonKeys;
			return this;
		}

	}

	private Partition(Scaffold scaffold) {
		this.labelers = scaffold.labelers;
		this.filter = scaffold.filter;
		this.retainPersonKeys = scaffold.retainPersonKeys;
	}

	/**
	 * Returns true if and only if the {@linkplain Partition} contains no
	 * labeling functions.
	 */
	public boolean isDegenerate() {
		return labelers.isEmpty();
	}

	/**
	 * Returns true if and only if partition cell keys should be retained by the
	 * partition for faster performance at the cost of higher memory usage.
	 */
	public boolean retainPersonKeys() {
		return retainPersonKeys;
	}

	private static class Scaffold {
		private boolean retainPersonKeys = true;

		private Set<Labeler> labelers = new LinkedHashSet<>();

		private Filter filter;
	}

	private final Set<Labeler> labelers;

	private final Filter filter;

	private final boolean retainPersonKeys;

	/**
	 * Returns the filter contained in this {@linkplain Partition}
	 */
	public Optional<Filter> getFilter() {
		return Optional.ofNullable(filter);
	}

	public Set<Labeler> getLabelers() {
		return new LinkedHashSet<>(labelers);
	}

}