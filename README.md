## _dashem_

An open source Dynamic Adaptive Streaming over HTTP (DASH) client emulator. 
This emulator is designed to allow researchers to test their own networking 
strategies (e.g. _buffering_, _bandwidth estimation_, and _bitrate selection_ 
algorithms) using video data from "real world" DASH services.

### Instructions

#### Initial Setup

The following instructions demonstrate how to setup _dashem_.

First, install Ubuntu Linux on two separate PCs.

* __PC1__ will serve as a _content server_. For this example,
let's assume that its IP address is __192.168.1.100__.
* __PC2__ will run _dashem_.

On __PC1__:

1. Install Apache HTTP Server. Although we only need an HTTP server, an easy way to
setup an entire LAMP stack is to run the command:

		sudo apt-get install lamp-server^

2. Place the directory named __dashemulatordata__ in the directory __/var/www/__.

On __PC2__:

1. Install the Java Runtime Environment with the command:

		sudo apt-get install openjdk-7-jre

2. Install _trickle_ with the command:

		sudo apt-get install trickle

3. Copy _Dashem.jar_ to the directory __~/dashem/__.

#### Running _dashem_

To run an instance of _dashem_ given the example architecture from above,
on __PC2__ navigate to the folder __~/dashem/__ and run the following command:

	trickle -u 750 -d 750 java -jar Dashem.jar 192.168.1.100 netflix boneknapper johndoe

This will cause _dashem_ to stream _Legend of the Boneknapper Dragon_. Additionally, _trickle_
will limit this instance of _dashem_ to 6 Mbps for both upload and download bandwidth.

### Video Profiles

_dashem_ includes profiles for the following Netflix videos:

1. __avengers__ = _The Avengers_ (2012), 142.87 min
2. __hungergames__ = _The Hunger Games_ (2012), 136.8 min
3. __boneknapper__ = _Legend of the Boneknapper Dragon_ (2010), 17.07 min
4. __office_s01e01__ = _The Office_, TV Show, Season 1 (2005), Episode 1, 23.13 min
5. __office_s01e02__ = _The Office_, TV Show, Season 1 (2005), Episode 2, 21.87 min
6. __walkingdead_s01e01__ = _The Walking Dead_, TV Show, Season 1 (2010), Episode 1, 67.2 min
7. __coldplay__ = _Coldplay Live 2012_ (2012), 59.13 min
8. __vampirediaries_s01e01__ = _The Vampire Diaries_, TV Show, Season 1 (2009), Episode 1, 42.33 min
9. __curiousgeorge_s01e01__ = _Curious George_, TV Show, Season 1 (2006), Episode 1, 23.73 min
10. __lost_s01e01__ = _Lost_, TV Show, Season 1 (2004), Episode 1, 42.26 min

### Creating Video Profiles

Use [Profiler](https://github.com/andrewreed/Profiler) to generate profiles of Netflix __and__ Amazon videos. This program is _fast_! 
_Profiler_ will generate profiles for 2-, 4-, 8-, and 16-second durations (regardless of a service's default segment duration).

### Additional Information

1. __Persistent TCP Connections__: _dashem_ will utilize a persistent TCP connection when streaming as long as the _content server_ allows for it (a default installation of Apache has its _KeepAlive_ directive _on_). This behavior stems from the fact that [Java's URLConnection places recently used connections in a cache](http://docs.oracle.com/javase/7/docs/technotes/guides/net/http-keepalive.html). Both Netflix and Amazon Instant Video use persistent TCP connections.
2. __trickle__: _trickle_ is a bandwidth-limiting utility that can be used to restrict _each instance_ of _dashem_. This is particularly useful if multiple instances of _dashem_ will be run on a PC that is connected to a high-speed interface, as each instance of _dashem_ can be limited according to either the (i) average downstream bandwidth to "video-watching" households in a geographic region or (ii) to the caps of a specific ISP.

	Still, _trickle_ is no substitute for the addition of delay to account for "real world" round trip times (RTTs). Indeed, we recommend that researchers employ both _trickle_ __and__ traffic controllers (to add delay) in order to replicate both aspects of "real world" connections. Furthermore, on networks with extremely low RTTs, _trickle_ may not correctly throttle the first video segment.

	One method to add delay to the architecture described in the _Initial Setup_ section is to run the following command on __PC1__:

		sudo tc qdisc add dev eth0 root netem delay 80ms

	Interestingly, _trickle_ uses kilobytes per second as its units, and _dashem_ samples bandwidth as bytes per millisecond. Thus, if _trickle_ is working properly, then the bandwidth samples reported by _dashem_ should be close to the "-d" argument given to _trickle_.

3. __HTTP GET padding__: In our paper, we detail how we replicate the sizes of inbound ADUs (video and audio segments). We note here that we also replicate the sizes of outbound ADUs (HTTP GETs) by padding the URL for the dummy file with a long path. This causes the sizes of outbound ADUs to order on approx. 433 bytes, which is at the lower end of what we have observed when using Firefox to stream a Netflix video (433-469 bytes, on average).

### Extra Analysis

We have included an Excel spreadsheet titled _Hunger Games - 1050 Actual vs 1050 Estimate_ to further demonstrate how well the estimated segment sizes reproduce the lower bitrate encodings. This spreadsheet depicts the actual segment sizes for the 1050 Kbps encoding of _The Hunger Games_ against their estimated sizes. Here, the estimated sizes are off by an average of 113 KB per segment, and the total sum of data generated by the estimate is off by 1 MB (0.096%).

### Credit / Copying

As a work of the United States Government, _dashem_ is 
in the public domain within the United States. Additionally, 
Andrew Reed waives copyright and related rights in the work 
worldwide through the CC0 1.0 Universal public domain dedication 
(which can be found at http://creativecommons.org/publicdomain/zero/1.0/).
