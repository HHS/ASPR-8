package manual.demo.trigger;

import java.util.LinkedHashMap;
import java.util.Map;

import net.jcip.annotations.Immutable;
import plugins.components.support.ComponentId;
import plugins.globals.support.GlobalComponentId;
import plugins.globals.support.GlobalPropertyId;
import plugins.properties.support.PropertyDefinition;
import plugins.properties.support.TimeTrackingPolicy;

@Immutable
public class TriggerContainer {

	public final static GlobalPropertyId TRIGGER_CONTAINER = new GlobalPropertyId() {
		@Override
		public String toString() {
			return "TRIGGER_CONTAINER";
		}
	};

	public final static PropertyDefinition getTriggerContainerPropertyDefinition() {
		return PropertyDefinition.builder().setDefaultValue(builder().build()).setPropertyValueMutability(false).setTimeTrackingPolicy(TimeTrackingPolicy.DO_NOT_TRACK_TIME).setType(TriggerContainer.class).build();
	}

	private final Map<GlobalComponentId, Object> map = new LinkedHashMap<>();

	@SuppressWarnings("unchecked")
	public <T> T getTrigger(ComponentId componentId) {
		return (T) map.get(componentId);
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {
		private TriggerContainer triggerContainer = new TriggerContainer();

		private Builder() {

		}

		public TriggerContainer build() {
			try {
				return triggerContainer;
			} finally {
				triggerContainer = new TriggerContainer();
			}
		}

		public Builder addTrigger(GlobalComponentId globalComponentId, Object trigger) {
			triggerContainer.map.put(globalComponentId, trigger);
			return this;
		}
	}

}
