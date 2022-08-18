package lessons.lesson_05;

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
	}
	
	public int getAlpha() {
		return alpha;
	}
	
	
	
	public double getBeta() {
		return beta;
	}
	
	public void setAlpha(int alpha) {
		int previousValue = this.alpha;
		this.alpha = alpha;			
		dataManagerContext.releaseEvent(new AlphaChangeEvent(previousValue, this.alpha));
	
	}
	
	public void setBeta(double beta) {
		double previousValue = this.beta;
		this.beta = beta;
		dataManagerContext.releaseEvent(new BetaChangeEvent(previousValue, this.beta));
	}
	
	
}
