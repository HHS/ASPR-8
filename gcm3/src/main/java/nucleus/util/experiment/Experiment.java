package nucleus.util.experiment;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import nucleus.Engine;
import util.TypeMap;

public final class Experiment {

	private static class Data {
		
		Function<TypeMap<InitialData>, Engine> engineFunction;

		List<Dimension> dimensions;

		List<Supplier<InitialDataBuilder>> initialDataBuilderSuppliers = new ArrayList<>();
		
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {
		private Data data = new Data();

		private Builder() {
		}

		public Experiment build() {
			try {
				return new Experiment(data);
			} finally {
				data = new Data();
			}
		}

		public Builder setEngineFunction(Function<TypeMap<InitialData>, Engine> engineFunction) {
			data.engineFunction = engineFunction;
			return this;
		}

		public Builder addDimension(Dimension dimension) {
			data.dimensions.add(dimension);
			return this;
		}

		public Builder addInitialDataSupplier(Supplier<InitialDataBuilder> initialDataSupplier) {
			data.initialDataBuilderSuppliers.add(initialDataSupplier);
			return this;
		}

	}

	private final Data data;

	private Experiment(Data data) {
		this.data = data;
	}

	public int getScenarioCount() {
		int result = 1;
		for (Dimension dimension : data.dimensions) {
			result *= dimension.getMemberGenerators().size();
		}
		return result;
	}

	public Scenario getScenario(int scenarioId) {
		TypeMap.Builder<InitialData> initialDataTypeMapBuilder = TypeMap.builder(InitialData.class);

		TypeMap.Builder<InitialDataBuilder> initialDataBuilderTypeMapBuilder = TypeMap.builder(InitialDataBuilder.class);
		for (Supplier<InitialDataBuilder> supplier : data.initialDataBuilderSuppliers) {
			InitialDataBuilder initialDataBuilder = supplier.get();
			initialDataBuilderTypeMapBuilder.add(initialDataBuilder);
		}
		TypeMap<InitialDataBuilder> initialDataBuilders = initialDataBuilderTypeMapBuilder.build();

		Map<String, String> dimensionValueMap = new LinkedHashMap<>();

		int modulus = 1;
		for (Dimension dimension : data.dimensions) {
			List<Function<TypeMap<InitialDataBuilder>, String>> memberGenerators = dimension.getMemberGenerators();
			int index = (scenarioId / modulus) % memberGenerators.size();
			Function<TypeMap<InitialDataBuilder>, String> memberGenerator = memberGenerators.get(index);
			String label = memberGenerator.apply(initialDataBuilders);
			dimensionValueMap.put(dimension.getName(), label);
			modulus *= memberGenerators.size();
		}

		TypeMap<InitialData> initialDataTypeMap = initialDataTypeMapBuilder.build();

		Engine engine = data.engineFunction.apply(initialDataTypeMap);

		return Scenario.builder().setEngine(engine).setId(scenarioId).setDimensionValueMap(null).build();
	}

}
