package nucleus.testsupport.testplugin;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import util.errors.ContractException;
import util.wrappers.MutableInteger;

public class TestSimulationOutputConsumer implements Consumer<Object> {
    private List<Object> outputItems;
    private boolean isComplete = false;

    public TestSimulationOutputConsumer() {
        this.outputItems = new ArrayList<>();
    }

    /**
	 * Handles all output from a simulation, but processes only
	 * TestScenarioReport items.
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain TestError#DUPLICATE_TEST_SCENARIO_REPORTS} if
	 *             multiple TestScenarioReport items are received</li>
	 */
    public void accept(Object obj) {
        this.outputItems.add(obj);

        if(obj instanceof TestScenarioReport) {
            if(this.isComplete) {
                throw new ContractException(TestError.DUPLICATE_TEST_SCENARIO_REPORTS);
            }
            this.isComplete = ((TestScenarioReport) obj).isComplete();
        }
    }

    /**
	 * Returns all outputs from a Simulation based on the Class Parameter.
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

    public boolean isComplete() {
        return this.isComplete;
    }
}
