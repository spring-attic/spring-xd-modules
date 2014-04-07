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

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.xd.analytics.ml.Analytic;
import org.springframework.xd.tuple.Tuple;

/**
 * @author Thomas Darimont
 */
public class PmmlAnalyticModelTests extends AbstractPmmlAnalyticTest {

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Test
	public void testShouldLoadModelByNameFromMultipleModels() {

		String modelName = "KMeans_Model2";
		Analytic<Tuple, Tuple> analytic = useAnalytic(modelName, "classpath:analytics/pmml/multiple-models.pmml.xml", null, Arrays.asList("predictedValue"));

		assertThat(analytic, is(instanceOf(PmmlAnalytic.class)));
		assertThat(((PmmlAnalytic) analytic).getSelectedModel().getModelName(), is(modelName));
	}

	@Test
	public void testShouldLoadDefaultModelFromMultipleModelsIfNoExplictModelNameIsGiven() {

		Analytic<Tuple, Tuple> analytic = useAnalytic(null, "classpath:analytics/pmml/multiple-models.pmml.xml", null, Arrays.asList("predictedValue"));

		assertThat(analytic, is(instanceOf(PmmlAnalytic.class)));
		assertThat(((PmmlAnalytic) analytic).getSelectedModel().getModelName(), is("KMeans_Model1"));
	}

	@Test
	public void testShouldThrowExceptionWhenRequestingAModelWithUnknownName() {

		expectedException.expect(RuntimeException.class);
		expectedException.expectMessage("Analytical model:");
		expectedException.expectMessage("UNKNOWN_MODEL_NAME");
		expectedException.expectMessage("not found!");

		useAnalytic("UNKNOWN_MODEL_NAME", "classpath:analytics/pmml/multiple-models.pmml.xml", null, Arrays.asList("predictedValue"));
	}
}
