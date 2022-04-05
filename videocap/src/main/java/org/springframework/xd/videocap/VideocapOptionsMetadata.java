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

import javax.validation.constraints.NotNull;

import org.springframework.xd.module.options.spi.ModuleOption;

/**
 * Documents the options of the videocap module.
 *
 * @author Simon Tao
 */
public class VideocapOptionsMetadata {

	private String sourceUrl;
	private int pollingDelay;

	@NotNull
	public String getSourceUrl() {
		return sourceUrl;
	}

	@ModuleOption("the url of the video source, either from camera or file")
	public void setSourceUrl(String sourceUrl) {
		this.sourceUrl = sourceUrl;
	}

	public int getPollingDelay() {
		return pollingDelay;
	}

	@ModuleOption(value = "the delay(ms) between pollings", defaultValue = "0")
	public void setPollingDelay(int pollingDelay) {
		this.pollingDelay = pollingDelay;
	}
}
