package plugins.reports.testsupport;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

import plugins.reports.support.ReportItem;
import util.MutableInteger;

/**
 * Output consumer of report items that counts the number of times each report
 * item is added. Intended for test support where report items are collected
 * from a simulation execution in one instance of this container while expected
 * report items are collected in a separate instance. The two instances are then
 * tested equality.
 */
public final class TestReportItemOutputConsumer implements Consumer<Object> {
	private Map<ReportItem, MutableInteger> reportItems = new LinkedHashMap<>();

	/**
	 * Returns the report items consumed by this consumer listed in the order of
	 * insertion on separate lines. Each line is formed of the string
	 * representation of the report item followed by a tab and then by count of
	 * times that report item was consumed.
	 * 
	 * <pre>
	 * TestReportItemOutputConsumer [reportItems=
	 * reportItem tab count
	 * reportItem tab count
	 * reportItem tab count
	 * reportItem tab count
	 * ...
	 * ]
	 * </pre>
	 */
	@Override
	public String toString() {
		String lineSeparator = System.getProperty("line.separator");

		StringBuilder builder = new StringBuilder();
		builder.append("TestReportItemOutputConsumer [reportItems=");

		builder.append(lineSeparator);

		for (ReportItem reportItem : reportItems.keySet()) {
			builder.append(reportItem);
			builder.append("\t");
			MutableInteger mutableInteger = reportItems.get(reportItem);
			builder.append("count = ");
			builder.append(mutableInteger.getValue());
			builder.append(lineSeparator);
		}
		builder.append("]");
		return builder.toString();
	}
	
	public String toValueStrings() {
		String lineSeparator = System.getProperty("line.separator");

		StringBuilder builder = new StringBuilder();
		

		

		for (ReportItem reportItem : reportItems.keySet()) {
			int size = reportItem.size();
			for(int i = 0;i<size;i++) {
				builder.append(reportItem.getValue(i));
				builder.append("\t");
			}
			MutableInteger mutableInteger = reportItems.get(reportItem);
			builder.append(mutableInteger.getValue());
			builder.append(lineSeparator);
		}
		
		return builder.toString();
	}

	/**
	 * Stores the {@link ReportItem} output, keep counts on duplicates.
	 * 
	 * @throws RuntimeException
	 *             if the input is not a {@link ReportItem}
	 */
	@Override
	public void accept(Object o) {
		if (o instanceof ReportItem) {
			ReportItem reportItem = (ReportItem) o;
			MutableInteger counter = reportItems.get(reportItem);
			if (counter == null) {
				counter = new MutableInteger();
				reportItems.put(reportItem, counter);
			}
			counter.increment();
		} else {
			throw new RuntimeException("unexpected output");
		}

	}

	/**
	 * Standard implementation of hash code that is order independent on inputs
	 * to this container.
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((reportItems == null) ? 0 : reportItems.hashCode());
		return result;
	}

	/**
	 * Standard implementation of equality that is order independent on inputs
	 * to this container. Two output consumers are equal if and only if they
	 * contain the same report items with the same associated counts.
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof TestReportItemOutputConsumer)) {
			return false;
		}
		TestReportItemOutputConsumer other = (TestReportItemOutputConsumer) obj;
		if (reportItems == null) {
			if (other.reportItems != null) {
				return false;
			}
		} else if (!reportItems.equals(other.reportItems)) {
			return false;
		}
		return true;
	}

	/**
	 * Returns the difference between this and other
	 */
	public Map<ReportItem, MutableInteger> diff(TestReportItemOutputConsumer other){
		Map<ReportItem, MutableInteger> result = new LinkedHashMap<>();
		for(ReportItem reportItem : this.reportItems.keySet()) {
			int value = this.reportItems.get(reportItem).getValue();
			MutableInteger m = new MutableInteger(value);
			result.put(reportItem, m);
		}

		for(ReportItem reportItem : other.reportItems.keySet()) {
			int value = other.reportItems.get(reportItem).getValue();
			MutableInteger m;
			if(result.containsKey(reportItem)) {
				m = result.get(reportItem);
			}else {
				m = new MutableInteger();
				result.put(reportItem, m);
			}
			m.decrement(value);
		}
		return result;
	}
}