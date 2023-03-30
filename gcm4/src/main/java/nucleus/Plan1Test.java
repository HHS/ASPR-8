package nucleus;

public class Plan1Test {
	private static class XPlanData implements PlanData{}
	
	private static void acceptA(ActorContext actorContext) {

	}

	public static void main(String[] args) {

		Plan1<ActorContext> plan = Plan1.builder(ActorContext.class)//
										.setCallbackConsumer(Plan1Test::acceptA)//
										.setCallbackConsumer((c) -> System.out.println("okay"))//										
										.setTime(13.67)//
										.setActive(false)//
										.setKey("key")//
										.setPriority(52345234534L)//
										.setPlanData(new XPlanData())//
										.build();//

		plan.getCallbackConsumer().accept(null);

	}
}
