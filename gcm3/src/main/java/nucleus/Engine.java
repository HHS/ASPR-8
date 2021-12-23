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
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.apache.commons.math3.util.FastMath;

import net.jcip.annotations.NotThreadSafe;
import util.ContractError;
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
public class Engine {

	private class PluginContextImpl implements PluginContext {

		@Override
		public void addPluginDependency(PluginId pluginId) {
			Engine.this.addPluginDependency(pluginId);
		}

		@Override
		public void defineResolver(ResolverId resolverId, Consumer<ResolverContext> init) {
			Engine.this.defineResolver(resolverId, init);
		}

	}

	private class BaseContextImpl implements Context {

		@Override
		public void releaseOutput(Object output) {
			Engine.this.releaseOutput(output);
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T extends DataView> Optional<T> getDataView(Class<T> c) {
			return Optional.ofNullable((T) dataViewMap.get(c));
		}

		@Override
		public double getTime() {
			return time;
		}

		@Override
		public void throwContractException(ContractError recoverableError) {
			Engine.this.throwContractException(recoverableError);
		}

		@Override
		public void throwContractException(ContractError recoverableError, Object details) {
			Engine.this.throwContractException(recoverableError, details);
		}
	}

	private class AgentContextImpl implements AgentContext {

		@Override
		public void addPlan(final Consumer<AgentContext> plan, final double planTime) {
			Engine.this.addAgentPlan(plan, planTime);
		}

		@Override
		public void addPlan(final Consumer<AgentContext> plan, final double planTime, final Object key) {
			Engine.this.addAgentPlan(plan, planTime, key);
		}

		@Override
		public boolean agentExists(final AgentId agentId) {
			return Engine.this.agentExists(agentId);
		}

		@Override
		public AgentId getCurrentAgentId() {
			return focalAgentId;
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T extends DataView> Optional<T> getDataView(final Class<T> c) {
			return Optional.ofNullable((T) dataViewMap.get(c));
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T extends Consumer<AgentContext>> Optional<T> getPlan(final Object key) {
			return (Optional<T>) Engine.this.getAgentPlan(key);
		}

		@Override
		public List<Object> getPlanKeys() {
			return Engine.this.getAgentPlanKeys();
		}

		@Override
		public Optional<Double> getPlanTime(final Object key) {
			return Engine.this.getAgentPlanTime(key);
		}

		@Override
		public double getTime() {
			return time;
		}

		@Override
		public void halt() {
			Engine.this.halt();
		}

		@Override
		public <T> Optional<T> removePlan(final Object key) {
			return Engine.this.removeAgentPlan(key);
		}

		@Override
		public void resolveEvent(final Event event) {
			Engine.this.resolveEventForAgent(event);
		}

		@Override
		public void releaseOutput(Object output) {
			Engine.this.releaseOutput(output);

		}

		@Override
		public void throwContractException(ContractError recoverableError) {
			Engine.this.throwContractException(recoverableError);

		}

		@Override
		public void throwContractException(ContractError recoverableError, Object details) {
			Engine.this.throwContractException(recoverableError, details);

		}

		@Override
		public <T extends Event> void subscribe(EventLabel<T> eventLabel, AgentEventConsumer<T> agentEventConsumer) {
			Engine.this.subscribeAgentToEvent(eventLabel, agentEventConsumer);
		}

		@Override
		public <T extends Event> void unsubscribe(EventLabel<T> eventLabel) {
			Engine.this.unsubscribeAgentFromEvent(eventLabel);

		}

		@Override
		public <T extends Event> void addEventLabeler(EventLabeler<T> eventLabeler) {
			Engine.this.addEventLabeler(eventLabeler);

		}

	}

	private static final String LINE_SEPARATOR = System.getProperty("line.separator");

	private static class PlanRec {

		private Planner planner;
		private double time;
		private long arrivalId;

		private Consumer<AgentContext> agentPlan;
		private AgentId agentId;

		private Consumer<ResolverContext> resolverPlan;
		private ResolverId resolverId;

		private Consumer<ReportContext> reportPlan;
		private ReportId reportId;

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
			case REPORT:
				builder.append(reportId);
				break;
			case RESOLVER:
				builder.append(resolverId);
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

	private static class ResolverContentRec {

		private Event event;

		private MetaResolverEventConsumer<?> metaResolverEventConsumer;

		private boolean agentIsEventSource;

		private Consumer<ResolverContext> plan;

		private ResolverId resolverId;
	}

	private static class ReportContentRec {

		private Event event;

		private Consumer<ReportContext> plan;

		private MetaReportEventConsumer<?> metaReportEventConsumer;

		private ReportId reportId;

		private boolean agentIsEventSource;

	}

	public static class EngineBuilder {

		private Scaffold scaffold = new Scaffold();

		private EngineBuilder() {

		}

		/**
		 * Sets the output consumer for the simulation. Tolerates null.
		 */
		public EngineBuilder setOutputConsumer(Consumer<Object> outputConsumer) {
			scaffold.outputConsumer = outputConsumer;
			return this;
		}

		/**
		 * Adds a plugin to this builder for inclusion in the simulation
		 * 
		 * @throws ContractException
		 *             <li>{@link NucleusError#NULL_PLUGIN_ID} if the plugin id
		 *             is null
		 * 
		 * @throws ContractException
		 *             <li>{@link NucleusError#NULL_PLUGIN_CONTEXT_CONSUMER} if
		 *             the plugin context consumer is null
		 */
		public EngineBuilder addPlugin(PluginId pluginId, Consumer<PluginContext> init) {
			if (pluginId == null) {
				throw new ContractException(NucleusError.NULL_PLUGIN_ID);
			}

			if (init == null) {
				throw new ContractException(NucleusError.NULL_PLUGIN_CONTEXT_CONSUMER);
			}

			if (scaffold.plugins.containsKey(pluginId)) {
				throw new ContractException(NucleusError.DUPLICATE_PLUGIN_ID);
			}

			scaffold.plugins.put(pluginId, init);
			return this;
		}

		/**
		 * Returns an Engine instance that is initialized with the plugins and
		 * output consumer collected by this builder.
		 * 
		 * @throws ContractException
		 *             <li>{@link NucleusError#DUPLICATE_RESOLVER_ID} if two
		 *             plugins define the same resolver id
		 * 
		 * 
		 *             <li>{@link NucleusError#MISSING_PLUGIN} if a plugin is
		 *             required by another plugin, but no instance of the needed
		 *             plugin was contributed to this builder
		 * 
		 *             <li>{@link NucleusError#CIRCULAR_PLUGIN_DEPENDENCIES} if
		 *             the contributed plugins form a circular chain of
		 *             dependencies
		 * 
		 * 
		 */
		public Engine build() {
			try {
				final Engine engine = new Engine();
				engine.init(scaffold);
				return engine;
			} finally {
				scaffold = new Scaffold();
			}
		}
	}

	/*
	 * Defines the type of actor that has created a plan. THE ORDER OF THIS ENUM
	 * IS CRITICAL TO THE FUNCTION OF THE SIMULATION!
	 */
	private static enum Planner {
		RESOLVER, AGENT, REPORT
	}

	private class ReportContextImpl implements ReportContext {

		@Override
		public void addPlan(final Consumer<ReportContext> plan, final double planTime) {
			Engine.this.addReportPlan(plan, planTime);
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T extends DataView> Optional<T> getDataView(final Class<T> c) {
			return Optional.ofNullable((T) dataViewMap.get(c));
		}

		@Override
		public double getTime() {
			return time;
		}

		@Override
		public void releaseOutput(Object output) {
			Engine.this.releaseOutput(output);

		}

		@Override
		// public <T extends Event> void subscribe(Class<? extends Event>
		// eventClass, ReportEventConsumer<T> reportConsumer) {
		public <T extends Event> void subscribe(Class<T> eventClass, ReportEventConsumer<T> reportConsumer) {
			Engine.this.subscribeReportToEvent(eventClass, reportConsumer);
		}

		@Override
		public void throwContractException(ContractError recoverableError) {
			Engine.this.throwContractException(recoverableError);

		}

		@Override
		public void throwContractException(ContractError recoverableError, Object details) {
			Engine.this.throwContractException(recoverableError, details);

		}

		@Override
		public void subscribeToSimulationClose(Consumer<ReportContext> closeHandler) {
			Engine.this.subscribeReportToSimulationClose(closeHandler);
		}

		@Override
		public <T extends Event> void subscribe(EventLabel<T> eventLabel, ReportEventConsumer<T> reportEventConsumer) {
			Engine.this.subscribeReportToEvent(eventLabel, reportEventConsumer);
		}

		@Override
		public ReportId getCurrentReportId() {
			return focalReport;
		}

		@Override
		public <T extends Event> void addEventLabeler(EventLabeler<T> eventLabeler) {
			Engine.this.addEventLabeler(eventLabeler);
		}

	}

	private class ResolverContextImpl implements ResolverContext {

		@Override
		public void addAgent(Consumer<AgentContext> init, AgentId agentId) {
			if (agentId == null) {
				throwContractException(NucleusError.NULL_AGENT_ID);
			}

			if (init == null) {
				throwContractException(NucleusError.NULL_AGENT_CONTEXT_CONSUMER);
			}

			int index = agentId.getValue();

			if (index < 0) {
				throwContractException(NucleusError.NEGATIVE_AGENT_ID);
			}

			int size = agentIds.size();

			if (index < size) {
				if (agentIds.get(index) != null) {
					throwContractException(NucleusError.AGENT_ID_IN_USE, agentId);
				}
				agentIds.set(index, agentId);
			} else {

				for (int i = size; i < index; i++) {
					agentIds.add(null);
				}
				agentIds.add(agentId);
			}

			final AgentContentRec agentContentRec = new AgentContentRec();
			agentContentRec.agentId = agentId;
			agentContentRec.plan = init;
			agentQueue.add(agentContentRec);

		}

		@Override
		public void addPlan(final Consumer<ResolverContext> plan, final double planTime) {
			Engine.this.addResolverPlan(plan, planTime);
		}

		@Override
		public void addPlan(final Consumer<ResolverContext> plan, final double planTime, final Object key) {
			Engine.this.addResolverPlan(plan, planTime, key);

		}

		@Override
		public boolean agentExists(final AgentId agentId) {
			return Engine.this.agentExists(agentId);
		}

		@Override
		public boolean currentAgentIsEventSource() {
			return agentIsEventSource;
		}

		@Override
		public AgentId getAvailableAgentId() {
			return new AgentId(masterAgentId++);
		}

		@Override
		public AgentId getCurrentAgentId() {
			return focalAgentId;
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T extends DataView> Optional<T> getDataView(final Class<T> c) {
			return Optional.ofNullable((T) dataViewMap.get(c));
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T extends Consumer<ResolverContext>> T getPlan(final Object key) {
			return (T) Engine.this.getResolverPlan(key);
		}

		@Override
		public List<Object> getPlanKeys() {
			return Engine.this.getResolverPlanKeys();
		}

		@Override
		public double getPlanTime(final Object key) {
			return Engine.this.getResolverPlanTime(key);
		}

		@Override
		public double getTime() {
			return time;
		}

		@Override
		public void halt() {
			Engine.this.halt();
		}

		@Override
		public void queueEventForResolution(final Event event) {
			Engine.this.resolveEventForResolver(event);

		}

		@Override
		public void removeAgent(final AgentId agentId) {
			if (agentId == null) {
				throwContractException(NucleusError.NULL_AGENT_ID);
			}

			int index = agentId.getValue();

			if (index < 0) {
				throwContractException(NucleusError.NEGATIVE_AGENT_ID);
			}

			int size = agentIds.size();

			if (index >= size) {
				throwContractException(NucleusError.UNKNOWN_AGENT_ID);
			}
			if (agentIds.get(index) == null) {
				throwContractException(NucleusError.UNKNOWN_AGENT_ID);
			}
			agentIds.set(index, null);
			agentMapContainsNulls = true;
		}

		@Override
		public <T> Optional<T> removePlan(final Object key) {
			return Engine.this.removeResolverPlan(key);
		}

		@Override
		public void releaseOutput(Object output) {
			Engine.this.releaseOutput(output);
		}

		@Override
		public void throwContractException(ContractError recoverableError) {
			Engine.this.throwContractException(recoverableError);
		}

		@Override
		public void throwContractException(ContractError recoverableError, Object details) {
			Engine.this.throwContractException(recoverableError, details);
		}

		@Override
		public void unSubscribeToEvent(Class<? extends Event> eventClass) {
			Engine.this.unSubscribeResolverToEvent(eventClass);
		}

		@Override
		public <T extends Event> void addEventLabeler(EventLabeler<T> eventLabeler) {
			Engine.this.addEventLabeler(eventLabeler);
		}

		@Override
		public Context getSafeContext() {
			return Engine.this.baseContext;

		}

		@Override
		public boolean subscribersExistForEvent(Class<? extends Event> eventClass) {
			return Engine.this.subscribersExistForEvent(eventClass);
		}

		@Override
		public void addReport(ReportId reportId, Consumer<ReportContext> init) {

			if (reportId == null) {
				throwContractException(NucleusError.NULL_REPORT_ID);
			}

			if (init == null) {
				throwContractException(NucleusError.NULL_REPORT_CONTEXT_CONSUMER);
			}

			if (reportIds.contains(reportId)) {
				throwContractException(NucleusError.REPORT_ID_IN_USE, reportId);
			}
			reportIds.add(reportId);

			final ReportContentRec contentRec = new ReportContentRec();
			contentRec.plan = init;
			contentRec.reportId = reportId;
			reportQueue.add(contentRec);

		}

		@Override
		public <T extends Event> void subscribeToEventValidationPhase(Class<T> eventClass, ResolverEventConsumer<T> resolverConsumer) {
			Engine.this.subscribeResolverToEventValidationPhase(eventClass, resolverConsumer);
		}

		@Override
		public <T extends Event> void subscribeToEventPostPhase(Class<T> eventClass, ResolverEventConsumer<T> resolverConsumer) {
			Engine.this.subscribeResolverToEventPostPhase(eventClass, resolverConsumer);
		}

		@Override
		public <T extends Event> void subscribeToEventExecutionPhase(Class<T> eventClass, ResolverEventConsumer<T> resolverConsumer) {
			Engine.this.subscribeResolverToEventExecutionPhase(eventClass, resolverConsumer);
		}

		@Override
		public void publishDataView(DataView dataView) {
			if (dataView == null) {
				throwContractException(NucleusError.NULL_DATA_VIEW);
			}
			dataViewMap.put(dataView.getClass(), dataView);
		}

		@Override
		public ResolverId getCurrentResolverId() {
			return focalResolver;
		}

	}

	private static class Scaffold {
		private Map<PluginId, Consumer<PluginContext>> plugins = new LinkedHashMap<>();
		private Consumer<Object> outputConsumer;

	}

	/**
	 * Returns a reusable EngineBuilder instance
	 */
	public static EngineBuilder builder() {
		return new EngineBuilder();
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

	// resolvers and reports
	private final Map<Class<? extends Event>, List<MetaResolverEventConsumer<?>>> resolverEventMap = new LinkedHashMap<>();
	private final ResolverContext resolverContext = new ResolverContextImpl();
	private final Map<ResolverId, Map<Object, PlanRec>> resolverPlanMap = new LinkedHashMap<>();
	private final Deque<ResolverContentRec> resolverQueue = new ArrayDeque<>();
	private ResolverId focalResolver;

	private final Map<Class<? extends Event>, Map<ReportId, MetaReportEventConsumer<?>>> reportEventMap = new LinkedHashMap<>();
	private final Set<ReportId> reportIds = new LinkedHashSet<>();
	private ReportId focalReport;

	private boolean resolverQueueActive;
	private ReportContext reportContext = new ReportContextImpl();

	// private final List<ReportId> reports = new ArrayList<>();
	private final Deque<ReportContentRec> reportQueue = new ArrayDeque<>();
	private final Map<ReportId, Consumer<ReportContext>> simulationCloseCallbacks = new LinkedHashMap<>();

	// agents
	private final AgentContext agentContext = new AgentContextImpl();
	private final List<AgentId> agentIds = new ArrayList<>();
	private boolean agentMapContainsNulls;

	private int masterAgentId;
	private final Map<AgentId, Map<Object, PlanRec>> agentPlanMap = new LinkedHashMap<>();
	private final Deque<AgentContentRec> agentQueue = new ArrayDeque<>();
	private AgentId focalAgentId;
	private boolean agentIsEventSource;

	Context baseContext = new BaseContextImpl();
	private Map<Class<?>, DataView> dataViewMap = new LinkedHashMap<>();
	private boolean started;

	private final PluginContext pluginContext = new PluginContextImpl();
	private PluginId focalPluginId;

	private Engine() {

	}

	private void validateAgentPlan(final Consumer<AgentContext> plan) {
		if (plan == null) {
			throwContractException(NucleusError.NULL_PLAN);
		}
	}

	private void validateReportPlan(final Consumer<ReportContext> plan) {
		if (plan == null) {
			throwContractException(NucleusError.NULL_PLAN);
		}
	}

	private void validateResolverPlan(final Consumer<ResolverContext> plan) {
		if (plan == null) {
			throwContractException(NucleusError.NULL_PLAN);
		}
	}

	private void _addAgentPlan(final Consumer<AgentContext> plan, final double time, final Object key) {

		validatePlanTime(time);
		validateAgentPlan(plan);

		final PlanRec planRec = new PlanRec();
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

		activePlanCount++;

		planningQueue.add(planRec);

	}

	private void addReportPlan(final Consumer<ReportContext> plan, final double time) {
		validateReportPlan(plan);
		validatePlanTime(time);

		final PlanRec planRec = new PlanRec();
		planRec.arrivalId = masterPlanningArrivalId++;
		planRec.planner = Planner.REPORT;
		planRec.time = FastMath.max(time, this.time);
		planRec.reportPlan = plan;

		planRec.reportId = focalReport;

		// DO NOT INCREMENT THE activePlanCount

		planningQueue.add(planRec);

	}

	private void _addResolverPlan(final Consumer<ResolverContext> plan, final double time, final Object key) {

		validateResolverPlan(plan);
		validatePlanTime(time);

		final PlanRec planRec = new PlanRec();
		planRec.arrivalId = masterPlanningArrivalId++;
		planRec.planner = Planner.RESOLVER;
		planRec.time = FastMath.max(time, this.time);
		planRec.resolverPlan = plan;
		planRec.key = key;

		Map<Object, PlanRec> map;

		planRec.resolverId = focalResolver;
		if (key != null) {
			map = resolverPlanMap.get(focalResolver);
			if (map == null) {
				map = new LinkedHashMap<>();
				resolverPlanMap.put(focalResolver, map);
			}
			map.put(key, planRec);
		}

		activePlanCount++;
		planningQueue.add(planRec);
	}

	private void addAgentPlan(final Consumer<AgentContext> plan, final double time) {
		_addAgentPlan(plan, time, null);
	}

	private void addAgentPlan(final Consumer<AgentContext> plan, final double time, final Object key) {
		validatePlanKeyNotNull(key);
		validateAgentPlanKeyNotDuplicate(key);
		_addAgentPlan(plan, time, key);
	}

	private void addResolverPlan(final Consumer<ResolverContext> plan, final double time) {
		_addResolverPlan(plan, time, null);
	}

	private void addResolverPlan(final Consumer<ResolverContext> plan, final double time, final Object key) {
		validatePlanKeyNotNull(key);
		validateResolverPlanKeyNotDuplicate(key);
		_addResolverPlan(plan, time, key);
	}

	private void validateResolverPlanKeyNotDuplicate(final Object key) {
		if (getResolverPlan(key) != null) {
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

	/**
	 * Executes this Engine instance. Contributed plugins are accessed and
	 * organized based on their dependency ordering. Time starts at zero and
	 * each contributed resolver is initialized. Time flows based on planning
	 * and when all plans are executed, time stops and reports are finalized.
	 * 
	 * @throws ContractException
	 *             <li>{@link NucleusError#REPEATED_EXECUTION} if execute is
	 *             invoked more than once
	 * 
	 * 
	 */
	public void execute() {

		if (started) {
			throw new ContractException(NucleusError.REPEATED_EXECUTION);
		}
		started = true;

		executeResolverQueue();
		executeAgentQueue();
		executeReportQueue();

		while (processEvents && (activePlanCount > 0)) {
			final PlanRec planRec = planningQueue.poll();
			time = planRec.time;

			switch (planRec.planner) {
			case AGENT:
				activePlanCount--;
				if (planRec.agentPlan != null) {
					if (planRec.key != null) {
						agentPlanMap.get(planRec.agentId).remove(planRec.key);
					}
					AgentContentRec agentContentRec = new AgentContentRec();
					agentContentRec.agentId = planRec.agentId;
					agentContentRec.plan = planRec.agentPlan;
					agentQueue.add(agentContentRec);
					executeAgentQueue();
					executeReportQueue();
				}
				break;
			case REPORT:
				if (planRec.reportPlan != null) {
					ReportContentRec reportContentRec = new ReportContentRec();
					reportContentRec.plan = planRec.reportPlan;
					reportContentRec.reportId = planRec.reportId;
					reportQueue.add(reportContentRec);
					executeReportQueue();
				}
				break;
			case RESOLVER:
				activePlanCount--;

				if (planRec.resolverPlan != null) {
					if (planRec.key != null) {
						resolverPlanMap.get(planRec.resolverId).remove(planRec.key);
					}
					ResolverContentRec resolverContentRec = new ResolverContentRec();
					resolverContentRec.plan = planRec.resolverPlan;
					resolverContentRec.resolverId = planRec.resolverId;
					resolverQueue.add(resolverContentRec);
					executeResolverQueue();
					executeAgentQueue();
					executeReportQueue();
				}
				break;
			default:
				throw new RuntimeException("unhandled planner type " + planRec.planner);
			}

		}

		for (ReportId reportId : simulationCloseCallbacks.keySet()) {
			Consumer<ReportContext> simulationCloseCallback = simulationCloseCallbacks.get(reportId);
			focalReport = reportId;
			simulationCloseCallback.accept(reportContext);
		}
		focalReport = null;
	}

	private void executeAgentQueue() {
		while (!agentQueue.isEmpty()) {
			final AgentContentRec agentContentRec = agentQueue.pollFirst();

			if (agentMapContainsNulls) {
				/*
				 * we know that the agent id was valid at some point and that
				 * the agentMap never shrinks, so we do not have to range check
				 * the agent id
				 */
				if (agentIds.get(agentContentRec.agentId.getValue()) == null) {
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

	private void executeReportQueue() {

		while (!reportQueue.isEmpty()) {
			final ReportContentRec contentRec = reportQueue.pollFirst();
			focalReport = contentRec.reportId;
			if (contentRec.plan != null) {
				contentRec.plan.accept(reportContext);
			} else if (contentRec.event != null) {
				agentIsEventSource = contentRec.agentIsEventSource;
				contentRec.metaReportEventConsumer.handleEvent(contentRec.event);
			}
			focalReport = null;
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

	private Consumer<ResolverContext> getResolverPlan(final Object key) {
		validatePlanKeyNotNull(key);
		Map<Object, PlanRec> map = resolverPlanMap.get(focalResolver);
		Consumer<ResolverContext> result = null;
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

	private List<Object> getResolverPlanKeys() {
		Map<Object, PlanRec> map = resolverPlanMap.get(focalResolver);
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

	private double getResolverPlanTime(final Object key) {
		validatePlanKeyNotNull(key);
		Map<Object, PlanRec> map = resolverPlanMap.get(focalResolver);
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
	private <T> Optional<T> removeResolverPlan(final Object key) {
		validatePlanKeyNotNull(key);

		Map<Object, PlanRec> map = resolverPlanMap.get(focalResolver);
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

	private <T extends Event> void subscribeReportToEvent(Class<? extends Event> eventClass, ReportEventConsumer<T> reportConsumer) {
		if (eventClass == null) {
			throwContractException(NucleusError.NULL_EVENT_CLASS);
		}

		if (reportConsumer == null) {
			throwContractException(NucleusError.NULL_EVENT_CONSUMER);
		}

		Map<ReportId, MetaReportEventConsumer<?>> map = reportEventMap.get(eventClass);
		if (map == null) {
			map = new LinkedHashMap<>();
			reportEventMap.put(eventClass, map);
		}
		MetaReportEventConsumer<T> metaReportEventConsumer = new MetaReportEventConsumer<>(reportContext, reportConsumer);
		map.put(focalReport, metaReportEventConsumer);
	}

	private <T extends Event> void subscribeReportToEvent(EventLabel<T> eventLabel, ReportEventConsumer<T> reportEventConsumer) {

		if (eventLabel == null) {
			throwContractException(NucleusError.NULL_EVENT_LABEL);
		}

		if (reportEventConsumer == null) {
			throwContractException(NucleusError.NULL_EVENT_CONSUMER);
		}
		Class<T> eventClass = eventLabel.getEventClass();
		if (eventClass == null) {
			throwContractException(NucleusError.NULL_EVENT_CLASS_IN_EVENT_LABEL);
		}

		EventLabelerId eventLabelerId = eventLabel.getLabelerId();

		if (eventLabelerId == null) {
			throwContractException(NucleusError.NULL_LABELER_ID_IN_EVENT_LABEL);
		}

		MetaEventLabeler<?> metaEventLabeler = id_Labeler_Map.get(eventLabelerId);

		if (metaEventLabeler == null) {
			throwContractException(NucleusError.UNKNOWN_EVENT_LABELER);
		}

		Object primaryKeyValue = eventLabel.getPrimaryKeyValue();
		if (primaryKeyValue == null) {
			throwContractException(NucleusError.NULL_PRIMARY_KEY_VALUE);
		}

		Map<Object, Map<EventLabelerId, Map<EventLabel<?>, Map<ReportId, MetaReportEventConsumer<?>>>>> map1 = reportPubSub.get(eventLabel.getEventClass());
		if (map1 == null) {
			map1 = new LinkedHashMap<>();
			reportPubSub.put(eventClass, map1);
			incrementSubscriberCount(eventClass);
		}

		Map<EventLabelerId, Map<EventLabel<?>, Map<ReportId, MetaReportEventConsumer<?>>>> map2 = map1.get(primaryKeyValue);
		if (map2 == null) {
			map2 = new LinkedHashMap<>();
			map1.put(primaryKeyValue, map2);
		}

		Map<EventLabel<?>, Map<ReportId, MetaReportEventConsumer<?>>> map3 = map2.get(eventLabelerId);
		if (map3 == null) {
			map3 = new LinkedHashMap<>();
			map2.put(eventLabelerId, map3);
		}

		Map<ReportId, MetaReportEventConsumer<?>> map4 = map3.get(eventLabel);
		if (map4 == null) {
			map4 = new LinkedHashMap<>();
			map3.put(eventLabel, map4);
		}

		MetaReportEventConsumer<T> metaEventConsumer = new MetaReportEventConsumer<>(reportContext, reportEventConsumer);
		map4.put(focalReport, metaEventConsumer);

	}

	private void releaseOutput(Object output) {
		if (outputConsumer != null) {
			outputConsumer.accept(output);
		}
	}

	private void throwContractException(ContractError recoverableError) {
		throwContractException(recoverableError, null);
	}

	private void throwContractException(ContractError contractError, Object details) {
		final StringBuilder sb = new StringBuilder();

		sb.append("time = ");
		sb.append(time);
		sb.append(": ");
		if (focalAgentId != null) {
			sb.append(focalAgentId);
			sb.append(": ");
		}
		if (focalResolver != null) {
			sb.append("Resolver[");
			sb.append(focalResolver);
			sb.append("]: ");
		}
		if (focalReport != null) {
			sb.append("Report[");
			sb.append(focalReport);
			sb.append("]: ");
		}

		sb.append(contractError.getDescription());

		if (details != null) {
			sb.append(": ");
			sb.append(details);
		}

		final String errorDescription = sb.toString();

		throw new ContractException(contractError, errorDescription);

	}

	private void executeResolverQueue() {
		if (resolverQueueActive) {
			return;
		}
		resolverQueueActive = true;
		try {
			try {
				while (!resolverQueue.isEmpty()) {
					final ResolverContentRec contentRec = resolverQueue.pollFirst();
					focalResolver = contentRec.resolverId;
					if (contentRec.plan != null) {
						contentRec.plan.accept(resolverContext);
					} else {
						agentIsEventSource = contentRec.agentIsEventSource;
						contentRec.metaResolverEventConsumer.handleEvent(contentRec.event);
					}
					focalResolver = null;
				}
			} catch (Exception e) {
				resolverQueue.clear();
				throw (e);
			}
		} finally {
			resolverQueueActive = false;
		}
	}

	private static class MetaEventLabeler<T extends Event> {

		private final EventLabeler<T> eventLabeler;
		private final Class<T> eventClass;
		private final EventLabelerId id;
		private final Engine engine;

		public MetaEventLabeler(Engine engine, EventLabeler<T> eventLabeler, EventLabelerId id, Class<T> eventClass) {
			this.id = id;
			this.eventClass = eventClass;
			this.eventLabeler = eventLabeler;
			this.engine = engine;
		}

		@SuppressWarnings("unchecked")
		public EventLabel<T> getEventLabel(Context context, Event event) {
			EventLabel<T> eventLabel = eventLabeler.getEventLabel(context, (T) event);
			if (!eventClass.equals(eventLabel.getEventClass())) {
				engine.throwContractException(NucleusError.LABLER_GENERATED_LABEL_WITH_INCORRECT_EVENT_CLASS);
			}
			if (!id.equals(eventLabel.getLabelerId())) {
				engine.throwContractException(NucleusError.LABLER_GENERATED_LABEL_WITH_INCORRECT_ID);
			}
			if (!event.getPrimaryKeyValue().equals(eventLabel.getPrimaryKeyValue())) {
				engine.throwContractException(NucleusError.LABLER_GENERATED_LABEL_WITH_INCORRECT_PRIMARY_KEY);
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
		if (agentId == null) {
			throwContractException(NucleusError.NULL_AGENT_ID);
		}
		int index = agentId.getValue();
		if (index < 0) {
			return false;
		}
		if (index >= agentIds.size()) {
			return false;
		}
		return agentIds.get(index) != null;
	}

	private static class MetaResolverEventConsumer<T extends Event> {

		private final ResolverEventConsumer<T> resolverEventConsumer;

		private final ResolverContext context;

		private final ResolverId resolverId;

		private final EventPhase eventPhase;

		public MetaResolverEventConsumer(ResolverContext context, ResolverId resolverId, ResolverEventConsumer<T> eventConsumer, EventPhase eventPhase) {
			this.resolverEventConsumer = eventConsumer;
			this.context = context;
			this.resolverId = resolverId;
			this.eventPhase = eventPhase;
		}

		@SuppressWarnings("unchecked")
		public void handleEvent(Event event) {

			try {
				resolverEventConsumer.handleEvent(context, (T) event);
			} catch (ClassCastException e) {
				throw new RuntimeException("Class cast exception likely due to improperly formed event label", e);
			}

		}
	}

	private static class MetaReportEventConsumer<T extends Event> {

		private final ReportEventConsumer<T> reportEventConsumer;

		private final ReportContext context;

		public MetaReportEventConsumer(ReportContext context, ReportEventConsumer<T> eventConsumer) {
			this.reportEventConsumer = eventConsumer;
			this.context = context;
		}

		@SuppressWarnings("unchecked")
		public void handleEvent(Event event) {

			try {
				reportEventConsumer.handleEvent(context, (T) event);
			} catch (ClassCastException e) {
				throw new RuntimeException("Class cast exception due to improperly mapped event for report", e);
			}

		}
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
			EventLabel<?> eventLabel = metaEventLabeler.getEventLabel(baseContext, event);
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

	private void broadcastEventToReportSubscribers(final Event event) {
		Map<Object, Map<EventLabelerId, Map<EventLabel<?>, Map<ReportId, MetaReportEventConsumer<?>>>>> map1 = reportPubSub.get(event.getClass());
		if (map1 == null) {
			return;
		}
		Object primaryKeyValue = event.getPrimaryKeyValue();
		Map<EventLabelerId, Map<EventLabel<?>, Map<ReportId, MetaReportEventConsumer<?>>>> map2 = map1.get(primaryKeyValue);
		if (map2 == null) {
			return;
		}

		for (EventLabelerId eventLabelerId : map2.keySet()) {
			MetaEventLabeler<?> metaEventLabeler = id_Labeler_Map.get(eventLabelerId);
			EventLabel<?> eventLabel = metaEventLabeler.getEventLabel(baseContext, event);
			Map<EventLabel<?>, Map<ReportId, MetaReportEventConsumer<?>>> map3 = map2.get(eventLabelerId);
			Map<ReportId, MetaReportEventConsumer<?>> map4 = map3.get(eventLabel);
			if (map4 != null) {
				for (ReportId reportId : map4.keySet()) {
					MetaReportEventConsumer<?> metaReportEventConsumer = map4.get(reportId);
					final ReportContentRec contentRec = new ReportContentRec();
					contentRec.agentIsEventSource = false;
					contentRec.reportId = reportId;
					contentRec.event = event;
					contentRec.metaReportEventConsumer = metaReportEventConsumer;
					reportQueue.add(contentRec);
				}
			}
		}
	}

	private <T extends Event> void addEventLabeler(EventLabeler<T> eventLabeler) {
		if (eventLabeler == null) {
			throwContractException(NucleusError.NULL_EVENT_LABELER);
		}

		Class<T> eventClass = eventLabeler.getEventClass();
		if (eventClass == null) {
			throwContractException(NucleusError.NULL_EVENT_CLASS_IN_EVENT_LABELER);
		}
		EventLabelerId id = eventLabeler.getId();
		if (id == null) {
			throwContractException(NucleusError.NULL_LABELER_ID_IN_EVENT_LABELER);
		}

		if (id_Labeler_Map.containsKey(id)) {
			throwContractException(NucleusError.DUPLICATE_LABELER_ID_IN_EVENT_LABELER);
		}

		MetaEventLabeler<T> metaEventLabeler = new MetaEventLabeler<>(this, eventLabeler, id, eventClass);

		id_Labeler_Map.put(metaEventLabeler.getId(), metaEventLabeler);
	}

	private <T extends Event> void subscribeAgentToEvent(EventLabel<T> eventLabel, AgentEventConsumer<T> agentEventConsumer) {

		if (eventLabel == null) {
			throwContractException(NucleusError.NULL_EVENT_LABEL);
		}

		if (agentEventConsumer == null) {
			throwContractException(NucleusError.NULL_EVENT_CONSUMER);
		}
		Class<T> eventClass = eventLabel.getEventClass();
		if (eventClass == null) {
			throwContractException(NucleusError.NULL_EVENT_CLASS_IN_EVENT_LABEL);
		}

		EventLabelerId eventLabelerId = eventLabel.getLabelerId();

		if (eventLabelerId == null) {
			throwContractException(NucleusError.NULL_LABELER_ID_IN_EVENT_LABEL);
		}

		MetaEventLabeler<?> metaEventLabeler = id_Labeler_Map.get(eventLabelerId);

		if (metaEventLabeler == null) {
			throwContractException(NucleusError.UNKNOWN_EVENT_LABELER);
		}

		Object primaryKeyValue = eventLabel.getPrimaryKeyValue();
		if (primaryKeyValue == null) {
			throwContractException(NucleusError.NULL_PRIMARY_KEY_VALUE);
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
			throwContractException(NucleusError.NULL_EVENT_LABEL);
		}

		Class<T> eventClass = eventLabel.getEventClass();
		if (eventClass == null) {
			throwContractException(NucleusError.NULL_EVENT_CLASS_IN_EVENT_LABELER);
		}

		EventLabelerId eventLabelerId = eventLabel.getLabelerId();
		if (eventLabelerId == null) {
			throwContractException(NucleusError.NULL_LABELER_ID_IN_EVENT_LABEL);
		}

		MetaEventLabeler<?> metaEventLabeler = id_Labeler_Map.get(eventLabelerId);
		if (metaEventLabeler == null) {
			throwContractException(NucleusError.UNKNOWN_EVENT_LABELER, eventLabelerId);
		}

		Object primaryKeyValue = eventLabel.getPrimaryKeyValue();
		if (primaryKeyValue == null) {
			throwContractException(NucleusError.NULL_PRIMARY_KEY_VALUE);
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
	private Map<Class<?>, Map<Object, Map<EventLabelerId, Map<EventLabel<?>, Map<ReportId, MetaReportEventConsumer<?>>>>>> reportPubSub = new LinkedHashMap<>();

	private void subscribeReportToSimulationClose(Consumer<ReportContext> closeHandler) {
		if (closeHandler == null) {
			throw new RuntimeException("null close handler");
		}
		simulationCloseCallbacks.put(focalReport, closeHandler);

	}

	private static enum EventPhase {
		VALIDATION, EXECUTION, POST_EXECUTION
	}

	private <T extends Event> void subscribeResolverToEventValidationPhase(Class<T> eventClass, ResolverEventConsumer<T> resolverConsumer) {

		if (eventClass == null) {
			throwContractException(NucleusError.NULL_EVENT_CLASS);
		}
		if (resolverConsumer == null) {
			throwContractException(NucleusError.NULL_EVENT_CONSUMER);
		}
		List<MetaResolverEventConsumer<?>> list = resolverEventMap.get(eventClass);
		if (list == null) {
			list = new ArrayList<>();
			resolverEventMap.put(eventClass, list);
			// invoke the increment only when adding to the map
			incrementSubscriberCount(eventClass);
		}
		MetaResolverEventConsumer<T> metaResolverEventConsumer = new MetaResolverEventConsumer<>(resolverContext, focalResolver, resolverConsumer, EventPhase.VALIDATION);

		int insertionIndex = -1;

		for (int i = 0; i < list.size(); i++) {
			MetaResolverEventConsumer<?> m = list.get(i);
			if (m.eventPhase != EventPhase.VALIDATION) {
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

	private <T extends Event> void subscribeResolverToEventExecutionPhase(Class<T> eventClass, ResolverEventConsumer<T> resolverConsumer) {
		if (eventClass == null) {
			throwContractException(NucleusError.NULL_EVENT_CLASS);
		}
		if (resolverConsumer == null) {
			throwContractException(NucleusError.NULL_EVENT_CONSUMER);
		}

		List<MetaResolverEventConsumer<?>> list = resolverEventMap.get(eventClass);
		if (list == null) {
			list = new ArrayList<>();
			resolverEventMap.put(eventClass, list);
			// invoke the increment only when adding to the map
			incrementSubscriberCount(eventClass);
		}
		MetaResolverEventConsumer<T> metaResolverEventConsumer = new MetaResolverEventConsumer<>(resolverContext, focalResolver, resolverConsumer, EventPhase.EXECUTION);

		int insertionIndex = -1;

		for (int i = 0; i < list.size(); i++) {
			MetaResolverEventConsumer<?> m = list.get(i);
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

	private <T extends Event> void subscribeResolverToEventPostPhase(Class<T> eventClass, ResolverEventConsumer<T> resolverConsumer) {
		if (eventClass == null) {
			throwContractException(NucleusError.NULL_EVENT_CLASS);
		}
		if (resolverConsumer == null) {
			throwContractException(NucleusError.NULL_EVENT_CONSUMER);
		}

		List<MetaResolverEventConsumer<?>> list = resolverEventMap.get(eventClass);
		if (list == null) {
			list = new ArrayList<>();
			resolverEventMap.put(eventClass, list);
			// invoke the increment only when adding to the map
			incrementSubscriberCount(eventClass);
		}
		MetaResolverEventConsumer<T> metaResolverEventConsumer = new MetaResolverEventConsumer<>(resolverContext, focalResolver, resolverConsumer, EventPhase.POST_EXECUTION);

		list.add(metaResolverEventConsumer);

	}

	private void unSubscribeResolverToEvent(Class<? extends Event> eventClass) {
		if (eventClass == null) {
			throwContractException(NucleusError.NULL_EVENT_CLASS);
		}

		List<MetaResolverEventConsumer<?>> list = resolverEventMap.get(eventClass);

		if (list != null) {
			Iterator<MetaResolverEventConsumer<?>> iterator = list.iterator();
			while (iterator.hasNext()) {
				MetaResolverEventConsumer<?> metaResolverEventConsumer = iterator.next();
				if (metaResolverEventConsumer.resolverId.equals(focalResolver)) {
					iterator.remove();
					decrementSubscriberCount(eventClass);
				}
			}

			if (list.isEmpty()) {
				resolverEventMap.remove(eventClass);
			}
		}
	}

	private void resolveEventForAgent(final Event event) {
		if (event == null) {
			throwContractException(NucleusError.NULL_EVENT);
		}
		List<MetaResolverEventConsumer<?>> list = resolverEventMap.get(event.getClass());
		if (list != null) {
			for (MetaResolverEventConsumer<?> metaResolverEventConsumer : list) {
				final ResolverContentRec contentRec = new ResolverContentRec();
				contentRec.agentIsEventSource = true;
				contentRec.resolverId = metaResolverEventConsumer.resolverId;
				contentRec.event = event;
				contentRec.metaResolverEventConsumer = metaResolverEventConsumer;
				resolverQueue.add(contentRec);
			}
		}
		executeResolverQueue();
	}

	private void resolveEventForResolver(final Event event) {

		if (event == null) {
			throwContractException(NucleusError.NULL_EVENT);
		}

		Map<ReportId, MetaReportEventConsumer<?>> reportMap = reportEventMap.get(event.getClass());
		if (reportMap != null) {
			for (final ReportId reportId : reportMap.keySet()) {
				MetaReportEventConsumer<?> metaReportEventConsumer = reportMap.get(reportId);
				final ReportContentRec contentRec = new ReportContentRec();
				contentRec.agentIsEventSource = false;
				contentRec.reportId = reportId;
				contentRec.event = event;
				contentRec.metaReportEventConsumer = metaReportEventConsumer;
				reportQueue.add(contentRec);
			}
		}

		broadcastEventToReportSubscribers(event);

		List<MetaResolverEventConsumer<?>> list = resolverEventMap.get(event.getClass());
		if (list != null) {
			for (MetaResolverEventConsumer<?> metaResolverEventConsumer : list) {
				final ResolverContentRec contentRec = new ResolverContentRec();
				contentRec.agentIsEventSource = false;
				contentRec.resolverId = metaResolverEventConsumer.resolverId;
				contentRec.event = event;
				contentRec.metaResolverEventConsumer = metaResolverEventConsumer;
				resolverQueue.add(contentRec);
			}
		}

		broadcastEventToAgentSubscribers(event);

		// executeResolverQueue();
	}

	/*
	 * A utility class to aid in the ordering of plugins
	 */
	private static class PluginRecord implements Comparable<PluginRecord> {
		private final int loadedOrder;
		private int dependencyOrder;
		private final PluginId pluginId;

		public PluginRecord(PluginId pluginId, int loadedOrder) {
			this.pluginId = pluginId;
			this.loadedOrder = loadedOrder;
		}

		@Override
		public int compareTo(PluginRecord pluginRecord) {
			int result = Integer.compare(dependencyOrder, pluginRecord.dependencyOrder);
			if (result == 0) {
				result = Integer.compare(loadedOrder, pluginRecord.loadedOrder);
			}
			return result;
		}

	}

	private void init(final Scaffold scaffold) {
		
		// set the output consumer
		outputConsumer = scaffold.outputConsumer;

		/*
		 * Create a map of plugin records to hold plugin data that will be used
		 * to order resolver initialization
		 */
		Map<PluginId, PluginRecord> pluginRecordMap = new LinkedHashMap<>();

		// Build the plugin records and stimulate each plugin
		int loadedOrder = 0;
		for (PluginId pluginId : scaffold.plugins.keySet()) {
			focalPluginId = pluginId;
			scaffold.plugins.get(pluginId).accept(pluginContext);
			PluginRecord pluginRecord = new PluginRecord(pluginId, loadedOrder++);
			pluginRecordMap.put(pluginId, pluginRecord);
			focalPluginId = null;
		}

		/*
		 * Check for missing plugins from the plugin dependencies that were
		 * collected from the known plugins.
		 */
		for (PluginId pluginId : pluginDependencyGraph.getNodes()) {
			if (!pluginRecordMap.containsKey(pluginId)) {
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

		// Create a graph from the plugin records
		MutableGraph<PluginRecord, Object> m = new MutableGraph<>();

		// Build the record dependency graph nodes since the edges may not
		// contain all the nodes
		for (PluginId pluginId : pluginRecordMap.keySet()) {
			PluginRecord pluginRecord = pluginRecordMap.get(pluginId);
			m.addNode(pluginRecord);
		}

		// Build the record dependency graph edges
		for (Object edge : pluginDependencyGraph.getEdges()) {
			PluginId originPluginId = pluginDependencyGraph.getOriginNode(edge);
			PluginId destinationPluginId = pluginDependencyGraph.getDestinationNode(edge);
			PluginRecord originPluginRecord = pluginRecordMap.get(originPluginId);
			PluginRecord destinationPluginRecord = pluginRecordMap.get(destinationPluginId);
			m.addEdge(new Object(), originPluginRecord, destinationPluginRecord);
		}

		/*
		 * Determine whether the graph is acyclic and generate a graph depth
		 * evaluator for the graph so that we can determine the order of
		 * initialization.
		 */
		Optional<GraphDepthEvaluator<PluginRecord>> optional = GraphDepthEvaluator.getGraphDepthEvaluator(m.toGraph());

		if (!optional.isPresent()) {
			/*
			 * Explain in detail why there is a circular dependency
			 */

			Graph<PluginRecord, Object> g = m.toGraph();
			g = Graphs.getSourceSinkReducedGraph(g);
			List<Graph<PluginRecord, Object>> cutGraphs = Graphs.cutGraph(g);
			StringBuilder sb = new StringBuilder();
			String lineSeparator = System.getProperty("line.separator");
			sb.append(lineSeparator);
			boolean firstCutGraph = true;

			for (Graph<PluginRecord, Object> cutGraph : cutGraphs) {
				if (firstCutGraph) {
					firstCutGraph = false;
				} else {
					sb.append(lineSeparator);
				}
				sb.append("Dependency group: ");
				sb.append(lineSeparator);
				Set<PluginRecord> nodes = cutGraph.getNodes().stream().collect(Collectors.toCollection(LinkedHashSet::new));

				for (PluginRecord node : nodes) {
					sb.append("\t");
					sb.append(node.pluginId);
					sb.append(" requires:");
					sb.append(lineSeparator);
					for (Object edge : cutGraph.getInboundEdges(node)) {
						PluginRecord dependencyNode = cutGraph.getOriginNode(edge);

						if (nodes.contains(dependencyNode)) {
							sb.append("\t");
							sb.append("\t");
							sb.append(dependencyNode.pluginId);
							sb.append(lineSeparator);
						}
					}
				}
			}
			throw new ContractException(NucleusError.CIRCULAR_PLUGIN_DEPENDENCIES, sb.toString());
		}

		// the graph is acyclic, so the depth evaluator is present
		GraphDepthEvaluator<PluginRecord> graphDepthEvaluator = optional.get();

		// assign the dependency order to the plugin records
		for (PluginRecord pluginRecord : pluginRecordMap.values()) {
			pluginRecord.dependencyOrder = graphDepthEvaluator.getDepth(pluginRecord);
		}

		// create the list of plugin records and sort them
		List<PluginRecord> pluginRecords = new ArrayList<>();
		pluginRecords.addAll(pluginRecordMap.values());
		Collections.sort(pluginRecords);

		// create a reverse mapping from plugins to resolvers
		Map<ResolverId, List<PluginId>> resolverIdToPluginIdMap = new LinkedHashMap<>();
		for (PluginId pluginId : pluginResolvers.keySet()) {
			Map<ResolverId, Consumer<ResolverContext>> map = pluginResolvers.get(pluginId);
			for (ResolverId resolverId : map.keySet()) {
				List<PluginId> list = resolverIdToPluginIdMap.get(resolverId);
				if (list == null) {
					list = new ArrayList<>();
					resolverIdToPluginIdMap.put(resolverId, list);
				}
				list.add(pluginId);
			}
		}

		// show that each resolver id has a single associated plugin id
		for (ResolverId resolverId : resolverIdToPluginIdMap.keySet()) {
			List<PluginId> pluginIds = resolverIdToPluginIdMap.get(resolverId);
			if (pluginIds.size() > 1) {
				StringBuilder sb = new StringBuilder();
				sb.append("The resolver id ");
				sb.append(resolverId);
				sb.append("is defined by multiple plugins: ");
				boolean first = true;
				for (PluginId pluginId : pluginIds) {
					if (first) {
						first = false;
					} else {
						sb.append(", ");
					}
					sb.append(pluginId);
				}
				throw new ContractException(NucleusError.DUPLICATE_RESOLVER_ID, sb.toString());
			}
		}

		/*
		 * Put the resolvers into the resolver queue in the order determined by
		 * the plugin dependencies
		 */
		for (PluginRecord pluginRecord : pluginRecords) {
			Map<ResolverId, Consumer<ResolverContext>> map = pluginResolvers.get(pluginRecord.pluginId);
			if (map != null) {
				for (final ResolverId resolverId : map.keySet()) {
					final ResolverContentRec contentRec = new ResolverContentRec();
					contentRec.plan = map.get(resolverId);
					contentRec.resolverId = resolverId;
					resolverQueue.add(contentRec);
				}
			}
		}
	}

	private void addPluginDependency(PluginId pluginId) {
		if (focalPluginId == null) {
			throw new ContractException(NucleusError.PLUGIN_INITIALIZATION_CLOSED);
		}
		pluginDependencyGraph.addEdge(new Object(), focalPluginId, pluginId);
	}

	private Map<PluginId, Map<ResolverId, Consumer<ResolverContext>>> pluginResolvers = new LinkedHashMap<>();

	private MutableGraph<PluginId, Object> pluginDependencyGraph = new MutableGraph<>();

	private void defineResolver(ResolverId resolverId, Consumer<ResolverContext> init) {
		if (focalPluginId == null) {
			throw new ContractException(NucleusError.PLUGIN_INITIALIZATION_CLOSED);
		}

		if (resolverId == null) {
			throw new ContractException(NucleusError.NULL_RESOLVER_ID);
		}

		Map<ResolverId, Consumer<ResolverContext>> map = pluginResolvers.get(focalPluginId);
		if (map == null) {
			map = new LinkedHashMap<>();
			pluginResolvers.put(focalPluginId, map);
		}
		map.put(resolverId, init);
	}

}