package plugins.reports.testsupport;

import java.util.LinkedHashMap;
import java.util.Map;

import nucleus.ExperimentContext;
import plugins.reports.support.ReportItem;
import util.wrappers.MutableInteger;

/**
 * Output consumer of report items that counts the number of times each report
 * item is added. Intended for experiment-level test support where report items
 * are collected from a simulation execution in one instance of this container
 * while expected report items are collected in a separate instance.
 */
public final class TestReportItemOutputConsumer {
	private Map<Integer, Map<ReportItem, MutableInteger>> reportItems = new LinkedHashMap<>();

	/**
	 * Stores the {@link ReportItem} output, keep counts on duplicates.
	 * 
	 * @throws RuntimeException
	 *             if the input is not a {@link ReportItem}
	 */

	private void handleReportItem(ExperimentContext experimentContext, Integer scenarioId, ReportItem reportItem) {
		Map<ReportItem, MutableInteger> map = reportItems.get(scenarioId);
		if (map == null) {
			map = new LinkedHashMap<>();
			reportItems.put(scenarioId, map);
		}
		MutableInteger counter = map.get(reportItem);
		if (counter == null) {
			counter = new MutableInteger();
			map.put(reportItem, counter);
		}
		counter.increment();
	}

	/**
	 * Initialize this output consumer. The output consumer registers for
	 * ReportItem handling.
	 */
	public synchronized void init(ExperimentContext experimentContext) {
		experimentContext.subscribeToOutput(ReportItem.class, this::handleReportItem);
	}

	/**
	 * Returns a Map from scenario id to a Map from report item to an Integer
	 * count of the number of times that report item was received by this output
	 * consumer.
	 */
	public Map<Integer, Map<ReportItem, Integer>> getReportItems() {
		Map<Integer, Map<ReportItem, Integer>> result = new LinkedHashMap<>();
		for (Integer sceanarioId : reportItems.keySet()) {
			Map<ReportItem, MutableInteger> sourceMap = reportItems.get(sceanarioId);
			Map<ReportItem, Integer> destinationMap = new LinkedHashMap<>();
			result.put(sceanarioId, destinationMap);
			for (ReportItem reportItem : sourceMap.keySet()) {
				MutableInteger mutableInteger = sourceMap.get(reportItem);
				destinationMap.put(reportItem, mutableInteger.getValue());
			}
		}
		return result;
	}
}