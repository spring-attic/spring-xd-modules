package org.springframework.xd.xslttransformer;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Properties;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.SubscribableChannel;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;





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

/**
 * @author Muhammad Ali
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class XslTransformerTest {


	@Test
	public void test() {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		context.register(TestConfiguration.class);
		context.refresh();

		MessageChannel input = context.getBean("input", MessageChannel.class);
		SubscribableChannel output = context.getBean("output", SubscribableChannel.class);

		final AtomicBoolean handled = new AtomicBoolean();
		output.subscribe(new MessageHandler() {
			@Override
			public void handleMessage(Message<?> message) throws MessagingException {
				handled.set(true);
				assertEquals("1002,OLV80UJS7YO", message.getPayload());
			}
		});

		@SuppressWarnings("resource")
		Scanner scanner = new Scanner(File.class.getResourceAsStream("/test.xml")).useDelimiter("\\Z");

	    String content = scanner.next();
		input.send(new GenericMessage<String>(content));
		assertTrue(handled.get());
	}

	@Configuration
	@Import(XsltTransformerModuleConfig.class)
	static class TestConfiguration {
		
		

		final static String XSLT = "classpath:test.xsl";


		@Bean
		public static String xslt()
		{
			return XSLT;
		}
		
		@Bean
		public static PropertySourcesPlaceholderConfigurer placeHolderConfigurer() {
			PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer = new
					PropertySourcesPlaceholderConfigurer();
			Properties properties = new Properties();
			properties.put("xslt","classpath:test.xsl");
			

			propertySourcesPlaceholderConfigurer.setProperties(properties);
			return propertySourcesPlaceholderConfigurer;
		}
	}
}

