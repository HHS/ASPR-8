package nucleus;

public class Plan3Test {
	
	private static class XPlanData implements PlanData{}
	
	public static void main(String[] args) {

		Plan3 plan = Plan3	.builder()//
							.setActive(false)//
							.setKey("key")//
							.setPriority(52345234534L)//
							.setPlanData(new XPlanData())//
							.setTime(13.67)//
							.setCallbackConsumer((c) -> {
								System.out.println("whatever");
							})//
							.build();//

		plan.getCallbackConsumer().accept(null);

	}
}
