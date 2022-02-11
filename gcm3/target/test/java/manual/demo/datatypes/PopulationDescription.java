package manual.demo.datatypes;

import java.util.ArrayList;
import java.util.List;

import net.jcip.annotations.Immutable;

@Immutable
public final class PopulationDescription {

	private final List<PopulationElement> populationElements;

	private PopulationDescription(List<PopulationElement> populationElements) {
		this.populationElements = populationElements;
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {
		List<PopulationElement> populationElements = new ArrayList<>();

		private Builder() {

		}

		public Builder addPopulationElement(PopulationElement populationElement) {
			populationElements.add(populationElement);
			return this;
		}

		public PopulationDescription build() {
			try {
				return new PopulationDescription(populationElements);
			} finally {
				populationElements = new ArrayList<>();
			}
		}
	}

	@Immutable
	public final static class PopulationElement {
		private final int age;
		private final String homeId;
		private final String schoolId;
		private final String workPlaceId;

		public int getAge() {
			return age;
		}

		public String getHomeId() {
			return homeId;
		}

		public String getSchoolId() {
			return schoolId;
		}

		public String getWorkPlaceId() {
			return workPlaceId;
		}

		public PopulationElement(int age, String homeId, String schoolId, String workPlaceId) {
			super();
			this.age = age;
			this.homeId = homeId;
			this.schoolId = schoolId;
			this.workPlaceId = workPlaceId;
		}

	}

	public List<PopulationElement> getPopulationElements() {
		return new ArrayList<>(populationElements);
	}
}
