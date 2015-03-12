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
package org.springframework.xd.analytics.linguistics;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.*;

import java.util.Map;

import org.junit.Test;
import org.springframework.xd.tuple.Tuple;

/**
 * @author Thomas Darimont
 */
public class RestrictedLanguageDetectorTests extends AbstractLanguageDetectorTests {

	@Override
	protected LanguageDetector createNewLanguageDetector() {

		LanguageDetector detector = super.createNewLanguageDetector();
		detector.setLanguagePriorities("en:0.1,de:0.2,fr:0.3");

		return detector;
	}

	@Test
	public void testLanguagePrioritiesTranslation_with_configured_translation() {

		assertThat(languageDetectionProcessor.languagePriorityMap.get("en"), is((Object) 0.1));
		assertThat(languageDetectionProcessor.languagePriorityMap.get("de"), is((Object) 0.2));
		assertThat(languageDetectionProcessor.languagePriorityMap.get("fr"), is((Object) 0.3));
	}

	@Test
	public void testLanguagePrioritiesTranslation_with_no_translation() {

		Map<String, Double> languagePriorityMap = languageDetectionProcessor.createLanguagePriorityMap(null);

		assertThat(languagePriorityMap.size(), is(equalTo(0)));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testLanguagePrioritiesTranslation_with_corrupt_translation() {

		Map<String, Double> languagePriorityMap = languageDetectionProcessor.createLanguagePriorityMap("aaaa:aaaa,bbb:");

		assertThat(languagePriorityMap.size(), is(equalTo(0)));
	}

	@Test
	public void testProcess_simple_short_italian_text_should_be_detected_as_english() throws Exception {

		Tuple input = newTupleWithText("Santo maccheroni");

		Tuple output = languageDetectionProcessor.process(input);

		assertThat(output.getString(predLangOutField), is("en"));
	}
}
