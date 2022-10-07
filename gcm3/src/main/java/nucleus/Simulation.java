package nucleus;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
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
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.math3.util.FastMath;
import org.apache.commons.math3.util.Pair;

import net.jcip.annotations.NotThreadSafe;
import nucleus.EventFilter.IdentifiableFunction;
import util.errors.ContractException;
import util.graph.Graph;
import util.graph.GraphDepthEvaluator;
import util.graph.Graphs;
import util.graph.MutableGraph;
import util.path.MapPathSolver;
import util.path.Path;

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
			dataManagerToDataManagerIdMap.put(dataManager, dataManagerId);
			dataManagerIdToPluginIdMap.put(dataManagerId, focalPluginId);
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
			return Simulation.this.getDataManagerForActor(dataManagerClass);
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
		public <T extends Event> void subscribe(EventFilter<T> eventFilter, BiConsumer<ActorContext, T> eventConsumer) {
			Simulation.this.subscribeActorToEventByFilter(eventFilter, eventConsumer);
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

		@Override
		public <T extends Event> void unsubscribe(EventFilter<T> eventFilter) {
			unsubscribeActorFromEventByFilter(eventFilter);
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
		 *             <li>{@link NucleusError#NULL_PLUGIN} if the plugin is
		 *             null
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
			return simulation.getDataManagerForDataManager(dataManagerClass, dataManagerId);
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
		public <T extends Event> void subscribe(Class<T> eventClass, BiConsumer<DataManagerContext, T> eventConsumer) {
			simulation.subscribeDataManagerToEvent(dataManagerId, eventClass, eventConsumer);
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

	private Graph<PluginId, Object> pluginDependencyGraph;

	private List<Plugin> getOrderedPlugins() {

		MutableGraph<PluginId, Object> mutableGraph = new MutableGraph<>();

		Map<PluginId, Plugin> pluginMap = new LinkedHashMap<>();

		/*
		 * Add the nodes to the graph, check for duplicate ids, build the
		 * mapping from plugin id back to plugin
		 */
		for (Plugin plugin : data.plugins) {
			focalPluginId = plugin.getPluginId();
			pluginMap.put(focalPluginId, plugin);
			// ensure that there are no duplicate plugins
			if (mutableGraph.containsNode(focalPluginId)) {
				throw new ContractException(NucleusError.DUPLICATE_PLUGIN, focalPluginId);
			}
			mutableGraph.addNode(focalPluginId);
			focalPluginId = null;
		}

		// Add the edges to the graph
		for (Plugin plugin : data.plugins) {
			focalPluginId = plugin.getPluginId();
			for (PluginId pluginId : plugin.getPluginDependencies()) {
				mutableGraph.addEdge(new Object(), focalPluginId, pluginId);
			}
			focalPluginId = null;
		}

		/*
		 * Check for missing plugins from the plugin dependencies that were
		 * collected from the known plugins.
		 */
		for (PluginId pluginId : mutableGraph.getNodes()) {
			if (!pluginMap.containsKey(pluginId)) {
				List<Object> inboundEdges = mutableGraph.getInboundEdges(pluginId);
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
					PluginId dependentPluginId = mutableGraph.getOriginNode(edge);
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
		Optional<GraphDepthEvaluator<PluginId>> optional = GraphDepthEvaluator.getGraphDepthEvaluator(mutableGraph.toGraph());

		if (!optional.isPresent()) {
			/*
			 * Explain in detail why there is a circular dependency
			 */

			Graph<PluginId, Object> g = mutableGraph.toGraph();
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

		pluginDependencyGraph = mutableGraph.toGraph();

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
		int dataManagerCount = dataManagerIdToPluginIdMap.size();
		dataManagerAccessPermissions = new boolean[dataManagerCount][dataManagerCount];

		MapPathSolver<PluginId, Object> mapPathSolver = new MapPathSolver<>(pluginDependencyGraph, (e) -> 1, (a, b) -> 0);

		// determine the access allowed between data managers
		for (DataManagerId dataManagerId1 : dataManagerIdToPluginIdMap.keySet()) {
			PluginId pluginId1 = dataManagerIdToPluginIdMap.get(dataManagerId1);
			for (DataManagerId dataManagerId2 : dataManagerIdToPluginIdMap.keySet()) {
				PluginId pluginId2 = dataManagerIdToPluginIdMap.get(dataManagerId2);
				// Optional<Path<Object>> optionalPath =
				// Paths.getPath(pluginDependencyGraph, pluginId1, pluginId2,
				// (e) -> 1, (a, b) -> 0);
				Optional<Path<Object>> optionalPath = mapPathSolver.getPath(pluginId1, pluginId2);
				if (optionalPath.isPresent()) {
					dataManagerAccessPermissions[dataManagerId1.getValue()][dataManagerId2.getValue()] = true;
				} else {
					// within a plugin, data managers have access to previously
					// added data managers in the same plugin.
					if (pluginId1.equals(pluginId2)) {
						if (dataManagerId1.getValue() > dataManagerId2.getValue()) {
							dataManagerAccessPermissions[dataManagerId1.getValue()][dataManagerId2.getValue()] = true;
						}
					}
				}
			}
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
				actorContentRec.consumer.accept(actorContentRec.event);
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

		Map<ActorId, Consumer<Event>> map = actorEventMap.get(eventClass);
		if (map == null) {
			map = new LinkedHashMap<>();
			actorEventMap.put(eventClass, map);
		}

		@SuppressWarnings("unchecked")
		Consumer<Event> consumer = event -> eventConsumer.accept(actorContext, (T) event);

		map.put(focalActorId, consumer);
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

	@SuppressWarnings("unchecked")
	private <T extends Event> void subscribeDataManagerToEvent(DataManagerId dataManagerId, Class<T> eventClass, BiConsumer<DataManagerContext, T> eventConsumer) {
		if (eventClass == null) {
			throw new ContractException(NucleusError.NULL_EVENT_CLASS);
		}
		if (eventConsumer == null) {
			throw new ContractException(NucleusError.NULL_EVENT_CONSUMER);
		}

		List<DataManagerEventConsumer> list = dataManagerEventMap.get(eventClass);
		if (list == null) {
			list = new ArrayList<>();
			dataManagerEventMap.put(eventClass, list);
		}
		DataManagerContext dataManagerContext = dataManagerIdToContextMap.get(dataManagerId);

		DataManagerEventConsumer dataManagerEventConsumer = new DataManagerEventConsumer(dataManagerId, event -> eventConsumer.accept(dataManagerContext, (T) event));

		list.add(dataManagerEventConsumer);
		Collections.sort(list);
	}

	private void unSubscribeDataManagerFromEvent(DataManagerId dataManagerId, Class<? extends Event> eventClass) {
		if (eventClass == null) {
			throw new ContractException(NucleusError.NULL_EVENT_CLASS);
		}

		List<DataManagerEventConsumer> list = dataManagerEventMap.get(eventClass);

		if (list != null) {
			Iterator<DataManagerEventConsumer> iterator = list.iterator();
			while (iterator.hasNext()) {
				DataManagerEventConsumer dataManagerEventConsumer = iterator.next();
				if (dataManagerEventConsumer.dataManagerId.equals(dataManagerId)) {
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
		Map<ActorId, Consumer<Event>> map = actorEventMap.get(eventClass);
		map.remove(focalActorId);
	}

	private void releaseEvent(final Event event) {

		if (event == null) {
			throw new ContractException(NucleusError.NULL_EVENT);
		}

		Map<ActorId, Consumer<Event>> consumerMap = actorEventMap.get(event.getClass());
		if (consumerMap != null) {
			for (final ActorId actorId : consumerMap.keySet()) {
				Consumer<Event> consumer = consumerMap.get(actorId);
				final ActorContentRec contentRec = new ActorContentRec();
				contentRec.actorId = actorId;
				contentRec.event = event;
				contentRec.consumer = consumer;
				actorQueue.add(contentRec);
			}
		}

		broadcastEventToActorSubscribers_ByLabels(event);

		broadcastEventToFilterNode(event, rootNode);

		List<DataManagerEventConsumer> list = dataManagerEventMap.get(event.getClass());
		if (list != null) {
			for (DataManagerEventConsumer dataManagerEventConsumer : list) {
				dataManagerEventConsumer.accept(event);
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
	private <T extends DataManager> T getDataManagerForActor(Class<T> dataManagerClass) {

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

	@SuppressWarnings("unchecked")
	private <T extends DataManager> T getDataManagerForDataManager(Class<T> dataManagerClass, DataManagerId dataManagerId) {

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

		int requestorId = dataManagerId.getValue();
		DataManagerId dataManagerId2 = dataManagerToDataManagerIdMap.get(dataManager);
		int requesteeId = dataManagerId2.getValue();

		boolean accessGranted = dataManagerAccessPermissions[requestorId][requesteeId];

		if (!accessGranted) {
			String dmName1 = dataManagerIdToDataManagerMap.get(dataManagerId).getClass().getSimpleName();
			String dmName2 = dataManagerClass.getSimpleName();
			throw new ContractException(NucleusError.DATA_MANAGER_ACCESS_VIOLATION, dmName1 + "-->" + dmName2);
		}
		return (T) dataManager;
	}
	/////////////////////////////////
	// data manager support
	/////////////////////////////////

	private int masterDataManagerIndex;

	private static class DataManagerEventConsumer implements Consumer<Event>, Comparable<DataManagerEventConsumer> {

		private final Consumer<Event> consumer;
		private final DataManagerId dataManagerId;

		public <T extends Event> DataManagerEventConsumer(DataManagerId dataManagerId, Consumer<Event> consumer) {
			this.consumer = consumer;
			this.dataManagerId = dataManagerId;
		}

		@Override
		public int compareTo(DataManagerEventConsumer other) {
			return this.dataManagerId.compareTo(other.dataManagerId);
		}

		@Override
		public void accept(Event event) {
			consumer.accept(event);
		}
	}

	// used for subscriptions
	private final Map<Class<? extends Event>, List<DataManagerEventConsumer>> dataManagerEventMap = new LinkedHashMap<>();

	// used for retrieving and canceling plans owned by data managers
	private final Map<DataManagerId, Map<Object, PlanRec>> dataManagerPlanMap = new LinkedHashMap<>();

	// used to locate data managers by class type
	private Map<Class<?>, DataManager> baseClassToDataManagerMap = new LinkedHashMap<>();
	private Map<Class<?>, DataManager> workingClassToDataManagerMap = new LinkedHashMap<>();

	// map of contexts for each data manager
	private Map<DataManagerId, DataManagerContext> dataManagerIdToContextMap = new LinkedHashMap<>();

	// maps of data manager id <--> data manager instances
	private Map<DataManagerId, DataManager> dataManagerIdToDataManagerMap = new LinkedHashMap<>();
	private Map<DataManager, DataManagerId> dataManagerToDataManagerIdMap = new LinkedHashMap<>();

	private Map<DataManagerId, PluginId> dataManagerIdToPluginIdMap = new LinkedHashMap<>();
	private boolean[][] dataManagerAccessPermissions;

	//////////////////////////////
	// actor support
	//////////////////////////////

	private final Map<Class<? extends Event>, Map<ActorId, Consumer<Event>>> actorEventMap = new LinkedHashMap<>();

	private final ActorContext actorContext = new ActorContextImpl();

	private final List<ActorId> actorIds = new ArrayList<>();

	private boolean containsDeletedActors;

	private final Map<ActorId, Map<Object, PlanRec>> actorPlanMap = new LinkedHashMap<>();

	private final Deque<ActorContentRec> actorQueue = new ArrayDeque<>();

	private ActorId focalActorId;

	private static class ActorContentRec {

		private Event event;

		private Consumer<Event> consumer;

		private Consumer<ActorContext> plan;

		private ActorId actorId;

	}

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

	private void broadcastEventToActorSubscribers_ByLabels(final Event event) {
		Map<Object, Map<EventLabelerId, Map<EventLabel<?>, Map<ActorId, Consumer<Event>>>>> map1 = actorPubSub.get(event.getClass());
		if (map1 == null) {
			return;
		}
		Object primaryKeyValue = event.getPrimaryKeyValue();
		Map<EventLabelerId, Map<EventLabel<?>, Map<ActorId, Consumer<Event>>>> map2 = map1.get(primaryKeyValue);
		if (map2 == null) {
			return;
		}

		for (EventLabelerId eventLabelerId : map2.keySet()) {
			MetaEventLabeler<?> metaEventLabeler = id_Labeler_Map.get(eventLabelerId);
			EventLabel<?> eventLabel = metaEventLabeler.getEventLabel(actorContext, event);
			Map<EventLabel<?>, Map<ActorId, Consumer<Event>>> map3 = map2.get(eventLabelerId);
			Map<ActorId, Consumer<Event>> map4 = map3.get(eventLabel);
			if (map4 != null) {
				for (ActorId actorId : map4.keySet()) {
					Consumer<Event> consumer = map4.get(actorId);
					final ActorContentRec actorContentRec = new ActorContentRec();
					actorContentRec.event = event;
					actorContentRec.actorId = actorId;
					actorContentRec.consumer = consumer;
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

		Map<Object, Map<EventLabelerId, Map<EventLabel<?>, Map<ActorId, Consumer<Event>>>>> map1 = actorPubSub.get(eventLabel.getEventClass());
		if (map1 == null) {
			map1 = new LinkedHashMap<>();
			actorPubSub.put(eventClass, map1);
		}

		Map<EventLabelerId, Map<EventLabel<?>, Map<ActorId, Consumer<Event>>>> map2 = map1.get(primaryKeyValue);
		if (map2 == null) {
			map2 = new LinkedHashMap<>();
			map1.put(primaryKeyValue, map2);
		}

		Map<EventLabel<?>, Map<ActorId, Consumer<Event>>> map3 = map2.get(eventLabelerId);
		if (map3 == null) {
			map3 = new LinkedHashMap<>();
			map2.put(eventLabelerId, map3);
		}

		Map<ActorId, Consumer<Event>> map4 = map3.get(eventLabel);
		if (map4 == null) {
			map4 = new LinkedHashMap<>();
			map3.put(eventLabel, map4);
		}

		@SuppressWarnings("unchecked")
		Consumer<Event> consumer = event -> eventConsumer.accept(actorContext, (T) event);

		map4.put(focalActorId, consumer);

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

		Map<Object, Map<EventLabelerId, Map<EventLabel<?>, Map<ActorId, Consumer<Event>>>>> map1 = actorPubSub.get(eventClass);

		if (map1 == null) {
			return;
		}

		Map<EventLabelerId, Map<EventLabel<?>, Map<ActorId, Consumer<Event>>>> map2 = map1.get(primaryKeyValue);

		if (map2 == null) {
			return;
		}

		Map<EventLabel<?>, Map<ActorId, Consumer<Event>>> map3 = map2.get(eventLabelerId);

		if (map3 == null) {
			return;
		}

		Map<ActorId, Consumer<Event>> map4 = map3.get(eventLabel);

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

	private Map<Class<?>, Map<Object, Map<EventLabelerId, Map<EventLabel<?>, Map<ActorId, Consumer<Event>>>>>> actorPubSub = new LinkedHashMap<>();

	/*
	 * Recursively processes the event through the filter node . Events should
	 * be processed through the root filter node. Each node's consumers have
	 * each such consumer scheduled onto the actor queue for delayed execution
	 * of the consumer.
	 */
	private void broadcastEventToFilterNode(final Event event, FilterNode filterNode) {

		// determine the value of the function for the given event
		Object value = filterNode.function.apply(event);

		// use that value to place any consumers that are matched to that value
		// on the actor queue
		Map<ActorId, Consumer<Event>> consumerMap = filterNode.consumers.get(value);
		if (consumerMap != null) {
			for (ActorId actorId : consumerMap.keySet()) {
				Consumer<Event> consumer = consumerMap.get(actorId);
				final ActorContentRec actorContentRec = new ActorContentRec();
				actorContentRec.event = event;
				actorContentRec.actorId = actorId;
				actorContentRec.consumer = consumer;
				actorQueue.add(actorContentRec);				
			}
		}

		// match the value to any child nodes and recursively call this method
		// on that node
		Map<IdentifiableFunction<?>, FilterNode> childMap = filterNode.children.get(value);
		if (childMap != null) {
			for (Object id : childMap.keySet()) {
				FilterNode childNode = childMap.get(id);
				if (childNode != null) {
					broadcastEventToFilterNode(event, childNode);
				}
			}
		}
	}

	/*
	 * Generates a filter node to be used as the root filter node.
	 */
	private FilterNode generateRootNode() {
		FilterNode result = new FilterNode();		
		result.function = (event) -> event.getClass();
		return result;
	}

	private final FilterNode rootNode = generateRootNode();

	/*
	 * A data structure for containing a function that process an event and
	 * returns a value. The node contains maps of the return value that either
	 * match actor consumers of the event or child nodes to this node. The nodes
	 * thus form a tree with a single root node that filters by event class
	 * type. The tree represents simple conjunctive event filters of the form:
	 * 
	 * F1(e)=A & F2(e)=B &...
	 */
	private static class FilterNode {

		// the parent node is used during the unsubscribe process
		private FilterNode parent;

		// the id is used during the unsubscribe process		
		private IdentifiableFunction<?> identifiableFunction;

		private Function<Event, Object> function;

		// value of function, id of child filter node, FilterNode
		private Map<Object, Map<IdentifiableFunction<?>, FilterNode>> children = new LinkedHashMap<>();

		// value of function, actor id, Consumer
		private Map<Object, Map<ActorId, Consumer<Event>>> consumers = new LinkedHashMap<>();

		/*
		 * Integrates a function and its id and target value into the tree at
		 * this node as a child node if the node does not already exist. Note
		 * that functions cannot be compared for equality and that the id value
		 * takes on this role for the function.
		 */
		@SuppressWarnings("unchecked")
		private <T extends Event> FilterNode addChildNode(Object value, IdentifiableFunction<T> identifiableFunction) {
			Map<IdentifiableFunction<?>, FilterNode> map = children.get(value);
			if (map == null) {
				map = new LinkedHashMap<>();
				children.put(value, map);
			}

			FilterNode filterNode = map.get(identifiableFunction);
			if (filterNode == null) {
				filterNode = new FilterNode();
				filterNode.identifiableFunction = identifiableFunction;
				filterNode.parent = this;
				filterNode.function = event -> identifiableFunction.getEventFunction().apply((T) event);
				map.put(identifiableFunction, filterNode);
			}
			return filterNode;
		}
	}

	/*
	 * Subscribes the current actor (focalActorId) to the event subject to the
	 * event filter. This overwrites the current consumer associated with this
	 * event and filter if it is present.
	 */
	private <T extends Event> void subscribeActorToEventByFilter(EventFilter<T> eventFilter, BiConsumer<ActorContext, T> eventConsumer) {
		if (eventFilter == null) {
			throw new ContractException(NucleusError.NULL_EVENT_FILTER);
		}

		if (eventConsumer == null) {
			throw new ContractException(NucleusError.NULL_EVENT_CONSUMER);
		}

		/*
		 * We wrap the typed consumer with a consumer of event, knowing that the
		 * cast to (T) is safe. This simplifies the FilterNode class.
		 */
		@SuppressWarnings("unchecked")
		Consumer<Event> consumer = event -> eventConsumer.accept(actorContext, (T) event);

		/*
		 * The event filter does not contain a FunctionValue associated with the
		 * root filter node, so we have to integrate it with the root node by
		 * assigning the associated value to the class type that matches the
		 * generics type of the event filter.
		 */
		Object value = eventFilter.getEventClass();
		FilterNode filterNode = rootNode;

		/*
		 * We loop through the (function,value) pairs, integrating each into the
		 * tree of filter nodes and creating new filter nodes as needed.
		 */
		for (Pair<IdentifiableFunction<T>,Object> pair : eventFilter.getFunctionValuePairs()) {
			filterNode = filterNode.addChildNode(value, pair.getFirst());
			value = pair.getSecond();
		}

		/*
		 * The final node will contain the consumer
		 */
		Map<ActorId, Consumer<Event>> consumerMap = filterNode.consumers.get(value);
		if (consumerMap == null) {
			consumerMap = new LinkedHashMap<>();
			filterNode.consumers.put(value, consumerMap);
		}
		consumerMap.put(focalActorId, consumer);
	}

	/*
	 * Removes the consumer from the filter node tree and removes nodes that are
	 * empty(no children, no consumers), except for the root node.
	 */
	private <T extends Event> void unsubscribeActorFromEventByFilter(EventFilter<T> eventFilter) {
		if (eventFilter == null) {
			throw new ContractException(NucleusError.NULL_EVENT_FILTER);
		}

		// start at the root filter node
		Object value = eventFilter.getEventClass();
		FilterNode filterNode = rootNode;

		/*
		 * Walk down the tree and if we find that any of the function id values
		 * are not present then we simply return since the consumer cannot exist
		 */
		for (Pair<IdentifiableFunction<T>,Object> pair : eventFilter.getFunctionValuePairs()) {
			Map<IdentifiableFunction<?>, FilterNode> map = filterNode.children.get(value);
			if (map == null) {
				return;
			}
			Object id = pair.getFirst();
			filterNode = map.get(id);
			if (filterNode == null) {
				return;
			}
			value = pair.getSecond();
		}

		/*
		 * The last node may contain the consumer
		 */
		Map<ActorId, Consumer<Event>> consumerMap = filterNode.consumers.get(value);
		if (consumerMap == null) {
			return;
		}
		consumerMap.remove(focalActorId);

		/*
		 * Walk back up the tree, removing nodes that have neither child nodes
		 * nor consumers. Once we hit a node that does not need removal we can
		 * stop since all ancestor nodes will also not be empty.
		 */
		while (filterNode.parent != null) {
			boolean removeNode = filterNode.children.isEmpty() && filterNode.consumers.isEmpty();
			if (removeNode) {
				filterNode.parent.children.remove(filterNode.identifiableFunction);
			} else {
				break;
			}
			filterNode = filterNode.parent;
		}

	}
}