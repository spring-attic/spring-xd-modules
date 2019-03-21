/*
* Copyright 2011-2015 the original author or authors.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* https://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.springframework.xd.videocap;

import java.util.concurrent.TimeUnit;

import nu.pattern.OpenCV;

import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.util.Assert;

/**
 * Message producer that reads from a video source, and generates messages with captured frames encoded as JPEG images.
 *
 * @author Simon Tao
 * @author Thomas Darimont
 */
public class VideocapChannelAdapter implements MessageSource<byte[]>, InitializingBean, DisposableBean {

	static {
		OpenCV.loadLibrary();
	}

	private final String sourceUrl;

	private VideoCapture capture;

	/**
	 * Creates a new {@link VideocapChannelAdapter} from the given {@code sourceUrl}.
	 * 
	 * @param sourceUrl must not be {@literal null}.
	 */
	public VideocapChannelAdapter(String sourceUrl) {

		Assert.notNull(sourceUrl, "SourceUrl must not be null!");

		this.sourceUrl = sourceUrl;
	}

	@Override
	public void afterPropertiesSet() throws Exception {

		capture = new VideoCapture(sourceUrl);

		Assert.isTrue(capture.isOpened(), "Can not access video source: " + sourceUrl);
	}

	@Override
	public void destroy() throws Exception {

		if (capture == null) {
			return;
		}

		if (capture.isOpened()) {
			capture.release();
		}
	}

	@Override
	public Message<byte[]> receive() {

		Mat image = new Mat();
		if (!capture.read(image)) {
			return null;
		}

		MatOfByte mob = new MatOfByte();
		Highgui.imencode(".jpeg", image, mob);
		byte[] content = mob.toArray();

		return MessageBuilder.withPayload(content).build();
	}
}
