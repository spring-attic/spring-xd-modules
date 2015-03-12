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

import static org.springframework.util.StringUtils.*;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

import com.cybozu.labs.langdetect.Detector;
import com.cybozu.labs.langdetect.DetectorFactory;
import com.cybozu.labs.langdetect.LangDetectException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.util.FileCopyUtils;
import org.springframework.xd.tuple.Tuple;
import org.springframework.xd.tuple.TupleBuilder;

/**
 * A processor that can predict the language of a piece of text extracted from a {@link org.springframework.xd.tuple.Tuple}.
 * <p>
 * Language prediction is supported for short and long texts via different language models.
 * Note since the {@code langdetect} library holds the language model as static state one
 * can only deal with one model instance per {@link java.lang.ClassLoader} at a time.
 * </p>
 *
 * The langdetect library has the following characteristics.
 * <ul>
 *     <li>Generate language profiles from Wikipedia abstract xml</li>
 *     <li>Detect language of a text using naive Bayesian filter</li>
 *     <li>99% over precision for 53 languages</li>
 * </ul>
 *
 * More details can be found the the <a href="https://code.google.com/p/language-detection/wiki/FrequentlyAskedQuestion">langdetect FAQ</a>.
 *
 * @author Thomas Darimont
 */
public class LanguageDetectionProcessor implements InitializingBean {

	private static final Logger LOG = LoggerFactory.getLogger(LanguageDetectionProcessor.class);

	private String languageProfileLocation;

	private TextModel textModel = TextModel.SHORTTEXT;

	private String inputTextContentPropertyName = "text";

	private String mostLikelyLanguageOutputPropertyName = "pred_lang";

	private String languageProbabilitiesOutputPropertyName = "pred_lang_probs";

	private boolean returnMostLikelyLanguage = true;

	private boolean returnLanguageProbabilities = false;

	/**
	 * Performs the language prediction based on text extracted from the given {@link org.springframework.xd.tuple.Tuple}.
	 * <p>
	 * The text used for the language prediction is extracted via the {@link org.springframework.xd.analytics.linguistics.LanguageDetectionProcessor#inputTextContentPropertyName}
	 * from the given {@code Tuple}.
	 * </p>
	 *
	 * @param input the {@code Tuple} to extract the text from
	 * @return a new {@code Tuple} with the predicted language information.
	 * @throws LangDetectException
	 */
	public Tuple process(Tuple input) throws LangDetectException {

		if (!isLanguageDetectionEnabled()) {
			return input;
		}

		String text = input.getString(getInputTextContentPropertyName());

		Detector detector = DetectorFactory.create();
		detector.append(text);

		List<String> names = new ArrayList<String>();
		List<Object> values = new ArrayList<Object>();

		names.addAll(input.getFieldNames());
		values.addAll(input.getValues());

		if (isReturnMostLikelyLanguage()) {
			names.add(getMostLikelyLanguageOutputPropertyName());
			values.add(detector.detect());
		}

		if (isReturnLanguageProbabilities()) {
			names.add(getLanguageProbabilitiesOutputPropertyName());
			values.add(detector.getProbabilities());
		}

		return TupleBuilder.tuple().ofNamesAndValues(names, values);
	}

	private boolean isLanguageDetectionEnabled() {
		return isReturnMostLikelyLanguage() || isReturnLanguageProbabilities();
	}

	@Override
	public void afterPropertiesSet() throws Exception {

		if (!DetectorFactory.getLangList().isEmpty()) {
			LOG.info("Skipping initialization of detector since langList has already been initialized.");
			return;
		}

		if (!isEmpty(this.languageProfileLocation)) {
			LOG.info("Using language profiles from {}.", languageProfileLocation);

			Resource languageProfileResource = new DefaultResourceLoader(getClass().getClassLoader()).getResource(languageProfileLocation);
			DetectorFactory.loadProfile(languageProfileResource.getFile());
		}

		DetectorFactory.loadProfile(loadEmbeddedLanguageModels());

		LOG.info("Loaded language profiles from {}.", languageProfileLocation);
	}

	private List<String> loadEmbeddedLanguageModels() {

		List<String> languageModels = new ArrayList<String>();

		Set<String> supportedLanguages = new TreeSet<String>();
		for (Locale locale : Locale.getAvailableLocales()) {

			if (locale.getLanguage().isEmpty()) {
				continue;
			}

			supportedLanguages.add(locale.getLanguage().toLowerCase());
		}

		//added these manually since they were not present in the available Locales.
		supportedLanguages.add("zh-cn");
		supportedLanguages.add("zh-tw");

		LOG.info("Using embedded language profiles from classpath.");

		for (String lang : supportedLanguages) {
			try (InputStream is = DetectorFactory.class.getClassLoader().getResourceAsStream("profiles/" + getTextModel().name().toLowerCase() + "/" + lang)) {
				String json = FileCopyUtils.copyToString(new InputStreamReader(is));
				languageModels.add(json);
			} catch (Exception ex) {
				continue;
			}
		}

		return languageModels;
	}

	public String getLanguageProfileLocation() {
		return languageProfileLocation;
	}

	public void setLanguageProfileLocation(String languageProfileLocation) {
		this.languageProfileLocation = languageProfileLocation;
	}

	public String getInputTextContentPropertyName() {
		return inputTextContentPropertyName;
	}

	public void setInputTextContentPropertyName(String inputTextContentPropertyName) {
		this.inputTextContentPropertyName = inputTextContentPropertyName;
	}

	public String getMostLikelyLanguageOutputPropertyName() {
		return mostLikelyLanguageOutputPropertyName;
	}

	public void setMostLikelyLanguageOutputPropertyName(String mostLikelyLanguageOutputPropertyName) {
		this.mostLikelyLanguageOutputPropertyName = mostLikelyLanguageOutputPropertyName;
	}

	public String getLanguageProbabilitiesOutputPropertyName() {
		return languageProbabilitiesOutputPropertyName;
	}

	public void setLanguageProbabilitiesOutputPropertyName(String languageProbabilitiesOutputPropertyName) {
		this.languageProbabilitiesOutputPropertyName = languageProbabilitiesOutputPropertyName;
	}

	public boolean isReturnMostLikelyLanguage() {
		return returnMostLikelyLanguage;
	}

	public void setReturnMostLikelyLanguage(boolean returnMostLikelyLanguage) {
		this.returnMostLikelyLanguage = returnMostLikelyLanguage;
	}

	public boolean isReturnLanguageProbabilities() {
		return returnLanguageProbabilities;
	}

	public void setReturnLanguageProbabilities(boolean returnLanguageProbabilities) {
		this.returnLanguageProbabilities = returnLanguageProbabilities;
	}

	public TextModel getTextModel() {
		return textModel;
	}

	public void setTextModel(TextModel textModel) {
		this.textModel = textModel;
	}

	/**
	 * Enum for the supported text language models.
	 */
	public static enum TextModel {
		SHORTTEXT, LONGTEXT
	}
}
