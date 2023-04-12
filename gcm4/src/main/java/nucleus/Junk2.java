package nucleus;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public class Junk2 {

	private static enum Action {
		INFECT, KILL, RECOVER
	}

	private static class PD1 implements PlanData {

	}

	private static class PD2 implements PlanData {
		private final Action action;

		private PD2(Action action) {
			this.action = action;
		}

		public Action getAction() {
			return action;
		}
	}

	private static Consumer<ActorContext> convert1(PD1 thing) {
		return (c) -> {
			System.out.println("cghxfgh");
		};
	}

	private static Consumer<ActorContext> convert2(PD2 thing) {
		Action action = thing.getAction();
		switch (action) {
		case INFECT:
			return (c) -> {
				System.out.println("infect");
			};
		case KILL:
			return (c) -> {
				System.out.println("kill");
			};
		case RECOVER:
			return (c) -> {
				System.out.println("recover");
			};
		default:
			throw new RuntimeException("unhandled action " + action);
		}

	}

	private ActorId focalActorId;
	
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

	public static void main(String[] args) {
		ActorId actorId0 = new ActorId(0);
		ActorId actorId1 = new ActorId(1);

		Junk2 junk = new Junk2();
		junk.focalActorId = actorId0;
		junk.setActorPlanDataConverter(PD1.class, Junk2::convert1);
		junk.focalActorId = actorId1;
		junk.setActorPlanDataConverter(PD2.class, Junk2::convert2);

		junk.focalActorId = actorId0;
		Consumer<ActorContext> consumer = junk.getActorContextConsumer(junk.focalActorId, new PD1());
		if (consumer != null) {
			consumer.accept(null);
		} else {
			System.out.println("could not convert case 1");
		}

		junk.focalActorId = actorId1;
		consumer = junk.getActorContextConsumer(junk.focalActorId, new PD2(Action.INFECT));
		if (consumer != null) {
			consumer.accept(null);
		} else {
			System.out.println("could not convert case 2");
		}
	}

}
