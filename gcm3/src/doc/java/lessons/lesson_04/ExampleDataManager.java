package lessons.lesson_04;

import nucleus.DataManager;
import nucleus.DataManagerContext;

public final class ExampleDataManager extends DataManager {
	
	private int alpha = 7;
	
	private double beta = 1.2345;
	
	private DataManagerContext dataManagerContext;
	
	@Override
	public void init(DataManagerContext dataManagerContext) {
		super.init(dataManagerContext);
		this.dataManagerContext = dataManagerContext;
		System.out.println("ExampleDataManager is initialized");
	}
	
	public int getAlpha() {
		return alpha;
	}
	
	public void setAlpha(int alpha) {
		this.alpha = alpha;
		System.out.println("ExampleDataManager sets alpha = "+alpha+" at time = "+dataManagerContext.getTime());
	}
	
	public double getBeta() {
		return beta;
	}
	
	public void setBeta(double beta) {
		this.beta = beta;
		System.out.println("ExampleDataManager sets beta = "+beta+" at time = "+dataManagerContext.getTime());
	}
	
}


