/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.xd.greenplum.gpfdist;

import java.util.Date;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.reactivestreams.Processor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandlingException;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.util.StringUtils;
import org.springframework.util.concurrent.SettableListenableFuture;
import org.springframework.xd.greenplum.GreenplumLoad;

import reactor.Environment;
import reactor.core.processor.RingBufferProcessor;
import reactor.io.buffer.Buffer;

import com.codahale.metrics.Meter;

public class GPFDistMessageHandler extends AbstractGPFDistMessageHandler {

	private final Log log = LogFactory.getLog(GPFDistMessageHandler.class);

	private final int port;

	private final int flushCount;

	private final int flushTime;

	private final int batchTime;

	private final int batchPeriod;

	private final String delimiter;

	private GreenplumLoad greenplumLoad;

	private Processor<Buffer, Buffer> processor;

	private GPFDistServer gpfdistServer;

	private TaskScheduler sqlTaskScheduler;

	private final TaskFuture taskFuture = new TaskFuture();

	// TODO: just for this poc to get perf numbers
	private Meter meter = new Meter();
	private int meterCount = 0;

	public GPFDistMessageHandler(int port, int flushCount, int flushTime, int batchTime, int batchPeriod, String delimiter) {
		super();
		this.port = port;
		this.flushCount = flushCount;
		this.flushTime = flushTime;
		this.batchTime = batchTime;
		this.batchPeriod = batchPeriod;
		this.delimiter = StringUtils.hasLength(delimiter) ? delimiter : null;
	}

	@Override
	protected void doWrite(Message<?> message) throws Exception {
		Object payload = message.getPayload();
		if (payload instanceof String) {
			String data = (String)payload;
			if (delimiter != null) {
				processor.onNext(Buffer.wrap(data+delimiter));
			} else {
				processor.onNext(Buffer.wrap(data));
			}
			if ((meterCount++ % 100000) == 0) {
				meter.mark(100000);
				log.info("METER 1m/" + meter.getOneMinuteRate() + " mean/" + meter.getMeanRate());
			}
		} else {
			throw new MessageHandlingException(message, "message not a String");
		}
	}

	@Override
	protected void onInit() throws Exception {
		super.onInit();
		Environment.initializeIfEmpty().assignErrorJournal();
		processor = RingBufferProcessor.create(false);
	}

	@Override
	protected void doStart() {
		try {
			log.info("Creating gpfdist protocol listener on port=" + port);
			gpfdistServer = new GPFDistServer(processor, port, flushCount, flushTime, batchTime);
			gpfdistServer.start();
		} catch (Exception e) {
			throw new RuntimeException("Error starting protocol listener", e);
		}

		if (greenplumLoad != null) {
			log.info("Scheduling gpload task with batchPeriod=" + batchPeriod);

			sqlTaskScheduler.schedule((new FutureTask<Void>(new Runnable() {
				@Override
				public void run() {
					try {
						while(!taskFuture.interrupted) {
							greenplumLoad.load();
							Thread.sleep(batchPeriod*1000);
						}
					} catch (Exception e) {
						taskFuture.set(false);
					}
				}
			}, null)), new Date());

		} else {
			log.info("Skipping gpload tasks because greenplumLoad is not set");
		}
	}

	@Override
	protected void doStop() {
		if (greenplumLoad != null) {
			taskFuture.interruptTask();
			try {
				taskFuture.get(batchPeriod, TimeUnit.SECONDS);
			} catch (Exception e1) {
			}
		}

		try {
			gpfdistServer.stop();
		} catch (Exception e) {
			log.warn("Error shutting down protocol listener", e);
		}
	}

	public void setSqlTaskScheduler(TaskScheduler sqlTaskScheduler) {
		this.sqlTaskScheduler = sqlTaskScheduler;
	}

	public void setGreenplumLoad(GreenplumLoad greenplumLoad) {
		this.greenplumLoad = greenplumLoad;
	}

	private static class TaskFuture extends SettableListenableFuture<Boolean> {

		boolean interrupted = false;

		@Override
		protected void interruptTask() {
			interrupted = true;
		}
	}
}
