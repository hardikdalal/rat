package com.rat;
public class Review {
	public static int count = 1;
	public int id, polarity;
	public String text, aspect, pos;
	public Review(String text,String aspect, String pos, int polarity) {
		id = Review.count;
		this.text = text;
		this.aspect = aspect;
		this.pos = pos;
		this.polarity = polarity;
		++Review.count;
	}
}