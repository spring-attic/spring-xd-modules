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
public class DeterministicLongTextLanguageDetectorTests extends AbstractLanguageDetectorTests {

	@Override
	protected LanguageDetector createNewLanguageDetector() {

		LanguageDetector processor = super.createNewLanguageDetector();
		processor.setTextModel(TextModel.LONGTEXT);
		processor.setDeterministicLanguageDetection(true);

		return processor;
	}

	@Test
	public void testProcess_simple_long_english_text() throws Exception {

		Tuple input = newTupleWithText(Texts.LONG_ENGLISH_1);

		Tuple output = languageDetectionProcessor.process(input);

		assertThat(output.getString(predLangOutField), is("en"));
	}

	@Test
	public void testProcess_simple_long_german_text() throws Exception {

		Tuple input = newTupleWithText(Texts.LONG_GERMAN_1);

		Tuple output = languageDetectionProcessor.process(input);

		assertThat(output.getString(predLangOutField), is("de"));
	}

	@Test
	public void testProcess_simple_short_italian_text() throws Exception {

		Tuple input = newTupleWithText(Texts.LONG_ITALIAN_1);

		Tuple output = languageDetectionProcessor.process(input);

		assertThat(output.getString(predLangOutField), is("it"));
	}

	@Test
	public void testProcess_simple_long_multi_language_text() throws Exception {

		Tuple input = newTupleWithText(Texts.LONG_ENGLISH_1 + Texts.LONG_GERMAN_1);

		Tuple output = languageDetectionProcessor.process(input);

		assertThat(output.hasFieldName(predLangOutField), is(true));
		assertThat(output.getString(predLangOutField), is("de"));

		assertThat(output.hasFieldName(predLangProbsOutputField), is(true));
		assertThat(output.getValue(predLangProbsOutputField), is(not(nullValue())));
	}


	@Test
	public void testProcess_should_return_the_same_probabilities_for_the_same_document() throws Exception {

		Tuple input = newTupleWithText(Texts.LONG_GERMAN_1);

		Tuple firstOutput = languageDetectionProcessor.process(input);
		Tuple secondOutput = languageDetectionProcessor.process(input);

		assertThat(extractLanguageProbability(firstOutput, "de"), is(equalTo(extractLanguageProbability(secondOutput, "de"))));
	}
}
