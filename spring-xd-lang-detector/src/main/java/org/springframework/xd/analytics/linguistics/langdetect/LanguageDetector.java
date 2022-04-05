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

import static org.springframework.util.StringUtils.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.*;

import com.cybozu.labs.langdetect.Detector;
import com.cybozu.labs.langdetect.DetectorFactory;
import com.cybozu.labs.langdetect.LangDetectException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.util.CollectionUtils;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.xd.tuple.Tuple;
import org.springframework.xd.tuple.TupleBuilder;

/**
 * A processor that can predict the language of a piece of text extracted from a {@link org.springframework.xd.tuple.Tuple}.
 * <p>
 * Language prediction is supported for short and long texts via different language models.
 * Note since the {@code langdetect} library holds the language model as static state one
 * can only deal with one model instance per {@link java.lang.ClassLoader} at a time.
 * </p>
 * The langdetect library has the following characteristics.
 * <ul>
 * <li>Generate language profiles from Wikipedia abstract xml</li>
 * <li>Detect language of a text using naive Bayesian filter</li>
 * <li>99% over precision for 53 languages</li>
 * </ul>
 * More details can be found the the <a href="https://code.google.com/p/language-detection/wiki/FrequentlyAskedQuestion">langdetect FAQ</a>.
 *
 * @author Thomas Darimont
 */
public class LanguageDetector implements InitializingBean {

	private static final Logger LOG = LoggerFactory.getLogger(LanguageDetector.class);

	private String languageProfileLocation;

	private TextModel textModel;

	private String inputTextContentPropertyName;

	private String mostLikelyLanguageOutputPropertyName;

	private String languageProbabilitiesOutputPropertyName;

	private boolean returnMostLikelyLanguage;

	private boolean returnLanguageProbabilities;

	private boolean deterministicLanguageDetection;

	private String languagePriorities;

	//we need to use concrete types here since Detector requires them :-(
	private HashMap<String,Double> languagePriorityMap;

	private DetectorFactoryState detectorFactoryState;

	private LanguagePriorityParser languagePriorityParser = new LanguagePriorityParser();

	/**
	 * Performs the language prediction based on text extracted from the given {@link org.springframework.xd.tuple.Tuple}.
	 * <p>
	 * The text used for the language prediction is extracted via the {@link LanguageDetector#inputTextContentPropertyName}
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

		Detector detector = newDetector(this.detectorFactoryState);

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

	/**
	 * This creates a new {@link com.cybozu.labs.langdetect.Detector} instance.
	 * We have to create the instance ourselves to avoid problems with the shared state inside {@link com.cybozu.labs.langdetect.DetectorFactory}.
	 *
	 * @param detectorFactoryState
	 * @return
	 */
	private Detector newDetector(DetectorFactoryState detectorFactoryState) throws LangDetectException {

		Detector detector = new Detector(detectorFactoryState.getWordLangProbMap(), detectorFactoryState.getLanguageList(), detectorFactoryState.getSeed());

		if (!CollectionUtils.isEmpty(languagePriorityMap)) {
			detector.setPriorMap(languagePriorityMap);
		}

		return detector;
	}

	private boolean isLanguageDetectionEnabled() {
		return isReturnMostLikelyLanguage() || isReturnLanguageProbabilities();
	}

	@Override
	public void afterPropertiesSet() throws Exception {

		this.languagePriorityMap = new HashMap<String,Double>(languagePriorityParser.parseToLanguagePriorityMap(languagePriorities));

		loadLanguageProfiles();

		this.detectorFactoryState = captureDetectorFactoryState();

		LOG.info("Loaded language profiles from {}.", languageProfileLocation);
	}

	private void loadLanguageProfiles() throws LangDetectException, IOException {

		if (isEmpty(this.languageProfileLocation)) {
			LOG.info("Using embedded language profiles.");
			loadEmbeddedLangaugeProfiles();
			return;
		}

		LOG.info("Using language profiles from {}.", languageProfileLocation);
		loadExternalLanguageProfiles();
	}

	private void loadExternalLanguageProfiles() throws LangDetectException, IOException {
		Resource languageProfileResource = new DefaultResourceLoader(getClass().getClassLoader()).getResource(languageProfileLocation);
		DetectorFactory.loadProfile(languageProfileResource.getFile());
	}

	private void loadEmbeddedLangaugeProfiles() throws LangDetectException {
		DetectorFactory.loadProfile(extractEmbeddedLanguageModels());
	}

	/**
	 * Captures the current state of the {@link com.cybozu.labs.langdetect.DetectorFactory} to make sure
	 * that state cannot be overridden by concurrent initializations later on.
	 * <p>This is necessary since, the state in the {@code DetectorFactory} is stored globally.</p>
	 *
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private DetectorFactoryState captureDetectorFactoryState() {

		Field detectorFactoryInstanceField = ReflectionUtils.findField(DetectorFactory.class, "instance_");
		ReflectionUtils.makeAccessible(detectorFactoryInstanceField);

		Field detectorFactoryWordLangProbMapField = ReflectionUtils.findField(DetectorFactory.class, "wordLangProbMap");
		ReflectionUtils.makeAccessible(detectorFactoryWordLangProbMapField);

		DetectorFactory instance = (DetectorFactory) ReflectionUtils.getField(detectorFactoryInstanceField, null);
		HashMap<String, double[]> wordLangProbMap = (HashMap<String, double[]>) ReflectionUtils.getField(detectorFactoryWordLangProbMapField, instance);
		ArrayList<String> languageList = new ArrayList<>(DetectorFactory.getLangList());

		return new DetectorFactoryState(languageList, wordLangProbMap, isDeterministicLanguageDetection() ? 0L : null);
	}

	private List<String> extractEmbeddedLanguageModels() {

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

	public TextModel getTextModel() {
		return textModel;
	}

	public void setTextModel(TextModel textModel) {
		this.textModel = textModel;
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

	public boolean isDeterministicLanguageDetection() {
		return deterministicLanguageDetection;
	}

	public void setDeterministicLanguageDetection(boolean deterministicLanguageDetection) {
		this.deterministicLanguageDetection = deterministicLanguageDetection;
	}

	public String getLanguagePriorities() {
		return languagePriorities;
	}

	public void setLanguagePriorities(String languagePriorities) {
		this.languagePriorities = languagePriorities;
	}

	/**
	 * Holds the configured state of the {@link com.cybozu.labs.langdetect.DetectorFactory} at construction time to avoid
	 * sudden value changes in between. This is necessary since the DetectorFactory state is stored on a global singleton.
	 */
	static class DetectorFactoryState {

		private final ArrayList<String> languageList;
		private final HashMap<String, double[]> wordLangProbMap;
		private final Long seed;

		public DetectorFactoryState(ArrayList<String> languageList, HashMap<String, double[]> wordLangProbMap, Long seed) {
			this.languageList = languageList;
			this.wordLangProbMap = wordLangProbMap;
			this.seed = seed;
		}

		public ArrayList<String> getLanguageList() {
			return languageList;
		}

		public HashMap<String, double[]> getWordLangProbMap() {
			return wordLangProbMap;
		}

		public Long getSeed() {
			return seed;
		}
	}
}
