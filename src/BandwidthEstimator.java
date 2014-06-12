public class BandwidthEstimator {

	private double estimate;
	private double cushion;
	private double alpha;
	private int currentQualityLevel = 0;
	private boolean currentQualityLevelIsSet = false;
	private String accountName;

	// Constructor
	public BandwidthEstimator(double initialEstimate, double cushion, double alpha, String accountName) {
		estimate = initialEstimate;
		this.cushion = cushion;
		this.alpha = alpha;
		this.accountName = accountName;
		//System.out.println("initial est = " + estimate);
	}

	public synchronized double getEstimate() {
		return (estimate * cushion);
	}

	public synchronized void update(double sampleBandwidth) {
		System.out.println(System.currentTimeMillis() + "\t" + accountName + "\tbw_sample\t" + sampleBandwidth);

		estimate = ((1 - alpha) * estimate) + (alpha * sampleBandwidth);

		System.out.println(System.currentTimeMillis() + "\t" + accountName + "\tbw_estimate\t" + estimate);
	}

	public synchronized void updateQualityLevel(int newQualityLevel) {
		if (!currentQualityLevelIsSet) {
			currentQualityLevel = newQualityLevel;
			currentQualityLevelIsSet = true;
			return;
		}
	
		if (currentQualityLevel > newQualityLevel) {
			System.out.println(System.currentTimeMillis() + "\t" + accountName + "\tdown\t" + currentQualityLevel + "\t" + newQualityLevel);
			currentQualityLevel = newQualityLevel;
			return;
		}
	
		if (currentQualityLevel < newQualityLevel) {
			System.out.println(System.currentTimeMillis() + "\t" + accountName + "\tup\t" + currentQualityLevel + "\t" + newQualityLevel);
			currentQualityLevel = newQualityLevel;
			return;
		}
	}
}
