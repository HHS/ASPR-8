package util.dimensiontree.internal;

/**
 * The common parameters shared by all nodes that takes up less memory than
 * storing these values on each node
 * 
 * @author Shawn Hatch
 *
 */
public class CommonState {

	public final int leafSize;

	public final int dimension;

	public final int childCount;

	public CommonState(int leafSize, int dimension) {
		this.leafSize = leafSize;
		this.dimension = dimension;
		this.childCount = 1 << dimension;
	}

}
