package nucleus;

public class Plan2Test {
	public static void main(String[] args) {
		Plan2.Builder<ActorContext> builder = Plan2.builder();

		Plan2<ActorContext> plan = builder	.setActive(false)//
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
