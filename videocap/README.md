Video Capturing Source Module
================================

This module supports capturing frames from video, which can originate from either a camera or file.
A message with the JPEG-encoded image data as the payload is emitted into the stream For each grabbed frame of the input video.
The main use case is to allow to ingest and decode video data into a sequence of images, amenable to later processing, i.e. object detection with computer-vision algorithms.

## Requirements

In order to install the module and run it in your Spring XD installation, you will need to have installed:

* Spring XD version 1.1.x or later ([Instructions](https://docs.spring.io/spring-xd/docs/current/reference/html/#getting-started))

## Building with Gradle

	$./gradlew clean test bootRepackage

This videocap source module depends on the open source computer vision library [opencv](https://opencv.org).
The version of this source module is built upon and tested against is 2.4.9-4. 

For this project, you need to install the opencv Java API support `opencv-2.4.9-4.jar` to the folder `[XD_HOME]/lib` on each xd-container host for this videocap module to work correctly. This is necessary to avoid loading the native opencv library twice into the JVM process. Note that all native libraries are included in the jar and will be extracted automatically if necessary.

## Using the Custom Module

The jar will be in `build/lib/videocap-1.0.0.BUILD-SNAPSHOT.jar`. To install and register the module to your Spring XD distribution, use the `module upload` Spring XD shell command
After the module was uploaded correctly you can start Spring XD and the XD Shell:


	_____                           __   _______
	/  ___|          (-)             \ \ / /  _  \
	\ `--. _ __  _ __ _ _ __   __ _   \ V /| | | |
 	`--. \ '_ \| '__| | '_ \ / _` |   / ^ \| | | |
	/\__/ / |_) | |  | | | | | (_| | / / \ \ |/ /
	\____/| .__/|_|  |_|_| |_|\__, | \/   \/___/
    	  | |                  __/ |
      	|_|                 |___/
	eXtreme Data
	1.1.0.BUILD-SNAPSHOT | Admin Server Target: http://localhost:9393
	Welcome to the Spring XD shell. For assistance hit TAB or type "help".
	xd:>module upload --file /path/to/videocap-1.0.0.BUILD-SNAPSHOT.jar --name videocap --type source
	Successfully uploaded module 'source:videocap'
	xd:>

You can also get information about the available module options:

```
xd:>module info --name source:videocap
Information about source module 'videocap':

  Option Name   Description                                              Default  Type
  ------------  -------------------------------------------------------  -------  --------
  pollingDelay  the delay(ms) between pollings                           0        int
  sourceUrl     the url of the video source, either from camera or file  <none>   String
  outputType    how this module should emit messages it produces         <none>   MimeType
```

Now create and deploy a stream:

	xd:>stream create videocapTest --definition "videocap --sourceUrl=rtsp://CameraIP:7654/sample | log" --deploy


You should see the output that indicates the log sink is receiving messages with byte array payload. Actually, each received byte array represents the binary of a JPEG-encoded image grabbed from the rtsp video stream. 

For testing purposes you can also grab the frames from a local video file:

	stream create video1 --definition "videocap --sourceUrl='/path/to/SampleVideo_720x480_1mb.mp4' | log"
	
Which will result in the following output sent to the console:
```
...
2015-05-18 14:03:11,208 1.2.0.SNAP  INFO DeploymentSupervisor-0 zk.ZKStreamDeploymentHandler - Deployment status for stream 'video1': DeploymentStatus{state=deployed}
2015-05-18 14:03:11,385 1.2.0.SNAP  INFO task-scheduler-4 sink.video1 - [B@53bd20b3
2015-05-18 14:03:11,519 1.2.0.SNAP  INFO task-scheduler-4 sink.video1 - [B@16bfa4a0
2015-05-18 14:03:11,651 1.2.0.SNAP  INFO task-scheduler-4 sink.video1 - [B@6557eee7
2015-05-18 14:03:11,774 1.2.0.SNAP  INFO task-scheduler-4 sink.video1 - [B@27e372a0
...
```