package gov.hhs.aspr.ms.gcm.nucleus.testsupport.runcontinuityplugin;

import gov.hhs.aspr.ms.gcm.nucleus.Plugin;
import gov.hhs.aspr.ms.util.errors.ContractException;
import net.jcip.annotations.ThreadSafe;

/**
 * A plugin providing a person property management to the simulation.
 */
@ThreadSafe
public final class RunContinuityPlugin {

	private static class Data {
		private RunContinuityPluginData runContinuityPluginData;
	}

	private RunContinuityPlugin() {

	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {
		private Builder() {
		}

		private Data data = new Data();

		private void validate() {
			if (data.runContinuityPluginData == null) {
				throw new ContractException(RunContinuityError.NULL_RUN_CONTINUITY_PLUGN_DATA);
			}
		}

		/**
		 * Builds the RunContinuityPlugin from the collected inputs
		 * 
		 * @throws ContractException {@linkplain RunContinuityError#NULL_RUN_CONTINUITY_PLUGN_DATA}
		 *                           if the runContinuityPluginData is null
		 */
		public Plugin build() {

			validate();
			Plugin.Builder builder = Plugin.builder();//
			builder.setPluginId(RunContinuityPluginId.PLUGIN_ID);//
			builder.addPluginData(data.runContinuityPluginData);//

			builder.setInitializer((c) -> {
				RunContinuityPluginData runContinuityPluginData = c.getPluginData(RunContinuityPluginData.class).get();
				c.addActor(new RunContinuityActor(runContinuityPluginData));
			});
			return builder.build();

		}

		/**
		 * Sets the RunContinuityPluginData
		 */
		public Builder setRunContinuityPluginData(RunContinuityPluginData runContinuityPluginData) {
			data.runContinuityPluginData = runContinuityPluginData;
			return this;
		}

	}

}
