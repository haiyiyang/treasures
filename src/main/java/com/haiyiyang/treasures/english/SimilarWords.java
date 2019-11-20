package com.haiyiyang.treasures.english;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import info.debatty.java.stringsimilarity.Levenshtein;

public class SimilarWords {

	private String leftWord;
	private String rightWord;

	private boolean startWith;
	private boolean endWith;

	private int ignoredLength;
	private double l9nDistance;
	private static final String leftParenthesis = "(";
	private static final String patternRight = "|\\w+)";

	public static void main(String[] args) {
		System.out.println("Continue...");
		Levenshtein l9n = new Levenshtein();

		String l = "abcyijk";
		String r = "abcdefghijk";
		System.out.println(l9n.distance(l, r));

		Pattern pattern = SimilarWords.compile(l);
		int diffCount = SimilarWords.countDifference(pattern, l, r);
		System.out.println("diff count:" + diffCount);
	}

	private static Pattern compile(String word) {
		StringBuffer strb = new StringBuffer(word.length() * 8);
		for (int i = 0; i < word.length(); i++) {
			strb.append(leftParenthesis).append(word.charAt(i)).append(patternRight);
		}
		System.out.println(strb.toString());
		return Pattern.compile(strb.toString());
	}

	private static int countDifference(Pattern pattern, String patternWord, String word) {
		Matcher m = pattern.matcher(word);
		if (m.find()) {
			int diffCount = 0;
			int groupCount = m.groupCount();
			System.out.println("groupCount:" + groupCount);
			for (int i = 1; i < groupCount; i++) {
				String groupWord = m.group(i);
				if (groupWord.length() > 1 || groupWord.charAt(0) != patternWord.charAt(i - 1)) {
					diffCount++;
				}
			}
			return diffCount;
		}
		return -1;
	}

	public String getLeftWord() {
		return leftWord;
	}

	public void setLeftWord(String leftWord) {
		this.leftWord = leftWord;
	}

	public String getRightWord() {
		return rightWord;
	}

	public void setRightWord(String rightWord) {
		this.rightWord = rightWord;
	}

	public boolean isStartWith() {
		return startWith;
	}

	public void setStartWith(boolean startWith) {
		this.startWith = startWith;
	}

	public boolean isEndWith() {
		return endWith;
	}

	public void setEndWith(boolean endWith) {
		this.endWith = endWith;
	}

	public int getIgnoredLength() {
		return ignoredLength;
	}

	public void setIgnoredLength(int ignoredLength) {
		this.ignoredLength = ignoredLength;
	}

	public double getL9nDistance() {
		return l9nDistance;
	}

	public void setL9nDistance(double l9nDistance) {
		this.l9nDistance = l9nDistance;
	}

}
