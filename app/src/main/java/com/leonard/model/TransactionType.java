package com.leonard.model;

public enum TransactionType {
	TRANSACTION(1), PENDING(2);
	private final int value;

	private TransactionType(final int mode) {
		this.value = mode;
	}

	public int getValue() {
		return value;
	}
}
