package org.springframework.xd.throughput;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotBlank;

import org.springframework.xd.module.options.spi.ModuleOption;
import org.springframework.xd.module.options.spi.ModulePlaceholders;

/**
 * Documents the options of the throughput module.
 *
 * @author Eric Bottard
 */
public class ThroughputOptionsMetadata {

	private long reportEveryMs = 1000L;

	private TimeUnit timeUnit = TimeUnit.s;

	private String logger = ModulePlaceholders.XD_STREAM_NAME;

	@NotBlank
	public String getLogger() {
		return logger;
	}

	@ModuleOption("the name of the logger to use (will use 'xd.sink.throughput.<logger>')")
	public void setLogger(String logger) {
		this.logger = logger;
	}

	@Min(0)
	@NotNull
	public long getReportEveryMs() {
		return reportEveryMs;
	}

	@ModuleOption("if positive, will report throughput this every milliseconds")
	public void setReportEveryMs(long reportEveryMs) {
		this.reportEveryMs = reportEveryMs;
	}

	@NotNull
	public TimeUnit getTimeUnit() {
		return timeUnit;
	}

	@ModuleOption("the time unit to use in reports")
	public void setTimeUnit(TimeUnit timeUnit) {
		this.timeUnit = timeUnit;
	}

}
