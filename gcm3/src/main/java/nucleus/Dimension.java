package nucleus;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import util.TypeMap;

public class Dimension {

	private static class Data {
		List<String> metaData = new ArrayList<>();
		List<Function<TypeMap<PluginDataBuilder>, List<String>>> points = new ArrayList<>();
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
		
		public Builder addPoint(Function<TypeMap<PluginDataBuilder>, List<String>> memberGenerator) {
			data.points.add(memberGenerator);
			return this;
		}
		public Builder addMetaDatum(String idValue) {
			data.metaData.add(idValue);
			return this;
		}
	}

	private Dimension(Data data) {
		this.data = data;
	}

	private final Data data;

	public List<String> getMetaData() {
		return new ArrayList<>(data.metaData);
	}

	public int size() {
		return data.points.size();
	}
		
	public Function<TypeMap<PluginDataBuilder>, List<String>> getPoint(int index) {
		return data.points.get(index);
	}

}
