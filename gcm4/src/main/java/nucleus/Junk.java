package nucleus;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public class Junk {

	private class PD1 implements PlanData {

	}

	private class PD2 implements PlanData {

	}

	private static Consumer<ActorContext> convert(PD1 shitHead) {
		return (c) -> {
			System.out.println("fuck yous");
		};
	}

	private ActorId focalActorId = new ActorId(77);

	private Map<ActorId, Map<Class, Function<PlanData, Consumer<ActorContext>>>> actorPlanDataConverters = new LinkedHashMap<>();

	public <T extends PlanData> void asdf(Class<T> planDataClass, Function<T, Consumer<ActorContext>> f) {
		Map<Class, Function<PlanData, Consumer<ActorContext>>> map = actorPlanDataConverters.get(focalActorId);
		if (map == null) {
			map = new LinkedHashMap<>();
			actorPlanDataConverters.put(focalActorId, map);
		}
		
		map.put((Class)planDataClass, f);
	}

	public static void main(String[] args) {
		Junk junk = new Junk();

		junk.asdf(PD1.class, Junk::convert);
	}

}
