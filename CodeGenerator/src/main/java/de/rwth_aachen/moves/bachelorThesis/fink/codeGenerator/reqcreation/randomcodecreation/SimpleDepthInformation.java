package de.rwth_aachen.moves.bachelorThesis.fink.codeGenerator.reqcreation.randomcodecreation;

/**
 * An instance of this class, at any point in time,
 * tells in what "depth we are" (relative to something)
 * as well as how many nodes are involved so far.
 * Used in conjunction with DepthController.java while generating the tree.
 */
class SimpleDepthInformation {
	private int mTotalNodeCount = 0;
	private int mCurrentDepth = 0;

	SimpleDepthInformation() { }

	private SimpleDepthInformation(SimpleDepthInformation copy) {
		this.mTotalNodeCount = copy.mTotalNodeCount;
		this.mCurrentDepth = copy.mCurrentDepth;
	}

	void goIntoDepthAndIncrementNodeCount() {
		++mTotalNodeCount;
		++mCurrentDepth;
	}

	void goOutOfDepth() {
		--mCurrentDepth;
		assert(mCurrentDepth >= 0);
	}

	int getTotalNodeCount() {
		return mTotalNodeCount;
	}

	int getCurrentDepth() {
		return mCurrentDepth;
	}

	void incrementNodeCount() {
		++mTotalNodeCount;
	}

	void increaseNodeCount(int i) {
		mTotalNodeCount += i;
	}

	SimpleDepthInformation copy() {
		return new SimpleDepthInformation(this);
	}

	void revertState(SimpleDepthInformation old) {
		this.mTotalNodeCount = old.getTotalNodeCount();
		this.mCurrentDepth = old.getCurrentDepth();
	}
}
