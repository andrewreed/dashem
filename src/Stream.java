import java.util.*;
import java.io.*;
import java.net.*;

class Stream implements Runnable {

	private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:23.0) Gecko/20100101 Firefox/23.0";
	private static final String DUMMY_FILE = "/dashemulatordata/dummy_file_for_traffic_generation/" +
				                                   "really_long_path_to_dummy_file_for_padding/" +
				                                   "sample_movie_segment_from_itec_klagenfurt_germany/" +
				                                   "elephants_dream/ed_15s/ed_15sec_8000kbit/ed_15sec3.m4s";

	private Buffer videoBuffer;
	private MovieData movie;
	private String contentServer;
	private BandwidthEstimator bandwidth;
	private List<Double> qualityList;
	private String accountName;

	public Stream(Buffer videoBuffer, MovieData movie, String contentServer, 
	              BandwidthEstimator bandwidth, List<Double> qualityList, String accountName) {
		this.videoBuffer = videoBuffer;
		this.movie = movie;
		this.contentServer = contentServer;
		this.bandwidth = bandwidth;
		this.qualityList = qualityList;
		this.accountName = accountName;
	}

	@Override
	public void run() {
		System.out.println(System.currentTimeMillis() + "\t" + accountName + "\tstreaming_start");

		while (true) {
			synchronized (videoBuffer) {
				while ((videoBuffer.getCount() >= videoBuffer.getTarget()) && (!videoBuffer.isComplete())) {
					try {
						videoBuffer.wait();
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
					}
				}
			}

			int currentSegmentSize = movie.getNextSize();

			if (currentSegmentSize == -1) {
				System.out.println(System.currentTimeMillis() + "\t" + accountName + "\tstreaming_complete");
				return;
			}

			if (currentSegmentSize == 0) {
				requestAudio();
			}
			else {
				requestVideo(currentSegmentSize);
			}

		} // end primary while loop
	} // end run()


	private void requestAudio() {
		int requestSize = movie.getAudioSize();

		URL url;
		HttpURLConnection conn;
		InputStream is;
		byte[] buffer = new byte[65536];
		int n;
		int c = 0;

		try {
			url = new URL("http://" + contentServer + DUMMY_FILE);
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("User-Agent", USER_AGENT);
			conn.setRequestProperty("Range", "bytes=1-" + requestSize);

			is = conn.getInputStream();
			while ((n = is.read(buffer)) != -1) {c+=n;}
			is.close();
		} catch (Exception e) {
			System.out.println(System.currentTimeMillis() + "\t" + accountName + "\tERROR: Unable to retrieve next audio segment.");
			System.exit(0);
		}
	} // end requestAudio()


	private void requestVideo(int currentSegmentSize) {
		int requestSize = (int)Math.round(currentSegmentSize * requestQuality());

		URL url;
		HttpURLConnection conn;
		InputStream is;
		byte[] buffer = new byte[65536];
		int n;
		int c = 0;

		long totalDownloadTime = 0;

		try {
			url = new URL("http://" + contentServer + DUMMY_FILE);
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("User-Agent", USER_AGENT);
			conn.setRequestProperty("Range", "bytes=1-" + requestSize);

			System.out.println(System.currentTimeMillis() + "\t" + accountName + "\tvideo_request\t" + 
												((int)Math.round(requestQuality() * movie.getMaxBitrate() * 8.0)) + "\t" + requestSize);

			long startDownloadTime = System.currentTimeMillis();
			is = conn.getInputStream();
			while ((n = is.read(buffer)) != -1) {c+=n;}
			long finishDownloadTime = System.currentTimeMillis();
			totalDownloadTime = finishDownloadTime - startDownloadTime;

			is.close();
		} catch (Exception e) {
			System.out.println(System.currentTimeMillis() + "\t" + accountName + "\tERROR: Unable to retrieve next video segment.");
			System.exit(0);
		}

		bandwidth.update((requestSize * 1.0) / totalDownloadTime);

		videoBuffer.increment();
	} // end requestVideo()	


	private double requestQuality() {
		double bwEstimate = bandwidth.getEstimate();
		double qualityToRequest = 1.0;

		for (double qualityIter : qualityList) {
			qualityToRequest = qualityIter;
			if (bwEstimate >= (qualityToRequest * movie.getMaxBitrate())) {
				break;
			}
		}
		
		bandwidth.updateQualityLevel((int)Math.round(qualityToRequest * movie.getMaxBitrate() * 8.0));
		return qualityToRequest;
	} // end requestQuality()
}
