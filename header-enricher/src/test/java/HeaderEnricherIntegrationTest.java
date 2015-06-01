/*
 * Copyright 2014 the original author or authors.
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

import org.junit.BeforeClass;
import org.junit.Test;

import org.springframework.messaging.Message;
import org.springframework.xd.dirt.server.singlenode.SingleNodeApplication;
import org.springframework.xd.dirt.test.SingleNodeIntegrationTestSupport;
import org.springframework.xd.dirt.test.SingletonModuleRegistry;
import org.springframework.xd.dirt.test.process.SingleNodeProcessingChain;
import org.springframework.xd.module.ModuleType;
import org.springframework.xd.test.RandomConfigurationSupport;

import static org.junit.Assert.assertEquals;
import static org.springframework.xd.dirt.test.process.SingleNodeProcessingChainSupport.chain;

/**
 * @author David Turanski
 */
public class HeaderEnricherIntegrationTest {
	private static SingleNodeApplication application;

	private static int RECEIVE_TIMEOUT = 5000;

	private static String moduleName = "header-enricher";

	SingleNodeProcessingChain chain;

	/**
	 * Start the single node container, binding random unused ports, etc. to not conflict with any other instances
	 * running on this host. Configure the ModuleRegistry to include the project module.
	 */
	@BeforeClass
	public static void setUp() {
		RandomConfigurationSupport randomConfigSupport = new RandomConfigurationSupport();
		application = new SingleNodeApplication().run();
		SingleNodeIntegrationTestSupport singleNodeIntegrationTestSupport = new SingleNodeIntegrationTestSupport
				(application);
		singleNodeIntegrationTestSupport.addModuleRegistry(new SingletonModuleRegistry(ModuleType.processor,
				moduleName));
	}

	@Test
	public void testSimpleExpression() {
		String streamName = "testSimpleExpression";

		String headers = "{\"bar\":\"payload.substring(1)\"}";

		String processingChainUnderTest = String.format("%s --headers=%s", moduleName, headers);

		chain = chain(application, streamName, processingChainUnderTest);

		chain.sendPayload("hello");
		Message<?> message = chain.receive(RECEIVE_TIMEOUT);
		assertEquals("ello", message.getHeaders().get("bar"));

		chain.destroy();

	}

	@Test
	public void testMultipleHeaders() {
		String streamName = "testMultipleHeaders";

		String headers = "{\"foo\":\"(payload+',world!').toUpperCase()\",\"bar\":\"payload.substring(1)\"}";

		String processingChainUnderTest = String.format("%s --headers=%s", moduleName, headers);

		chain = chain(application, streamName, processingChainUnderTest);

		chain.sendPayload("hello");
		Message<?> message = chain.receive(RECEIVE_TIMEOUT);
		assertEquals("HELLO,WORLD!", message.getHeaders().get("foo"));
		assertEquals("ello", message.getHeaders().get("bar"));

		chain.destroy();

	}
			
	@Test
	public void testLiteralNoSpace() {
		String streamName = "testLiteralNoSpace";

		String headers = "{\"foo\":\"\'literal\'\"}";

		String processingChainUnderTest = String.format("%s --headers=%s", moduleName, headers);

		chain = chain(application, streamName, processingChainUnderTest);

		chain.sendPayload("hello");
		Message<?> message = chain.receive(RECEIVE_TIMEOUT);
		assertEquals("literal", message.getHeaders().get("foo"));

		chain.destroy();

	}

	@Test
	public void testLiteralWithSpaces() {
		String streamName = "testLiteralWtihSpaces";

		String headers = "{\"foo\":\"\'this\\u0020is\\u0020a\\u0020literal\'\"}";

		String processingChainUnderTest = String.format("%s --headers=%s", moduleName, headers);

		chain = chain(application, streamName, processingChainUnderTest);

		chain.sendPayload("hello");
		Message<?> message = chain.receive(RECEIVE_TIMEOUT);
		assertEquals("this is a literal", message.getHeaders().get("foo"));

		chain.destroy();
	}

	@Test
	public void testJsonPath() {
		String streamName = "testJsonPath";

		String headers = "{\"foo\":\"#jsonPath(payload,'$.duration')\"}";

		String processingChainUnderTest = String.format("%s --headers=%s", moduleName, headers);

		chain = chain(application, streamName, processingChainUnderTest);

		chain.sendPayload("{\"duration\":123}");
		Message<?> message = chain.receive(RECEIVE_TIMEOUT);
		assertEquals(123, message.getHeaders().get("foo"));

		chain.destroy();
	}
}
