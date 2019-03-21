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

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

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
	public void testProcess_simple_short_italian_text_should_be_detected_as_english() throws Exception {

		Tuple input = newTupleWithText(Texts.SHORT_ITALIAN_1);

		Tuple output = languageDetectionProcessor.process(input);

		assertThat(output.getString(predLangOutField), is("en"));
	}
}
