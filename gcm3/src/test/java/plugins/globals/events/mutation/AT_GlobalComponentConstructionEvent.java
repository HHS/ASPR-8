package plugins.globals.events.mutation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.function.Consumer;

import org.junit.jupiter.api.Test;

import nucleus.AgentContext;
import plugins.globals.support.GlobalComponentId;
import plugins.globals.support.SimpleGlobalComponentId;
import util.annotations.UnitTest;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

@UnitTest(target = GlobalComponentConstructionEvent.class)
public final class AT_GlobalComponentConstructionEvent {

	@Test
	@UnitTestConstructor(args = { GlobalComponentId.class, Consumer.class })
	public void testConstructor() {
		assertNotNull(new GlobalComponentConstructionEvent(null, null));
	}

	@Test
	@UnitTestMethod(name = "getGlobalComponentId", args = {})
	public void testGetGlobalComponentId() {
		GlobalComponentId globalComponentId = new SimpleGlobalComponentId("id");
		Consumer<AgentContext> consumer = (c) -> {};
		GlobalComponentConstructionEvent globalComponentConstructionEvent = new GlobalComponentConstructionEvent(globalComponentId, consumer);

		assertEquals(globalComponentId, globalComponentConstructionEvent.getGlobalComponentId());
	}

	@Test
	@UnitTestMethod(name = "getConsumer", args = {})
	public void testGetConsumer() {
		GlobalComponentId globalComponentId = new SimpleGlobalComponentId("id");
		Consumer<AgentContext> consumer = (c) -> {};
		GlobalComponentConstructionEvent globalComponentConstructionEvent = new GlobalComponentConstructionEvent(globalComponentId, consumer);

		assertEquals(consumer, globalComponentConstructionEvent.getConsumer());

	}

}
