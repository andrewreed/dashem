class Watch implements Runnable {

	private Buffer videoBuffer;
	private int movieLength, segmentDurationMillis;
	private String accountName;
	private double minimumFill;

	public Watch(Buffer videoBuffer, int movieLength, int segmentDuration, String accountName, double minimumFill) {
		this.videoBuffer = videoBuffer;
		this.movieLength = movieLength;
		this.segmentDurationMillis = segmentDuration * 1000;
		this.accountName = accountName;
		this.minimumFill = minimumFill;
	}

	@Override
	public void run() {
		synchronized (videoBuffer) {
			while ((videoBuffer.getPercentage() < minimumFill) && (!videoBuffer.isComplete())) {
				try {
					videoBuffer.wait();
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}
		}

		long elapsed = 0;
		long totalWatched = 0;
		long currentSegmentWatched = 0;
		long currentTime;
		long lastTime = System.currentTimeMillis();
		System.out.println(System.currentTimeMillis() + "\t" + accountName + "\tplayback_start");
		videoBuffer.decrement();

		while (true) {
			currentTime = System.currentTimeMillis();
			elapsed = currentTime - lastTime;
			totalWatched += elapsed;

			if ((totalWatched / 1000.0) >= movieLength) {
				System.out.println(System.currentTimeMillis() + "\t" + accountName + "\tplayback_complete");
				return;
			}

			currentSegmentWatched += elapsed;

			if (currentSegmentWatched >= segmentDurationMillis) {
				currentSegmentWatched -= segmentDurationMillis;

				if (videoBuffer.isEmpty()) {
					System.out.println(System.currentTimeMillis() + "\t" + accountName + "\tplayback_pause");
					synchronized (videoBuffer) {
						while ((videoBuffer.getPercentage() < minimumFill) && (!videoBuffer.isComplete())) {
							try {
								videoBuffer.wait();
							} catch (InterruptedException e) {
								Thread.currentThread().interrupt();
							}
						}
					}
					System.out.println(System.currentTimeMillis() + "\t" + accountName + "\tplayback_resume");
					currentTime = System.currentTimeMillis();
				}

				videoBuffer.decrement();
			}

			lastTime = currentTime;

			long timeLeftInSegment = segmentDurationMillis - currentSegmentWatched;
			if (timeLeftInSegment > 10) {
				try {
					Thread.sleep(timeLeftInSegment - 3);
				} catch (InterruptedException e) {
								Thread.currentThread().interrupt();
				}
			}
		} // end while loop
	} // end run()
}
