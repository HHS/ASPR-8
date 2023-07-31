package nucleus;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.apache.commons.math3.util.FastMath;

import net.jcip.annotations.NotThreadSafe;
import util.errors.ContractException;
import util.graph.Graph;
import util.graph.GraphDepthEvaluator;
import util.graph.Graphs;
import util.graph.MutableGraph;

/**
 * An instance of the Simulation orchestrates the execution of a scenario from a
 * set of contributed plugins.
 * 
 * Plugins are loaded primarily based on the directed acyclic graph implied by
 * their dependencies and then secondarily on the order in which the plugins
 * were added to the experiment or simulation.
 * 
 * Each plugin contributes an initialization behavior that adds actors and data
 * managers to the simulation at simulation startup. The data managers are
 * initialized in the order they are added to the simulation. Actor
 * initialization then follows in a similar order.
 * 
 * After initialization is over, time flows based on the execution of planning.
 * Plans are collected from both actors and data managers. When no more plans
 * remain, the simulation halts.
 * 
 * 
 * @author Shawn Hatch
 *
 */
@NotThreadSafe
public class Simulation {

	private class PluginContextImpl implements PluginContext {

		@Override
		public void addDataManager(DataManager dataManager) {

			if (focalPluginId == null) {
				throw new ContractException(NucleusError.PLUGIN_INITIALIZATION_CLOSED);
			}

			if (dataManager == null) {
				throw new ContractException(NucleusError.NULL_DATA_MANAGER);
			}

			if (baseClassToDataManagerMap.containsKey(dataManager.getClass())) {
				throw new ContractException(NucleusError.DUPLICATE_DATA_MANAGER_TYPE, dataManager.getClass());
			}

			DataManagerId dataManagerId = new DataManagerId(masterDataManagerIndex++);
			DataManagerContext dataManagerContext = new DataManagerContextImpl(Simulation.this, dataManagerId);
			dataManagerIdToContextMap.put(dataManagerId, dataManagerContext);
			baseClassToDataManagerMap.put(dataManager.getClass(), dataManager);
			dataManagerIdToDataManagerMap.put(dataManagerId, dataManager);

		}

		@Override
		public ActorId addActor(Consumer<ActorContext> consumer) {

			if (consumer == null) {
				throw new ContractException(NucleusError.NULL_ACTOR_CONTEXT_CONSUMER);
			}

			if (focalPluginId == null) {
				throw new ContractException(NucleusError.PLUGIN_INITIALIZATION_CLOSED);
			}

			// assign an actorId
			ActorId actorId = new ActorId(actorIds.size());
			actorIds.add(actorId);

			// add the actor's initialization to the actor queue
			final ActorContentRec actorContentRec = new ActorContentRec();
			actorContentRec.actorId = actorId;
			actorContentRec.plan = consumer;
			actorQueue.add(actorContentRec);

			return actorId;

		}

		@SuppressWarnings("unchecked")
		@Override

		public <T extends PluginData> T getPluginData(Class<T> pluginDataClass) {
			if (pluginDataClass == null) {
				throw new ContractException(NucleusError.NULL_PLUGIN_DATA_CLASS);
			}

			PluginData pluginData = workingPluginDataMap.get(pluginDataClass);
			if (pluginData == null) {
				List<Class<?>> candidates = new ArrayList<>();
				for (Class<?> c : basePluginDataMap.keySet()) {
					if (pluginDataClass.isAssignableFrom(c)) {
						candidates.add(c);
					}
				}
				if (candidates.size() > 1) {
					throw new ContractException(NucleusError.AMBIGUOUS_PLUGIN_DATA_CLASS);
				}
				if (candidates.size() == 1) {
					pluginData = basePluginDataMap.get(candidates.get(0));
					workingPluginDataMap.put(pluginDataClass, pluginData);
				}
			}
			if (pluginData == null) {
				throw new ContractException(NucleusError.UNKNOWN_PLUGIN_DATA_CLASS);
			}

			return (T) pluginData;
		}

	}

	private class ActorContextImpl implements ActorContext {

		@Override
		public void addPlan(final Consumer<ActorContext> plan, final double planTime) {
			addActorPlan(plan, planTime, true, null);
		}

		@Override
		public void addKeyedPlan(final Consumer<ActorContext> plan, final double planTime, final Object key) {
			validatePlanKeyNotNull(key);
			validateActorPlanKeyNotDuplicate(key);
			addActorPlan(plan, planTime, true, key);
		}

		@Override
		public void addPassivePlan(final Consumer<ActorContext> plan, final double planTime) {
			addActorPlan(plan, planTime, false, null);
		}

		@Override
		public void addPassiveKeyedPlan(final Consumer<ActorContext> plan, final double planTime, final Object key) {
			validatePlanKeyNotNull(key);
			validateActorPlanKeyNotDuplicate(key);
			addActorPlan(plan, planTime, false, key);
		}

		@Override
		public boolean actorExists(final ActorId actorId) {
			return Simulation.this.actorExists(actorId);
		}

		@Override
		public ActorId getActorId() {
			return focalActorId;
		}

		@Override
		public <T extends DataManager> T getDataManager(Class<T> dataManagerClass) {
			return Simulation.this.getDataManager(dataManagerClass);
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T extends Consumer<ActorContext>> Optional<T> getPlan(final Object key) {
			return (Optional<T>) Simulation.this.getActorPlan(key);
		}

		@Override
		public List<Object> getPlanKeys() {
			return Simulation.this.getActorPlanKeys();
		}

		@Override
		public Optional<Double> getPlanTime(final Object key) {
			return Simulation.this.getActorPlanTime(key);
		}

		@Override
		public double getTime() {
			return time;
		}

		@Override
		public void halt() {
			Simulation.this.halt();
		}

		@Override
		public <T> Optional<T> removePlan(final Object key) {
			return Simulation.this.removeActorPlan(key);
		}

		@Override
		public void releaseOutput(Object output) {
			Simulation.this.releaseOutput(output);

		}

		@Override
		public void releaseEvent(final Event event) {
			Simulation.this.releaseEvent(event);

		}

		@Override
		public <T extends Event> void subscribe(EventLabel<T> eventLabel, BiConsumer<ActorContext, T> eventConsumer) {
			Simulation.this.subscribeActorToEventByLabel(eventLabel, eventConsumer);
		}

		@Override
		public <T extends Event> void subscribe(Class<T> eventClass, BiConsumer<ActorContext, T> eventConsumer) {
			Simulation.this.subscribeActorToEventByClass(eventClass, eventConsumer);
		}

		@Override
		public <T extends Event> void unsubscribe(EventLabel<T> eventLabel) {
			Simulation.this.unsubscribeActorFromEventByLabel(eventLabel);
		}

		@Override
		public <T extends Event> void addEventLabeler(EventLabeler<T> eventLabeler) {
			Simulation.this.addEventLabeler(eventLabeler);
		}

		@Override
		public void subscribeToSimulationClose(Consumer<ActorContext> consumer) {
			subscribeActorToSimulationClose(consumer);
		}

		@Override
		public boolean subscribersExist(Class<? extends Event> eventClass) {
			return Simulation.this.subscribersExistForEvent(eventClass);
		}

		@Override
		public ActorId addActor(Consumer<ActorContext> consumer) {
			return Simulation.this.addActor(consumer);
		}

		@Override
		public void removeActor(ActorId actorId) {
			Simulation.this.removeActor(actorId);
		}

		@Override
		public <T extends Event> void unsubscribe(Class<T> eventClass) {
			unSubscribeActorFromEventByClass(eventClass);
		}

	}

	private static final String LINE_SEPARATOR = System.getProperty("line.separator");

	private static class PlanRec {

		private Planner planner;
		private double time;
		private long arrivalId;
		private boolean isActive;

		private Consumer<ActorContext> actorPlan;
		private ActorId actorId;

		private Consumer<DataManagerContext> dataManagerPlan;
		private DataManagerId dataManagerId;

		private Object key;

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();

			builder.append("time = ");
			builder.append(time);
			builder.append(LINE_SEPARATOR);

			builder.append("arrivalId = ");
			builder.append(arrivalId);
			builder.append(LINE_SEPARATOR);

			builder.append("key = ");
			builder.append(key);
			builder.append(LINE_SEPARATOR);

			builder.append(planner);
			builder.append(" = ");
			switch (planner) {
			case ACTOR:
				builder.append(actorId);
				break;
			case DATA_MANAGER:
				builder.append(dataManagerId);
				break;
			default:				
				throw new RuntimeException("unhandled planner case");

			}
			builder.append(LINE_SEPARATOR);
			builder.append(LINE_SEPARATOR);

			return builder.toString();
		}
	}

	public static class Builder {

		private Data data = new Data();

		private Builder() {

		}

		/**
		 * Sets the output consumer for the simulation. Tolerates null.
		 */
		public Builder setOutputConsumer(Consumer<Object> outputConsumer) {
			data.outputConsumer = outputConsumer;
			return this;
		}

		/**
		 * Adds a plugin initializer to this builder for inclusion in the
		 * simulation
		 * 
		 * @throws ContractException
		 *             <li>{@link NucleusError#NULL_PLUGIN_INITIALIZER} if the
		 *             plugin intializer is null
		 * 
		 */

		public Builder addPlugin(Plugin plugin) {
			if (plugin == null) {
				throw new ContractException(NucleusError.NULL_PLUGIN);
			}
			data.plugins.add(plugin);
			return this;
		}

		/**
		 * Returns an Engine instance that is initialized with the plugins and
		 * output consumer collected by this builder.
		 */
		public Simulation build() {
			try {
				return new Simulation(data);
			} finally {
				data = new Data();
			}
		}
	}

	/*
	 * Defines the type of actor that has created a plan. THE ORDER OF THIS ENUM
	 * IS CRITICAL TO THE FUNCTION OF THE SIMULATION!
	 */
	private static enum Planner {
		DATA_MANAGER, ACTOR
	}

	private static class DataManagerContextImpl implements DataManagerContext {
		private final DataManagerId dataManagerId;
		private final Simulation simulation;

		private DataManagerContextImpl(Simulation simulation, DataManagerId dataManagerId) {
			this.simulation = simulation;
			this.dataManagerId = dataManagerId;
		}

		@Override
		public ActorId addActor(Consumer<ActorContext> consumer) {
			return simulation.addActor(consumer);
		}

		@Override
		public void addPlan(final Consumer<DataManagerContext> plan, final double planTime) {
			simulation.addDataManagerPlan(dataManagerId, plan, planTime, true, null);
		}

		@Override
		public void addKeyedPlan(final Consumer<DataManagerContext> plan, final double planTime, final Object key) {
			simulation.validatePlanKeyNotNull(key);
			simulation.validateDataManagerPlanKeyNotDuplicate(dataManagerId, key);
			simulation.addDataManagerPlan(dataManagerId, plan, planTime, true, key);
		}

		@Override
		public void addPassivePlan(final Consumer<DataManagerContext> plan, final double planTime) {
			simulation.addDataManagerPlan(dataManagerId, plan, planTime, false, null);
		}

		@Override
		public void addPassiveKeyedPlan(final Consumer<DataManagerContext> plan, final double planTime, final Object key) {
			simulation.validatePlanKeyNotNull(key);
			simulation.validateDataManagerPlanKeyNotDuplicate(dataManagerId, key);
			simulation.addDataManagerPlan(dataManagerId, plan, planTime, false, key);
		}

		@Override
		public boolean actorExists(final ActorId actorId) {
			return simulation.actorExists(actorId);
		}

		@Override
		public <T extends DataManager> T getDataManager(Class<T> dataManagerClass) {
			return simulation.getDataManager(dataManagerClass);
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T extends Consumer<DataManagerContext>> Optional<T> getPlan(final Object key) {
			return (Optional<T>) simulation.getDataManagerPlan(dataManagerId, key);
		}

		@Override
		public List<Object> getPlanKeys() {
			return simulation.getDataManagerPlanKeys(dataManagerId);
		}

		@Override
		public Optional<Double> getPlanTime(final Object key) {
			return simulation.getDataManagerPlanTime(dataManagerId, key);
		}

		@Override
		public double getTime() {
			return simulation.time;
		}

		@Override
		public void halt() {
			simulation.halt();
		}

		@Override
		public void releaseEvent(final Event event) {
			simulation.releaseEvent(event);

		}

		@Override
		public void removeActor(final ActorId actorId) {
			simulation.removeActor(actorId);
		}

		@Override
		public <T> Optional<T> removePlan(final Object key) {
			return simulation.removeDataManagerPlan(dataManagerId, key);
		}

		@Override
		public void releaseOutput(Object output) {
			simulation.releaseOutput(output);
		}

		@Override
		public void unSubscribe(Class<? extends Event> eventClass) {
			simulation.unSubscribeDataManagerFromEvent(dataManagerId, eventClass);
		}

		@Override
		public <T extends Event> void addEventLabeler(EventLabeler<T> eventLabeler) {
			simulation.addEventLabeler(eventLabeler);
		}

		@Override
		public boolean subscribersExist(Class<? extends Event> eventClass) {
			return simulation.subscribersExistForEvent(eventClass);
		}

		@Override
		public <T extends Event> void subscribePostOrder(Class<T> eventClass, BiConsumer<DataManagerContext, T> eventConsumer) {
			simulation.subscribeDataManagerToEventPostPhase(dataManagerId, eventClass, eventConsumer);
		}

		@Override
		public <T extends Event> void subscribe(Class<T> eventClass, BiConsumer<DataManagerContext, T> eventConsumer) {
			simulation.subscribeDataManagerToEventExecutionPhase(dataManagerId, eventClass, eventConsumer);
		}

		@Override
		public DataManagerId getDataManagerId() {
			return dataManagerId;
		}

		@Override
		public void subscribeToSimulationClose(Consumer<DataManagerContext> consumer) {
			simulation.subscribeDataManagerToSimulationClose(dataManagerId, consumer);
		}

	}

	private static class Data {
		private List<Plugin> plugins = new ArrayList<>();
		private Consumer<Object> outputConsumer;
	}

	/**
	 * Returns a reusable EngineBuilder instance
	 */
	public static Builder builder() {
		return new Builder();
	}

	private final Comparator<PlanRec> futureComparable = new Comparator<PlanRec>() {

		@Override
		public int compare(final PlanRec plannedEvent1, final PlanRec plannedEvent2) {
			int result = Double.compare(plannedEvent1.time, plannedEvent2.time);
			if (result == 0) {
				result = plannedEvent1.planner.compareTo(plannedEvent2.planner);
				if (result == 0) {
					result = Long.compare(plannedEvent1.arrivalId, plannedEvent2.arrivalId);
				}
			}
			return result;
		}
	};

	// planning
	private long masterPlanningArrivalId;
	private double time;
	private boolean processEvents = true;
	private int activePlanCount;
	private final PriorityQueue<PlanRec> planningQueue = new PriorityQueue<>(futureComparable);

	// actors

	private final Map<ActorId, Consumer<ActorContext>> simulationCloseActorCallbacks = new LinkedHashMap<>();

	private final Map<DataManagerId, Consumer<DataManagerContext>> simulationCloseDataManagerCallbacks = new LinkedHashMap<>();

	private boolean started;

	private final PluginContext pluginContext = new PluginContextImpl();

	private PluginId focalPluginId;

	private final Map<Class<?>, PluginData> basePluginDataMap = new LinkedHashMap<>();
	private final Map<Class<?>, PluginData> workingPluginDataMap = new LinkedHashMap<>();

	private final Data data;

	private Simulation(Data data) {
		this.data = data;
	}

	private void validateActorPlan(final Consumer<ActorContext> plan) {
		if (plan == null) {
			throw new ContractException(NucleusError.NULL_PLAN);
		}
	}

	private void validateDataManagerPlan(final Consumer<DataManagerContext> plan) {
		if (plan == null) {
			throw new ContractException(NucleusError.NULL_PLAN);
		}
	}

	private void addActorPlan(final Consumer<ActorContext> plan, final double time, final boolean isActivePlan, final Object key) {

		validatePlanTime(time);
		validateActorPlan(plan);

		final PlanRec planRec = new PlanRec();
		planRec.isActive = isActivePlan;
		planRec.arrivalId = masterPlanningArrivalId++;
		planRec.planner = Planner.ACTOR;
		planRec.time = FastMath.max(time, this.time);
		planRec.actorPlan = plan;
		planRec.key = key;

		Map<Object, PlanRec> map;

		planRec.actorId = focalActorId;

		if (key != null) {
			map = actorPlanMap.get(focalActorId);
			if (map == null) {
				map = new LinkedHashMap<>();
				actorPlanMap.put(focalActorId, map);
			}
			map.put(key, planRec);
		}

		if (isActivePlan) {
			activePlanCount++;
		}
		planningQueue.add(planRec);

	}

	private void addDataManagerPlan(final DataManagerId dataManagerId, final Consumer<DataManagerContext> plan, final double time, final boolean isActivePlan, final Object key) {

		validateDataManagerPlan(plan);
		validatePlanTime(time);

		final PlanRec planRec = new PlanRec();
		planRec.isActive = isActivePlan;
		planRec.arrivalId = masterPlanningArrivalId++;
		planRec.planner = Planner.DATA_MANAGER;
		planRec.time = FastMath.max(time, this.time);
		planRec.dataManagerPlan = plan;
		planRec.key = key;

		Map<Object, PlanRec> map;

		planRec.dataManagerId = dataManagerId;
		if (key != null) {
			map = dataManagerPlanMap.get(dataManagerId);
			if (map == null) {
				map = new LinkedHashMap<>();
				dataManagerPlanMap.put(dataManagerId, map);
			}
			map.put(key, planRec);
		}

		if (isActivePlan) {
			activePlanCount++;
		}
		planningQueue.add(planRec);
	}

	private void validateDataManagerPlanKeyNotDuplicate(DataManagerId dataManagerId, final Object key) {
		if (getDataManagerPlan(dataManagerId, key).isPresent()) {
			throw new ContractException(NucleusError.DUPLICATE_PLAN_KEY);
		}
	}

	private void validateActorPlanKeyNotDuplicate(final Object key) {
		if (getActorPlan(key).isPresent()) {
			throw new ContractException(NucleusError.DUPLICATE_PLAN_KEY);
		}
	}

	@SuppressWarnings("unchecked")
	private <T> Optional<T> removeActorPlan(final Object key) {
		validatePlanKeyNotNull(key);

		Map<Object, PlanRec> map = actorPlanMap.get(focalActorId);

		T result = null;
		if (map != null) {
			final PlanRec planRecord = map.remove(key);
			if (planRecord != null) {
				result = (T) planRecord.actorPlan;
				planRecord.actorPlan = null;
			}
		}
		return Optional.ofNullable(result);

	}

	private List<Plugin> getOrderedPlugins() {

		MutableGraph<PluginId, Object> pluginDependencyGraph = new MutableGraph<>();

		Map<PluginId, Plugin> pluginMap = new LinkedHashMap<>();

		/*
		 * Add the nodes to the graph, check for duplicate ids, build the
		 * mapping from plugin id back to plugin
		 */
		for (Plugin plugin : data.plugins) {
			focalPluginId = plugin.getPluginId();
			pluginMap.put(focalPluginId, plugin);
			// ensure that there are no duplicate plugins
			if (pluginDependencyGraph.containsNode(focalPluginId)) {
				throw new ContractException(NucleusError.DUPLICATE_PLUGIN, focalPluginId);
			}
			pluginDependencyGraph.addNode(focalPluginId);
			focalPluginId = null;
		}

		// Add the edges to the graph
		for (Plugin plugin : data.plugins) {
			focalPluginId = plugin.getPluginId();
			for (PluginId pluginId : plugin.getPluginDependencies()) {
				pluginDependencyGraph.addEdge(new Object(), focalPluginId, pluginId);
			}
			focalPluginId = null;
		}

		/*
		 * Check for missing plugins from the plugin dependencies that were
		 * collected from the known plugins.
		 */
		for (PluginId pluginId : pluginDependencyGraph.getNodes()) {
			if (!pluginMap.containsKey(pluginId)) {
				List<Object> inboundEdges = pluginDependencyGraph.getInboundEdges(pluginId);
				StringBuilder sb = new StringBuilder();
				sb.append("cannot locate instance of ");
				sb.append(pluginId);
				sb.append(" needed for ");
				boolean first = true;
				for (Object edge : inboundEdges) {
					if (first) {
						first = false;
					} else {
						sb.append(", ");
					}
					PluginId dependentPluginId = pluginDependencyGraph.getOriginNode(edge);
					sb.append(dependentPluginId);
				}
				throw new ContractException(NucleusError.MISSING_PLUGIN, sb.toString());
			}
		}

		/*
		 * Determine whether the graph is acyclic and generate a graph depth
		 * evaluator for the graph so that we can determine the order of
		 * initialization.
		 */
		Optional<GraphDepthEvaluator<PluginId>> optional = GraphDepthEvaluator.getGraphDepthEvaluator(pluginDependencyGraph.toGraph());

		if (!optional.isPresent()) {
			/*
			 * Explain in detail why there is a circular dependency
			 */

			Graph<PluginId, Object> g = pluginDependencyGraph.toGraph();
			g = Graphs.getSourceSinkReducedGraph(g);
			List<Graph<PluginId, Object>> cutGraphs = Graphs.cutGraph(g);
			StringBuilder sb = new StringBuilder();
			String lineSeparator = System.getProperty("line.separator");
			sb.append(lineSeparator);
			boolean firstCutGraph = true;

			for (Graph<PluginId, Object> cutGraph : cutGraphs) {
				if (firstCutGraph) {
					firstCutGraph = false;
				} else {
					sb.append(lineSeparator);
				}
				sb.append("Dependency group: ");
				sb.append(lineSeparator);
				Set<PluginId> nodes = cutGraph.getNodes().stream().collect(Collectors.toCollection(LinkedHashSet::new));

				for (PluginId node : nodes) {
					sb.append("\t");
					sb.append(node);
					sb.append(" requires:");
					sb.append(lineSeparator);
					for (Object edge : cutGraph.getInboundEdges(node)) {
						PluginId dependencyNode = cutGraph.getOriginNode(edge);
						if (nodes.contains(dependencyNode)) {
							sb.append("\t");
							sb.append("\t");
							sb.append(dependencyNode);
							sb.append(lineSeparator);
						}
					}
				}
			}
			throw new ContractException(NucleusError.CIRCULAR_PLUGIN_DEPENDENCIES, sb.toString());
		}

		// the graph is acyclic, so the depth evaluator is present
		GraphDepthEvaluator<PluginId> graphDepthEvaluator = optional.get();

		List<PluginId> orderedPluginIds = graphDepthEvaluator.getNodesInRankOrder();

		List<Plugin> orderedPlugins = new ArrayList<>();
		for (PluginId pluginId : orderedPluginIds) {
			orderedPlugins.add(pluginMap.get(pluginId));
		}
		return orderedPlugins;
	}

	/**
	 * Executes this Simulation instance. Contributed plugin initializers are
	 * accessed in the order of their addition to the builder. Actors and data
	 * managers are organized based on their plugin dependency ordering. Time
	 * starts at zero and flows based on planning. When all plans are executed,
	 * time stops and the simulation halts.
	 * 
	 * @throws ContractException
	 *             <li>{@link NucleusError#REPEATED_EXECUTION} if execute is
	 *             invoked more than once
	 *
	 *             <li>{@link NucleusError#MISSING_PLUGIN} if the contributed
	 *             plugins contain dependencies on plugins that have not been
	 *             added to the simulation
	 * 
	 *             <li>{@link NucleusError#MISSING_PLUGIN} if the contributed
	 *             plugins contain duplicate plugin ids
	 * 
	 *             <li>{@link NucleusError#CIRCULAR_PLUGIN_DEPENDENCIES} if the
	 *             contributed plugins form a circular chain of dependencies
	 *             <li>{@link NucleusError#DATA_MANAGER_INITIALIZATION_FAILURE}
	 *             if a data manager does not invoke
	 *             {@linkplain DataManager#init(DataManagerContext)} in its
	 *             override of init().
	 * 
	 * 
	 */
	public void execute() {

		// start the simulation
		if (started) {
			throw new ContractException(NucleusError.REPEATED_EXECUTION);
		}
		started = true;

		// set the output consumer
		outputConsumer = data.outputConsumer;

		/*
		 * Get the plugins listed in dependency order
		 */
		List<Plugin> orderedPlugins = getOrderedPlugins();

		// Make the plugin data available
		for (Plugin plugin : orderedPlugins) {
			focalPluginId = plugin.getPluginId();
			for (PluginData pluginData : plugin.getPluginDatas()) {
				basePluginDataMap.put(pluginData.getClass(), pluginData);
			}
		}

		// Have each plugin contribute data managers and actors
		for (Plugin plugin : orderedPlugins) {
			focalPluginId = plugin.getPluginId();
			Optional<Consumer<PluginContext>> optionalInitializer = plugin.getInitializer();
			if (optionalInitializer.isPresent()) {
				optionalInitializer.get().accept(pluginContext);
			}
			focalPluginId = null;
		}

		// initialize the data managers
		for (DataManagerId dataManagerId : dataManagerIdToDataManagerMap.keySet()) {
			DataManager dataManager = dataManagerIdToDataManagerMap.get(dataManagerId);
			DataManagerContext dataManagerContext = dataManagerIdToContextMap.get(dataManagerId);
			dataManager.init(dataManagerContext);
			if (!dataManager.isInitialized()) {
				throw new ContractException(NucleusError.DATA_MANAGER_INITIALIZATION_FAILURE, dataManager.getClass().getSimpleName());
			}
		}

		// initialize the actors by flushing the actor queue
		executeActorQueue();

		// start the planning-based portion of the simulation where time flows
		while (processEvents && (activePlanCount > 0)) {
			final PlanRec planRec = planningQueue.poll();
			time = planRec.time;
			if (planRec.isActive) {
				activePlanCount--;
			}
			switch (planRec.planner) {
			case ACTOR:

				if (planRec.actorPlan != null) {
					if (planRec.key != null) {
						actorPlanMap.get(planRec.actorId).remove(planRec.key);
					}
					ActorContentRec actorContentRec = new ActorContentRec();
					actorContentRec.actorId = planRec.actorId;
					actorContentRec.plan = planRec.actorPlan;
					actorQueue.add(actorContentRec);
					executeActorQueue();
				}
				break;
			case DATA_MANAGER:
				if (planRec.dataManagerPlan != null) {
					if (planRec.key != null) {
						dataManagerPlanMap.get(planRec.dataManagerId).remove(planRec.key);
					}
					DataManagerContext dataManagerContext = dataManagerIdToContextMap.get(planRec.dataManagerId);
					planRec.dataManagerPlan.accept(dataManagerContext);
					executeActorQueue();
				}
				break;
			default:				
				throw new RuntimeException("unhandled planner type " + planRec.planner);
			}
		}

		// signal to the data managers that the simulation is closing
		for (DataManagerId dataManagerId : simulationCloseDataManagerCallbacks.keySet()) {
			Consumer<DataManagerContext> dataManagerCloseCallback = simulationCloseDataManagerCallbacks.get(dataManagerId);
			DataManagerContext dataManagerContext = dataManagerIdToContextMap.get(dataManagerId);
			dataManagerCloseCallback.accept(dataManagerContext);
		}

		// signal to the actors that the simulation is closing
		for (ActorId actorId : simulationCloseActorCallbacks.keySet()) {
			if (actorIds.get(actorId.getValue()) != null) {
				focalActorId = actorId;
				Consumer<ActorContext> simulationCloseCallback = simulationCloseActorCallbacks.get(actorId);
				simulationCloseCallback.accept(actorContext);
				focalActorId = null;
			}
		}

	}

	private void executeActorQueue() {
		while (!actorQueue.isEmpty()) {
			final ActorContentRec actorContentRec = actorQueue.pollFirst();

			if (containsDeletedActors) {
				/*
				 * we know that the actor id was valid at some point and that
				 * the actorMap never shrinks, so we do not have to range check
				 * the actor id
				 */
				if (actorIds.get(actorContentRec.actorId.getValue()) == null) {
					continue;
				}
			}

			focalActorId = actorContentRec.actorId;
			if (actorContentRec.event != null) {
				actorContentRec.metaActorEventConsumer.handleEvent(actorContentRec.event);
			} else {
				actorContentRec.plan.accept(actorContext);
			}
			focalActorId = null;
		}

	}

	private Optional<Consumer<ActorContext>> getActorPlan(final Object key) {
		validatePlanKeyNotNull(key);
		Map<Object, PlanRec> map = actorPlanMap.get(focalActorId);
		Consumer<ActorContext> result = null;
		if (map != null) {
			final PlanRec planRecord = map.get(key);
			if (planRecord != null) {
				result = planRecord.actorPlan;
			}
		}
		return Optional.ofNullable(result);
	}

	private Optional<Consumer<DataManagerContext>> getDataManagerPlan(DataManagerId dataManagerId, final Object key) {
		validatePlanKeyNotNull(key);
		Map<Object, PlanRec> map = dataManagerPlanMap.get(dataManagerId);
		Consumer<DataManagerContext> result = null;
		if (map != null) {
			final PlanRec planRecord = map.get(key);
			if (planRecord != null) {
				result = planRecord.dataManagerPlan;
			}
		}
		return Optional.ofNullable(result);
	}

	private List<Object> getActorPlanKeys() {
		Map<Object, PlanRec> map = actorPlanMap.get(focalActorId);
		if (map != null) {
			return new ArrayList<>(map.keySet());
		}
		return new ArrayList<>();
	}

	private List<Object> getDataManagerPlanKeys(DataManagerId dataManagerId) {
		Map<Object, PlanRec> map = dataManagerPlanMap.get(dataManagerId);
		if (map != null) {
			return new ArrayList<>(map.keySet());
		}
		return new ArrayList<>();
	}

	private Optional<Double> getActorPlanTime(final Object key) {
		validatePlanKeyNotNull(key);
		Map<Object, PlanRec> map = actorPlanMap.get(focalActorId);
		Double result = null;
		if (map != null) {
			final PlanRec planRecord = map.get(key);
			if (planRecord != null) {
				result = planRecord.time;
			}
		}
		return Optional.ofNullable(result);
	}

	private Optional<Double> getDataManagerPlanTime(final DataManagerId dataManagerId, final Object key) {
		validatePlanKeyNotNull(key);
		Map<Object, PlanRec> map = dataManagerPlanMap.get(dataManagerId);
		Double result = null;
		if (map != null) {
			final PlanRec planRecord = map.get(key);
			if (planRecord != null) {
				result = planRecord.time;
			}
		}
		return Optional.ofNullable(result);
	}

	private void halt() {
		if (processEvents) {
			processEvents = false;
		}
	}

	private Consumer<Object> outputConsumer;

	/*
	 * We drop the plan out of the plan map and thus have no way to reference
	 * the plan directly. However, we do not remove the plan from the planQueue
	 * and instead simply mark the plan record as cancelled. When the cancelled
	 * plan record reaches the top of the queue, it is popped off and ignored.
	 * This avoids the inefficiency of walking the queue and removing the plan.
	 *
	 * Note that we are allowing components to delete plans that do not exist.
	 * This was done to ease any bookkeeping burdens on the component and seems
	 * generally harmless.
	 *
	 * 
	 */

	@SuppressWarnings("unchecked")
	private <T> Optional<T> removeDataManagerPlan(DataManagerId dataManagerId, final Object key) {
		validatePlanKeyNotNull(key);

		Map<Object, PlanRec> map = dataManagerPlanMap.get(dataManagerId);
		T result = null;
		if (map != null) {
			final PlanRec planRecord = map.remove(key);
			if (planRecord != null) {
				result = (T) planRecord.dataManagerPlan;
				planRecord.dataManagerPlan = null;
			}
		}
		return Optional.ofNullable(result);
	}

	private void validatePlanKeyNotNull(final Object key) {
		if (key == null) {
			throw new ContractException(NucleusError.NULL_PLAN_KEY, "");
		}
	}

	private void validatePlanTime(final double planTime) {
		if (planTime < time) {
			throw new ContractException(NucleusError.PAST_PLANNING_TIME);
		}
	}

	private <T extends Event> void subscribeActorToEventByClass(Class<? extends Event> eventClass, BiConsumer<ActorContext, T> eventConsumer) {
		if (eventClass == null) {
			throw new ContractException(NucleusError.NULL_EVENT_CLASS);
		}

		if (eventConsumer == null) {
			throw new ContractException(NucleusError.NULL_EVENT_CONSUMER);
		}

		Map<ActorId, MetaActorEventConsumer<?>> map = actorEventMap.get(eventClass);
		if (map == null) {
			map = new LinkedHashMap<>();
			actorEventMap.put(eventClass, map);
		}
		MetaActorEventConsumer<T> metaActorEventConsumer = new MetaActorEventConsumer<>(actorContext, eventConsumer);
		map.put(focalActorId, metaActorEventConsumer);
	}

	private void releaseOutput(Object output) {
		if (outputConsumer != null) {
			outputConsumer.accept(output);
		}
	}

	private static class MetaEventLabeler<T extends Event> {

		private final EventLabeler<T> eventLabeler;
		private final Class<T> eventClass;
		private final EventLabelerId id;

		public MetaEventLabeler(EventLabeler<T> eventLabeler, Class<T> eventClass) {
			this.id = eventLabeler.getEventLabelerId();
			this.eventClass = eventClass;
			this.eventLabeler = eventLabeler;

		}

		@SuppressWarnings("unchecked")
		public EventLabel<T> getEventLabel(SimulationContext simulationContext, Event event) {
			EventLabel<T> eventLabel = eventLabeler.getEventLabel(simulationContext, (T) event);
			if (eventLabel == null) {
				throw new ContractException(NucleusError.NULL_EVENT_LABEL,
						"event labeler for class = " + eventLabeler.getEventClass().getSimpleName() + " and label id =" + eventLabeler.getEventLabelerId() + " returned a null event label");
			}
			if (!eventClass.equals(eventLabel.getEventClass())) {
				throw new ContractException(NucleusError.LABLER_GENERATED_LABEL_WITH_INCORRECT_EVENT_CLASS);
			}
			if (!id.equals(eventLabel.getLabelerId())) {
				throw new ContractException(NucleusError.LABLER_GENERATED_LABEL_WITH_INCORRECT_ID);
			}
			if (!event.getPrimaryKeyValue().equals(eventLabel.getPrimaryKeyValue())) {
				throw new ContractException(NucleusError.LABLER_GENERATED_LABEL_WITH_INCORRECT_PRIMARY_KEY);
			}
			return eventLabel;
		}

		public EventLabelerId getId() {
			return id;
		}

	}

	private static class MetaActorEventConsumer<T extends Event> {

		private final BiConsumer<ActorContext, T> eventConsumer;

		private final ActorContext context;

		public MetaActorEventConsumer(ActorContext context, BiConsumer<ActorContext, T> eventConsumer) {
			this.eventConsumer = eventConsumer;
			this.context = context;
		}

		@SuppressWarnings("unchecked")
		public void handleEvent(Event event) {

			try {
				eventConsumer.accept(context, (T) event);
			} catch (ClassCastException e) {				
				throw new RuntimeException("Class cast exception likely due to improperly formed event label", e);
			}

		}
	}

	private boolean actorExists(final ActorId actorId) {
		if (actorId == null) {
			return false;
		}
		int index = actorId.getValue();
		if (index < 0) {
			return false;
		}
		if (index >= actorIds.size()) {
			return false;
		}

		return actorIds.get(index) != null;
	}

	private void subscribeActorToSimulationClose(Consumer<ActorContext> consumer) {
		if (consumer == null) {
			throw new ContractException(NucleusError.NULL_ACTOR_CONTEXT_CONSUMER);
		}
		simulationCloseActorCallbacks.put(focalActorId, consumer);
	}

	private void subscribeDataManagerToSimulationClose(DataManagerId dataManagerId, Consumer<DataManagerContext> consumer) {
		if (consumer == null) {
			throw new ContractException(NucleusError.NULL_DATA_MANAGER_CONTEXT_CONSUMER);
		}
		simulationCloseDataManagerCallbacks.put(dataManagerId, consumer);
	}

	private static enum EventPhase {
		EXECUTION, POST_EXECUTION
	}

	private <T extends Event> void subscribeDataManagerToEventExecutionPhase(DataManagerId dataManagerId, Class<T> eventClass, BiConsumer<DataManagerContext, T> eventConsumer) {
		if (eventClass == null) {
			throw new ContractException(NucleusError.NULL_EVENT_CLASS);
		}
		if (eventConsumer == null) {
			throw new ContractException(NucleusError.NULL_EVENT_CONSUMER);
		}

		List<MetaDataManagerEventConsumer<?>> list = dataManagerEventMap.get(eventClass);
		if (list == null) {
			list = new ArrayList<>();
			dataManagerEventMap.put(eventClass, list);
		}
		DataManagerContext dataManagerContext = dataManagerIdToContextMap.get(dataManagerId);
		MetaDataManagerEventConsumer<T> metaDataManagerEventConsumer = new MetaDataManagerEventConsumer<>(dataManagerContext, dataManagerId, eventConsumer, EventPhase.EXECUTION);

		int insertionIndex = -1;

		for (int i = 0; i < list.size(); i++) {
			MetaDataManagerEventConsumer<?> m = list.get(i);
			if (m.eventPhase == EventPhase.POST_EXECUTION) {
				insertionIndex = i;
				break;
			}
		}

		if (insertionIndex < 0) {
			list.add(metaDataManagerEventConsumer);
		} else {
			list.add(insertionIndex, metaDataManagerEventConsumer);
		}
	}

	private <T extends Event> void subscribeDataManagerToEventPostPhase(DataManagerId dataManagerId, Class<T> eventClass, BiConsumer<DataManagerContext, T> eventConsumer) {
		if (eventClass == null) {
			throw new ContractException(NucleusError.NULL_EVENT_CLASS);
		}
		if (eventConsumer == null) {
			throw new ContractException(NucleusError.NULL_EVENT_CONSUMER);
		}

		List<MetaDataManagerEventConsumer<?>> list = dataManagerEventMap.get(eventClass);
		if (list == null) {
			list = new ArrayList<>();
			dataManagerEventMap.put(eventClass, list);
		}
		DataManagerContext dataManagerContext = dataManagerIdToContextMap.get(dataManagerId);
		MetaDataManagerEventConsumer<T> metaDataManagerEventConsumer = new MetaDataManagerEventConsumer<>(dataManagerContext, dataManagerId, eventConsumer, EventPhase.POST_EXECUTION);

		list.add(metaDataManagerEventConsumer);

	}

	private void unSubscribeDataManagerFromEvent(DataManagerId dataManagerId, Class<? extends Event> eventClass) {
		if (eventClass == null) {
			throw new ContractException(NucleusError.NULL_EVENT_CLASS);
		}

		List<MetaDataManagerEventConsumer<?>> list = dataManagerEventMap.get(eventClass);

		if (list != null) {
			Iterator<MetaDataManagerEventConsumer<?>> iterator = list.iterator();
			while (iterator.hasNext()) {
				MetaDataManagerEventConsumer<?> metaDataManagerEventConsumer = iterator.next();
				if (metaDataManagerEventConsumer.dataManagerId.equals(dataManagerId)) {
					iterator.remove();
				}
			}

			if (list.isEmpty()) {
				dataManagerEventMap.remove(eventClass);
			}
		}
	}

	private <T extends Event> void unSubscribeActorFromEventByClass(Class<T> eventClass) {
		if (eventClass == null) {
			throw new ContractException(NucleusError.NULL_EVENT_CLASS);
		}
		Map<ActorId, MetaActorEventConsumer<?>> map = actorEventMap.get(eventClass);
		map.remove(focalActorId);
	}

	private void releaseEvent(final Event event) {

		if (event == null) {
			throw new ContractException(NucleusError.NULL_EVENT);
		}

		Map<ActorId, MetaActorEventConsumer<?>> consumerMap = actorEventMap.get(event.getClass());
		if (consumerMap != null) {
			for (final ActorId actorId : consumerMap.keySet()) {
				MetaActorEventConsumer<?> metaActorEventConsumer = consumerMap.get(actorId);
				final ActorContentRec contentRec = new ActorContentRec();
				contentRec.actorId = actorId;
				contentRec.event = event;
				contentRec.metaActorEventConsumer = metaActorEventConsumer;
				actorQueue.add(contentRec);
			}
		}

		broadcastEventToActorSubscribers(event);

		List<MetaDataManagerEventConsumer<?>> list = dataManagerEventMap.get(event.getClass());
		if (list != null) {
			for (MetaDataManagerEventConsumer<?> metaDataManagerEventConsumer : list) {
				metaDataManagerEventConsumer.handleEvent(event);
			}
		}
	}

	private ActorId addActor(Consumer<ActorContext> consumer) {

		if (consumer == null) {
			throw new ContractException(NucleusError.NULL_ACTOR_CONTEXT_CONSUMER);
		}

		ActorId result = new ActorId(actorIds.size());
		actorIds.add(result);

		final ActorContentRec actorContentRec = new ActorContentRec();
		actorContentRec.actorId = result;
		actorContentRec.plan = consumer;
		actorQueue.add(actorContentRec);
		return result;
	}

	private void removeActor(final ActorId actorId) {
		if (actorId == null) {
			throw new ContractException(NucleusError.NULL_ACTOR_ID);
		}

		int actorIndex = actorId.getValue();

		if (actorIndex < 0) {
			throw new ContractException(NucleusError.UNKNOWN_ACTOR_ID);
		}

		if (actorIndex >= actorIds.size()) {
			throw new ContractException(NucleusError.UNKNOWN_ACTOR_ID);
		}

		ActorId existingActorId = actorIds.get(actorIndex);

		if (existingActorId == null) {
			throw new ContractException(NucleusError.UNKNOWN_ACTOR_ID);
		}

		actorIds.set(actorIndex, null);

		containsDeletedActors = true;
	}

	@SuppressWarnings("unchecked")
	private <T extends DataManager> T getDataManager(Class<T> dataManagerClass) {

		if (dataManagerClass == null) {
			throw new ContractException(NucleusError.NULL_DATA_MANAGER_CLASS);
		}

		DataManager dataManager = workingClassToDataManagerMap.get(dataManagerClass);
		/*
		 * If the working map does not contain the data manager, try to find a
		 * single match from the base map that was collected from the plugins.
		 * 
		 * If two or more matches are found, then throw an exception.
		 * 
		 * If exactly one match is found, update the working map.
		 * 
		 * If no matches are found, nothing is done, but we are vulnerable to
		 * somewhat slower performance if the data manager is sought repeatedly.
		 */
		if (dataManager == null) {
			List<Class<?>> candidates = new ArrayList<>();
			for (Class<?> c : baseClassToDataManagerMap.keySet()) {
				if (dataManagerClass.isAssignableFrom(c)) {
					candidates.add(c);
				}
			}
			if (candidates.size() > 1) {
				throw new ContractException(NucleusError.AMBIGUOUS_DATA_MANAGER_CLASS);
			}
			if (candidates.size() == 1) {
				dataManager = baseClassToDataManagerMap.get(candidates.get(0));
				workingClassToDataManagerMap.put(dataManagerClass, dataManager);
			}
		}

		if (dataManager == null) {
			throw new ContractException(NucleusError.UNKNOWN_DATA_MANAGER, " : " + dataManagerClass.getSimpleName());
		}
		return (T) dataManager;
	}

	/////////////////////////////////
	// data manager support
	/////////////////////////////////

	private int masterDataManagerIndex;

	private static class MetaDataManagerEventConsumer<T extends Event> {

		private final BiConsumer<DataManagerContext, T> dataManagerEventConsumer;

		private final DataManagerContext context;

		private final DataManagerId dataManagerId;

		private final EventPhase eventPhase;

		public MetaDataManagerEventConsumer(DataManagerContext context, DataManagerId dataManagerId, BiConsumer<DataManagerContext, T> eventConsumer, EventPhase eventPhase) {
			this.dataManagerEventConsumer = eventConsumer;
			this.context = context;
			this.dataManagerId = dataManagerId;
			this.eventPhase = eventPhase;
		}

		@SuppressWarnings("unchecked")
		public void handleEvent(Event event) {

			try {
				dataManagerEventConsumer.accept(context, (T) event);
			} catch (ClassCastException e) {				
				throw new RuntimeException("Class cast exception likely due to improperly formed event label", e);
			}

		}
	}

	// used for subscriptions
	private final Map<Class<? extends Event>, List<MetaDataManagerEventConsumer<?>>> dataManagerEventMap = new LinkedHashMap<>();

	// used for retrieving and canceling plans owned by data managers
	private final Map<DataManagerId, Map<Object, PlanRec>> dataManagerPlanMap = new LinkedHashMap<>();

	// used to locate data managers by class type
	private Map<Class<?>, DataManager> baseClassToDataManagerMap = new LinkedHashMap<>();
	private Map<Class<?>, DataManager> workingClassToDataManagerMap = new LinkedHashMap<>();

	// map of contexts for each data manager
	private Map<DataManagerId, DataManagerContext> dataManagerIdToContextMap = new LinkedHashMap<>();

	// map of data manager id to data manager instances
	private Map<DataManagerId, DataManager> dataManagerIdToDataManagerMap = new LinkedHashMap<>();

	//////////////////////////////
	// actor support
	//////////////////////////////

	private final Map<Class<? extends Event>, Map<ActorId, MetaActorEventConsumer<?>>> actorEventMap = new LinkedHashMap<>();

	private final ActorContext actorContext = new ActorContextImpl();

	private final List<ActorId> actorIds = new ArrayList<>();

	private boolean containsDeletedActors;

	private final Map<ActorId, Map<Object, PlanRec>> actorPlanMap = new LinkedHashMap<>();

	private final Deque<ActorContentRec> actorQueue = new ArrayDeque<>();

	private ActorId focalActorId;

	private static class ActorContentRec {

		private Event event;

		private MetaActorEventConsumer<?> metaActorEventConsumer;

		private Consumer<ActorContext> plan;

		private ActorId actorId;

	}

	/////////////////////////////////////////////////////////////////////////

	private <T extends Event> void addEventLabeler(EventLabeler<T> eventLabeler) {
		if (eventLabeler == null) {
			throw new ContractException(NucleusError.NULL_EVENT_LABELER);
		}

		Class<T> eventClass = eventLabeler.getEventClass();

		EventLabelerId id = eventLabeler.getEventLabelerId();

		if (id_Labeler_Map.containsKey(id)) {
			throw new ContractException(NucleusError.DUPLICATE_LABELER_ID_IN_EVENT_LABELER);
		}

		MetaEventLabeler<T> metaEventLabeler = new MetaEventLabeler<>(eventLabeler, eventClass);

		id_Labeler_Map.put(metaEventLabeler.getId(), metaEventLabeler);
	}

	private void broadcastEventToActorSubscribers(final Event event) {
		Map<Object, Map<EventLabelerId, Map<EventLabel<?>, Map<ActorId, MetaActorEventConsumer<?>>>>> map1 = actorPubSub.get(event.getClass());
		if (map1 == null) {
			return;
		}
		Object primaryKeyValue = event.getPrimaryKeyValue();
		Map<EventLabelerId, Map<EventLabel<?>, Map<ActorId, MetaActorEventConsumer<?>>>> map2 = map1.get(primaryKeyValue);
		if (map2 == null) {
			return;
		}

		for (EventLabelerId eventLabelerId : map2.keySet()) {
			MetaEventLabeler<?> metaEventLabeler = id_Labeler_Map.get(eventLabelerId);
			EventLabel<?> eventLabel = metaEventLabeler.getEventLabel(actorContext, event);
			Map<EventLabel<?>, Map<ActorId, MetaActorEventConsumer<?>>> map3 = map2.get(eventLabelerId);
			Map<ActorId, MetaActorEventConsumer<?>> map4 = map3.get(eventLabel);
			if (map4 != null) {
				for (ActorId actorId : map4.keySet()) {
					MetaActorEventConsumer<?> metaConsumer = map4.get(actorId);
					final ActorContentRec actorContentRec = new ActorContentRec();
					actorContentRec.event = event;
					actorContentRec.actorId = actorId;
					actorContentRec.metaActorEventConsumer = metaConsumer;
					actorQueue.add(actorContentRec);

				}
			}
		}
	}

	private <T extends Event> void subscribeActorToEventByLabel(EventLabel<T> eventLabel, BiConsumer<ActorContext, T> eventConsumer) {

		if (eventLabel == null) {
			throw new ContractException(NucleusError.NULL_EVENT_LABEL);
		}

		if (eventConsumer == null) {
			throw new ContractException(NucleusError.NULL_EVENT_CONSUMER);
		}
		Class<T> eventClass = eventLabel.getEventClass();
		EventLabelerId eventLabelerId = eventLabel.getLabelerId();
		MetaEventLabeler<?> metaEventLabeler = id_Labeler_Map.get(eventLabelerId);

		if (metaEventLabeler == null) {
			throw new ContractException(NucleusError.UNKNOWN_EVENT_LABELER);
		}

		Object primaryKeyValue = eventLabel.getPrimaryKeyValue();

		Map<Object, Map<EventLabelerId, Map<EventLabel<?>, Map<ActorId, MetaActorEventConsumer<?>>>>> map1 = actorPubSub.get(eventLabel.getEventClass());
		if (map1 == null) {
			map1 = new LinkedHashMap<>();
			actorPubSub.put(eventClass, map1);
		}

		Map<EventLabelerId, Map<EventLabel<?>, Map<ActorId, MetaActorEventConsumer<?>>>> map2 = map1.get(primaryKeyValue);
		if (map2 == null) {
			map2 = new LinkedHashMap<>();
			map1.put(primaryKeyValue, map2);
		}

		Map<EventLabel<?>, Map<ActorId, MetaActorEventConsumer<?>>> map3 = map2.get(eventLabelerId);
		if (map3 == null) {
			map3 = new LinkedHashMap<>();
			map2.put(eventLabelerId, map3);
		}

		Map<ActorId, MetaActorEventConsumer<?>> map4 = map3.get(eventLabel);
		if (map4 == null) {
			map4 = new LinkedHashMap<>();
			map3.put(eventLabel, map4);
		}

		MetaActorEventConsumer<T> metaEventConsumer = new MetaActorEventConsumer<>(actorContext, eventConsumer);
		map4.put(focalActorId, metaEventConsumer);

	}

	private <T extends Event> void unsubscribeActorFromEventByLabel(EventLabel<T> eventLabel) {

		if (eventLabel == null) {
			throw new ContractException(NucleusError.NULL_EVENT_LABEL);
		}

		Class<T> eventClass = eventLabel.getEventClass();
		EventLabelerId eventLabelerId = eventLabel.getLabelerId();
		MetaEventLabeler<?> metaEventLabeler = id_Labeler_Map.get(eventLabelerId);
		if (metaEventLabeler == null) {
			throw new ContractException(NucleusError.UNKNOWN_EVENT_LABELER, eventLabelerId);
		}

		Object primaryKeyValue = eventLabel.getPrimaryKeyValue();

		Map<Object, Map<EventLabelerId, Map<EventLabel<?>, Map<ActorId, MetaActorEventConsumer<?>>>>> map1 = actorPubSub.get(eventClass);

		if (map1 == null) {
			return;
		}

		Map<EventLabelerId, Map<EventLabel<?>, Map<ActorId, MetaActorEventConsumer<?>>>> map2 = map1.get(primaryKeyValue);

		if (map2 == null) {
			return;
		}

		Map<EventLabel<?>, Map<ActorId, MetaActorEventConsumer<?>>> map3 = map2.get(eventLabelerId);

		if (map3 == null) {
			return;
		}

		Map<ActorId, MetaActorEventConsumer<?>> map4 = map3.get(eventLabel);

		if (map4 == null) {
			return;
		}

		map4.remove(focalActorId);

		if (map4.isEmpty()) {
			map3.remove(eventLabel);
			if (map3.isEmpty()) {
				map2.remove(eventLabelerId);
				if (map2.isEmpty()) {
					map1.remove(primaryKeyValue);
					if (map1.isEmpty()) {
						actorPubSub.remove(eventClass);
					}
				}
			}
		}
	}

	private boolean subscribersExistForEvent(Class<? extends Event> eventClass) {
		return (dataManagerEventMap.containsKey(eventClass) || actorPubSub.containsKey(eventClass) || actorEventMap.containsKey(eventClass));
	}

	private Map<EventLabelerId, MetaEventLabeler<?>> id_Labeler_Map = new LinkedHashMap<>();

	private Map<Class<?>, Map<Object, Map<EventLabelerId, Map<EventLabel<?>, Map<ActorId, MetaActorEventConsumer<?>>>>>> actorPubSub = new LinkedHashMap<>();

}