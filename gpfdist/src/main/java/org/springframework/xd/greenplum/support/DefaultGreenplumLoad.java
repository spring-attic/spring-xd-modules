/*
 * Copyright 2015 the original author or authors.
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
package org.springframework.xd.greenplum.support;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.Assert;
import org.springframework.xd.greenplum.GreenplumLoad;
import org.springframework.xd.greenplum.LoadService;

public class DefaultGreenplumLoad implements GreenplumLoad {

	private final static Log log = LogFactory.getLog(DefaultGreenplumLoad.class);

	private final LoadService loadService;

	private final LoadConfiguration loadConfiguration;

	public DefaultGreenplumLoad(LoadConfiguration loadConfiguration, LoadService loadService) {
		this.loadConfiguration = loadConfiguration;
		this.loadService = loadService;
		Assert.notNull(loadConfiguration, "Load configuration must be set");
		Assert.notNull(loadService, "Load service must be set");
	}

	@Override
	public void load() {
		log.debug("Doing greenplum load");
		loadService.load(loadConfiguration);
	}

}