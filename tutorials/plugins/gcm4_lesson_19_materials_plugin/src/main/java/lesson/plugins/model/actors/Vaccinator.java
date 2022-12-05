package lesson.plugins.model.actors;

import lesson.plugins.model.support.MaterialsProducer;
import nucleus.ActorContext;

public class Vaccinator {
	
	public void init(ActorContext actorContext) {		
		actorContext.addActor(new VaccineProducer(MaterialsProducer.VACCINE_PRODUCER)::init);
		actorContext.addActor(new AntigenProducer(MaterialsProducer.ANTIGEN_PRODUCER_1)::init);
//		actorContext.addPlan((c)->{
//			c.addActor(new AntigenProducer(MaterialsProducer.ANTIGEN_PRODUCER_2)::init);	
//		}, 60.0);
	}

}
