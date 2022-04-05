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

import javax.validation.constraints.NotNull;

import org.springframework.xd.module.options.spi.ModuleOption;

/**
 * Documents the options of the langdetect module.
 *
 * @author Thomas Darimont
 */
public class LanguageDetectorOptionsMetadata {

	private String languageProfileLocation = "";

	private TextModel textModel = TextModel.SHORTTEXT;

	private String inputTextContentPropertyName = "text";

	private String mostLikelyLanguageOutputPropertyName = "pred_lang";

	private String languageProbabilitiesOutputPropertyName = "pred_lang_probs";

	private boolean returnMostLikelyLanguage = true;

	private boolean returnLanguageProbabilities;

	private boolean deterministicLanguageDetection;

	private String languagePriorities;

	public String getLanguageProfileLocation() {
		return languageProfileLocation;
	}

	@ModuleOption("the location of the language model. If empty we fall back to the profiles shipped with langdetect")
	public void setLanguageProfileLocation(String languageProfileLocation) {
		this.languageProfileLocation = languageProfileLocation;
	}

	@NotNull
	public String getInputTextContentPropertyName() {
		return inputTextContentPropertyName;
	}

	@ModuleOption("the name of the property that contains the input text")
	public void setInputTextContentPropertyName(String inputTextContentPropertyName) {
		this.inputTextContentPropertyName = inputTextContentPropertyName;
	}

	@NotNull
	public String getMostLikelyLanguageOutputPropertyName() {
		return mostLikelyLanguageOutputPropertyName;
	}

	@ModuleOption("the name of the output property the detected language is written to")
	public void setMostLikelyLanguageOutputPropertyName(String mostLikelyLanguageOutputPropertyName) {
		this.mostLikelyLanguageOutputPropertyName = mostLikelyLanguageOutputPropertyName;
	}

	@NotNull
	public String getLanguageProbabilitiesOutputPropertyName() {
		return languageProbabilitiesOutputPropertyName;
	}

	@ModuleOption("the name of the output property the detected language probabilities are written to")
	public void setLanguageProbabilitiesOutputPropertyName(String languageProbabilitiesOutputPropertyName) {
		this.languageProbabilitiesOutputPropertyName = languageProbabilitiesOutputPropertyName;
	}

	public boolean isReturnMostLikelyLanguage() {
		return returnMostLikelyLanguage;
	}

	@ModuleOption("returns the most likely detected language if enabled")
	public void setReturnMostLikelyLanguage(boolean returnMostLikelyLanguage) {
		this.returnMostLikelyLanguage = returnMostLikelyLanguage;
	}

	public boolean isReturnLanguageProbabilities() {
		return returnLanguageProbabilities;
	}

	@ModuleOption("outputs the detected language probabilities as a list if enabled")
	public void setReturnLanguageProbabilities(boolean returnLanguageProbabilities) {
		this.returnLanguageProbabilities = returnLanguageProbabilities;
	}

	@NotNull
	public TextModel getTextModel() {
		return textModel;
	}

	@ModuleOption("the name of the text model that should be used either SHORTTEXT or LONGTEXT")
	public void setTextModel(TextModel textModel) {
		this.textModel = textModel;
	}

	public boolean isDeterministicLanguageDetection() {
		return deterministicLanguageDetection;
	}

	@ModuleOption("the same language and probability is returned for the same text if enabled")
	public void setDeterministicLanguageDetection(boolean deterministicLanguageDetection) {
		this.deterministicLanguageDetection = deterministicLanguageDetection;
	}

	public String getLanguagePriorities() {
		return languagePriorities;
	}

	@ModuleOption("allows to prioritize languages via pattern, e.g. en:0.1,de:0.1,fr:0.1")
	public void setLanguagePriorities(String languagePriorities) {
		this.languagePriorities = languagePriorities;
	}
}
