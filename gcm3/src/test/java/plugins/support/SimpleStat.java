package plugins.support;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import net.jcip.annotations.NotThreadSafe;
import util.stats.Stat;

@NotThreadSafe
public class SimpleStat implements Stat {

	private List<Double> values = new ArrayList<>();

	public void add(double value) {
		values.add(value);
	}

	@Override
	public Optional<Double> getMean() {
		if (values.size() == 0) {
			return Optional.empty();
		}
		double result = 0;
		for (Double value : values) {
			result += value;
		}
		result /= values.size();
		return Optional.of(result);
	}

	@Override
	public Optional<Double> getVariance() {
		int n = values.size();
		if (n == 0) {
			return Optional.empty();
		}
		if (n == 1) {
			return Optional.of(0d);
		}
		double sum = 0;
		double sumSq = 0;
		for (Double value : values) {
			sum += value;
			sumSq += value * value;
		}

		double result = sumSq * n - sum * sum;
		result /= n;
		result /= n;

		return Optional.of(result);
	}

	@Override
	public Optional<Double> getStandardDeviation() {
		Optional<Double> optional = getVariance();
		if (optional.isPresent()) {
			return Optional.of(Math.sqrt(optional.get()));
		}
		return Optional.empty();
	}

	@Override
	public Optional<Double> getMax() {
		if (values.size() == 0) {
			return Optional.empty();
		}
		double result = values.get(0);
		for (Double value : values) {
			if (value > result) {
				result = value;
			}
		}
		return Optional.of(result);
	}

	@Override
	public Optional<Double> getMin() {
		if (values.size() == 0) {
			return Optional.empty();
		}
		double result = values.get(0);
		for (Double value : values) {
			if (value < result) {
				result = value;
			}
		}
		return Optional.of(result);
	}

	@Override
	public int size() {
		return values.size();
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("SimpleStats [getMean()=");
		builder.append(getMean());
		builder.append(", getVariance()=");
		builder.append(getVariance());
		builder.append(", getStandardDeviation()=");
		builder.append(getStandardDeviation());
		builder.append(", getMax()=");
		builder.append(getMax());
		builder.append(", getMin()=");
		builder.append(getMin());
		builder.append(", size()=");
		builder.append(size());
		builder.append("]");
		return builder.toString();
	}

}
