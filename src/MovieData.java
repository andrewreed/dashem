import java.math.*;

public class MovieData {

	private int maxBitrate;      // in Bytes per millisecond
	private int[] sizeArray;
	private int movieLength;     // in seconds
	private int videoPerAudio;
	private int audioSize;
	private int nextSegment = 0;
	private boolean audioHasBeenRequested = false;

	// Constructor
	public MovieData(int maxBitrate, String sizes, int segmentDuration, int videoPerAudio, int audioSize) {
		this.maxBitrate = maxBitrate;
		this.videoPerAudio = videoPerAudio;
		this.audioSize = audioSize;

		String[] sizeArrayStrings = sizes.split("\\s");
		int[] tempSizeArray = new int[sizeArrayStrings.length];
		for (int i = 0; i < sizeArrayStrings.length; i++) {
			tempSizeArray[i] = Integer.parseInt(sizeArrayStrings[i]);
		}
		sizeArray = tempSizeArray;

		movieLength = sizeArray.length * segmentDuration;
	}

	public int getMaxBitrate() {
		return maxBitrate;
	}

	public int getMovieLength() {
		return movieLength;
	}

	public int getNumSegments() {
		return sizeArray.length;
	}

	public int getAudioSize() {
		return audioSize;
	}

	public synchronized int getNextSize() {
		assert nextSegment <= sizeArray.length;

		// tell the streamer that no more video segments are left
		if (nextSegment == sizeArray.length) {
			return -1;
		}

		// tell the streamer that audio must be downloaded every {videoPerAudio} segments
		if (((nextSegment % videoPerAudio) == 0) && (!audioHasBeenRequested)) {
			audioHasBeenRequested = true;
			return 0;
		}
		else {
			audioHasBeenRequested = false;
			return sizeArray[nextSegment++];
		}
	}
}
