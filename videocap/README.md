Video Capturing Source Module
================================

Is a source that captures frame image from video, which can be from either camera or file. This video capturing source module keeps grabbing frames from the video and emitting messages with JPEG-encoded image as the payload. This is to provide
a method for users to ingest and decode video into a sequence of images, amenable to later processing, i.e. object detection with computer-vision algorithms.

## Requirements

In order to install the module and run it in your Spring XD installation, you will need to have installed:

* Spring XD version 1.1.x or later ([Instructions](http://docs.spring.io/spring-xd/docs/current/reference/html/#getting-started))

## Building with Gradle

	$./gradlew build

This videocap source module depends on opencv, and the specific opencv version this source module is built upon and tested againt is 2.4.9. For this project, opencv needs to be built seperately and its java API support opencv-249.jar need to be in folder `[project-dir]/lib`. Also the shared library libopencv_java249.so built from opencv should be located in folder `/usr/local/share/OpenCV/java/` on each xd-container host for this videocap module to work correctly.

## Using the Custom Module

The jar will be in `[project-build-dir]/videocap-1.0.0.BUILD-SNAPSHOT.jar`. To install and register the module to your Spring XD distribution, use the `module upload` Spring XD shell command. Start Spring XD and the shell:


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
	xd:>module upload --file [path-to]/videocap-1.0.0.BUILD-SNAPSHOT.jar --name videocap --type source
	Successfully uploaded module 'source:videocap'
	xd:>


You can also get information about the available module options:

```
xd:>module info --name source:videocap
Information about source module 'videocap':

  Option Name   Description                                              Default              Type
  ------------  -------------------------------------------------------  -------------------  --------
  pollingDelay  the delay(ms) between pollings                           0                    int
  sourceUrl	the url of the video source, either from camera or file  <none>               String
  outputType    how this module should emit messages it produces         <none>               MimeType
```

Now create and deploy a stream:

	xd:>stream create videocapTest --definition "videocap --sourceUrl=rtsp://localhost:7654/sample | log" --deploy


You should see the output that indicates the log sink is receiving messages with byte array payload. Actually, each received byte array represents the binary of a JPEG-encoded image grabbed from the rtsp video stream. 
