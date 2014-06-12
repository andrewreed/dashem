/*────────────────────────────────────────────────────────────────────┐
│ Andrew C. Reed, Major, U.S. Army                                    │
│ reed@cs.unc.edu                                                     │
│                                                                     │
│ Please read the following at https://github.com/andrewreed/dashem   │
│  - README.md                                                        │
└────────────────────────────────────────────────────────────────────*/

import java.util.*;
import java.math.*;
import java.io.*;
import java.net.*;

public class Dashem {

	private static final double BUFFER_LENGTH = 240.0;    // length of movie to buffer (in seconds)
	private static final double MINIMUM_FILL = 0.125;     // buffer must be this full for playback to start/resume
	private static final double THROUGHPUT_CUSHION = 0.6; // amount to multiply the bandwidth estimate by (i.e. create a safe margin)
	private static final double ALPHA = 0.125;            // smoothing constant for the EWMA used in the bandwidth estimator

	private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:23.0) Gecko/20100101 Firefox/23.0";
	private static final String DUMMY_FILE = "/dashemulatordata/dummy_file_for_traffic_generation/" +
				                                   "really_long_path_to_dummy_file_for_padding/" +
				                                   "sample_movie_segment_from_itec_klagenfurt_germany/" +
				                                   "elephants_dream/ed_15s/ed_15sec_8000kbit/ed_15sec3.m4s";

	public static void main(String args[]) {

		// Connect to the content server and retrieve setup information
		String config = setup(args[0], args[1], args[2], args[3]);
		String[] settings = config.split(",");
		
		// Convert the max bitrate (which is provided as kbps) into Bytes per millisecond
		int maxBitrate = (int)Math.round(Integer.parseInt(settings[0]) / 8.0);

		// Load the quality levels into a list as percentages
		String[] qualityLevels = settings[1].split("\\s");
		List<Double> qualityList = new LinkedList<Double>();
		for (int i = 0; i < qualityLevels.length ; i++) {
			qualityList.add(Double.parseDouble(qualityLevels[i]) / 100.0);
		}

		// Initialize info about the movie
		int segmentDuration = Integer.parseInt(settings[2]);
		int videoPerAudio = Integer.parseInt(settings[3]);
		int audioSize = Integer.parseInt(settings[4]);
		MovieData movie = new MovieData(maxBitrate, settings[5], segmentDuration, videoPerAudio, audioSize);

		// Initialize a buffer that will store BUFFER_LENGTH seconds of the movie
		Buffer videoBuffer = new Buffer((int)(long)Math.ceil(BUFFER_LENGTH / segmentDuration), movie.getNumSegments());

		// Initialize the bandwidth estimate to the bitrate of the lowest quality level
		BandwidthEstimator bandwidth = new BandwidthEstimator((qualityList.get(qualityList.size()-1) * maxBitrate), 
		                                                      THROUGHPUT_CUSHION, ALPHA, args[3]);

		Stream streamer = new Stream(videoBuffer, movie, args[0], bandwidth, qualityList, args[3]);
		Thread streamer1 = new Thread(streamer, "streamer1");

		Watch watcher = new Watch(videoBuffer, movie.getMovieLength(), segmentDuration, args[3], MINIMUM_FILL);
		Thread watcher1 = new Thread(watcher, "watcher1");

		streamer1.start();
		watcher1.start();

		try {
			streamer1.join();
			watcher1.join();
		} catch (InterruptedException e) {
			System.out.println(System.currentTimeMillis() + "\t" + args[3] + "\tERROR: Threads did not join() properly.");
		}

	} // end main()


	public static String setup(String contentServerAddr, String service, String video, String accountName) {
		URL url;
		HttpURLConnection conn;
		InputStream is;
		BufferedReader rd;
		String line;
		String config = "";
		
		try {
			// Retrieve the service-wide configuration data
			url = new URL("http://" + contentServerAddr + "/dashemulatordata/" + service + "/config/servicewideconfig.txt");
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("User-Agent", USER_AGENT);
			is = conn.getInputStream();
			rd = new BufferedReader(new InputStreamReader(is));
			while ((line = rd.readLine()) != null) {
				config += line + ",";
			}
			rd.close();
			is.close();

			// Retrieve the segment sizes for the movie
			url = new URL("http://" + contentServerAddr + "/dashemulatordata/" + service + "/videos/" + video + ".txt");
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("User-Agent", USER_AGENT);
			is = conn.getInputStream();
			rd = new BufferedReader(new InputStreamReader(is));
			while ((line = rd.readLine()) != null) {
				config += line + " ";
			}
			rd.close();
			//is.close();
			conn.disconnect();

		} catch (Exception e) {
			System.out.println(System.currentTimeMillis() + "\t" + accountName + "\tERROR: Unable to retrieve configuration settings.");
			System.exit(0);
		}
			
		return config;
	} // end setup()
}
