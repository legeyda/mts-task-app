package com.legeyda.mts.util;

public class Sleep implements Runnable {
	private final long duration;

	public Sleep(long duration) {
		this.duration = duration;
	}

	@Override
	public void run() {
		try {
			Thread.sleep(this.duration);
		} catch (InterruptedException e) {
			throw new RuntimeException("thread was interrupted", e);
		}
	}
}
