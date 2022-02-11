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
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.apache.commons.math3.util.FastMath;

import net.jcip.annotations.NotThreadSafe;

import util.ContractException;
import util.graph.Graph;
import util.graph.GraphDepthEvaluator;
import util.graph.Graphs;
import util.graph.MutableGraph;

/**
 * An instance of the Engine orchestrates the execution of a simulation instance
 * from a set of contributed plugins. Plugins are loaded from the included
 * builder class and organized based upon their dependency requirementsF. Each
 * plugin contributes zero to many initialization behaviors that 1) start the
 * simulation, 2)initialize and publish data views, 3)create agents, 4) generate
 * initial events(mutations to data views), 5) register for event observation
 * and 6)schedule future plans. Time moves forward via planning and the
 * simulation halts once all plans are complete.
 * 
 * @author Shawn Hatch
 *
 */
@NotThreadSafe
public class Simulation {

	private class PluginContextImpl implements PluginContext {

		@Override
		public void addPluginDependency(PluginId pluginId) {
			if (focalPluginId == null) {
				throw new ContractException(NucleusError.PLUGIN_INITIALIZATION_CLOSED);
			}
			pluginDependencyGraph.addEdge(new Object(), focalPluginId, pluginId);
		}

		@Override
		public void addDataManager(DataManager dataManager) {

			if (focalPluginId == null) {
				throw new ContractException(NucleusError.PLUGIN_INITIALIZATION_CLOSED);
			}

			Set<DataManager> set = dataMangagersMap.get(focalPluginId);
			if (set == null) {
				set = new LinkedHashSet<>();
				dataMangagersMap.put(focalPluginId, set);
			}
			set.add(dataManager);

		}

		@Override
		public void addAgent(AgentId agentId, Consumer<AgentContext> consumer) {
			if (agentId == null) {
				throw new ContractException(NucleusError.NULL_AGENT_ID);
			}

			if (consumer == null) {
				throw new ContractException(NucleusError.NULL_AGENT_CONTEXT_CONSUMER);
			}

			if (focalPluginId == null) {
				throw new ContractException(NucleusError.PLUGIN_INITIALIZATION_CLOSED);
			}
			Map<AgentId, Consumer<AgentContext>> map = agentsMap.get(focalPluginId);
			if(map == null) {
				map = new LinkedHashMap<>();
				agentsMap.put(focalPluginId, map);
			}

			map.put(agentId, consumer);
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T extends PluginData> Optional<T> getPluginData(Class<T> pluginDataClass) {
			return Optional.ofNullable((T) pluginDataMap.get(pluginDataClass));
		}

	}

	

	private class AgentContextImpl implements AgentContext {

		@Override
		public void addPlan(final Consumer<AgentContext> plan, final double planTime) {
			addAgentPlan(plan, planTime, true, null);
		}

		@Override
		public void addKeyedPlan(final Consumer<AgentContext> plan, final double planTime, final Object key) {
			validatePlanKeyNotNull(key);
			validateAgentPlanKeyNotDuplicate(key);
			addAgentPlan(plan, planTime, true, key);
		}

		@Override
		public void addPassivePlan(final Consumer<AgentContext> plan, final double planTime) {
			addAgentPlan(plan, planTime, false, null);
		}

		@Override
		public void addPassiveKeyedPlan(final Consumer<AgentContext> plan, final double planTime, final Object key) {
			validatePlanKeyNotNull(key);
			validateAgentPlanKeyNotDuplicate(key);
			addAgentPlan(plan, planTime, false, key);
		}

		@Override
		public boolean agentExists(final AgentId agentId) {
			return Simulation.this.agentExists(agentId);
		}

		@Override
		public AgentId getCurrentAgentId() {
			return focalAgentId;
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T extends DataManager> Optional<T> getDataManager(Class<T> dataManagerClass) {
			return Optional.ofNullable((T) dataManagerMap.get(dataManagerClass));
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T extends Consumer<AgentContext>> Optional<T> getPlan(final Object key) {
			return (Optional<T>) Simulation.this.getAgentPlan(key);
		}

		@Override
		public List<Object> getPlanKeys() {
			return Simulation.this.getAgentPlanKeys();
		}

		@Override
		public Optional<Double> getPlanTime(final Object key) {
			return Simulation.this.getAgentPlanTime(key);
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
			return Simulation.this.removeAgentPlan(key);
		}

		@Override
		public void releaseOutput(Object output) {
			Simulation.this.releaseOutput(output);

		}

		@Override
		public <T extends Event> void subscribe(EventLabel<T> eventLabel, AgentEventConsumer<T> agentEventConsumer) {
			Simulation.this.subscribeAgentToEvent(eventLabel, agentEventConsumer);
		}

		@Override
		public <T extends Event> void subscribe(Class<T> eventClass, AgentEventConsumer<T> agentConsumer) {
			Simulation.this.subscribeAgentToEvent(eventClass, agentConsumer);
		}

		@Override
		public <T extends Event> void unsubscribe(EventLabel<T> eventLabel) {
			Simulation.this.unsubscribeAgentFromEvent(eventLabel);
		}

		@Override
		public <T extends Event> void addEventLabeler(EventLabeler<T> eventLabeler) {
			Simulation.this.addEventLabeler(eventLabeler);
		}

		@Override
		public void subscribeToSimulationClose(Consumer<AgentContext> closeHandler) {
			Simulation.this.subscribeAgentToSimulationClose(closeHandler);

		}

	}

	private static final String LINE_SEPARATOR = System.getProperty("line.separator");

	private static class PlanRec {

		private Planner planner;
		private double time;
		private long arrivalId;
		private boolean isActive;

		private Consumer<AgentContext> agentPlan;
		private AgentId agentId;

		private Consumer<DataManagerContext> resolverPlan;
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
			case AGENT:
				builder.append(agentId);
				break;
			case DATA_MANAGER:
				builder.append(dataManagerId);
				break;
			default:
				throw new RuntimeException("unhandled case");

			}
			builder.append(LINE_SEPARATOR);
			builder.append(LINE_SEPARATOR);

			return builder.toString();
		}
	}

	private static class AgentContentRec {

		private Event event;

		private MetaAgentEventConsumer<?> metaAgentEventConsumer;

		private Consumer<AgentContext> plan;

		private AgentId agentId;

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

		public Builder addPluginInitializer(PluginInitializer pluginInitializer) {
			if (pluginInitializer == null) {
				throw new ContractException(NucleusError.NULL_PLUGIN_INITIALIZER);
			}
			data.pluginInitializers.add(pluginInitializer);
			return this;
		}

		/**
		 * Adds a plugin to this builder for inclusion in the simulation
		 * 
		 * @throws ContractException
		 *             <li>{@link NucleusError#NULL_PLUGIN_DATA} if the plugin
		 *             data is null
		 */
		public Builder addPluginData(PluginData pluginData) {
			if (pluginData == null) {
				throw new ContractException(NucleusError.NULL_PLUGIN_DATA);
			}
			data.pluginDatas.add(pluginData);
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
		DATA_MANAGER, AGENT
	}

	private static class DataManagerContextImpl implements DataManagerContext {
		private final DataManagerId dataManagerId;
		private final Simulation simulation;

		private DataManagerContextImpl(Simulation simulation, DataManagerId dataManagerId) {
			this.simulation = simulation;
			this.dataManagerId = dataManagerId;
		}

		@Override
		public void addAgent(Consumer<AgentContext> init, AgentId agentId) {
			if (agentId == null) {
				throw new ContractException(NucleusError.NULL_AGENT_ID);
			}

			if (init == null) {
				throw new ContractException(NucleusError.NULL_AGENT_CONTEXT_CONSUMER);
			}

			if (simulation.agentIds.contains(agentId)) {
				throw new ContractException(NucleusError.AGENT_ID_IN_USE, agentId);
			}

			simulation.agentIds.add(agentId);

			final AgentContentRec agentContentRec = new AgentContentRec();
			agentContentRec.agentId = agentId;
			agentContentRec.plan = init;
			simulation.agentQueue.add(agentContentRec);

		}

		@Override
		public void addPlan(final Consumer<DataManagerContext> plan, final double planTime) {
			simulation.addResolverPlan(dataManagerId, plan, planTime, true, null);
		}

		@Override
		public void addKeyedPlan(final Consumer<DataManagerContext> plan, final double planTime, final Object key) {
			simulation.validatePlanKeyNotNull(key);
			simulation.validateResolverPlanKeyNotDuplicate(dataManagerId, key);
			simulation.addResolverPlan(dataManagerId, plan, planTime, true, key);
		}

		@Override
		public void addPassivePlan(final Consumer<DataManagerContext> plan, final double planTime) {
			simulation.addResolverPlan(dataManagerId, plan, planTime, false, null);
		}

		@Override
		public void addKeyedPassivePlan(final Consumer<DataManagerContext> plan, final double planTime, final Object key) {
			simulation.validatePlanKeyNotNull(key);
			simulation.validateResolverPlanKeyNotDuplicate(dataManagerId, key);
			simulation.addResolverPlan(dataManagerId, plan, planTime, false, key);
		}

		@Override
		public boolean agentExists(final AgentId agentId) {
			return simulation.agentExists(agentId);
		}

		@Override
		public Optional<AgentId> getCurrentAgentId() {
			return Optional.ofNullable(simulation.focalAgentId);
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T extends DataManager> Optional<T> getDataManager(Class<T> dataManagerClass) {
			return Optional.ofNullable((T) simulation.dataManagerMap.get(dataManagerClass));
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T extends Consumer<DataManagerContext>> T getPlan(final Object key) {
			return (T) simulation.getResolverPlan(dataManagerId, key);
		}

		@Override
		public List<Object> getPlanKeys() {
			return simulation.getResolverPlanKeys(dataManagerId);
		}

		@Override
		public double getPlanTime(final Object key) {
			return simulation.getResolverPlanTime(dataManagerId, key);
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
		public void resolveEvent(final Event event) {
			simulation.resolveEventForDataManager(event);

		}

		@Override
		public void removeAgent(final AgentId agentId) {
			if (agentId == null) {
				throw new ContractException(NucleusError.NULL_AGENT_ID);
			}

			boolean removed = simulation.agentIds.remove(agentId);

			if (!removed) {
				throw new ContractException(NucleusError.UNKNOWN_AGENT_ID);
			}

			simulation.containsDeletedAgents = true;
		}

		@Override
		public <T> Optional<T> removePlan(final Object key) {
			return simulation.removeResolverPlan(dataManagerId, key);
		}

		@Override
		public void releaseOutput(Object output) {
			simulation.releaseOutput(output);
		}

		@Override
		public void unSubscribeToEvent(Class<? extends Event> eventClass) {
			simulation.unSubscribeResolverToEvent(dataManagerId, eventClass);
		}

		@Override
		public <T extends Event> void addEventLabeler(EventLabeler<T> eventLabeler) {
			simulation.addEventLabeler(eventLabeler);
		}


		@Override
		public boolean subscribersExistForEvent(Class<? extends Event> eventClass) {
			return simulation.subscribersExistForEvent(eventClass);
		}


		@Override
		public <T extends Event> void subscribeToEventPostPhase(Class<T> eventClass, DataManagerEventConsumer<T> resolverConsumer) {
			simulation.subscribeResolverToEventPostPhase(dataManagerId, eventClass, resolverConsumer);
		}

		@Override
		public <T extends Event> void subscribeToEventExecutionPhase(Class<T> eventClass, DataManagerEventConsumer<T> resolverConsumer) {
			simulation.subscribeResolverToEventExecutionPhase(dataManagerId, eventClass, resolverConsumer);
		}

	}

	private static class Data {
		private List<PluginInitializer> pluginInitializers = new ArrayList<>();
		private List<PluginData> pluginDatas = new ArrayList<>();
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

	private Map<PluginId, Map<AgentId, Consumer<AgentContext>>> agentsMap = new LinkedHashMap<>();

	// planning
	private long masterPlanningArrivalId;
	private double time;
	private boolean processEvents = true;
	private int activePlanCount;
	private final PriorityQueue<PlanRec> planningQueue = new PriorityQueue<>(futureComparable);

	// agents

	private final Map<Class<? extends Event>, Map<AgentId, MetaAgentEventConsumer<?>>> agentEventMap = new LinkedHashMap<>();

	private final Map<AgentId, Consumer<AgentContext>> simulationCloseCallbacks = new LinkedHashMap<>();

	
	private final AgentContext agentContext = new AgentContextImpl();
	
	private final Set<AgentId> agentIds = new LinkedHashSet<>();
	
	private boolean containsDeletedAgents;

	private final Map<AgentId, Map<Object, PlanRec>> agentPlanMap = new LinkedHashMap<>();
	
	private final Deque<AgentContentRec> agentQueue = new ArrayDeque<>();
	
	private AgentId focalAgentId;

	private boolean started;

	private final PluginContext pluginContext = new PluginContextImpl();
	
	private PluginId focalPluginId;

	private final Map<Class<?>, PluginData> pluginDataMap = new LinkedHashMap<>();

	private final Data data;

	private Simulation(Data data) {
		this.data = data;
	}

	private void validateAgentPlan(final Consumer<AgentContext> plan) {
		if (plan == null) {
			throw new ContractException(NucleusError.NULL_PLAN);
		}
	}

	private void validateResolverPlan(final Consumer<DataManagerContext> plan) {
		if (plan == null) {
			throw new ContractException(NucleusError.NULL_PLAN);
		}
	}

	private void addAgentPlan(final Consumer<AgentContext> plan, final double time, final boolean isActivePlan, final Object key) {

		validatePlanTime(time);
		validateAgentPlan(plan);

		final PlanRec planRec = new PlanRec();
		planRec.isActive = isActivePlan;
		planRec.arrivalId = masterPlanningArrivalId++;
		planRec.planner = Planner.AGENT;
		planRec.time = FastMath.max(time, this.time);
		planRec.agentPlan = plan;
		planRec.key = key;

		Map<Object, PlanRec> map;

		planRec.agentId = focalAgentId;

		if (key != null) {
			map = agentPlanMap.get(focalAgentId);
			if (map == null) {
				map = new LinkedHashMap<>();
				agentPlanMap.put(focalAgentId, map);
			}
			map.put(key, planRec);
		}

		if (isActivePlan) {
			activePlanCount++;
		}
		planningQueue.add(planRec);

	}

	private void addResolverPlan(final DataManagerId dataManagerId, final Consumer<DataManagerContext> plan, final double time, final boolean isActivePlan, final Object key) {

		validateResolverPlan(plan);
		validatePlanTime(time);

		final PlanRec planRec = new PlanRec();
		planRec.isActive = isActivePlan;
		planRec.arrivalId = masterPlanningArrivalId++;
		planRec.planner = Planner.DATA_MANAGER;
		planRec.time = FastMath.max(time, this.time);
		planRec.resolverPlan = plan;
		planRec.key = key;

		Map<Object, PlanRec> map;

		planRec.dataManagerId = dataManagerId;
		if (key != null) {
			map = resolverPlanMap.get(dataManagerId);
			if (map == null) {
				map = new LinkedHashMap<>();
				resolverPlanMap.put(dataManagerId, map);
			}
			map.put(key, planRec);
		}

		if (isActivePlan) {
			activePlanCount++;
		}
		planningQueue.add(planRec);
	}

	private void validateResolverPlanKeyNotDuplicate(DataManagerId dataManagerId, final Object key) {
		if (getResolverPlan(dataManagerId, key) != null) {
			throw new ContractException(NucleusError.DUPLICATE_PLAN_KEY);
		}
	}

	private void validateAgentPlanKeyNotDuplicate(final Object key) {
		if (getAgentPlan(key).isPresent()) {
			throw new ContractException(NucleusError.DUPLICATE_PLAN_KEY);
		}
	}

	@SuppressWarnings("unchecked")
	private <T> Optional<T> removeAgentPlan(final Object key) {
		validatePlanKeyNotNull(key);

		Map<Object, PlanRec> map = agentPlanMap.get(focalAgentId);

		T result = null;
		if (map != null) {
			final PlanRec planRecord = map.remove(key);
			if (planRecord != null) {
				result = (T) planRecord.agentPlan;
				planRecord.agentPlan = null;
			}
		}
		return Optional.ofNullable(result);

	}

	private List<PluginId> getOrderedPluginIds() {
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

		return graphDepthEvaluator.getNodesInRankOrder();
	}

	/**
	 * Executes this Simulation instance. Contributed plugin initializers are
	 * accessed in the order of their addition to the builder. Agents and data
	 * managers are organized based on their plugin dependency ordering. Time
	 * starts at zero and flows based on planning. When all plans are executed,
	 * time stops and the simulation halts.
	 * 
	 * @throws ContractException
	 *             <li>{@link NucleusError#REPEATED_EXECUTION} if execute is
	 *             invoked more than once
	 * 
	 *             <li>{@link NucleusError#CIRCULAR_PLUGIN_DEPENDENCIES} if the
	 *             contributed plugin initializers form a circular chain of
	 *             dependencies
	 * 
	 */
	public void execute() {
		if (started) {
			throw new ContractException(NucleusError.REPEATED_EXECUTION);
		}
		started = true;

		// set the output consumer
		outputConsumer = data.outputConsumer;

		// Make the plugin data available to the plugin initializers
		for (PluginData pluginData : data.pluginDatas) {
			pluginDataMap.put(pluginData.getClass(), pluginData);
		}

		for (PluginInitializer pluginInitializer : data.pluginInitializers) {
			focalPluginId = pluginInitializer.getPluginId();
			pluginDependencyGraph.addNode(focalPluginId);
			pluginInitializer.init(pluginContext);
			focalPluginId = null;
		}

		/*
		 * Retrieve the data managers and agents from the plugins in the correct order
		 */
		for (PluginId pluginId : getOrderedPluginIds()) {
			
			Set<DataManager> dataManagerSet = dataMangagersMap.get(pluginId);
			if (dataManagerSet != null) {
				for (DataManager dataManager : dataManagerSet) {
					DataManagerId dataManagerId = new DataManagerId(masterDataManagerIndex++);
					DataManagerContext dataManagerContext = new DataManagerContextImpl(this, dataManagerId);
					dataManagerContextMap.put(dataManagerId, dataManagerContext);
					dataManagerMap.put(dataManager.getClass(), dataManager);
					dataManager.init(dataManagerContext);
				}
			}
			Map<AgentId, Consumer<AgentContext>> map = agentsMap.get(pluginId);
			for (AgentId agentId : map.keySet()) {
				agentIds.add(agentId);
				final AgentContentRec agentContentRec = new AgentContentRec();
				agentContentRec.agentId = agentId;
				agentContentRec.plan = map.get(agentId);
				agentQueue.add(agentContentRec);
			}
		}
		
		//TODO -- the data managers should all be in place before initializing any of them
//		for(DataManager dataManager : dataManagerMap.values()) {
//			
//		};

		// flush the agent queue
		executeAgentQueue();

		while (processEvents && (activePlanCount > 0)) {
			final PlanRec planRec = planningQueue.poll();
			time = planRec.time;
			if (planRec.isActive) {
				activePlanCount--;
			}
			switch (planRec.planner) {
			case AGENT:
				
				if (planRec.agentPlan != null) {
					if (planRec.key != null) {
						agentPlanMap.get(planRec.agentId).remove(planRec.key);
					}
					AgentContentRec agentContentRec = new AgentContentRec();
					agentContentRec.agentId = planRec.agentId;
					agentContentRec.plan = planRec.agentPlan;
					agentQueue.add(agentContentRec);
					executeAgentQueue();					
				}
				break;			
			case DATA_MANAGER:				
				if (planRec.resolverPlan != null) {
					if (planRec.key != null) {
						resolverPlanMap.get(planRec.dataManagerId).remove(planRec.key);
					}
					DataManagerContext dataManagerContext = dataManagerContextMap.get(planRec.dataManagerId);
					planRec.resolverPlan.accept(dataManagerContext);
					executeAgentQueue();					
				}
				break;
			default:
				throw new RuntimeException("unhandled planner type " + planRec.planner);
			}
		}

		for (AgentId agentId : simulationCloseCallbacks.keySet()) {
			Consumer<AgentContext> simulationCloseCallback = simulationCloseCallbacks.get(agentId);
			focalAgentId = agentId;
			simulationCloseCallback.accept(agentContext);
		}
		focalAgentId = null;
	}

	private void executeAgentQueue() {
		while (!agentQueue.isEmpty()) {
			final AgentContentRec agentContentRec = agentQueue.pollFirst();

			if (containsDeletedAgents) {
				/*
				 * we know that the agent id was valid at some point and that
				 * the agentMap never shrinks, so we do not have to range check
				 * the agent id
				 */
				if (!agentIds.contains(agentContentRec.agentId)) {
					continue;
				}
			}

			focalAgentId = agentContentRec.agentId;
			if (agentContentRec.event != null) {
				agentContentRec.metaAgentEventConsumer.handleEvent(agentContentRec.event);
			} else {
				agentContentRec.plan.accept(agentContext);
			}
			focalAgentId = null;
		}

	}

	private Optional<Consumer<AgentContext>> getAgentPlan(final Object key) {
		validatePlanKeyNotNull(key);
		Map<Object, PlanRec> map = agentPlanMap.get(focalAgentId);
		Consumer<AgentContext> result = null;
		if (map != null) {
			final PlanRec planRecord = map.get(key);
			if (planRecord != null) {
				result = planRecord.agentPlan;
			}
		}
		return Optional.ofNullable(result);
	}

	private Consumer<DataManagerContext> getResolverPlan(DataManagerId dataManagerId, final Object key) {
		validatePlanKeyNotNull(key);
		Map<Object, PlanRec> map = resolverPlanMap.get(dataManagerId);
		Consumer<DataManagerContext> result = null;
		if (map != null) {
			final PlanRec planRecord = map.get(key);
			if (planRecord != null) {
				result = planRecord.resolverPlan;
			}
		}
		return result;
	}

	private List<Object> getAgentPlanKeys() {
		Map<Object, PlanRec> map = agentPlanMap.get(focalAgentId);
		if (map != null) {
			return new ArrayList<>(map.keySet());
		}
		return new ArrayList<>();
	}

	private List<Object> getResolverPlanKeys(DataManagerId dataManagerId) {
		Map<Object, PlanRec> map = resolverPlanMap.get(dataManagerId);
		if (map != null) {
			return new ArrayList<>(map.keySet());
		}
		return new ArrayList<>();
	}

	private Optional<Double> getAgentPlanTime(final Object key) {
		validatePlanKeyNotNull(key);
		Map<Object, PlanRec> map = agentPlanMap.get(focalAgentId);
		Double result = null;
		if (map != null) {
			final PlanRec planRecord = map.get(key);
			if (planRecord != null) {
				result = planRecord.time;
			}
		}
		return Optional.ofNullable(result);
	}

	private double getResolverPlanTime(final DataManagerId dataManagerId, final Object key) {
		validatePlanKeyNotNull(key);
		Map<Object, PlanRec> map = resolverPlanMap.get(dataManagerId);
		double result = -1;
		if (map != null) {
			final PlanRec planRecord = map.get(key);
			if (planRecord != null) {
				result = planRecord.time;
			}
		}
		return result;
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
	private <T> Optional<T> removeResolverPlan(DataManagerId dataManagerId, final Object key) {
		validatePlanKeyNotNull(key);

		Map<Object, PlanRec> map = resolverPlanMap.get(dataManagerId);
		T result = null;
		if (map != null) {
			final PlanRec planRecord = map.remove(key);
			if (planRecord != null) {
				result = (T) planRecord.resolverPlan;
				planRecord.resolverPlan = null;
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

	private <T extends Event> void subscribeAgentToEvent(Class<? extends Event> eventClass, AgentEventConsumer<T> reportConsumer) {
		if (eventClass == null) {
			throw new ContractException(NucleusError.NULL_EVENT_CLASS);
		}

		if (reportConsumer == null) {
			throw new ContractException(NucleusError.NULL_EVENT_CONSUMER);
		}

		Map<AgentId, MetaAgentEventConsumer<?>> map = agentEventMap.get(eventClass);
		if (map == null) {
			map = new LinkedHashMap<>();
			agentEventMap.put(eventClass, map);
		}
		MetaAgentEventConsumer<T> metaAgentEventConsumer = new MetaAgentEventConsumer<>(agentContext, reportConsumer);
		map.put(focalAgentId, metaAgentEventConsumer);
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

		public MetaEventLabeler(EventLabeler<T> eventLabeler, EventLabelerId id, Class<T> eventClass) {
			this.id = id;
			this.eventClass = eventClass;
			this.eventLabeler = eventLabeler;

		}

		@SuppressWarnings("unchecked")
		public EventLabel<T> getEventLabel(SimulationContext simulationContext, Event event) {
			EventLabel<T> eventLabel = eventLabeler.getEventLabel(simulationContext, (T) event);
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

	private static class MetaAgentEventConsumer<T extends Event> {

		private final AgentEventConsumer<T> agentEventConsumer;

		private final AgentContext context;

		public MetaAgentEventConsumer(AgentContext context, AgentEventConsumer<T> eventConsumer) {
			this.agentEventConsumer = eventConsumer;
			this.context = context;
		}

		@SuppressWarnings("unchecked")
		public void handleEvent(Event event) {

			try {
				agentEventConsumer.handleEvent(context, (T) event);
			} catch (ClassCastException e) {
				throw new RuntimeException("Class cast exception likely due to improperly formed event label", e);
			}

		}
	}

	private static class Counter {
		int count;
	}

	private Map<Class<? extends Event>, Counter> subscriberExistanceMap = new LinkedHashMap<>();

	private void incrementSubscriberCount(Class<? extends Event> eventClass) {
		Counter counter = subscriberExistanceMap.get(eventClass);
		if (counter == null) {
			counter = new Counter();
			subscriberExistanceMap.put(eventClass, counter);
		}
		counter.count++;
	}

	private void decrementSubscriberCount(Class<? extends Event> eventClass) {
		Counter counter = subscriberExistanceMap.get(eventClass);
		if (counter != null) {
			counter.count--;
		}
	}

	private boolean subscribersExistForEvent(Class<? extends Event> eventClass) {

		Counter counter = subscriberExistanceMap.get(eventClass);
		if (counter != null) {
			return counter.count > 0;
		}
		return false;
	}

	private boolean agentExists(final AgentId agentId) {
		return agentIds.contains(agentId);
	}

	private void broadcastEventToAgentSubscribers(final Event event) {
		Map<Object, Map<EventLabelerId, Map<EventLabel<?>, Map<AgentId, MetaAgentEventConsumer<?>>>>> map1 = agentPubSub.get(event.getClass());
		if (map1 == null) {
			return;
		}
		Object primaryKeyValue = event.getPrimaryKeyValue();
		Map<EventLabelerId, Map<EventLabel<?>, Map<AgentId, MetaAgentEventConsumer<?>>>> map2 = map1.get(primaryKeyValue);
		if (map2 == null) {
			return;
		}

		for (EventLabelerId eventLabelerId : map2.keySet()) {
			MetaEventLabeler<?> metaEventLabeler = id_Labeler_Map.get(eventLabelerId);
			EventLabel<?> eventLabel = metaEventLabeler.getEventLabel(agentContext, event);
			Map<EventLabel<?>, Map<AgentId, MetaAgentEventConsumer<?>>> map3 = map2.get(eventLabelerId);
			Map<AgentId, MetaAgentEventConsumer<?>> map4 = map3.get(eventLabel);
			if (map4 != null) {
				for (AgentId agentId : map4.keySet()) {
					MetaAgentEventConsumer<?> metaConsumer = map4.get(agentId);
					final AgentContentRec agentContentRec = new AgentContentRec();
					agentContentRec.event = event;
					agentContentRec.agentId = agentId;
					agentContentRec.metaAgentEventConsumer = metaConsumer;
					agentQueue.add(agentContentRec);

				}
			}
		}
	}

	

	private <T extends Event> void addEventLabeler(EventLabeler<T> eventLabeler) {
		if (eventLabeler == null) {
			throw new ContractException(NucleusError.NULL_EVENT_LABELER);
		}

		Class<T> eventClass = eventLabeler.getEventClass();
		if (eventClass == null) {
			throw new ContractException(NucleusError.NULL_EVENT_CLASS_IN_EVENT_LABELER);
		}
		EventLabelerId id = eventLabeler.getId();
		if (id == null) {
			throw new ContractException(NucleusError.NULL_LABELER_ID_IN_EVENT_LABELER);
		}

		if (id_Labeler_Map.containsKey(id)) {
			throw new ContractException(NucleusError.DUPLICATE_LABELER_ID_IN_EVENT_LABELER);
		}

		MetaEventLabeler<T> metaEventLabeler = new MetaEventLabeler<>(eventLabeler, id, eventClass);

		id_Labeler_Map.put(metaEventLabeler.getId(), metaEventLabeler);
	}

	private <T extends Event> void subscribeAgentToEvent(EventLabel<T> eventLabel, AgentEventConsumer<T> agentEventConsumer) {

		if (eventLabel == null) {
			throw new ContractException(NucleusError.NULL_EVENT_LABEL);
		}

		if (agentEventConsumer == null) {
			throw new ContractException(NucleusError.NULL_EVENT_CONSUMER);
		}
		Class<T> eventClass = eventLabel.getEventClass();
		if (eventClass == null) {
			throw new ContractException(NucleusError.NULL_EVENT_CLASS_IN_EVENT_LABEL);
		}

		EventLabelerId eventLabelerId = eventLabel.getLabelerId();

		if (eventLabelerId == null) {
			throw new ContractException(NucleusError.NULL_LABELER_ID_IN_EVENT_LABEL);
		}

		MetaEventLabeler<?> metaEventLabeler = id_Labeler_Map.get(eventLabelerId);

		if (metaEventLabeler == null) {
			throw new ContractException(NucleusError.UNKNOWN_EVENT_LABELER);
		}

		Object primaryKeyValue = eventLabel.getPrimaryKeyValue();
		if (primaryKeyValue == null) {
			throw new ContractException(NucleusError.NULL_PRIMARY_KEY_VALUE);
		}

		Map<Object, Map<EventLabelerId, Map<EventLabel<?>, Map<AgentId, MetaAgentEventConsumer<?>>>>> map1 = agentPubSub.get(eventLabel.getEventClass());
		if (map1 == null) {
			map1 = new LinkedHashMap<>();
			agentPubSub.put(eventClass, map1);
			incrementSubscriberCount(eventClass);
		}

		Map<EventLabelerId, Map<EventLabel<?>, Map<AgentId, MetaAgentEventConsumer<?>>>> map2 = map1.get(primaryKeyValue);
		if (map2 == null) {
			map2 = new LinkedHashMap<>();
			map1.put(primaryKeyValue, map2);
		}

		Map<EventLabel<?>, Map<AgentId, MetaAgentEventConsumer<?>>> map3 = map2.get(eventLabelerId);
		if (map3 == null) {
			map3 = new LinkedHashMap<>();
			map2.put(eventLabelerId, map3);
		}

		Map<AgentId, MetaAgentEventConsumer<?>> map4 = map3.get(eventLabel);
		if (map4 == null) {
			map4 = new LinkedHashMap<>();
			map3.put(eventLabel, map4);
		}

		MetaAgentEventConsumer<T> metaEventConsumer = new MetaAgentEventConsumer<>(agentContext, agentEventConsumer);
		map4.put(focalAgentId, metaEventConsumer);

	}

	private <T extends Event> void unsubscribeAgentFromEvent(EventLabel<T> eventLabel) {

		if (eventLabel == null) {
			throw new ContractException(NucleusError.NULL_EVENT_LABEL);
		}

		Class<T> eventClass = eventLabel.getEventClass();
		if (eventClass == null) {
			throw new ContractException(NucleusError.NULL_EVENT_CLASS_IN_EVENT_LABELER);
		}

		EventLabelerId eventLabelerId = eventLabel.getLabelerId();
		if (eventLabelerId == null) {
			throw new ContractException(NucleusError.NULL_LABELER_ID_IN_EVENT_LABEL);
		}

		MetaEventLabeler<?> metaEventLabeler = id_Labeler_Map.get(eventLabelerId);
		if (metaEventLabeler == null) {
			throw new ContractException(NucleusError.UNKNOWN_EVENT_LABELER, eventLabelerId);
		}

		Object primaryKeyValue = eventLabel.getPrimaryKeyValue();
		if (primaryKeyValue == null) {
			throw new ContractException(NucleusError.NULL_PRIMARY_KEY_VALUE);
		}

		Map<Object, Map<EventLabelerId, Map<EventLabel<?>, Map<AgentId, MetaAgentEventConsumer<?>>>>> map1 = agentPubSub.get(eventClass);

		if (map1 == null) {
			return;
		}

		Map<EventLabelerId, Map<EventLabel<?>, Map<AgentId, MetaAgentEventConsumer<?>>>> map2 = map1.get(primaryKeyValue);

		if (map2 == null) {
			return;
		}

		Map<EventLabel<?>, Map<AgentId, MetaAgentEventConsumer<?>>> map3 = map2.get(eventLabelerId);

		if (map3 == null) {
			return;
		}

		Map<AgentId, MetaAgentEventConsumer<?>> map4 = map3.get(eventLabel);

		if (map4 == null) {
			return;
		}

		map4.remove(focalAgentId);

		if (map4.isEmpty()) {
			map3.remove(eventLabel);
			if (map3.isEmpty()) {
				map2.remove(eventLabelerId);
				if (map2.isEmpty()) {
					map1.remove(primaryKeyValue);
					if (map1.isEmpty()) {
						agentPubSub.remove(eventClass);
						decrementSubscriberCount(eventClass);
					}
				}
			}
		}
	}

	private Map<EventLabelerId, MetaEventLabeler<?>> id_Labeler_Map = new LinkedHashMap<>();

	private Map<Class<?>, Map<Object, Map<EventLabelerId, Map<EventLabel<?>, Map<AgentId, MetaAgentEventConsumer<?>>>>>> agentPubSub = new LinkedHashMap<>();

	private void subscribeAgentToSimulationClose(Consumer<AgentContext> closeHandler) {
		if (closeHandler == null) {
			throw new RuntimeException("null close handler");
		}
		simulationCloseCallbacks.put(focalAgentId, closeHandler);
	}

	private static enum EventPhase {
		EXECUTION, POST_EXECUTION
	}

	

	private <T extends Event> void subscribeResolverToEventExecutionPhase(DataManagerId dataMangerId, Class<T> eventClass, DataManagerEventConsumer<T> resolverConsumer) {
		if (eventClass == null) {
			throw new ContractException(NucleusError.NULL_EVENT_CLASS);
		}
		if (resolverConsumer == null) {
			throw new ContractException(NucleusError.NULL_EVENT_CONSUMER);
		}

		List<MetaDataManagerEventConsumer<?>> list = dataManagerEventMap.get(eventClass);
		if (list == null) {
			list = new ArrayList<>();
			dataManagerEventMap.put(eventClass, list);
			// invoke the increment only when adding to the map
			incrementSubscriberCount(eventClass);
		}
		DataManagerContext dataManagerContext = dataManagerContextMap.get(dataMangerId);
		MetaDataManagerEventConsumer<T> metaResolverEventConsumer = new MetaDataManagerEventConsumer<>(dataManagerContext, dataMangerId, resolverConsumer, EventPhase.EXECUTION);

		int insertionIndex = -1;

		for (int i = 0; i < list.size(); i++) {
			MetaDataManagerEventConsumer<?> m = list.get(i);
			if (m.eventPhase == EventPhase.POST_EXECUTION) {
				insertionIndex = i;
				break;
			}
		}

		if (insertionIndex < 0) {
			list.add(metaResolverEventConsumer);
		} else {
			list.add(insertionIndex, metaResolverEventConsumer);
		}
	}

	private <T extends Event> void subscribeResolverToEventPostPhase(DataManagerId dataManagerId, Class<T> eventClass, DataManagerEventConsumer<T> resolverConsumer) {
		if (eventClass == null) {
			throw new ContractException(NucleusError.NULL_EVENT_CLASS);
		}
		if (resolverConsumer == null) {
			throw new ContractException(NucleusError.NULL_EVENT_CONSUMER);
		}

		List<MetaDataManagerEventConsumer<?>> list = dataManagerEventMap.get(eventClass);
		if (list == null) {
			list = new ArrayList<>();
			dataManagerEventMap.put(eventClass, list);
			// invoke the increment only when adding to the map
			incrementSubscriberCount(eventClass);
		}
		DataManagerContext dataManagerContext = dataManagerContextMap.get(dataManagerId);
		MetaDataManagerEventConsumer<T> metaResolverEventConsumer = new MetaDataManagerEventConsumer<>(dataManagerContext, dataManagerId, resolverConsumer, EventPhase.POST_EXECUTION);

		list.add(metaResolverEventConsumer);

	}

	private void unSubscribeResolverToEvent(DataManagerId dataManagerId, Class<? extends Event> eventClass) {
		if (eventClass == null) {
			throw new ContractException(NucleusError.NULL_EVENT_CLASS);
		}

		List<MetaDataManagerEventConsumer<?>> list = dataManagerEventMap.get(eventClass);

		if (list != null) {
			Iterator<MetaDataManagerEventConsumer<?>> iterator = list.iterator();
			while (iterator.hasNext()) {
				MetaDataManagerEventConsumer<?> metaResolverEventConsumer = iterator.next();
				if (metaResolverEventConsumer.dataManagerId.equals(dataManagerId)) {
					iterator.remove();
					decrementSubscriberCount(eventClass);
				}
			}

			if (list.isEmpty()) {
				dataManagerEventMap.remove(eventClass);
			}
		}
	}

	private void resolveEventForDataManager(final Event event) {

		if (event == null) {
			throw new ContractException(NucleusError.NULL_EVENT);
		}

		Map<AgentId, MetaAgentEventConsumer<?>> reportMap = agentEventMap.get(event.getClass());
		if (reportMap != null) {
			for (final AgentId agentId : reportMap.keySet()) {
				MetaAgentEventConsumer<?> metaAgentEventConsumer = reportMap.get(agentId);
				final AgentContentRec contentRec = new AgentContentRec();
				contentRec.agentId = agentId;
				contentRec.event = event;
				contentRec.metaAgentEventConsumer = metaAgentEventConsumer;
				agentQueue.add(contentRec);
			}
		}
		
		broadcastEventToAgentSubscribers(event);

		List<MetaDataManagerEventConsumer<?>> list = dataManagerEventMap.get(event.getClass());
		if (list != null) {
			for (MetaDataManagerEventConsumer<?> metaResolverEventConsumer : list) {
				metaResolverEventConsumer.handleEvent(event);
			}
		}
	}

	/////////////////////////////////
	// data manager support
	/////////////////////////////////
	private MutableGraph<PluginId, Object> pluginDependencyGraph = new MutableGraph<>();

	private int masterDataManagerIndex;

	/*
	 * id class for data mangers with overridden equals contract to improve
	 * performance as a key
	 */
	private static class DataManagerId {
		private final int id;

		public DataManagerId(int id) {
			this.id = id;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + id;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (!(obj instanceof DataManagerId)) {
				return false;
			}
			DataManagerId other = (DataManagerId) obj;
			if (id != other.id) {
				return false;
			}
			return true;
		}

	}

	private static class MetaDataManagerEventConsumer<T extends Event> {

		private final DataManagerEventConsumer<T> dataManagerEventConsumer;

		private final DataManagerContext context;

		private final DataManagerId dataManagerId;

		private final EventPhase eventPhase;

		public MetaDataManagerEventConsumer(DataManagerContext context, DataManagerId dataManagerId, DataManagerEventConsumer<T> eventConsumer, EventPhase eventPhase) {
			this.dataManagerEventConsumer = eventConsumer;
			this.context = context;
			this.dataManagerId = dataManagerId;
			this.eventPhase = eventPhase;
		}

		@SuppressWarnings("unchecked")
		public void handleEvent(Event event) {

			try {
				dataManagerEventConsumer.handleEvent(context, (T) event);
			} catch (ClassCastException e) {
				throw new RuntimeException("Class cast exception likely due to improperly formed event label", e);
			}

		}
	}

	// used to contain the data managers while the plugins are getting organized
	private Map<PluginId, Set<DataManager>> dataMangagersMap = new LinkedHashMap<>();

	// used for subscriptions
	private final Map<Class<? extends Event>, List<MetaDataManagerEventConsumer<?>>> dataManagerEventMap = new LinkedHashMap<>();

	// used for retrieving and canceling plans owned by data managers
	private final Map<DataManagerId, Map<Object, PlanRec>> resolverPlanMap = new LinkedHashMap<>();

	// used to locate data managers by class type
	private Map<Class<?>, DataManager> dataManagerMap = new LinkedHashMap<>();

	// map of contexts for each data manager
	private Map<DataManagerId, DataManagerContext> dataManagerContextMap = new LinkedHashMap<>();

	//////////////////////////////
	// agent support
	//////////////////////////////

}