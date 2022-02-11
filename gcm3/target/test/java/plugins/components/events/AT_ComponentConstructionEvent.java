package plugins.components.events;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.function.Consumer;

import org.junit.jupiter.api.Test;

import nucleus.AgentContext;
import plugins.components.support.ComponentId;
import plugins.components.testsupport.SimpleComponentId;
import util.annotations.UnitTest;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

@UnitTest(target = ComponentConstructionEvent.class)
public final class AT_ComponentConstructionEvent {

	@Test
	@UnitTestConstructor(args = { ComponentId.class, Consumer.class })
	public void testConstructor() {
		assertNotNull(new ComponentConstructionEvent(new ComponentId() {
		}, (c) -> {
		}));
	}

	@Test
	@UnitTestMethod(name = "getComponentId", args = {})
	public void testGetComponentId() {
		
		for (int i = 0; i < 10; i++) {
			Consumer<AgentContext> consumer = (c) -> {};
			ComponentId componentId = new SimpleComponentId(i);
			ComponentConstructionEvent componentConstructionEvent = new ComponentConstructionEvent(componentId, consumer);
			assertEquals(componentId, componentConstructionEvent.getComponentId());
		}
	}

	@Test
	@UnitTestMethod(name = "getConsumer", args = {})
	public void testGetConsumer() {
		
		for (int i = 0; i < 10; i++) {
			Consumer<AgentContext> consumer = (c) -> {};
			ComponentId componentId = new SimpleComponentId(i);
			ComponentConstructionEvent componentConstructionEvent = new ComponentConstructionEvent(componentId, consumer);
			assertEquals(consumer, componentConstructionEvent.getConsumer());
		}
	}

	@Test
	@UnitTestMethod(name = "getPrimaryKeyValue", args = {})
	public void testGetPrimaryKeyValue() {
		for (int i = 0; i < 10; i++) {
			Consumer<AgentContext> consumer = (c) -> {};
			ComponentId componentId = new SimpleComponentId(i);
			ComponentConstructionEvent componentConstructionEvent = new ComponentConstructionEvent(componentId, consumer);
			assertEquals(ComponentConstructionEvent.class, componentConstructionEvent.getPrimaryKeyValue());
		}

	}

}
