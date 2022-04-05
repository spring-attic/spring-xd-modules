/*
 * Copyright 2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.xd.analytics.ml.pmml;

import org.hibernate.validator.constraints.NotBlank;
import org.springframework.xd.module.options.spi.ModuleOption;

/**
 * Captures module options to the {@code analytic-pmml} module.
 * 
 * @author Eric Bottard
 */
public class AnalyticPmmlProcessorOptionsMetadata {

	private String modelName;

	private String location;

	private String inputFieldMapping;

	private String outputFieldMapping;

	public String getModelName() {
		return modelName;
	}

	@NotBlank
	public String getLocation() {
		return location;
	}

	public String getInputFieldMapping() {
		return inputFieldMapping;
	}

	public String getOutputFieldMapping() {
		return outputFieldMapping;
	}

	@ModuleOption("the name of the model to use, from the PMML document")
	public void setModelName(String modelName) {
		this.modelName = modelName;
	}

	@ModuleOption("the location of the PMML xml file")
	public void setLocation(String location) {
		this.location = location;
	}

	@ModuleOption("mapping of input tuple fields to model input fields")
	public void setInputFieldMapping(String inputFieldMapping) {
		this.inputFieldMapping = inputFieldMapping;
	}

	@ModuleOption("mapping of model output fields to output fields")
	public void setOutputFieldMapping(String outputFieldMapping) {
		this.outputFieldMapping = outputFieldMapping;
	}

}
