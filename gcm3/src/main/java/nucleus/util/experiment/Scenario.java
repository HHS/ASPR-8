package nucleus.util.experiment;

import java.util.LinkedHashMap;
import java.util.Map;

import nucleus.Engine;

public final class Scenario {

	private static class Data {
		private int id;
		private Engine engine;
		private Map<String, String> dimensionValueMap = new LinkedHashMap<>();
	}
	
	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {
		
		private Builder() {
			
		}
		private Data data = new Data();

		public Scenario build() {
			try {
				return new Scenario(data);
			} finally {
				data = new Data();
			}
		}
		
		public Builder setEngine(Engine engine) {
			data.engine = engine;
			return this;
		}
		
		public Builder setId(int id) {
			data.id = id;
			return this;
		}
		
		public Builder setDimensionValueMap(Map<String, String> dimensionValueMap) {
			data.dimensionValueMap = new LinkedHashMap<>(dimensionValueMap);
			return this;
		}

	}

	private final Data data;

	private Scenario(Data data) {
		this.data = data;
	}

	public int getId() {
		return data.id;
	}

	public Engine getEngine() {
		return data.engine;
	}

	public Map<String, String> getDimensionValueMap() {
		return new LinkedHashMap<>(data.dimensionValueMap);
	}

}
