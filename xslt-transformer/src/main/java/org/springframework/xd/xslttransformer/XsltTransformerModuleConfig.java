/*
 * Copyright 2015 the original author or authors.
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

package org.springframework.xd.xslttransformer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.xml.transformer.XsltPayloadTransformer;
import org.springframework.messaging.MessageChannel;

/**
 * @author Muhammad Ali
 */
@Configuration
@EnableIntegration
public class XsltTransformerModuleConfig {

	@Autowired
	ApplicationContext applicationContext;


	@Bean
	public MessageChannel input() {
		return new DirectChannel();
	}

	@Bean
	MessageChannel output() {
		return new DirectChannel();
	}

	@Bean
	public IntegrationFlow myFlow(@Value("${xslt}") String xslt) {
		return IntegrationFlows.from(this.input())
				.transform(getXsltTransformer(xslt))
				.channel(this.output())
				.get();
	}

	@Bean
	public XsltPayloadTransformer getXsltTransformer(String xslt)
	{
		return new XsltPayloadTransformer(applicationContext.getResource(xslt));
	}
}
