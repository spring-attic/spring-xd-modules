/*
 *
 * Copyright (c) 2015 Pivotal Software, Inc. All Rights Reserved
 *
 * This software contains the intellectual property of Pivotal Software, Inc. or is
 * licensed to Pivotal Software, Inc. from third parties. Use of this software and
 * the intellectual property contained therein is expressly limited to the
 * terms and conditions of the License Agreement under which it is provided
 * by or on behalf of Pivotal Software, Inc.
 */

package com.acme;

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
