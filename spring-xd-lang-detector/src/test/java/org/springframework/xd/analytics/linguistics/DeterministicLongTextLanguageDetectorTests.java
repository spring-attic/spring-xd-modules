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

import org.junit.Test;
import org.springframework.xd.tuple.Tuple;

/**
 * @author Thomas Darimont
 */
public class DeterministicLongTextLanguageDetectorTests extends AbstractLanguageDetectorTests {

	private String LONG_ENGLISH_1 = "This document describes the runtime architecture of Spring XD and the core components and used for processing data. Use the sidebar to navigate the various sections of the documentation. The documentation on the wiki reflects the state of the master branch and the latest released version it applies to is 1.1.0.RELEASE";
	private String LONG_GERMAN_1 = "Wie jede Blüte welkt und jede Jugend  Dem Alter weicht, blüht jede Lebensstufe, Blüht jede Weisheit auch und jede Tugend  Zu ihrer Zeit und darf nicht ewig dauern. Es muß das Herz bei jedem Lebensrufe  Bereit zum Abschied sein und Neubeginne,  Um sich in Tapferkeit und ohne Trauern  In andre, neue Bindungen zu geben. Und jedem Anfang wohnt ein Zauber inne, Der uns beschützt und der uns hilft, zu leben.";
	private String LONG_ITALIANO_1 = "Fondata secondo la tradizione il 21 aprile 753 a.C. (sebbene scavi recenti nel Lapis niger farebbero risalire la fondazione a 2 secoli prima[11][12]), nel corso dei suoi tre millenni di storia è stata la prima grande metropoli dell'umanità[13], cuore di una delle più importanti civiltà antiche, che influenzò la società, la cultura, la lingua, la letteratura, l'arte, l'architettura, la filosofia, la religione, il diritto e i costumi dei secoli successivi. Luogo di origine della lingua latina, fu capitale dell'Impero romano, che estendeva il suo dominio su tutto il bacino del Mediterraneo e gran parte dell'Europa, dello Stato Pontificio, sottoposto al potere temporale dei papi, e del Regno d'Italia (dal 1871).";

	@Override
	protected LanguageDetector createNewLanguageDetector() {

		LanguageDetector processor = super.createNewLanguageDetector();
		processor.setTextModel(TextModel.LONGTEXT);
		processor.setDeterministicLanguageDetection(true);

		return processor;
	}

	@Test
	public void testProcess_simple_long_english_text() throws Exception {

		Tuple input = newTupleWithText(LONG_ENGLISH_1);

		Tuple output = languageDetectionProcessor.process(input);

		assertThat(output.getString(predLangOutField), is("en"));
	}

	@Test
	public void testProcess_simple_long_german_text() throws Exception {

		Tuple input = newTupleWithText(LONG_GERMAN_1);

		Tuple output = languageDetectionProcessor.process(input);

		assertThat(output.getString(predLangOutField), is("de"));
	}

	@Test
	public void testProcess_simple_short_italian_text() throws Exception {

		Tuple input = newTupleWithText(LONG_ITALIANO_1);

		Tuple output = languageDetectionProcessor.process(input);

		assertThat(output.getString(predLangOutField), is("it"));
	}

	@Test
	public void testProcess_simple_long_multi_language_text() throws Exception {

		Tuple input = newTupleWithText(LONG_ENGLISH_1 + LONG_GERMAN_1);

		Tuple output = languageDetectionProcessor.process(input);

		assertThat(output.hasFieldName(predLangOutField), is(true));
		assertThat(output.getString(predLangOutField), is("de"));

		assertThat(output.hasFieldName(predLangProbsOutputField), is(true));
		assertThat(output.getValue(predLangProbsOutputField), is(not(nullValue())));
	}


	@Test
	public void testProcess_should_return_the_same_probabilities_for_the_same_document() throws Exception {

		Tuple input = newTupleWithText(LONG_GERMAN_1);

		Tuple firstOutput = languageDetectionProcessor.process(input);
		Tuple secondOutput = languageDetectionProcessor.process(input);

		assertThat(extractLanguageProbability(firstOutput, "de"), is(equalTo(extractLanguageProbability(secondOutput, "de"))));
	}
}
