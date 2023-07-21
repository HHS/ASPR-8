package lesson.plugins.model.actors.antigenproducer;

import nucleus.PlanData;
import plugins.materials.support.StageId;

public final class StagePlanData implements PlanData {
		private final StageId stageId;
		private final double planTime;

		public StagePlanData(StageId stageId, double planTime) {
			super();
			this.stageId = stageId;
			this.planTime = planTime;
		}

		public StageId getStageId() {
			return stageId;
		}

		public double getPlanTime() {
			return planTime;
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("EndFermentationStagePlanData [stageId=");
			builder.append(stageId);
			builder.append(", planTime=");
			builder.append(planTime);
			builder.append("]");
			return builder.toString();
		}
		
		
		
	}