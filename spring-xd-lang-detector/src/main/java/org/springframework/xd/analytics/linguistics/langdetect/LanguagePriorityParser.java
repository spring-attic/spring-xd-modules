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
package org.springframework.xd.analytics.linguistics.langdetect;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.springframework.util.StringUtils;

/**
 * Parses a languagePriority configuration from a {@link java.lang.String} into a {@code Map&lt;String,Double&gt;}.
 * More details on language prioritization can be found <a href="https://code.google.com/p/language-detection/wiki/FrequentlyAskedQuestion">here</a>.
 *
 * @author Thomas Darimont
 */
public class LanguagePriorityParser {

	/**
	 * Parses a comma separated list of language code and weight pairs into a {@code Map} representation.
	 * An example for a valid input string is <pre>en:0.1,de:0.2,fr:0.3</pre>.
	 *
	 * @param input
	 * @return a {@code Map} of language to double weight or an empty {@code Map} if the input string was {@literal null} or empty.
	 */
	public Map<String, Double> parseToLanguagePriorityMap(String input) {

		if (StringUtils.isEmpty(input)) {
			return Collections.emptyMap();
		}

		try {
			String[] langCodeAndWeightPairs = input.split(",");

			Map<String, Double> map = new HashMap<String, Double>();
			for (String pair : langCodeAndWeightPairs) {
				String[] langCodeAndWeight = pair.split(":");
				map.put(langCodeAndWeight[0], Double.parseDouble(langCodeAndWeight[1]));
			}

			return map;
		} catch (Exception ex) {
			throw new IllegalArgumentException("Given input string must be a list of lang:double, e.g. de:0.1,en:01 - but got <" + input + ">");
		}
	}
}
