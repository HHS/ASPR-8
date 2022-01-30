package nucleus.util.experiment;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import util.TypeMap;

public class Dimension {

	private static class Data {
		List<String> ids = new ArrayList<>();
		List<Function<TypeMap<InitialDataBuilder>, List<String>>> memberGenerators = new ArrayList<>();
	}

	public static Builder builder() {
		return new Builder();
	}
	
	public static class Builder {
		private Data data = new Data();

		private Builder() {
		}

		public Dimension build() {
			try {
				return new Dimension(data);
			} finally {
				data = new Data();
			}
		}
		
		public Builder addMemberGenerator(Function<TypeMap<InitialDataBuilder>, List<String>> memberGenerator) {
			data.memberGenerators.add(memberGenerator);
			return this;
		}
		public Builder addIdValue(String idValue) {
			data.ids.add(idValue);
			return this;
		}
	}

	private Dimension(Data data) {
		this.data = data;
	}

	private final Data data;

	public List<String> getIds() {
		return new ArrayList<>(data.ids);
	}

	public List<Function<TypeMap<InitialDataBuilder>, List<String>>> getMemberGenerators() {
		return new ArrayList<>(data.memberGenerators);
	}

}
