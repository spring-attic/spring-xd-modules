/*
 * Copyright 2015 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.springframework.xd.throughput;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.context.Lifecycle;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;

/**
 * A simple handler that will count messages and log witnessed throughput at some interval.
 *
 * @author Eric Bottard
 * @author Marius Bogoevici
 */
public class ThroughputMessageHandler implements MessageHandler, Lifecycle {

	private Logger logger;

	private final AtomicLong counter = new AtomicLong();

	private final AtomicLong start = new AtomicLong(-1);

	private final AtomicLong bytes = new AtomicLong(-1);

	private final AtomicLong intermediateCounter = new AtomicLong();

	private final AtomicLong intermediateBytes = new AtomicLong();

	private long reportEveryMs;

	private TimeUnit timeUnit = TimeUnit.s;

	private final Clock clock = new Clock();

	private volatile boolean running;

	private ExecutorService executorService;

	private boolean reportBytes = false;

	public ThroughputMessageHandler() {

	}

	@Override
	public void start() {
		if (executorService == null) {
			executorService = Executors.newFixedThreadPool(1);
		}
		this.running = true;
	}

	@Override
	public void stop() {
		this.running = false;
	}

	@Override
	public boolean isRunning() {
		return running;
	}

	@Override
	public void handleMessage(Message<?> message) throws MessagingException {
		if (start.get() == -1L) {
			synchronized (start) {
				if (start.get() == -1L) {
					// assume a homogeneous message structure - this is intended for perf tests so we can safely assume
					// that the messages are similar,  Therefore we'll do our reporting based on the first message
					Object payload = message.getPayload();
					if (payload instanceof byte[] || payload instanceof String) {
						reportBytes = true;
					}
					start.set(clock.now());
					executorService.execute(new ReportStats());
				}
			}
		}
		intermediateCounter.incrementAndGet();
		if (reportBytes) {
			Object payload = message.getPayload();
			if (payload instanceof byte[]) {
				intermediateBytes.addAndGet(((byte[]) payload).length);
			}
			else if (payload instanceof String) {
				intermediateBytes.addAndGet((((String) payload).getBytes()).length);
			}
		}
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

	public void setTimeUnit(TimeUnit timeUnit) {
		this.timeUnit = timeUnit;
	}

	public void setLogger(String name) {
		this.logger = LoggerFactory.getLogger("xd.sink.throughput." + name);
	}

	private class ReportStats implements Runnable {
		@Override
		public void run() {
			while (isRunning()) {
				long intervalStart = clock.now();
				try {
					Thread.sleep(reportEveryMs);
					long timeNow = clock.now();
					long currentCounter = intermediateCounter.getAndSet(0L);
					long currentBytes = intermediateBytes.getAndSet(0L);
					long totalCounter = counter.addAndGet(currentCounter);
					long totalBytes = bytes.addAndGet(currentBytes);

					logger.info(
							String.format("Messages: %10d in %5.2f%s = %11.2f/s",
									currentCounter,
									(timeNow - intervalStart)/ 1000.0, timeUnit, ((double) currentCounter * 1000 / reportEveryMs)));
					logger.info(
							String.format("Messages: %10d in %5.2f%s = %11.2f/s",
									totalCounter, (timeNow - start.get()) / 1000.0, timeUnit,
									((double) totalCounter * 1000 / (timeNow - start.get()))));
					if (reportBytes) {
						logger.info(
								String.format("Throughput: %12d in %5.2f%s = %11.2fMB/s, ",
										currentBytes,
										(timeNow - intervalStart)/ 1000.0, timeUnit,
										((currentBytes / (1024.0 * 1024)) * 1000 / reportEveryMs)));
						logger.info(
								String.format("Throughput: %12d in %5.2f%s = %11.2fMB/s",
										totalBytes, (timeNow - start.get()) / 1000.0, timeUnit,
										((totalBytes / (1024.0 * 1024)) * 1000 / (timeNow - start.get()))));
					}
				}
				catch (InterruptedException e) {
					if (!isRunning()) {
						Thread.currentThread().interrupt();
					}
				}
			}
		}
	}
}

