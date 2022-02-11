package plugins.properties.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import nucleus.SimulationContext;
import nucleus.testsupport.MockSimulationContext;
import util.MutableDouble;
import util.annotations.UnitTest;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

@UnitTest(target = PropertyValueRecord.class)
public class AT_PropertyValueRecord {

	
	
	
	
	/**
	 * test for {@link PropertyValueRecord#getValue()}
	 */
	@Test
	@UnitTestMethod(name = "getValue", args = {})
	public void testGetValue() {
		MutableDouble time = new MutableDouble(345.6);
		MockSimulationContext mockSimulationContext = MockSimulationContext.builder().setTimeSupplier(()->time.getValue()).build();
		
		PropertyValueRecord propertyValueRecord = new PropertyValueRecord(mockSimulationContext);
		propertyValueRecord.setPropertyValue("cat");
		assertEquals("cat", propertyValueRecord.getValue());

		time.setValue(456.2);		
		propertyValueRecord.setPropertyValue("dog");
		assertEquals("dog", propertyValueRecord.getValue());
	}

	/**
	 * test for {@link PropertyValueRecord#setPropertyValue(Object)}
	 */
	@Test
	@UnitTestMethod(name = "setPropertyValue", args = { Object.class })
	public void testSetPropertyValue() {
		MutableDouble time = new MutableDouble(345.6);
		MockSimulationContext mockSimulationContext = MockSimulationContext.builder().setTimeSupplier(()->time.getValue()).build();
		
		PropertyValueRecord propertyValueRecord = new PropertyValueRecord(mockSimulationContext);
		propertyValueRecord.setPropertyValue("cat");
		assertEquals("cat", propertyValueRecord.getValue());

		time.setValue(456.2);		
		propertyValueRecord.setPropertyValue("dog");
		assertEquals("dog", propertyValueRecord.getValue());
	}

	/**
	 * test for {@link PropertyValueRecord#getAssignmentTime()}
	 */
	@Test
	@UnitTestMethod(name = "getAssignmentTime", args = {})
	public void testGetAssignmentTime() {
		MutableDouble time = new MutableDouble(345.6);
		MockSimulationContext mockSimulationContext = MockSimulationContext.builder().setTimeSupplier(()->time.getValue()).build();
		
		PropertyValueRecord propertyValueRecord = new PropertyValueRecord(mockSimulationContext);
		propertyValueRecord.setPropertyValue("cat");
		assertEquals(time.getValue(), propertyValueRecord.getAssignmentTime(), 0);

		time.setValue(456.2);		
		propertyValueRecord.setPropertyValue("dog");
		assertEquals(time.getValue(), propertyValueRecord.getAssignmentTime(), 0);
	}

	@Test
	@UnitTestConstructor(args = { SimulationContext.class })
	public void testConstructor() {
		MockSimulationContext mockSimulationContext = MockSimulationContext.builder().build();
		PropertyValueRecord propertyValueRecord = new PropertyValueRecord(mockSimulationContext);
		assertNotNull(propertyValueRecord);
		assertEquals(0, propertyValueRecord.getAssignmentTime());		
	}

}