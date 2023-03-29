package nucleus;

public class Plan1Test {
	public static void main(String[] args) {

		Plan1<ActorContext> plan = Plan1.builder(ActorContext.class)//
										.setActive(false)//
										.setKey("key")//
										.setArrivalId(52345234534L)//
										.setPlanData(456)//
										.setTime(13.67)//
										.setCallbackConsumer((i) -> {
											System.out.println("okay");
										})//
										.build();//

		
		plan.getCallbackConsumer().accept(null);

	}
}
