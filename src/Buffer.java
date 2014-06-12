public class Buffer {

	private int target;
	private int numSegments;
	private int count = 0;
	private int totalDownloaded = 0;
	private boolean complete = false;

	// Constructor
	public Buffer(int target, int numSegments) {
		this.target = target;
		this.numSegments = numSegments;
	}

	public synchronized int getTarget() {
		return target;
	}

	public synchronized int getCount() {
		return count;
	}

	public synchronized void decrement() {
		count--;
		assert count >= 0;

		notifyAll();		
	}

	public synchronized void increment() {
		count++;
		assert count > 0;	

		totalDownloaded++;
		assert totalDownloaded <= numSegments;

		if (totalDownloaded == numSegments) {
			complete = true;
		}

		notifyAll();		
	}

	public synchronized boolean isEmpty() {
		if (count == 0) {
			return true;
		}
		else {
			return false;
		}
	}

	public synchronized double getPercentage() {
		return ((count * 1.0) / (target * 1.0));
	}

	public synchronized boolean isComplete() {
		return complete;
	}
}
