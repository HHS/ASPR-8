package nucleus.testsupport.testplugin;

public class AT_TestSimulation {

//	@Test
//	@UnitTestMethod(target = TestSimulation.class, name = "executeSimulation", args = { List.class })
//	public void testExecuteSimulation_Plugins() {
//		MutableBoolean executed = new MutableBoolean();
//		TestPluginData testPluginData = TestPluginData.builder().addTestActorPlan("actor", new TestActorPlan(0, c -> executed.setValue(true))).build();
//		List<Plugin> plugins = Arrays.asList(TestPlugin.getTestPlugin(testPluginData));
//		assertDoesNotThrow(() -> TestSimulation.executeSimulation(plugins));
//		assertTrue(executed.getValue());
//
//		// precondition: list of plugins is null
//		ContractException contractException = assertThrows(ContractException.class, () -> {
//			List<Plugin> nullPluginList = null;
//			TestSimulation.executeSimulation(nullPluginList);
//		});
//		assertEquals(NucleusError.NULL_PLUGINS, contractException.getErrorType());
//
//		// precondition: list of plugins is contains a null plugin
//		List<Plugin> pluginList = new ArrayList<>();
//		pluginList.add(null);
//		contractException = assertThrows(ContractException.class, () -> TestSimulation.executeSimulation(pluginList));
//		assertEquals(NucleusError.NULL_PLUGIN, contractException.getErrorType());
//
//		// precondition: if the simulation does not complete successfully
//		testPluginData = TestPluginData	.builder()//
//										.addTestActorPlan("actor", new TestActorPlan(0, c -> c.halt()))//
//										.addTestActorPlan("actor", new TestActorPlan(1, c -> {
//										}))//
//										.build();
//		List<Plugin> plugins2 = Arrays.asList(TestPlugin.getTestPlugin(testPluginData));
//
//		contractException = assertThrows(ContractException.class, () -> {
//			TestSimulation.executeSimulation(plugins2);
//		});
//		assertEquals(TestError.TEST_EXECUTION_FAILURE, contractException.getErrorType());
//	}

}
