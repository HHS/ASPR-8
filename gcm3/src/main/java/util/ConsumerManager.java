package util;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 * A convenience utility for avoiding the instance-of mechanism when mapping
 * events to handling logic
 * 
 * @author Shawn Hatch
 *
 */
public final class ConsumerManager {

	private final boolean requiresConsumption;

	public ConsumerManager(boolean requiresConsumption) {
		this.requiresConsumption = requiresConsumption;
	}

	private class MetaConsumer<T> {

		private final Consumer<T> consumer;

		public MetaConsumer(Consumer<T> consumer) {
			this.consumer = consumer;
		}

		@SuppressWarnings("unchecked")
		public void accept(Object input) {
			consumer.accept((T) input);
		}
	}

	private Map<Class<?>, MetaConsumer<?>> consumerMap = new LinkedHashMap<>();

	public <T> void setConsumer(Class<T> eventClass, Consumer<T> consumer) {
		MetaConsumer<T> metaConsumer = new MetaConsumer<>(consumer);
		consumerMap.put(eventClass, metaConsumer);
	}

	private String getClassName(Object input) {
		if (input == null) {
			return "null";
		}
		return input.getClass().getCanonicalName();
	}

	public boolean accept(Object input) {

		if (input == null) {
			throw new RuntimeException("null input");
		}
		MetaConsumer<?> metaConsumer = consumerMap.get(input.getClass());
		if (metaConsumer == null) {
			Set<Class<?>> matchingKeys = new LinkedHashSet<>();
			for (Class<?> x : consumerMap.keySet()) {
				if (x.isAssignableFrom(input.getClass())) {
					matchingKeys.add(x);
				}
			}

			switch (matchingKeys.size()) {
			case 0:
				if (requiresConsumption) {
					throw new RuntimeException("no consumers for " + getClassName(input));
				}
				break;
			case 1:
				metaConsumer = consumerMap.get(matchingKeys.iterator().next());
				consumerMap.put(input.getClass(), metaConsumer);
				break;
			default:
				throw new RuntimeException("multiple consumers for " + getClassName(input));
			}
		}

		if (metaConsumer != null) {
			metaConsumer.accept(input);
			return true;
		}
		return false;

	}

	@SuppressWarnings("unchecked")
	public <T> Set<Class<? extends T>> getHandledTypes(Class<T> t) {
		Set<Class<? extends T>> result = new LinkedHashSet<>();
		for (Class<?> x : consumerMap.keySet()) {
			if (t.isAssignableFrom(x)) {
				result.add((Class<T>) x);
			}
		}
		return result;
	}

}
