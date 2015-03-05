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

	private long reportEveryMs = 10_000L;

	private long reportEveryNumber = Long.MAX_VALUE;

	private long reportEveryBytes = Long.MAX_VALUE;

	private long totalExpected = 0L;

	private TimeUnit timeUnit = TimeUnit.s;

	private SizeUnit sizeUnit = SizeUnit.MB;

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

	@Min(0)
	@NotNull
	public long getReportEveryNumber() {
		return reportEveryNumber;
	}

	@ModuleOption("if positive, will report throughput this every received messages")
	public void setReportEveryNumber(long reportEveryNumber) {
		this.reportEveryNumber = reportEveryNumber;
	}

	@Min(0)
	@NotNull
	public long getReportEveryBytes() {
		return reportEveryBytes;
	}

	@ModuleOption("if positive, will report throughput this every bytes received")
	public void setReportEveryBytes(long reportEveryBytes) {
		this.reportEveryBytes = reportEveryBytes;
	}

	@Min(0)
	@NotNull
	public long getTotalExpected() {
		return totalExpected;
	}

	@ModuleOption("if positive, will report throughput once exactly that many messages have been received")
	public void setTotalExpected(long totalExpected) {
		this.totalExpected = totalExpected;
	}

	@NotNull
	public TimeUnit getTimeUnit() {
		return timeUnit;
	}

	@ModuleOption("the time unit to use in reports")
	public void setTimeUnit(TimeUnit timeUnit) {
		this.timeUnit = timeUnit;
	}

	@NotNull
	public SizeUnit getSizeUnit() {
		return sizeUnit;
	}

	@ModuleOption("the size unit to use in reports")
	public void setSizeUnit(SizeUnit sizeUnit) {
		this.sizeUnit = sizeUnit;
	}
}
