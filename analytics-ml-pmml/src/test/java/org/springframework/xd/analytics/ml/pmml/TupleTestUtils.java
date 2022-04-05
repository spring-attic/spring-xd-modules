/*
 * Copyright 2014 the original author or authors.
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

import static org.springframework.xd.tuple.TupleBuilder.*;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;
import org.springframework.xd.tuple.Tuple;

/**
 * Helper class to be able to work with {@link org.springframework.xd.tuple.Tuple} in Tests.
 * 
 * @author Thomas Darimont
 */
public class TupleTestUtils {

	/**
	 * 
	 * <pre>
	 *  Tuple output = analytic.evaluate(objectToTuple(new Object() {
	 *    @Value("Sepal.Length") double sepalLength = 6.4;
	 *    @Value("Sepal.Width") double sepalWidth = 3.2;
	 *    @Value("Petal.Length") double petalLength = 4.5;
	 *    @Value("Petal.Width") double petalWidth = 1.5;
	 * }));
	 * 
	 * <pre>
	 * 
	 * @param o must not be {@literal null}
	 * @return
	 */
	public static Tuple objectToTuple(final Object o) {

		Assert.notNull(o, "o");

		final List<String> fieldNames = new ArrayList<String>();
		final List<Object> fieldValues = new ArrayList<Object>();

		ReflectionUtils.doWithFields(o.getClass(), new ReflectionUtils.FieldCallback() {

			@Override
			public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {

				if (field.isSynthetic()) {
					return;
				}

				// We abuse the @Value annotation here to declare the attribute name that is used inside the PMML ml.
				// We should use another annotation here.
				Value value = field.getAnnotation(Value.class);
				fieldNames.add(value == null ? field.getName() : value.value());
				fieldValues.add(field.get(o));
			}
		});

		return tuple().ofNamesAndValues(fieldNames, fieldValues);
	}
}
