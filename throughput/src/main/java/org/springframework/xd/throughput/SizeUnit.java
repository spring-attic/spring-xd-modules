package org.springframework.xd.throughput;

/**
 *
 *
 * @author Eric Bottard
 */
public enum SizeUnit {

	bit(1),
	Kbit(1000),
	Kibit(1024),
	Mbit(1000L * 1000),
	Mibit(1024L * 1024),
	Gbit(1000L * 1000 * 1000),
	Gibit(1024L * 1024 * 1024),
	Tbit(1000L * 1000 * 1000 * 1000),
	Tibit(1024L * 1024 * 1024 * 1024),

	B(8),
	KB(1000 * 8),
	KiB(1024 * 8),
	MB(1000L * 1000 * 8),
	MiB(1024L * 1024 * 8),
	GB(1000L * 1000 * 1000 * 8),
	GiB(1024L * 1024 * 1024 * 8),
	TB(1000L * 1000 * 1000 * 1000 * 8),
	TiB(1024L * 1024 * 1024 * 1024 * 8);

	private long bits;

	SizeUnit(long bits) {
		this.bits = bits;
	}

	/**
	 * Convert into *this* unit the amount {@code howMany} interpreted in the {@code original} unit.
	 */
	public double convert(long howMany, SizeUnit original) {
		return (double) howMany * original.bits / this.bits;
	}

}
