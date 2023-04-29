package plugins.util.properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import nucleus.testsupport.testplugin.TestPluginFactory;
import nucleus.testsupport.testplugin.TestPluginFactory.Factory;
import nucleus.testsupport.testplugin.TestSimulation;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;
import util.errors.ContractException;


public class AT_AbstractIndexedPropertyManager {

	/*
	 * 
	 * A simple concrete extension of AbstractIndexedPropertyManager used to
	 * test AbstractIndexedPropertyManager.
	 *
	 */
	private static class SimplePropertyManager extends AbstractIndexedPropertyManager {

		public SimplePropertyManager(PropertyDefinition propertyDefinition, int initialSize) {
			super(propertyDefinition, initialSize);
		}

		@Override
		public <T> T getPropertyValue(int id) {
			return null;
		}

	}

	@Test
	@UnitTestConstructor(target = AbstractIndexedPropertyManager.class,args = { PropertyDefinition.class, int.class })
	public void testConstructor() {
		
		
		Factory factory = TestPluginFactory.factory((c) -> {

			PropertyDefinition goodPropertyDefinition = PropertyDefinition.builder().setType(Boolean.class).setDefaultValue(false).build();

			// if the property definition is null
			ContractException contractException = assertThrows(ContractException.class, () -> new BooleanPropertyManager(null, 0));
			assertEquals(PropertyError.NULL_PROPERTY_DEFINITION, contractException.getErrorType());

			// if the initial size is negative
			contractException = assertThrows(ContractException.class, () -> new BooleanPropertyManager(goodPropertyDefinition, -1));
			assertEquals(PropertyError.NEGATIVE_INITIAL_SIZE, contractException.getErrorType());

			SimplePropertyManager simplePropertyManager = new SimplePropertyManager(goodPropertyDefinition, 0);
			assertNotNull(simplePropertyManager);
		});
		
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
	}

	@Test
	@UnitTestMethod(target = AbstractIndexedPropertyManager.class,name = "setPropertyValue", args = { int.class, Object.class })
	public void testSetPropertyValue() {
		Factory factory = TestPluginFactory.factory((c) -> {

			PropertyDefinition propertyDefinition = PropertyDefinition.builder()//
					.setType(Boolean.class)//
					.setDefaultValue(false)//					
					.build();

			// precondition tests
			SimplePropertyManager simplePropertyManager = new SimplePropertyManager(propertyDefinition, 0);
			ContractException contractException = assertThrows(ContractException.class, () -> simplePropertyManager.setPropertyValue(-1, false));
			assertEquals(PropertyError.NEGATIVE_INDEX, contractException.getErrorType());
		});
		
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
	}

	
	@Test
	@UnitTestMethod(target = AbstractIndexedPropertyManager.class,name = "removeId", args = { int.class })
	public void testRemoveId() {
		Factory factory = TestPluginFactory.factory((c) -> {
			// precondition tests
			PropertyDefinition def = PropertyDefinition.builder().setType(Boolean.class).setDefaultValue(true)//
					.build();
			SimplePropertyManager spm = new SimplePropertyManager(def, 0);

			ContractException contractException = assertThrows(ContractException.class, () -> spm.removeId(-1));
			assertEquals(PropertyError.NEGATIVE_INDEX, contractException.getErrorType());
		});
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
	}

	@Test
	@UnitTestMethod(target = AbstractIndexedPropertyManager.class,name = "incrementCapacity", args = { int.class })
	public void testIncrementCapacity() {
		Factory factory = TestPluginFactory.factory((c) -> {

			PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Boolean.class).setDefaultValue(false)//
					.build();

			SimplePropertyManager simplePropertyManager = new SimplePropertyManager(propertyDefinition, 0);

			// precondition tests
			ContractException contractException = assertThrows(ContractException.class, () -> simplePropertyManager.incrementCapacity(-1));
			assertEquals(PropertyError.NEGATIVE_CAPACITY_INCREMENT, contractException.getErrorType());
		});
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
	}
}
