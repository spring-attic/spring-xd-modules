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

import java.util.List;
import java.util.NoSuchElementException;

import com.cybozu.labs.langdetect.DetectorFactory;
import com.cybozu.labs.langdetect.Language;
import org.junit.Before;
import org.springframework.xd.tuple.Tuple;
import org.springframework.xd.tuple.TupleBuilder;

/**
 * @author Thomas Darimont
 */
public abstract class AbstractLanguageDetectorTests {

	protected LanguageDetector languageDetectionProcessor;
	protected String predLangOutField = "pred_lang";
	protected String predLangProbsOutputField = "pred_lang_probs";

	@Before
	public void setup() throws Exception {

		DetectorFactory.clear();

		languageDetectionProcessor = createNewLanguageDetector();
		languageDetectionProcessor.afterPropertiesSet();
	}


	protected Tuple newTupleWithText(String text) {
		return TupleBuilder.tuple().of("text", text);
	}

	@SuppressWarnings("unchecked")
	protected double extractLanguageProbability(Tuple tuple, String language) {

		List<Language> langs = (List<Language>) tuple.getValue(predLangProbsOutputField);
		for (Language lang : langs) {
			if (lang.lang.equals(language)) {
				return lang.prob;
			}
		}

		throw new NoSuchElementException("Language with code: " + language + " could not be found.");
	}

	protected LanguageDetector createNewLanguageDetector() {

		LanguageDetector detector = new LanguageDetector();

		detector.setReturnMostLikelyLanguage(true);
		detector.setReturnLanguageProbabilities(true);
		detector.setTextModel(TextModel.SHORTTEXT);
		detector.setInputTextContentPropertyName("text");
		detector.setMostLikelyLanguageOutputPropertyName("pred_lang");
		detector.setLanguageProbabilitiesOutputPropertyName("pred_lang_probs");

		return detector;
	}
}