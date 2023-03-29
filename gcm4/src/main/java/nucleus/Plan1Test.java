package nucleus;

public class Plan1Test {

	private static void acceptD(DataManagerContext dataManagerContext) {

	}

	private static void acceptA(ActorContext actorContext) {

	}

	public static void main(String[] args) {

		Plan1<ActorContext> plan = Plan1.builder(ActorContext.class)//
										.setActive(false)//
										.setKey("key")//
										.setArrivalId(52345234534L)//
										.setPlanData(456)//
										.setTime(13.67)//
										.setCallbackConsumer(Plan1Test::acceptA)//
										.setCallbackConsumer((c) -> System.out.println("okay"))//									
										//.setCallbackConsumer(Plan1Test::acceptD)//
										.build();//

		plan.getCallbackConsumer().accept(null);

	}
}
