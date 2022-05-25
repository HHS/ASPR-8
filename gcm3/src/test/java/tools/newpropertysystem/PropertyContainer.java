package tools.newpropertysystem;

import java.util.HashMap;
import java.util.Map;

public class PropertyContainer {

    private Map<Property<?>, Map<Integer, Object>> values = new HashMap<>();

    public <T> void set(Property<T> property, int index, T value) {
        values.computeIfAbsent(property, x -> new HashMap<>()).put(index, value);
    }

    @SuppressWarnings("unchecked")
	public <T> T get(Property<T> property, int index) {
        return (T) values.computeIfAbsent(property, x -> new HashMap<>()).getOrDefault(index, property.defaultValue());
    }
}
