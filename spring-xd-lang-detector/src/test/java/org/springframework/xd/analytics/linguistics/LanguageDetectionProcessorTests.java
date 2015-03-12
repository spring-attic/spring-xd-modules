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
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.springframework.xd.tuple.Tuple;
import org.springframework.xd.tuple.TupleBuilder;

/**
 * @author Thomas Darimont
 */
public class LanguageDetectionProcessorTests {

	LanguageDetectionProcessor languageDetectionProcessor;
	private String predLangOutField = "pred_lang";
	private String predLangProbsOutputField = "pred_lang_probs";

	@Before
	public void setup() throws Exception {

		languageDetectionProcessor = new LanguageDetectionProcessor();
		languageDetectionProcessor.setReturnMostLikelyLanguage(true);
		languageDetectionProcessor.setReturnLanguageProbabilities(true);

		languageDetectionProcessor.afterPropertiesSet();
	}

	@Test
	public void testProcess_simple_short_english_text() throws Exception {

		Tuple input = TupleBuilder.tuple().of("text", "Hello World");

		Tuple output = languageDetectionProcessor.process(input);

		assertThat(output.getString(predLangOutField), is("en"));
	}

	@Test
	public void testProcess_simple_short_german_text() throws Exception {

		Tuple input = TupleBuilder.tuple().of("text", "Hallo Welt");

		Tuple output = languageDetectionProcessor.process(input);

		assertThat(output.getString(predLangOutField), is("de"));
	}

	@Test
	public void testProcess_simple_short_italian_text() throws Exception {

		Tuple input = TupleBuilder.tuple().of("text", "Santo maccheroni");

		Tuple output = languageDetectionProcessor.process(input);

		assertThat(output.getString(predLangOutField), is("it"));
	}

	@Test
	public void testProcess_simple_short_multi_language_text() throws Exception {

		Tuple input = TupleBuilder.tuple().of("text", "Bonjour Howdy");

		Tuple output = languageDetectionProcessor.process(input);

		assertThat(output.hasFieldName(predLangOutField), is(true));
		assertThat(output.getString(predLangOutField), is("en"));

		assertThat(output.hasFieldName(predLangProbsOutputField), is(true));
		assertThat(output.getValue(predLangProbsOutputField), is(not(nullValue())));
	}
}