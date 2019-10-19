package com.legeyda.mts.util;

public class Sleep implements Runnable {
	private final long durationMillis;

	public Sleep(long durationMillis) {
		this.durationMillis = durationMillis;
	}

	@Override
	public void run() {
		try {
			Thread.sleep(this.durationMillis);
		} catch (InterruptedException e) {
			throw new RuntimeException("thread was interrupted", e);
		}
	}
}
