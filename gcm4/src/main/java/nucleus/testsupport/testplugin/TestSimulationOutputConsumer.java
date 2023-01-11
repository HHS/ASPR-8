package nucleus.testsupport.testplugin;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import util.wrappers.MutableInteger;

public class TestSimulationOutputConsumer implements Consumer<Object> {
    private List<Object> outputItems;
    private boolean isComplete = false;

    public TestSimulationOutputConsumer() {
        this.outputItems = new ArrayList<>();
    }

    public void accept(Object obj) {
        this.outputItems.add(obj);

        if(obj instanceof TestScenarioReport) {
            this.isComplete = ((TestScenarioReport) obj).isComplete();
        }
    }

    @SuppressWarnings("unchecked")
    public <T> Map<T, Integer> getOutputItems(Class<T> clazz) {
        Map<T, MutableInteger> sourceMap = new LinkedHashMap<>();
        Map<T, Integer> retMap = new LinkedHashMap<>();

        for (Object item : outputItems) {
            if (item.getClass().isAssignableFrom(clazz)) {
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
