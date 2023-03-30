package nucleus;

public class Plan2Test {
	private static class XPlanData implements PlanData{}
	
	public static void main(String[] args) {
		Plan2.Builder<ActorContext> builder = Plan2.builder();

		Plan2<ActorContext> plan = builder	.setActive(false)//
											.setKey("key")//
											.setPriority(52345234534L)//
											.setPlanData(new XPlanData())//
											.setTime(13.67)//
											.setCallbackConsumer((c) -> {
												System.out.println("okay");
											})//
											.build();//

		plan.getCallbackConsumer().accept(null);

	}
}
