/*
* Copyright 2011-2015 the original author or authors.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.springframework.xd.videocap;

import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;


/**
* Message producer that reads from video source, and generates messages 
* with captured frame images which are JPEG-encoded   
*
* @author Simon Tao
* 
*/
public class VideocapChannelAdapter implements MessageSource<byte[]>, InitializingBean, DisposableBean {

	private static final String OPENCV_LIBPATH= "/usr/local/share/OpenCV/java/libopencv_java249.so";
	
	static {
		System.load(OPENCV_LIBPATH);
	}
	
	private String sourceUrl;
	
	private VideoCapture capture;

	public VideocapChannelAdapter(String sourceUrl) {
		super();
		this.sourceUrl = sourceUrl;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		capture = new VideoCapture(sourceUrl);
		if(!capture.isOpened()) { 
			throw new Exception("Can not access video source: " +  sourceUrl);
		}
	}

	@Override
	public Message<byte[]> receive() {
		Mat image = new Mat();
		if (capture.read(image)) {
			MatOfByte mob = new MatOfByte();
			Highgui.imencode(".jpeg", image, mob);
			byte[] content = mob.toArray();

			if (content != null) {
				return MessageBuilder.withPayload(content).build();
			}
		}
		return null;
	}

	@Override
	public void destroy() throws Exception {
		if ((capture != null) && capture.isOpened()) {
			capture.release();
		}
	}
		
}
