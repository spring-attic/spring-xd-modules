/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.xd.analytics.ml.pmml;

import java.io.BufferedInputStream;
import java.net.URL;

import org.springframework.util.Assert;
import org.springframework.util.ResourceUtils;
import org.xml.sax.InputSource;

/**
 * An {@link AbstractPmmlLoader} that can load
 * PMML documents from a {@code modelLocation} given as resource URI.
 *
 * @author Thomas Darimont
 */
public class ResourcePmmlLoader extends AbstractPmmlLoader {

	/**
	 * @param modelLocation must not be {@literal null}
	 * @return
	 * @throws Exception
	 */
	@Override
	protected InputSource getPmmlText(String modelLocation) throws Exception {

		Assert.notNull(modelLocation, "modelLocation must not be null!");

		URL url = ResourceUtils.getURL(modelLocation);

		if (log.isDebugEnabled()) {
			log.debug("Trying to load pmml from modelLocation: " + modelLocation);
		}

		return new InputSource(new BufferedInputStream(url.openStream()));
	}
}
