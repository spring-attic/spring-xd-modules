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

package org.springframework.xd.loadgenerator;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.integration.endpoint.MessageProducerSupport;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;

/**
 * Is a source module that generates a series of fixed size messages to be
 * dispatched to a stream.  The load-generator is used to test the performance of
 * XD in different environments.
 *
 * @author Glenn Renfro
 * @author Marius Bogoevici
 * @author Mark Pollack
 */
public class LoadGenerator extends MessageProducerSupport {

    private static final TestMessageHeaders HEADERS = new TestMessageHeaders(null);

    private final int producers;

    private final int messageSize;

    private final int messageCount;

    private final boolean generateTimestamp;

    private final AtomicBoolean running = new AtomicBoolean(false);

    private ExecutorService executorService;

    Logger logger = LoggerFactory.getLogger(LoadGenerator.class);

    public LoadGenerator(int producers, int messageSize, int messageCount, boolean generateTimestamp) {
        this.producers = producers;
        this.messageSize = messageSize;
        this.messageCount = messageCount;
        this.generateTimestamp = generateTimestamp;
    }

    @Override
    protected void doStart() {
        executorService = Executors.newFixedThreadPool(producers);
        if (running.compareAndSet(false, true)) {
            for (int x = 0; x < producers; x++) {
                executorService.execute(new Producer(x));
            }
        }
    }

    @Override
    protected void doStop() {
        if (running.compareAndSet(true, false)) {
            executorService.shutdown();
        }
    }

    protected class Producer implements Runnable {

        int producerId;

        public Producer(int producerId) {
            this.producerId = producerId;
        }

        private void send() {
            logger.info("Producer " + producerId + " sending " + messageCount + " messages");
            for (int x = 0; x < messageCount; x++) {
                final byte[] message = createPayload(x);
                sendMessage(new TestMessage(message));
            }
            logger.info("All Messages Dispatched");
        }

        /**
         * Creates a message for consumption by the load-generator sink.  The payload will
         * optionally contain a timestamp and sequence number if the generateTimestamp
         * property is set to true.
         *
         * @param sequenceNumber a number to be prepended to the message
         * @return a byte array containing a series of numbers that match the message size as
         * specified by the messageSize constructor arg.
         */
        private byte[] createPayload(int sequenceNumber) {
            byte message[] = new byte[messageSize];
            if (generateTimestamp) {
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
            } else {
                return message;
            }
        }

        public void run() {
            send();
        }

        private class TestMessage implements Message<byte[]> {
            private final byte[] message;

            private final TestMessageHeaders headers;

            public TestMessage(byte[] message) {
                this.message = message;
                this.headers = HEADERS;
            }

            @Override
            public byte[] getPayload() {
                return message;
            }

            @Override
            public MessageHeaders getHeaders() {
                return headers;
            }

        }

    }

    @SuppressWarnings("serial")
    private static class TestMessageHeaders extends MessageHeaders {
        public TestMessageHeaders(Map<String, Object> headers) {
            super(headers, ID_VALUE_NONE, -1L);
        }
    }

}
