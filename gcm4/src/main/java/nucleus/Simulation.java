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

import org.apache.commons.math3.util.Pair;

import net.jcip.annotations.NotThreadSafe;
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
 *
 */
@NotThreadSafe
public class Simulation {

	private static final String LINE_SEPARATOR = System.getProperty("line.separator");

	private static class PlanRec {
		private Plan<?> plan;

		private Planner planner;
		private long arrivalId;

		private double time;
		private boolean isActive;
		private Object key;

		private Consumer<ReportContext> reportPlan;
		private ReportId reportId;

		private Consumer<ActorContext> actorPlan;
		private ActorId actorId;

		private Consumer<DataManagerContext> dataManagerPlan;
		private DataManagerId dataManagerId;

		@Override
		public String toString() {

			StringBuilder builder = new StringBuilder();

			builder.append("\t");
			builder.append("time = ");
			builder.append(time);
			builder.append(LINE_SEPARATOR);

			builder.append("\t");
			builder.append("arrivalId = ");
			builder.append(arrivalId);
			builder.append(LINE_SEPARATOR);

			builder.append("\t");
			builder.append("isActive = ");
			builder.append(isActive);
			builder.append(LINE_SEPARATOR);

			builder.append("\t");
			builder.append("key = ");
			builder.append(key);
			builder.append(LINE_SEPARATOR);

			builder.append("\t");
			builder.append(planner);
			builder.append(" = ");
			switch (planner) {
			case ACTOR:
				builder.append(actorId);
				builder.append(LINE_SEPARATOR);
				builder.append("\t");
				builder.append(actorPlan);
				break;
			case DATA_MANAGER:
				builder.append(dataManagerId);
				builder.append(LINE_SEPARATOR);
				builder.append("\t");
				builder.append(dataManagerPlan);
				break;
			case REPORT:
				builder.append(reportId);
				builder.append("\t");
				builder.append(LINE_SEPARATOR);
				builder.append("\t");
				builder.append(reportPlan);
				break;
			default:
				throw new RuntimeException("unhandled planner case");
			}

			builder.append(LINE_SEPARATOR);
			builder.append("\t");
			builder.append("plan = ");
			builder.append(LINE_SEPARATOR);
			builder.append("\t");
			builder.append(plan);

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
		 * Signals to simulation components to record their state as plugin data
		 * as output to the experiment Defaults to false.
		 */
		public Builder setRecordState(boolean recordState) {
			data.stateRecordingIsScheduled = recordState;
			return this;
		}

		/**
		 * Sets the halt time for the simulation. Defaults to -1, which is
		 * equivalent to not halting. If the simulation has been instructed to
		 * produce its state at halt, then the halt time must be set to a
		 * positive value. Setting this to a non-negative value that is less
		 * than the simulation time used to start the simulation will result in
		 * an exception.
		 */
		public Builder setSimulationHaltTime(double simulationHaltTime) {
			data.simulationHaltTime = simulationHaltTime;
			return this;
		}

		/**
		 * Set the simulation state. Defaults to the current date and a start
		 * time of zero.
		 * 
		 * @throws ContractException
		 *             <li>{@link NucleusError#NULL_SIMULATION_TIME} if the
		 *             simulation time is null
		 * 
		 */
		public Builder setSimulationState(SimulationState simulationState) {
			if (simulationState == null) {
				throw new ContractException(NucleusError.NULL_SIMULATION_TIME);
			}
			data.simulationState = simulationState;
			return this;
		}

		/**
		 * Adds a plugin to this builder for inclusion in the simulation
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

		private void validate() {
			if (data.stateRecordingIsScheduled) {
				if (data.simulationHaltTime < 0) {
					throw new ContractException(NucleusError.MISSING_SIM_HALT_TIME);
				}
			}
			if (data.simulationHaltTime >= 0) {
				if (data.simulationHaltTime < data.simulationState.getStartTime()) {
					throw new ContractException(NucleusError.SIM_HALT_TIME_TOO_EARLY);
				}
			}
		}

		/**
		 * Returns an Engine instance that is initialized with the plugins and
		 * output consumer collected by this builder.
		 * 
		 * @throws ContractException
		 *             <li>{@linkplain NucleusError#SIM_HALT_TIME_TOO_EARLY} If
		 *             the simulation halt time is non-negative and less than
		 *             the start time of the simulation</li>
		 *             <li>{@linkplain NucleusError#MISSING_SIM_HALT_TIME} If
		 *             simulation state is being recorded and the simulation
		 *             halt time is not set.</li>
		 */
		public Simulation build() {
			validate();
			return new Simulation(new Data(data));
		}
	}

	private static class Data {
		private double simulationHaltTime = -1;
		private boolean stateRecordingIsScheduled;
		private List<Plugin> plugins = new ArrayList<>();
		private Consumer<Object> outputConsumer;
		private SimulationState simulationState = SimulationState.builder().build();

		public Data() {
		}

		public Data(Data data) {
			simulationHaltTime = data.simulationHaltTime;
			stateRecordingIsScheduled = data.stateRecordingIsScheduled;
			plugins.addAll(data.plugins);
			outputConsumer = data.outputConsumer;
			simulationState = data.simulationState;
		}
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

	private static enum PlanningQueueMode {
		READY, RUNNING, CLOSED
	}

	// planning
	private long masterPlanningArrivalId;
	protected double time;
	double simulationHaltTime;
	boolean forcedHaltPresent;
	private boolean eventProcessingAllowed;
	private int activePlanCount;
	private final PriorityQueue<PlanRec> planningQueue = new PriorityQueue<>(futureComparable);
	private PlanningQueueMode planningQueueMode = PlanningQueueMode.READY;

	// actors
	private final Map<ReportId, List<Consumer<ReportContext>>> simulationCloseReportCallbacks = new LinkedHashMap<>();

	private final Map<ActorId, List<Consumer<ActorContext>>> simulationCloseActorCallbacks = new LinkedHashMap<>();

	private final Map<DataManagerId, List<Consumer<DataManagerContext>>> simulationCloseDataManagerCallbacks = new LinkedHashMap<>();

	private boolean started;

	private PluginContext pluginContext;

	private PluginId focalPluginId;

	private final Map<Class<?>, List<PluginData>> basePluginDataMap = new LinkedHashMap<>();
	private final Map<Class<?>, List<PluginData>> workingPluginDataMap = new LinkedHashMap<>();

	private final Data data;

	private Simulation(Data data) {
		this.data = data;
	}

	private void validateActorPlan(final Consumer<ActorContext> plan) {
		if (plan == null) {
			throw new ContractException(NucleusError.NULL_PLAN);
		}
	}

	private void validateReportPlan(final Consumer<ReportContext> plan) {
		if (plan == null) {
			throw new ContractException(NucleusError.NULL_PLAN);
		}
	}

	private void validateDataManagerPlan(final Consumer<DataManagerContext> plan) {
		if (plan == null) {
			throw new ContractException(NucleusError.NULL_PLAN);
		}
	}

	@SuppressWarnings("unchecked")
	protected <T extends PluginData> Optional<T> getPluginData(Class<T> pluginDataClass) {
		if (pluginDataClass == null) {
			throw new ContractException(NucleusError.NULL_PLUGIN_DATA_CLASS);
		}

		PluginData result = null;

		List<PluginData> pluginDatas = workingPluginDataMap.get(pluginDataClass);
		if (pluginDatas == null) {

			pluginDatas = new ArrayList<>();

			for (Class<?> c : basePluginDataMap.keySet()) {
				if (pluginDataClass.isAssignableFrom(c)) {
					pluginDatas.addAll(basePluginDataMap.get(c));
				}
			}
			workingPluginDataMap.put(pluginDataClass, pluginDatas);
		}
		if (pluginDatas.size() > 1) {
			throw new ContractException(NucleusError.AMBIGUOUS_PLUGIN_DATA_CLASS);
		}
		if (pluginDatas.size() == 1) {
			result = pluginDatas.get(0);
		}

		return Optional.ofNullable((T) result);
	}

	@SuppressWarnings("unchecked")
	protected <T extends PluginData> List<T> getPluginDatas(Class<T> pluginDataClass) {
		if (pluginDataClass == null) {
			throw new ContractException(NucleusError.NULL_PLUGIN_DATA_CLASS);
		}

		List<PluginData> pluginDatas = workingPluginDataMap.get(pluginDataClass);
		if (pluginDatas == null) {

			pluginDatas = new ArrayList<>();

			for (Class<?> c : basePluginDataMap.keySet()) {
				if (pluginDataClass.isAssignableFrom(c)) {
					pluginDatas.addAll(basePluginDataMap.get(c));
				}
			}
			workingPluginDataMap.put(pluginDataClass, pluginDatas);

		}
		List<T> result = new ArrayList<>();
		for (PluginData pluginData : pluginDatas) {
			result.add((T) pluginData);
		}
		return result;
	}

	protected void addDataManagerForPlugin(DataManager dataManager) {

		if (focalPluginId == null) {
			throw new ContractException(NucleusError.PLUGIN_INITIALIZATION_CLOSED);
		}

		if (dataManager == null) {
			throw new ContractException(NucleusError.NULL_DATA_MANAGER);
		}

		if (baseClassToDataManagerMap.containsKey(dataManager.getClass())) {
			throw new ContractException(NucleusError.DUPLICATE_DATA_MANAGER_TYPE, dataManager.getClass());
		}

		DataManagerId dataManagerId = new DataManagerId(dataManagerIds.size());
		dataManagerIds.add(dataManagerId);

		/*
		 * Used to ensure that there is at most one instance of each data
		 * manager since classes are the identifiers for data managers outside
		 * of the simulation class. Used to find data managers for actors and
		 * data managers.
		 */
		baseClassToDataManagerMap.put(dataManager.getClass(), dataManager);

		DataManagerContext dataManagerContext = new DataManagerContext(dataManagerId, this);
		dataManagerIdToDataManagerContextMap.put(dataManagerId, dataManagerContext);

		/*
		 * 
		 */
		dataManagerIdToDataManagerMap.put(dataManagerId, dataManager);
		dataManagerToDataManagerIdMap.put(dataManager, dataManagerId);

		// used for establishing visibility of data managers to each other
		dataManagerIdToPluginIdMap.put(dataManagerId, focalPluginId);

		DataManagerContentRec dataManagerContentRec = new DataManagerContentRec();
		dataManagerContentRec.dmPlan = dataManager::init;
		dataManagerContentRec.dataManagerId = dataManagerId;

		dataManagerQueue.add(dataManagerContentRec);

	}

	protected void addActorPlan(Plan<ActorContext> plan) {

		if (planningQueueMode == PlanningQueueMode.CLOSED) {
			throw new ContractException(NucleusError.PLANNING_QUEUE_CLOSED);
		}

		validatePlanTime(plan.getTime());
		validateActorPlan(plan.getCallbackConsumer());

		final PlanRec planRec = new PlanRec();

		planRec.arrivalId = masterPlanningArrivalId++;

		planRec.plan = plan;
		planRec.isActive = plan.isActive();

		planRec.planner = Planner.ACTOR;
		planRec.time = plan.getTime();
		planRec.actorPlan = plan.getCallbackConsumer();
		planRec.key = plan.getKey();

		Map<Object, PlanRec> map;

		planRec.actorId = focalActorId;

		if (planRec.key != null) {
			map = actorPlanMap.get(focalActorId);
			if (map == null) {
				map = new LinkedHashMap<>();
				actorPlanMap.put(focalActorId, map);
			}
			map.put(planRec.key, planRec);
		}

		if (planRec.isActive) {
			activePlanCount++;
		}

		planningQueue.add(planRec);
	}

	protected void addReportPlan(Plan<ReportContext> plan) {

		if (planningQueueMode == PlanningQueueMode.CLOSED) {
			throw new ContractException(NucleusError.PLANNING_QUEUE_CLOSED);
		}
		validatePlanTime(plan.getTime());
		validateReportPlan(plan.getCallbackConsumer());

		final PlanRec planRec = new PlanRec();

		planRec.arrivalId = masterPlanningArrivalId++;

		planRec.plan = plan;
		planRec.isActive = false;
		planRec.planner = Planner.REPORT;
		planRec.time = plan.getTime();
		planRec.reportPlan = plan.getCallbackConsumer();
		planRec.key = plan.getKey();

		Map<Object, PlanRec> map;

		planRec.reportId = focalReportId;

		if (planRec.key != null) {
			map = reportPlanMap.get(focalReportId);
			if (map == null) {
				map = new LinkedHashMap<>();
				reportPlanMap.put(focalReportId, map);
			}
			map.put(planRec.key, planRec);
		}
		planningQueue.add(planRec);

	}

	protected void addDataManagerPlan(DataManagerId dataManagerId, Plan<DataManagerContext> plan) {

		if (planningQueueMode == PlanningQueueMode.CLOSED) {
			throw new ContractException(NucleusError.PLANNING_QUEUE_CLOSED);
		}
		validateDataManagerPlan(plan.getCallbackConsumer());
		validatePlanTime(plan.getTime());

		final PlanRec planRec = new PlanRec();

		planRec.arrivalId = masterPlanningArrivalId++;

		planRec.plan = plan;
		planRec.isActive = plan.isActive();
		planRec.planner = Planner.DATA_MANAGER;
		planRec.time = plan.getTime();
		planRec.dataManagerPlan = plan.getCallbackConsumer();
		planRec.key = plan.getKey();

		Map<Object, PlanRec> map;

		planRec.dataManagerId = dataManagerId;
		if (planRec.key != null) {
			map = dataManagerPlanMap.get(dataManagerId);
			if (map == null) {
				map = new LinkedHashMap<>();
				dataManagerPlanMap.put(dataManagerId, map);
			}
			map.put(planRec.key, planRec);
		}

		if (planRec.isActive) {
			activePlanCount++;
		}
		planningQueue.add(planRec);
	}

	protected void validateDataManagerPlanKeyNotDuplicate(DataManagerId dataManagerId, final Object key) {
		if (getDataManagerPlan(dataManagerId, key).isPresent()) {
			throw new ContractException(NucleusError.DUPLICATE_PLAN_KEY);
		}
	}

	protected void validateActorPlanKeyNotDuplicate(final Object key) {
		if (getActorPlan(key).isPresent()) {
			throw new ContractException(NucleusError.DUPLICATE_PLAN_KEY);
		}
	}

	protected void validateReportPlanKeyNotDuplicate(final Object key) {
		if (getReportPlan(key).isPresent()) {
			throw new ContractException(NucleusError.DUPLICATE_PLAN_KEY);
		}
	}

	@SuppressWarnings("unchecked")
	protected Optional<Plan<ActorContext>> removeActorPlan(final Object key) {
		validatePlanKeyNotNull(key);

		Map<Object, PlanRec> map = actorPlanMap.get(focalActorId);

		Plan<ActorContext> result = null;
		if (map != null) {
			final PlanRec planRecord = map.remove(key);
			if (planRecord != null) {
				result = (Plan<ActorContext>) planRecord.plan;
				planRecord.actorPlan = null;
				planRecord.plan = null;
			}
		}
		return Optional.ofNullable(result);

	}

	@SuppressWarnings("unchecked")
	protected Optional<Plan<ReportContext>> removeReportPlan(final Object key) {
		validatePlanKeyNotNull(key);

		Map<Object, PlanRec> map = reportPlanMap.get(focalReportId);

		Plan<ReportContext> result = null;
		if (map != null) {
			final PlanRec planRecord = map.remove(key);
			if (planRecord != null) {
				result = (Plan<ReportContext>) planRecord.plan;
				planRecord.reportPlan = null;
				planRecord.plan = null;
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
			g = Graphs.getEdgeReducedGraph(g);
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

	protected double getScheduledSimulationHaltTime() {
		return data.simulationHaltTime;
	}

	protected boolean stateRecordingIsScheduled() {
		return data.stateRecordingIsScheduled;
	}

	protected boolean plansRequirePlanData(double time) {
		if (data.stateRecordingIsScheduled) {
			if (time > data.simulationHaltTime) {
				return true;
			}
		}
		return false;
	}

	private final Map<DataManagerId, Map<Class<? extends PlanData>, Function<PlanData, Consumer<DataManagerContext>>>> dataManagerPlanDataConversionMap = new LinkedHashMap<>();

	@SuppressWarnings("unchecked")
	protected <T extends PlanData> void setDataManagerPlanDataConverter(DataManagerId dataManagerId, Class<T> planDataClass, Function<T, Consumer<DataManagerContext>> conversionFunction) {
		Map<Class<? extends PlanData>, Function<PlanData, Consumer<DataManagerContext>>> map = dataManagerPlanDataConversionMap.get(dataManagerId);

		if (map == null) {
			map = new LinkedHashMap<>();
			dataManagerPlanDataConversionMap.put(dataManagerId, map);
		}
		Function<PlanData, Consumer<DataManagerContext>> f = (planData) -> {
			return conversionFunction.apply((T) planData);
		};
		map.put(planDataClass, f);
	}

	private Consumer<DataManagerContext> getDataManagerContextConsumer(DataManagerId dataManagerId, PlanData planData) {
		Consumer<DataManagerContext> result = null;
		Map<Class<? extends PlanData>, Function<PlanData, Consumer<DataManagerContext>>> map = dataManagerPlanDataConversionMap.get(dataManagerId);
		if (map != null) {
			Function<PlanData, Consumer<DataManagerContext>> function = map.get(planData.getClass());
			if (function != null) {
				result = function.apply(planData);
			}
		}
		return result;
	}

	private final Map<ActorId, Map<Class<? extends PlanData>, Function<PlanData, Consumer<ActorContext>>>> actorPlanDataConversionMap = new LinkedHashMap<>();

	@SuppressWarnings("unchecked")
	protected <T extends PlanData> void setActorPlanDataConverter(Class<T> planDataClass, Function<T, Consumer<ActorContext>> conversionFunction) {
		Map<Class<? extends PlanData>, Function<PlanData, Consumer<ActorContext>>> map = actorPlanDataConversionMap.get(focalActorId);

		if (map == null) {
			map = new LinkedHashMap<>();
			actorPlanDataConversionMap.put(focalActorId, map);
		}
		Function<PlanData, Consumer<ActorContext>> f = (planData) -> {
			return conversionFunction.apply((T) planData);
		};
		map.put(planDataClass, f);
	}

	private Consumer<ActorContext> getActorContextConsumer(ActorId actorId, PlanData planData) {
		Consumer<ActorContext> result = null;
		Map<Class<? extends PlanData>, Function<PlanData, Consumer<ActorContext>>> map = actorPlanDataConversionMap.get(actorId);
		if (map != null) {
			Function<PlanData, Consumer<ActorContext>> function = map.get(planData.getClass());
			if (function != null) {
				result = function.apply(planData);
			}
		}
		return result;
	}

	private final Map<ReportId, Map<Class<? extends PlanData>, Function<PlanData, Consumer<ReportContext>>>> reportPlanDataConversionMap = new LinkedHashMap<>();

	@SuppressWarnings("unchecked")
	protected <T extends PlanData> void setReportPlanDataConverter(Class<T> planDataClass, Function<T, Consumer<ReportContext>> conversionFunction) {
		Map<Class<? extends PlanData>, Function<PlanData, Consumer<ReportContext>>> map = reportPlanDataConversionMap.get(focalReportId);

		if (map == null) {
			map = new LinkedHashMap<>();
			reportPlanDataConversionMap.put(focalReportId, map);
		}
		Function<PlanData, Consumer<ReportContext>> f = (planData) -> {
			return conversionFunction.apply((T) planData);
		};
		map.put(planDataClass, f);
	}

	private Consumer<ReportContext> getReportContextConsumer(ReportId reportId, PlanData planData) {
		Consumer<ReportContext> result = null;
		Map<Class<? extends PlanData>, Function<PlanData, Consumer<ReportContext>>> map = reportPlanDataConversionMap.get(reportId);
		if (map != null) {
			Function<PlanData, Consumer<ReportContext>> function = map.get(planData.getClass());
			if (function != null) {
				result = function.apply(planData);
			}
		}
		return result;
	}

	private void loadExistingPlans() {
		List<PlanQueueData> planQueueDatas = data.simulationState.getPlanQueueDatas();
		for (PlanQueueData planQueueData : planQueueDatas) {
			PlanRec planRec = new PlanRec();
			// convert the plan data into a consumer
			Planner planner = planQueueData.getPlanner();
			switch (planner) {
			case ACTOR:
				planRec.actorId = actorIds.get(planQueueData.getPlannerId());
				planRec.actorPlan = getActorContextConsumer(planRec.actorId, planQueueData.getPlanData());
				planRec.plan = Plan	.builder(ActorContext.class)//
									.setActive(planQueueData.isActive())//
									.setKey(planQueueData.getKey())//
									.setPlanData(planQueueData.getPlanData())//
									.setTime(planQueueData.getTime())//
									.setCallbackConsumer(planRec.actorPlan)//
									.build();

				break;
			case DATA_MANAGER:
				planRec.dataManagerId = dataManagerIds.get(planQueueData.getPlannerId());
				planRec.dataManagerPlan = getDataManagerContextConsumer(planRec.dataManagerId, planQueueData.getPlanData());
				planRec.plan = Plan	.builder(DataManagerContext.class)//
									.setActive(planQueueData.isActive())//
									.setKey(planQueueData.getKey())//
									.setPlanData(planQueueData.getPlanData())//
									.setTime(planQueueData.getTime())//
									.setCallbackConsumer(planRec.dataManagerPlan)//
									.build();

				break;
			case REPORT:
				planRec.reportId = reportIds.get(planQueueData.getPlannerId());
				planRec.reportPlan = getReportContextConsumer(planRec.reportId, planQueueData.getPlanData());
				planRec.plan = Plan	.builder(ReportContext.class)//
									.setActive(planQueueData.isActive())//
									.setKey(planQueueData.getKey())//
									.setPlanData(planQueueData.getPlanData())//
									.setTime(planQueueData.getTime())//
									.setCallbackConsumer(planRec.reportPlan)//
									.build();

				break;
			default:
				throw new RuntimeException("unhandled case " + planner);
			}
			planRec.arrivalId = planQueueData.getArrivalId();
			planRec.isActive = planQueueData.isActive();
			planRec.key = planQueueData.getKey();
			planRec.planner = planQueueData.getPlanner();
			planRec.time = planQueueData.getTime();

			if (planRec.isActive) {
				activePlanCount++;
			}
			if (planRec.plan.getCallbackConsumer() != null) {
				planningQueue.add(planRec);
				Map<Object, PlanRec> map;
				if (planRec.key != null) {
					switch (planner) {
					case ACTOR:
						map = actorPlanMap.get(planRec.actorId);
						if (map == null) {
							map = new LinkedHashMap<>();
							actorPlanMap.put(planRec.actorId, map);
						}
						map.put(planRec.key, planRec);
						break;
					case DATA_MANAGER:
						map = dataManagerPlanMap.get(planRec.dataManagerId);
						if (map == null) {
							map = new LinkedHashMap<>();
							dataManagerPlanMap.put(planRec.dataManagerId, map);
						}
						map.put(planRec.key, planRec);
						break;
					case REPORT:
						map = reportPlanMap.get(planRec.reportId);
						if (map == null) {
							map = new LinkedHashMap<>();
							reportPlanMap.put(planRec.reportId, map);
						}
						map.put(planRec.key, planRec);
						break;
					default:
						throw new RuntimeException("unhandled case " + planner);
					}
				}
			}
		}
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
		reportContext = new ReportContext(this);
		actorContext = new ActorContext(this);
		pluginContext = new PluginContext(this);

		// start the simulation
		if (started) {
			throw new ContractException(NucleusError.REPEATED_EXECUTION);
		}
		started = true;

		time = data.simulationState.getStartTime();

		masterPlanningArrivalId = data.simulationState.getPlanningQueueArrivalId();

		simulationHaltTime = data.simulationHaltTime;
		forcedHaltPresent = simulationHaltTime >= 0;

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
				List<PluginData> pluginDatas = basePluginDataMap.get(pluginData.getClass());
				if (pluginDatas == null) {
					pluginDatas = new ArrayList<>();
					basePluginDataMap.put(pluginData.getClass(), pluginDatas);
				}
				pluginDatas.add(pluginData);
			}
			focalPluginId = null;
		}

		// Have each plugin contribute data managers reports and actors
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
		eventProcessingAllowed = true;
		// execute the data manager queue -- this will in turn execute the
		// report queue
		executeDataManagerQueue();

		for (DataManager dataManager : dataManagerToDataManagerIdMap.keySet()) {
			if (!dataManager.isInitialized()) {
				throw new ContractException(NucleusError.DATA_MANAGER_INITIALIZATION_FAILURE, dataManager.getClass().getSimpleName());
			}
		}

		// initialize the actors by flushing the actor queue
		executeActorQueue();

		loadExistingPlans();

		planningQueueMode = PlanningQueueMode.RUNNING;

		while (activePlanCount > 0) {
			if (forcedHaltPresent) {
				if (planningQueue.peek().time > simulationHaltTime) {
					break;
				}
			}

			final PlanRec planRec = planningQueue.poll();
			// System.out.println(planRec);

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
					DataManagerContentRec dataManagerContentRec = new DataManagerContentRec();
					dataManagerContentRec.dmPlan = planRec.dataManagerPlan;
					dataManagerContentRec.dataManagerId = planRec.dataManagerId;
					dataManagerQueue.add(dataManagerContentRec);
					executeDataManagerQueue();
					executeActorQueue();
				}
				break;

			case REPORT:
				if (planRec.reportPlan != null) {
					if (planRec.key != null) {
						reportPlanMap.get(planRec.reportId).remove(planRec.key);
					}
					ReportContentRec reportContentRec = new ReportContentRec();
					reportContentRec.reportPlan = planRec.reportPlan;
					reportContentRec.reportId = planRec.reportId;
					reportQueue.add(reportContentRec);
					executeReportQueue();
				}

				break;

			default:
				throw new RuntimeException("unhandled planner type " + planRec.planner);
			}
		}

		if (forcedHaltPresent) {
			time = simulationHaltTime;
		}

		planningQueueMode = PlanningQueueMode.CLOSED;

		eventProcessingAllowed = false;

		// signal to the data managers that the simulation is closing
		for (DataManagerId dataManagerId : simulationCloseDataManagerCallbacks.keySet()) {
			DataManagerContext dataManagerContext = dataManagerIdToDataManagerContextMap.get(dataManagerId);
			for (Consumer<DataManagerContext> dataManagerCloseCallback : simulationCloseDataManagerCallbacks.get(dataManagerId)) {
				dataManagerCloseCallback.accept(dataManagerContext);
			}
		}

		// signal to the actors that the simulation is closing
		for (ActorId actorId : simulationCloseActorCallbacks.keySet()) {
			if (actorIds.get(actorId.getValue()) != null) {
				focalActorId = actorId;
				for (Consumer<ActorContext> simulationCloseCallback : simulationCloseActorCallbacks.get(actorId)) {
					simulationCloseCallback.accept(actorContext);
				}
				focalActorId = null;

			}
		}

		// signal to the reports that the simulation is closing
		for (ReportId reportId : simulationCloseReportCallbacks.keySet()) {
			focalReportId = reportId;
			for (Consumer<ReportContext> simulationCloseCallback : simulationCloseReportCallbacks.get(reportId)) {
				simulationCloseCallback.accept(reportContext);
			}
			focalReportId = null;
		}

		if (data.stateRecordingIsScheduled && outputConsumer != null) {
			SimulationState.Builder simulationStateBuilder = SimulationState.builder();
			simulationStateBuilder.setBaseDate(data.simulationState.getBaseDate());
			simulationStateBuilder.setStartTime(time);
			simulationStateBuilder.setPlanningQueueArrivalId(masterPlanningArrivalId);

			PlanQueueData.Builder planQueueDataBuilder = PlanQueueData.builder();
			while (!planningQueue.isEmpty()) {
				PlanRec planRec = planningQueue.poll();
				PlanData planData = planRec.plan.getPlanData();
				if (planData != null) {
					planQueueDataBuilder.setActive(planRec.isActive)//
										.setArrivalId(planRec.arrivalId)//
										.setKey(planRec.key)//
										.setPlanData(planData)//
										.setPlanner(planRec.planner)//
										.setTime(planRec.time);//

					switch (planRec.planner) {
					case ACTOR:
						planQueueDataBuilder.setPlannerId(planRec.actorId.getValue());
						break;
					case DATA_MANAGER:
						planQueueDataBuilder.setPlannerId(planRec.dataManagerId.getValue());
						break;
					case REPORT:
						planQueueDataBuilder.setPlannerId(planRec.reportId.getValue());
						break;
					default:
						throw new RuntimeException("unhandled case " + planRec.planner);
					}

					PlanQueueData planQueueData = planQueueDataBuilder.build();
					simulationStateBuilder.addPlanQueueData(planQueueData);
				}
			}

			outputConsumer.accept(simulationStateBuilder.build());
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

	private boolean dataManagerQueueActive;

	private void executeReportQueue() {
		while (!reportQueue.isEmpty()) {
			final ReportContentRec contentRec = reportQueue.pollFirst();
			if (contentRec.reportPlan != null) {
				focalReportId = contentRec.reportId;
				contentRec.reportPlan.accept(reportContext);
				focalReportId = null;
			} else {
				focalReportId = contentRec.reportId;
				contentRec.consumer.accept(contentRec.event);
				focalReportId = null;
			}
		}
	}

	protected void executeDataManagerQueue() {
		if (dataManagerQueueActive) {
			return;
		}
		dataManagerQueueActive = true;
		try {
			try {
				while (!dataManagerQueue.isEmpty()) {
					final DataManagerContentRec contentRec = dataManagerQueue.pollFirst();
					if (contentRec.dmPlan != null) {
						DataManagerContext dataManagerContext = dataManagerIdToDataManagerContextMap.get(contentRec.dataManagerId);
						contentRec.dmPlan.accept(dataManagerContext);
					} else {
						contentRec.consumer.accept(contentRec.event);
					}
				}
				executeReportQueue();
			} catch (Exception e) {
				dataManagerQueue.clear();
				throw (e);
			}
		} finally {
			dataManagerQueueActive = false;
		}
	}

	@SuppressWarnings("unchecked")
	protected Optional<Plan<ReportContext>> getReportPlan(final Object key) {
		validatePlanKeyNotNull(key);
		Map<Object, PlanRec> map = reportPlanMap.get(focalReportId);
		Plan<ReportContext> result = null;
		if (map != null) {
			final PlanRec planRecord = map.get(key);
			if (planRecord != null) {
				result = (Plan<ReportContext>) planRecord.plan;
			}
		}
		return Optional.ofNullable(result);
	}

	@SuppressWarnings("unchecked")
	protected Optional<Plan<ActorContext>> getActorPlan(final Object key) {
		validatePlanKeyNotNull(key);
		Map<Object, PlanRec> map = actorPlanMap.get(focalActorId);
		Plan<ActorContext> result = null;
		if (map != null) {
			final PlanRec planRecord = map.get(key);
			if (planRecord != null) {
				result = (Plan<ActorContext>) planRecord.plan;
			}
		}
		return Optional.ofNullable(result);
	}

	@SuppressWarnings("unchecked")
	protected Optional<Plan<DataManagerContext>> getDataManagerPlan(DataManagerId dataManagerId, final Object key) {
		validatePlanKeyNotNull(key);
		Map<Object, PlanRec> map = dataManagerPlanMap.get(dataManagerId);
		Plan<DataManagerContext> result = null;
		if (map != null) {
			final PlanRec planRecord = map.get(key);
			if (planRecord != null) {
				result = (Plan<DataManagerContext>) planRecord.plan;
			}
		}
		return Optional.ofNullable(result);
	}

	protected List<Object> getActorPlanKeys() {
		Map<Object, PlanRec> map = actorPlanMap.get(focalActorId);
		if (map != null) {
			return new ArrayList<>(map.keySet());
		}
		return new ArrayList<>();
	}

	protected List<Object> getReportPlanKeys() {
		Map<Object, PlanRec> map = reportPlanMap.get(focalReportId);
		if (map != null) {
			return new ArrayList<>(map.keySet());
		}
		return new ArrayList<>();
	}

	protected List<Object> getDataManagerPlanKeys(DataManagerId dataManagerId) {
		Map<Object, PlanRec> map = dataManagerPlanMap.get(dataManagerId);
		if (map != null) {
			return new ArrayList<>(map.keySet());
		}
		return new ArrayList<>();
	}

	protected void halt() {
		if (!data.stateRecordingIsScheduled) {
			simulationHaltTime = time;
			forcedHaltPresent = true;
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
	protected Optional<Plan<DataManagerContext>> removeDataManagerPlan(DataManagerId dataManagerId, final Object key) {
		validatePlanKeyNotNull(key);

		Map<Object, PlanRec> map = dataManagerPlanMap.get(dataManagerId);
		Plan<DataManagerContext> result = null;
		if (map != null) {
			final PlanRec planRecord = map.remove(key);
			if (planRecord != null) {
				result = (Plan<DataManagerContext>) planRecord.plan;
				planRecord.dataManagerPlan = null;
				planRecord.plan = null;
			}
		}
		return Optional.ofNullable(result);
	}

	protected void validatePlanKeyNotNull(final Object key) {
		if (key == null) {
			throw new ContractException(NucleusError.NULL_PLAN_KEY, "");
		}
	}

	private void validatePlanTime(final double planTime) {
		if (planTime < time) {
			throw new ContractException(NucleusError.PAST_PLANNING_TIME);
		}
	}

	protected void releaseOutput(Object output) {
		if (outputConsumer != null) {
			outputConsumer.accept(output);
		}
	}

	protected boolean actorExists(final ActorId actorId) {
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

	protected void subscribeReportToSimulationClose(Consumer<ReportContext> consumer) {
		if (consumer == null) {
			throw new ContractException(NucleusError.NULL_REPORT_CONTEXT_CONSUMER);
		}
		List<Consumer<ReportContext>> list = simulationCloseReportCallbacks.get(focalReportId);
		if (list == null) {
			list = new ArrayList<>();
			simulationCloseReportCallbacks.put(focalReportId, list);
		}
		list.add(consumer);
	}

	protected void subscribeActorToSimulationClose(Consumer<ActorContext> consumer) {
		if (consumer == null) {
			throw new ContractException(NucleusError.NULL_ACTOR_CONTEXT_CONSUMER);
		}
		List<Consumer<ActorContext>> list = simulationCloseActorCallbacks.get(focalActorId);
		if (list == null) {
			list = new ArrayList<>();
			simulationCloseActorCallbacks.put(focalActorId, list);
		}
		list.add(consumer);
	}

	protected void subscribeDataManagerToSimulationClose(DataManagerId dataManagerId, Consumer<DataManagerContext> consumer) {
		if (consumer == null) {
			throw new ContractException(NucleusError.NULL_DATA_MANAGER_CONTEXT_CONSUMER);
		}
		List<Consumer<DataManagerContext>> list = simulationCloseDataManagerCallbacks.get(dataManagerId);
		if (list == null) {
			list = new ArrayList<>();
			simulationCloseDataManagerCallbacks.put(dataManagerId, list);
		}
		list.add(consumer);
	}

	@SuppressWarnings("unchecked")
	protected <T extends Event> void subscribeDataManagerToEvent(DataManagerId dataManagerId, Class<T> eventClass, BiConsumer<DataManagerContext, T> eventConsumer) {

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

		for (DataManagerEventConsumer dataManagerEventConsumer : list) {
			if (dataManagerEventConsumer.dataManagerId.equals(dataManagerId)) {
				throw new ContractException(NucleusError.DUPLICATE_EVENT_SUBSCRIPTION);
			}
		}

		DataManagerContext dataManagerContext = dataManagerIdToDataManagerContextMap.get(dataManagerId);
		DataManagerEventConsumer dataManagerEventConsumer = new DataManagerEventConsumer(dataManagerId, event -> eventConsumer.accept(dataManagerContext, (T) event));

		list.add(dataManagerEventConsumer);
		Collections.sort(list);

	}

	protected void unsubscribeDataManagerFromEvent(DataManagerId dataManagerId, Class<? extends Event> eventClass) {
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

	@SuppressWarnings("unchecked")
	protected <T extends Event> void subscribeReportToEvent(Class<T> eventClass, BiConsumer<ReportContext, T> eventConsumer) {

		if (eventClass == null) {
			throw new ContractException(NucleusError.NULL_EVENT_CLASS);
		}

		if (eventConsumer == null) {
			throw new ContractException(NucleusError.NULL_EVENT_CONSUMER);
		}

		List<ReportEventConsumer> list = reportEventMap.get(eventClass);
		if (list == null) {
			list = new ArrayList<>();
			reportEventMap.put(eventClass, list);
		}

		for (ReportEventConsumer reportEventConsumer : list) {
			if (reportEventConsumer.reportId.equals(focalReportId)) {
				throw new ContractException(NucleusError.DUPLICATE_EVENT_SUBSCRIPTION);
			}
		}

		ReportEventConsumer reportEventConsumer = new ReportEventConsumer(focalReportId, event -> eventConsumer.accept(reportContext, (T) event));

		list.add(reportEventConsumer);

	}

	protected void unsubscribeReportFromEvent(Class<? extends Event> eventClass) {
		if (eventClass == null) {
			throw new ContractException(NucleusError.NULL_EVENT_CLASS);
		}

		List<ReportEventConsumer> list = reportEventMap.get(eventClass);

		if (list != null) {
			Iterator<ReportEventConsumer> iterator = list.iterator();
			while (iterator.hasNext()) {
				ReportEventConsumer reportEventConsumer = iterator.next();
				if (reportEventConsumer.reportId.equals(focalReportId)) {
					iterator.remove();
				}
			}

			if (list.isEmpty()) {
				reportEventMap.remove(eventClass);
			}
		}

	}

	protected void releaseObservationEventForDataManager(final Event event) {

		if (event == null) {
			throw new ContractException(NucleusError.NULL_EVENT);
		}

		if (!dataManagerQueueActive) {
			throw new ContractException(NucleusError.OBSERVATION_EVENT_IMPROPER_RELEASE);
		}

		// queue the event handling by reports
		List<ReportEventConsumer> reportConsumers = reportEventMap.get(event.getClass());
		if (reportConsumers != null) {
			for (ReportEventConsumer reportEventConsumer : reportConsumers) {
				ReportContentRec reportContentRec = new ReportContentRec();
				reportContentRec.event = event;
				reportContentRec.consumer = reportEventConsumer;
				reportContentRec.reportId = reportEventConsumer.reportId;
				reportQueue.add(reportContentRec);
			}
		}

		// queue the event handling for actors
		broadcastEventToFilterNode(event, rootNode);

		// queue the event handling by data managers
		List<DataManagerEventConsumer> dataManagerEventConsumers = dataManagerEventMap.get(event.getClass());
		if (dataManagerEventConsumers != null) {
			for (DataManagerEventConsumer dataManagerEventConsumer : dataManagerEventConsumers) {

				DataManagerContentRec dataManagerContentRec = new DataManagerContentRec();
				dataManagerContentRec.event = event;
				dataManagerContentRec.consumer = dataManagerEventConsumer;
				dataManagerContentRec.dataManagerId = dataManagerEventConsumer.dataManagerId;
				dataManagerQueue.add(dataManagerContentRec);

			}
		}
	}

	protected void releaseMutationEventForDataManager(final Event event) {

		if (event == null) {
			throw new ContractException(NucleusError.NULL_EVENT);
		}

		if (focalReportId != null) {
			throw new ContractException(NucleusError.REPORT_ATTEMPTING_MUTATION, focalReportId);
		}

		if (!eventProcessingAllowed) {
			throw new ContractException(NucleusError.DATA_MANAGER_ATTEMPTING_MUTATION);
		}

		// queue the event handling by data managers
		List<DataManagerEventConsumer> dataManagerEventConsumers = dataManagerEventMap.get(event.getClass());
		if (dataManagerEventConsumers != null) {
			for (DataManagerEventConsumer dataManagerEventConsumer : dataManagerEventConsumers) {

				DataManagerContentRec dataManagerContentRec = new DataManagerContentRec();
				dataManagerContentRec.event = event;
				dataManagerContentRec.consumer = dataManagerEventConsumer;
				dataManagerContentRec.dataManagerId = dataManagerEventConsumer.dataManagerId;
				dataManagerQueue.add(dataManagerContentRec);

			}
		}

		executeDataManagerQueue();
	}

	protected ActorId addActor(Consumer<ActorContext> consumer) {

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

	protected ActorId addActorForPlugin(Consumer<ActorContext> consumer) {

		if (focalPluginId == null) {
			throw new ContractException(NucleusError.PLUGIN_INITIALIZATION_CLOSED);
		}
		return addActor(consumer);

	}

	protected void addReportForPlugin(Consumer<ReportContext> consumer) {
		if (focalPluginId == null) {
			throw new ContractException(NucleusError.PLUGIN_INITIALIZATION_CLOSED);
		}

		if (consumer == null) {
			throw new ContractException(NucleusError.NULL_REPORT_CONTEXT_CONSUMER);
		}

		ReportId reportId = new ReportId(reportIds.size());
		reportIds.add(reportId);

		final ReportContentRec reportContentRec = new ReportContentRec();
		reportContentRec.reportId = reportId;
		reportContentRec.reportPlan = consumer;
		reportQueue.add(reportContentRec);

	}

	protected void removeActor(final ActorId actorId) {
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
	protected <T extends DataManager> T getDataManagerForActor(Class<T> dataManagerClass) {

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
	protected <T extends DataManager> T getDataManagerForDataManager(DataManagerId dataManagerId, Class<T> dataManagerClass) {

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

	private static class ReportEventConsumer implements Consumer<Event> {

		private final Consumer<Event> consumer;
		private final ReportId reportId;

		public <T extends Event> ReportEventConsumer(ReportId reportId, Consumer<Event> consumer) {
			this.consumer = consumer;
			this.reportId = reportId;
		}

		@Override
		public void accept(Event event) {
			consumer.accept(event);
		}
	}

	// used for subscriptions
	private final Map<Class<? extends Event>, List<DataManagerEventConsumer>> dataManagerEventMap = new LinkedHashMap<>();
	private final Map<Class<? extends Event>, List<ReportEventConsumer>> reportEventMap = new LinkedHashMap<>();

	// used for retrieving and canceling plans owned by data managers
	private final Map<DataManagerId, Map<Object, PlanRec>> dataManagerPlanMap = new LinkedHashMap<>();

	// used to locate data managers by class type
	private Map<Class<?>, DataManager> baseClassToDataManagerMap = new LinkedHashMap<>();
	private Map<Class<?>, DataManager> workingClassToDataManagerMap = new LinkedHashMap<>();

	/*
	 * Maps of data manager id <--> data manager instances used primarily for
	 * access permissions between data managers
	 */
	private List<DataManagerId> dataManagerIds = new ArrayList<>();
	private Map<DataManagerId, DataManager> dataManagerIdToDataManagerMap = new LinkedHashMap<>();
	private Map<DataManager, DataManagerId> dataManagerToDataManagerIdMap = new LinkedHashMap<>();

	private Map<DataManagerId, DataManagerContext> dataManagerIdToDataManagerContextMap = new LinkedHashMap<>();

	private Map<DataManagerId, PluginId> dataManagerIdToPluginIdMap = new LinkedHashMap<>();
	private boolean[][] dataManagerAccessPermissions;

	//////////////////////////////
	// actor support
	//////////////////////////////

	private ActorContext actorContext;
	private ReportContext reportContext;

	private final List<ActorId> actorIds = new ArrayList<>();
	private final List<ReportId> reportIds = new ArrayList<>();

	private boolean containsDeletedActors;

	private final Map<ActorId, Map<Object, PlanRec>> actorPlanMap = new LinkedHashMap<>();
	private final Map<ReportId, Map<Object, PlanRec>> reportPlanMap = new LinkedHashMap<>();

	private final Deque<ActorContentRec> actorQueue = new ArrayDeque<>();
	private final Deque<DataManagerContentRec> dataManagerQueue = new ArrayDeque<>();
	private final Deque<ReportContentRec> reportQueue = new ArrayDeque<>();

	protected ActorId focalActorId;
	protected ReportId focalReportId;

	private static class ActorContentRec {

		private Event event;

		private Consumer<Event> consumer;

		private Consumer<ActorContext> plan;

		private ActorId actorId;

	}

	private static class ReportContentRec {

		private Event event;

		private Consumer<Event> consumer;

		private Consumer<ReportContext> reportPlan;

		private ReportId reportId;

	}

	private static class DataManagerContentRec {

		private Event event;

		private Consumer<Event> consumer;

		private Consumer<DataManagerContext> dmPlan;

		private DataManagerId dataManagerId;

	}

	protected boolean subscribersExistForEvent(Class<? extends Event> eventClass) {
		return (reportEventMap.containsKey(eventClass) || dataManagerEventMap.containsKey(eventClass) || rootNode.children.containsKey(eventClass) || rootNode.consumers.containsKey(eventClass));
	}

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
				filterNode.function = event -> identifiableFunction.getFunction().apply((T) event);
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
	protected <T extends Event> void subscribeActorToEventByFilter(EventFilter<T> eventFilter, BiConsumer<ActorContext, T> eventConsumer) {
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
		for (Pair<IdentifiableFunction<T>, Object> pair : eventFilter.getFunctionValuePairs()) {
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
		Consumer<Event> previousConsumer = consumerMap.put(focalActorId, consumer);
		if (previousConsumer != null) {
			throw new ContractException(NucleusError.DUPLICATE_EVENT_SUBSCRIPTION);
		}

	}

	/*
	 * Removes the consumer from the filter node tree and removes nodes that are
	 * empty(no children, no consumers), except for the root node.
	 */
	protected <T extends Event> void unsubscribeActorFromEventByFilter(EventFilter<T> eventFilter) {
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
		for (Pair<IdentifiableFunction<T>, Object> pair : eventFilter.getFunctionValuePairs()) {
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
		if (consumerMap.isEmpty()) {
			filterNode.consumers.remove(value);
		}

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