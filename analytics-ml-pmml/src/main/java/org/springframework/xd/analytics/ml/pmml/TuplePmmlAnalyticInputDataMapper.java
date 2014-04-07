/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.xd.analytics.ml.pmml;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dmg.pmml.FieldName;
import org.jpmml.evaluator.EvaluatorUtil;
import org.springframework.util.Assert;
import org.springframework.xd.analytics.ml.AbstractFieldMappingAwareDataMapper;
import org.springframework.xd.analytics.ml.InputMapper;
import org.springframework.xd.tuple.Tuple;

/**
 * An {@link org.springframework.xd.analytics.ml.InputMapper} that can map the
 * {@link org.springframework.xd.tuple.Tuple} to a to an appropriate input for a {@link org.dmg.pmml.PMML} model
 * evaluation.
 *
 * @author Thomas Darimont
 */
public class TuplePmmlAnalyticInputDataMapper extends AbstractFieldMappingAwareDataMapper implements
		InputMapper<Tuple, PmmlAnalytic<Tuple, Tuple>, Map<FieldName, Object>> {

	private final Map<String, String> inputFieldToModelInputNameMapping;

	/**
	 * Creates a new {@link TuplePmmlAnalyticInputDataMapper}.
	 *
	 * @param inputFieldNameMapping
	 */
	public TuplePmmlAnalyticInputDataMapper(List<String> inputFieldNameMapping) {

		if (inputFieldNameMapping == null || inputFieldNameMapping.isEmpty()) {
			this.inputFieldToModelInputNameMapping = null;
			return;
		}

		this.inputFieldToModelInputNameMapping = new HashMap<String, String>(inputFieldNameMapping.size());

		registerInputFieldMapping(inputFieldNameMapping);
	}

	/**
	 * @param inputFieldNameMapping must not be {@literal null}.
	 */
	private void registerInputFieldMapping(List<String> inputFieldNameMapping) {

		Assert.notNull(inputFieldNameMapping, "inputFieldNameMapping");

		Map<String, String> mapping = extractFieldNameMappingFrom(inputFieldNameMapping);
		for (Map.Entry<String, String> toFromMapping : mapping.entrySet()) {
			this.inputFieldToModelInputNameMapping.put(toFromMapping.getKey(), toFromMapping.getValue());
		}
	}

	/**
	 * Maps the given input {@code Tuple} into an appropriate model-input {@code Map} for the given
	 * {@link org.springframework.xd.analytics.ml.pmml.PmmlAnalytic}.
	 *
	 * @param analytic must not be {@literal null}.
	 * @param input must not be {@literal null}.
	 * @return
	 */
	@Override
	public Map<FieldName, Object> mapInput(PmmlAnalytic<Tuple, Tuple> analytic, Tuple input) {

		Assert.notNull(analytic, "analytic");
		Assert.notNull(input, "input");

		Map<FieldName, Object> inputData = new HashMap<FieldName, Object>();
		for (String fieldName : input.getFieldNames()) {

			//inputFieldToModelInputNameMapping is null we map all input fields to
			String modelInputFieldNameToUse = inputFieldToModelInputNameMapping == null ? fieldName
					: inputFieldToModelInputNameMapping.get(fieldName);

			if (modelInputFieldNameToUse == null) {
				//there is no implicit or explicit mapping available for the current fieldName, so we skip it.
				continue;
			}

			Object rawModelInputValue = input.getValue(fieldName);
			FieldName modelInputFieldName = new FieldName(modelInputFieldNameToUse);

			Object modelInputValue = prepareModelInputValue(analytic, modelInputFieldName, rawModelInputValue);

			inputData.put(modelInputFieldName, modelInputValue);
		}

		return inputData;
	}

	/**
	 * Potentially transforms the given {@code rawModelInputValue} to a more suitable form for the model, e.g.:
	 * <ol>
	 * <li>outlier treatment</li>
	 * <li>missing value treatment</li>
	 * <li>invalid value treatment</li>
	 * <li>type conversion</li>
	 * <ol>
	 *
	 * @param analytic must not be {@literal null}
	 * @param modelInputFieldName must not be {@literal null}
	 * @param rawModelInputValue my be {@literal null}
	 * @return
	 */
	protected Object prepareModelInputValue(PmmlAnalytic<Tuple, Tuple> analytic, FieldName modelInputFieldName, Object rawModelInputValue) {

		Assert.notNull(analytic, "analytic");
		Assert.notNull(modelInputFieldName, "modelInputFieldName");

		return EvaluatorUtil.prepare(analytic.getPmmlEvaluator(), modelInputFieldName, rawModelInputValue);
	}
}
