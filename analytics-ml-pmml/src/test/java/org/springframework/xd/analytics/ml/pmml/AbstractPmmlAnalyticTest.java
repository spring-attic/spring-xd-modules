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

import java.util.List;

import org.springframework.xd.analytics.ml.Analytic;
import org.springframework.xd.tuple.Tuple;

/**
 * @author Thomas Darimont
 */
public class AbstractPmmlAnalyticTest {

	protected Analytic<Tuple, Tuple> useAnalytic(String modelName, List<String> inputFieldNames, List<String> outputFieldNames) {
		return this.useAnalytic(modelName, null, new TuplePmmlAnalyticInputDataMapper(inputFieldNames),
				new TuplePmmlAnalyticOutputDataMapper(outputFieldNames));
	}

	protected Analytic<Tuple, Tuple> useAnalytic(String modelName, String modelLocation, List<String> inputFieldNames, List<String> outputFieldNames) {
		return this.useAnalytic(modelName, modelLocation, new TuplePmmlAnalyticInputDataMapper(inputFieldNames),
				new TuplePmmlAnalyticOutputDataMapper(outputFieldNames));
	}

	protected Analytic<Tuple, Tuple> useAnalytic(String modelName, String modelLocation,
												 TuplePmmlAnalyticInputDataMapper inputMapper, TuplePmmlAnalyticOutputDataMapper outputMapper) {
		return new TuplePmmlAnalytic(modelName, modelLocation != null ? modelLocation : "classpath:analytics/pmml/" + modelName + ".pmml.xml", inputMapper, outputMapper);
	}
}
