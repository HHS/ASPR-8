package plugins.gcm.experiment;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import plugins.gcm.experiment.output.OutputItemHandler;
import util.ContractException;

/**
 * Implementor of OutputItemManager.
 * 
 * @author Shawn Hatch
 *
 */

public final class OutputItemConsumerManager {

	private final Map<Class<?>, Set<OutputItemHandler>> handlerMap = new LinkedHashMap<>();
	private final List<OutputItemHandler> outputItemHandlers = new ArrayList<>();
	private ScenarioId scenarioId;
	private ReplicationId replicationId;

	public OutputItemConsumerManager(ScenarioId scenarioId, ReplicationId replicationId, List<OutputItemHandler> outputItemHandlers) {

		this.scenarioId = scenarioId;
		this.replicationId = replicationId;
		this.outputItemHandlers.addAll(outputItemHandlers);

		/*
		 * Map the handlers to the output item sub-types they handle.
		 */
		for (OutputItemHandler outputItemHandler : outputItemHandlers) {
			Set<Class<?>> handledClasses = outputItemHandler.getHandledClasses();
			for (Class<?> outputItemClass : handledClasses) {
				Set<OutputItemHandler> handlers = handlerMap.get(outputItemClass);
				if (handlers == null) {
					handlers = new LinkedHashSet<>();
					handlerMap.put(outputItemClass, handlers);
				}
				handlers.add(outputItemHandler);
			}
		}
		/*
		 * Inform each output item handler that the simulation has started.
		 */
		for (OutputItemHandler outputItemHandler : outputItemHandlers) {
			outputItemHandler.openSimulation(scenarioId, replicationId);
		}
	}

	public void resolveEvent(Object output) {
		if (output == null) {
			throw new ContractException(ExperimentError.NULL_OUTPUT_ITEM);
		}
		

		Set<OutputItemHandler> handlers = handlerMap.get(output.getClass());

		/*
		 * It may happen that the class of an output item do not explicitly
		 * match any handler, but that it compatible with that handler.
		 * 
		 * For example suppose handlerX lists OutputItemY class as a type it
		 * handles. When we encounter an instance of OutputItemZ that is a
		 * descendant of OutputItemY then we would want to extend the content of
		 * the handlerMap so that all OutputItemZ are mapped to handlerX.
		 */
		if (handlers == null) {
			handlers = new LinkedHashSet<>();
			for (Class<?> outputItemClass : handlerMap.keySet()) {
				if (outputItemClass.isAssignableFrom(output.getClass())) {
					handlers.addAll(handlerMap.get(outputItemClass));
				}
			}
			handlerMap.put(output.getClass(), handlers);
		}

		/*
		 * It is possible that the handlers set is empty. In that case the
		 * output item will be ignored.
		 */
		for (OutputItemHandler outputItemHandler : handlers) {
			outputItemHandler.handle(scenarioId, replicationId, output);
		}
	}

	public void close() {
		/*
		 * Let the output item handlers know that the simulation is closed
		 */
		for (OutputItemHandler outputItemHandler : outputItemHandlers) {
			outputItemHandler.closeSimulation(scenarioId, replicationId);
		}

	}

}
