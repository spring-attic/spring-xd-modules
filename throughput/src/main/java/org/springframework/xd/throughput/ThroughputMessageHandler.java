/*
 * Copyright 2015 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.springframework.xd.throughput;

import static org.springframework.xd.throughput.SizeUnit.*;
import static org.springframework.xd.throughput.TimeUnit.*;

import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;

/**
 * A simple handler that will count messages and log witnessed throughput at some interval.
 *
 * @author Eric Bottard
 */
public class ThroughputMessageHandler implements MessageHandler {

	/*default*/ Logger logger;

	private final AtomicLong counter = new AtomicLong();

	private final AtomicLong start = new AtomicLong();

	private final AtomicLong bytes = new AtomicLong();

	private final AtomicLong intermediateStart = new AtomicLong();

	private final AtomicLong intermediateCounter = new AtomicLong();

	private final AtomicLong intermediateBytes = new AtomicLong();

	private long reportEveryMs;

	private long reportEveryNumber;

	private long reportEveryBytes;

	private long totalExpected;

	private TimeUnit timeUnit = TimeUnit.s;

	private SizeUnit sizeUnit = SizeUnit.MB;

	private Clock clock = new Clock();

	@Override
	public void handleMessage(Message<?> message) throws MessagingException {
		long now = clock.now();
		start.compareAndSet(0L, now);
		intermediateStart.compareAndSet(0L, now);
		Object payload = message.getPayload();
		long intermediateBytesP = 0L;
		long bytesP = 0L;
		if (payload instanceof byte[]) {
			int size = ((byte[]) payload).length;
			intermediateBytesP = intermediateBytes.addAndGet(size);
			bytesP = bytes.addAndGet(size);
		} else if (payload instanceof String) {
			int size = ((String) payload).getBytes().length;
			intermediateBytesP = intermediateBytes.addAndGet(size);
			bytesP = bytes.addAndGet(size);
		}

		long fullDelta = now - start.get();
		long intermediateStartP = intermediateStart.get();

		long counterP = counter.incrementAndGet();
		long intermediateCounterP = intermediateCounter.incrementAndGet();

		long intermediateDelta = now - intermediateStartP;
		if (intermediateCounterP >= reportEveryNumber || intermediateDelta >= reportEveryMs || intermediateBytesP >= reportEveryBytes || counterP == totalExpected) {
			double scaledIntermediateDelta = timeUnit.convert(intermediateDelta, ms);
			double scaledFullDelta = timeUnit.convert(fullDelta, ms);

			double deltaThroughput = (double) intermediateCounterP / scaledIntermediateDelta;
			double fullThroughput = (double) counterP / scaledFullDelta;
			String intermediateMsgs = String.format("Messages + %10d in %11.2f%s = %11.2f/%3$s", intermediateCounterP, scaledIntermediateDelta, timeUnit, deltaThroughput);
			String fullMsgs = String.format("Messages = %10d in %11.2f%s = %11.2f/%3$s", counterP, scaledFullDelta, timeUnit, fullThroughput);

			String intermediatePayload = "";
			String fullPayload = "";
			if (bytesP > 0L) {
				double bytesDeltaThroughput = sizeUnit.convert(intermediateBytesP, B) / scaledIntermediateDelta;
				double bytesThroughput = sizeUnit.convert(bytesP, B) / scaledFullDelta;
				intermediatePayload = String.format("    --    Bytes + %10.2f%s in %11.2f%s = %11.2f%2$s/%4$s", sizeUnit.convert(intermediateBytesP, B), sizeUnit, scaledIntermediateDelta, timeUnit, bytesDeltaThroughput);
				fullPayload = String.format("    --    Bytes = %10.2f%s in %11.2f%s = %11.2f%2$s/%4$s", sizeUnit.convert(bytesP, B), sizeUnit, scaledFullDelta, timeUnit, bytesThroughput);
			}
			logger.info(intermediateMsgs + intermediatePayload);
			logger.info(fullMsgs + fullPayload);
			intermediateStart.set(now);
			intermediateCounter.set(0L);
			intermediateBytes.set(0L);
			if (counterP == totalExpected) {
				reset();
			}
		}

	}

	private void reset() {
		start.set(0L);
		counter.set(0L);
		intermediateStart.set(0L);
		intermediateCounter.set(0L);
		intermediateBytes.set(0L);
	}

	/**
	 * As a strategy class to ease unit testing.
	 */
	public static class Clock {
		public long now() {
			return System.currentTimeMillis();
		}
	}

	public void setReportEveryMs(long reportEveryMs) {
		this.reportEveryMs = reportEveryMs;
	}

	public void setReportEveryNumber(long reportEveryNumber) {
		this.reportEveryNumber = reportEveryNumber;
	}

	public void setReportEveryBytes(long reportEveryBytes) {
		this.reportEveryBytes = reportEveryBytes;
	}

	public void setTotalExpected(long totalExpected) {
		this.totalExpected = totalExpected;
	}

	public void setTimeUnit(TimeUnit timeUnit) {
		this.timeUnit = timeUnit;
	}

	public void setSizeUnit(SizeUnit sizeUnit) {
		this.sizeUnit = sizeUnit;
	}

	public void setLogger(String name) {
		this.logger = LoggerFactory.getLogger("xd.sink.throughput." + name);

	}
}
