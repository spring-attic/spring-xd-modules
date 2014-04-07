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

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dmg.pmml.FieldName;
import org.dmg.pmml.Model;
import org.dmg.pmml.PMML;
import org.jpmml.evaluator.Evaluator;
import org.jpmml.evaluator.ModelEvaluatorFactory;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.xd.analytics.ml.InputMapper;
import org.springframework.xd.analytics.ml.MappedAnalytic;
import org.springframework.xd.analytics.ml.OutputMapper;

/**
 * A {@link org.springframework.xd.analytics.ml.MappedAnalytic} that can evaluate {@link org.dmg.pmml.PMML} models.
 *
 * @author Thomas Darimont
 */
public class PmmlAnalytic<I, O> extends
		MappedAnalytic<I, O, Map<FieldName, Object>, Map<FieldName, Object>, PmmlAnalytic<I, O>> {

	private final Log log = LogFactory.getLog(this.getClass());

	private final String modelName;

	private final String modelLocation;

	private final PMML pmml;

	private final Evaluator pmmlEvaluator;

	/**
	 * Creates a new {@link PmmlAnalytic}.
	 *
	 * @param modelName may be {@literal null}
	 * @param modelLocation must not be {@literal null}
	 * @param inputMapper must not be {@literal null}
	 * @param outputMapper must not be {@literal null}
	 */
	public PmmlAnalytic(String modelName,
						String modelLocation,
						InputMapper<I, PmmlAnalytic<I, O>, Map<FieldName, Object>> inputMapper,
						OutputMapper<I, O, PmmlAnalytic<I, O>, Map<FieldName, Object>> outputMapper) {
		this(modelName, modelLocation, new ResourcePmmlLoader(), inputMapper, outputMapper);
	}

	/**
	 * Creates a new {@link PmmlAnalytic}.
	 *
	 * @param modelName may be {@literal null}
	 * @param modelLocation must not be {@literal null}
	 * @param pmmlLoader may be {@literal null}
	 * @param inputMapper must not be {@literal null}
	 * @param outputMapper must not be {@literal null}
	 */
	public PmmlAnalytic(String modelName,
						String modelLocation,
						PmmlLoader pmmlLoader,
						InputMapper<I, PmmlAnalytic<I, O>, Map<FieldName, Object>> inputMapper,
						OutputMapper<I, O, PmmlAnalytic<I, O>, Map<FieldName, Object>> outputMapper) {

		super(inputMapper, outputMapper);

		Assert.notNull(modelLocation, "modelLocation");
		Assert.notNull(pmmlLoader,"pmmlLoader");

		this.modelName = StringUtils.trimAllWhitespace(modelName);
		this.modelLocation = StringUtils.trimAllWhitespace(modelLocation);

		PMML pmml = pmmlLoader.loadPmml(this.modelLocation);

		this.pmml = pmml;
		this.pmmlEvaluator = createModelEvaluator(pmml, modelName);

		if (log.isDebugEnabled()) {
			log.debug(String.format("PmmlAnalytic created for model with modelName: %s and modelLocation: %s",modelName, modelLocation));
		}
	}

	/**
	 * Creates the {@link org.jpmml.evaluator.Evaluator} that should be used to evaluate the selected model.
	 *
	 * @return
	 */
	protected Evaluator createModelEvaluator(PMML pmml, String modelName) {
		return (Evaluator) ModelEvaluatorFactory.getInstance().getModelManager(pmml, getModel(modelName));
	}

	/**
	 * Evaluates the given {@code modelInput} with the analytic provided by {@link PMML} definition.
	 *
	 * @param modelInput must not be {@literal null}
	 * @return
	 */
	@Override
	@SuppressWarnings("unchecked")
	protected Map<FieldName, Object> evaluateInternal(Map<FieldName, Object> modelInput) {

		Assert.notNull(modelInput, "modelInput");

		if (log.isDebugEnabled()) {
			log.debug("Before pmml evaluation - input: " + modelInput);
		}

		Map<FieldName, Object> result = (Map<FieldName, Object>) this.pmmlEvaluator.evaluate(modelInput);

		if (log.isDebugEnabled()) {
			log.debug("After pmml evaluation - result: " + result);
		}

		return result;
	}

	/**
	 * Returns the {@link org.dmg.pmml.Model} for the given {@code modelName}.
	 *
	 * @param modelName may be {@literal null}
	 * @return
	 */
	Model getModel(String modelName){

		//if no model name given try returning default name
		if (!StringUtils.hasText(modelName)) {
			return getDefaultModel();
		}

		if (!this.pmml.getModels().isEmpty()) {

			//look for a model with the given name
			for (Model model : this.pmml.getModels()) {

				if(model.getModelName() == null){
					continue;
				}

				if (model.getModelName().equals(modelName)) {
					return model;
				}
			}
		}

		throw new IllegalStateException("Analytical model: " + modelName + " not found!");
	}

	/**
	 * Returns the corresponding {@link org.dmg.pmml.Model} for the configured {@code modelName}.
	 * If no modelName is configured the default model from {@link #getDefaultModel()} is returned.
	 *
	 * @return
	 */
	Model getSelectedModel() {
		return getModel(this.modelName);
	}

	/**
	 * Returns the default {@link org.dmg.pmml.Model} of the wrapped {@link PMML} object. According to the PMML
	 * specification, this is the first {@code Model} in the {@code PMML} structure. Every {@code PMML} model contain at
	 * least one {@code Model}.
	 *
	 * @return
	 */
	Model getDefaultModel() {

		List<Model> models = this.pmml.getModels();

		if(models.isEmpty()){
			throw new IllegalStateException(String.format("PMML document doesn't contain any model in modelLocation: %s ", this.modelLocation));
		}

		return models.get(0);
	}

	/**
	 * Returns the {@link org.jpmml.evaluator.Evaluator} that should be use to evaluate the current model.
	 *
	 * @return
	 */
	public Evaluator getPmmlEvaluator() {
		return pmmlEvaluator;
	}

	/**
	 * Returns the configured {@code modelLocation}.
	 *
	 * @return
	 */
	public String getModelLocation() {
		return modelLocation;
	}

	@Override
	public String toString() {
		return "PmmlAnalytic{" + "modelName='" + modelName + '\'' + '}' + "@"
				+ Integer.toHexString(System.identityHashCode(this));
	}
}
