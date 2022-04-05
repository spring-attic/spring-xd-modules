/*
* Copyright 2015 the original author or authors.
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

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import org.junit.Test;
import org.springframework.messaging.Message;

/**
 * Tests for {@link VideocapChannelAdapter}.
 * 
 * @author Thomas Darimont
 */
public class VideocapChannelAdapterTests {

	@Test
	public void testGrabFirstFrame() throws Exception {

		String pathToVideo = getClass().getClassLoader().getResource("samples/SampleVideo_720x480_1mb.mp4").getFile();

		VideocapChannelAdapter adapter = new VideocapChannelAdapter(pathToVideo);
		adapter.afterPropertiesSet();

		Message<byte[]> msg = adapter.receive();
		assertThat(msg, is(notNullValue()));

		adapter.destroy();
	}
}
