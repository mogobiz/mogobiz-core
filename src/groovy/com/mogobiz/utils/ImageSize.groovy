package com.mogobiz.utils;

public enum ImageSize {
	ICON(32,32), SMALL(240,240)
	
	public int height() {
		return this.height
	}
	public int width() {
		return this.width
	}
	private final int width;
	private final int height
	ImageSize (int width, int height) {
		this.width = width
		this.height = height
	}
}
