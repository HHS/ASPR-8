package nucleus.testsupport.testplugin;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import nucleus.Plugin;
import nucleus.PluginData;
import util.errors.ContractException;
import util.wrappers.MutableInteger;

/**
 * A Testing utility class that will consume simulation output and includes a
 * method to retrieve output items by class type
 */
public class TestOutputConsumer implements Consumer<Object> {
	private List<Object> outputItems;


	public TestOutputConsumer() {
		this.outputItems = new ArrayList<>();
	}

	/**
	 * Handles all output from a simulation, but processes only
	 * TestScenarioReport items.
	 * 
	 * @throws ContractException             
	 * 
	 *             <li>{@linkplain TestError#NULL_OUTPUT_ITEM} if the obj is
	 *             null</li>
	 * 
	 */
	public void accept(Object obj) {
		if (obj == null) {
			throw new ContractException(TestError.NULL_OUTPUT_ITEM);
		}
		this.outputItems.add(obj);
	}

	/**
	 * Returns all outputs from a Simulation based on the Class Parameter in a
	 * map where the keys of the map are the output items and the values are the
	 * counts of how many times those items were encountered. The returned map
	 * contains no null keys or values.
	 * 
	 * @param <T>
	 *            This type is derived from the class parameter and also
	 *            determines the return type of this method.
	 * @param classRef
	 *            The class for which you want to get output items of
	 * 
	 * 
	 * @return - returns a {@link Map} containing the output items as keys and
	 *         the number of occurances as the value
	 */
	@SuppressWarnings("unchecked")
	public <T> Map<T, Integer> getOutputItems(Class<T> classRef) {
		Map<T, MutableInteger> sourceMap = new LinkedHashMap<>();
		Map<T, Integer> retMap = new LinkedHashMap<>();

		for (Object item : outputItems) {
			if (classRef.isAssignableFrom(item.getClass())) {
				T interestedItem = (T) item;
				sourceMap.putIfAbsent(interestedItem, new MutableInteger());
				sourceMap.get(interestedItem).increment();
			}
		}

		for (T item : sourceMap.keySet()) {
			retMap.put(item, sourceMap.get(item).getValue());
		}

		return retMap;
	}
	
	@SuppressWarnings("unchecked")
	public <T extends PluginData> Optional<T> getPluginData(Class<T> classRef) {

		Map<Plugin, Integer> outputItems = getOutputItems(Plugin.class);
		for (Plugin plugin : outputItems.keySet()) {
			for (PluginData pluginData : plugin.getPluginDatas()) {
				if (pluginData.getClass() == classRef) {
					return Optional.of((T) pluginData);
				}
			}
		}
		return Optional.empty();
	}

	
}
