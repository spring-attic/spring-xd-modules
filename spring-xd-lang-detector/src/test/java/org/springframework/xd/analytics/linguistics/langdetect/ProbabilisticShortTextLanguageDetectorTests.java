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
public class ProbabilisticShortTextLanguageDetectorTests extends AbstractLanguageDetectorTests {

	@Override
	protected LanguageDetector createNewLanguageDetector() {

		LanguageDetector processor = super.createNewLanguageDetector();
		processor.setDeterministicLanguageDetection(false);

		return processor;
	}

	@Test
	public void testProcess_simple_short_english_text() throws Exception {

		Tuple input = newTupleWithText("Hello World");

		Tuple output = languageDetectionProcessor.process(input);

		assertThat(output.getString(predLangOutField), is("en"));
	}

	@Test
	public void testProcess_simple_short_german_text() throws Exception {

		Tuple input = newTupleWithText("Hallo Welt");

		Tuple output = languageDetectionProcessor.process(input);

		assertThat(output.getString(predLangOutField), is("de"));
	}

	@Test
	public void testProcess_simple_short_italian_text() throws Exception {

		Tuple input = newTupleWithText(Texts.SHORT_ITALIAN_1);

		Tuple output = languageDetectionProcessor.process(input);

		assertThat(output.getString(predLangOutField), is("it"));
	}

	@Test
	public void testProcess_simple_short_multi_language_text() throws Exception {

		Tuple input = newTupleWithText(Texts.SHORT_ENGLISH_FRENCH_MIX_1);

		Tuple output = languageDetectionProcessor.process(input);

		assertThat(output.hasFieldName(predLangOutField), is(true));
		assertThat("en fr".contains(output.getString(predLangOutField)), is(true));

		assertThat(output.hasFieldName(predLangProbsOutputField), is(true));
		assertThat(output.getValue(predLangProbsOutputField), is(not(nullValue())));
	}


	@Test
	public void testProcess_should_return_different_probabilities_for_the_same_document() throws Exception {

		Tuple input = newTupleWithText(Texts.SHORT_ENGLISH_2);

		Tuple firstOutput = languageDetectionProcessor.process(input);
		Tuple secondOutput = languageDetectionProcessor.process(input);

		assertThat(extractLanguageProbability(firstOutput, "en"), is(not(equalTo(extractLanguageProbability(secondOutput, "en")))));
	}
}
