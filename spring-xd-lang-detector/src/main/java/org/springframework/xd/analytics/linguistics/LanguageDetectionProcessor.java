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

	public static enum TextModel {
		SHORTTEXT, LONGTEXT
	}
}
