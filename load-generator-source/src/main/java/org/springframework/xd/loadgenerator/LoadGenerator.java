/*
 * Copyright 2015 the original author or authors.
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

package org.springframework.xd.loadgenerator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.endpoint.MessageProducerSupport;
import org.springframework.integration.support.MessageBuilder;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Is a source module that generates a series of fixed size messages to be 
 * dispatched to a stream.  The load-generator is used to test the performance of
 * XD in different environments.
 *
 * @author Glenn Renfro
 */
public class LoadGenerator extends MessageProducerSupport implements Runnable {

	private int producers;
	private int messageSize;
	private int messageCount;

	Logger logger = LoggerFactory.getLogger(LoadGenerator.class);

	public LoadGenerator(int producers, int messageSize, int messageCount) {
		this.producers = producers;
		this.messageSize = messageSize;
		this.messageCount = messageCount;
	}

	@Override
	protected void doStart() {
		for (int x = 0; x < producers; x++) {
			Thread t = new Thread(this);
			t.start();
		}
	}

	private void send() {
		logger.info("Sending " + messageCount +" messages");
		for (int x = 0; x < messageCount; x++) {
			sendMessage(MessageBuilder.withPayload(createMessage(messageCount)).build());
		}
		logger.info("All Messages Dispatched");
	}

	/**
	 * Creates a message that can be consumed by the Rabbit perfTest Client
	 *
	 * @param sequenceNumber a number to be prepended to the message
	 * @return a byte array containing a series of numbers that match the message size as
	 * specified by the messageSize constructor arg.
	 */
	private byte[] createMessage(int sequenceNumber) {
		byte message[] = new byte[messageSize];
		try {
			ByteArrayOutputStream acc = new ByteArrayOutputStream();
			DataOutputStream d = new DataOutputStream(acc);
			long nano = System.nanoTime();
			d.writeInt(sequenceNumber);
			d.writeLong(nano);
			d.flush();
			acc.flush();
			byte[] m = acc.toByteArray();
			if (m.length <= messageSize) {
				System.arraycopy(m, 0, message, 0, m.length);
				return message;
			} else {
				return m;
			}
		} catch (IOException ioe) {
			throw new IllegalStateException(ioe);
		}
	}

	public void run() {
		send();
	}
}
