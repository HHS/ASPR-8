package gov.hhs.aspr.ms.gcm.nucleus;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

public class AT_PrioritizedPlanData {
	
	private static class LocalPlanData implements PlanData{

		@Override
		public int hashCode() {
			return 31;			
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == null) {
				return false;
			}
			if (this == obj) {
				return true;
			}
			if (!(obj instanceof LocalPlanData)) {
				return false;
			}			
			return true;
		}

		@Override
		public String toString() {			
			return "LocalPlanData []";
		}
		
		
	} 
	
	@Test
	@UnitTestConstructor(target = PrioritizedPlanData.class,args= {PlanData.class, long.class})
	public void testPrioritizedPlanData() {
		//nothing to test
	}
	
	@Test
	@UnitTestMethod(target = PrioritizedPlanData.class,name="getPlanData",args= {})
	public void testGetPlanData() {
		LocalPlanData localPlanData = new LocalPlanData();
		PrioritizedPlanData prioritizedPlanData = new PrioritizedPlanData(localPlanData,22345L);
		assertEquals(localPlanData,	prioritizedPlanData.getPlanData());
	}
	
	@Test
	@UnitTestMethod(target = PrioritizedPlanData.class,name="getPriority",args= {})
	public void testGetPriority() {
		LocalPlanData localPlanData = new LocalPlanData();
		PrioritizedPlanData prioritizedPlanData = new PrioritizedPlanData(localPlanData,22345L);
		assertEquals(22345L,	prioritizedPlanData.getPriority());
	}

	@Test
	@UnitTestMethod(target = PrioritizedPlanData.class,name="toString",args= {})
	public void testToString() {
		LocalPlanData localPlanData = new LocalPlanData();
		PrioritizedPlanData prioritizedPlanData = new PrioritizedPlanData(localPlanData,1234L);		
		assertEquals("PrioritizedPlanData [planData=LocalPlanData [], priority=1234]",	prioritizedPlanData.toString());
	}



//	toString()
}
