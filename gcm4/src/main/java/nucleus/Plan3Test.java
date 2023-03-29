package nucleus;

public class Plan3Test {
	public static void main(String[] args) {

		Plan3 plan = Plan3	.builder()//
							.setActive(false)//
							.setKey("key")//
							.setArrivalId(52345234534L)//
							.setPlanData(456)//
							.setTime(13.67)//
							.setCallbackConsumer((c) -> {
								System.out.println("whatever");
							})//
							.build();//

		plan.getCallbackConsumer().accept(null);

	}
}
