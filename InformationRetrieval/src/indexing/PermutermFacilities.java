package indexing;

import java.util.ArrayList;
import java.util.List;

public final class PermutermFacilities {

	public static final char PERMUTERM_SYMBOL = '$';
	public static final char WILDCARD_SYMBOL = '*';

//	public static List<String> producePermutermList(String token) {
//		if (token == null) {
//			return null;
//		}
//
//		token += PERMUTERM_SYMBOL;
//
//		List<String> result = new ArrayList<String>();
//		result.add(token);
//		for (int i = 1; i < token.length(); i++) {
//			token = token.substring(1, token.length()) + token.charAt(0);
//			result.add(token);
//		}
//
//		return result;
//
//	}

//	public static String translateToPostfixWildcard(String text) {
//		int pos = -1;
//		int count = 0;
//		for (int i = 0; i < text.length(); i++) {
//			if (text.charAt(i) == '*') {
//				pos = i;
//				count++;
//			}
//		}
//
////		if (count > 1) {
////			System.out.println(String.format(
////									"More than 1 wildcard char found in token '%s', removing all of them.",
////									text));
////			count = 0;
////			pos = -1;
////			text = text.replaceAll("[*]", "");
////		}
//
//		text += PERMUTERM_SYMBOL;
//
//		if (count == 1) {
//			String X = text.substring(0, pos + 1);
//			String Y = text.substring(pos + 1, text.length());
//			text = Y + X;
//			assert (text.charAt(text.length() - 1) == WILDCARD_SYMBOL);
//		}
//
//		return text;
//	}
	
	public static boolean isPostfixWildcard(String term) {
		if (term == null || term.length() == 0) return false;
		return term.charAt(term.length() - 1) == WILDCARD_SYMBOL;
	}
	
	public static String removePostfixWildcard(String term) {
		if (isPostfixWildcard(term)) {
			return term.substring(0, term.length() - 1);
		}
		return term;
	}

	public static String shiftWildCardToEnd(String input) {
		
		if(!input.contains("*"))
			return input;
//		if (input.lastIndexOf(WILDCARD_SYMBOL) != input
//				.indexOf(WILDCARD_SYMBOL))
//			throw new RuntimeException("more than one '*'");
		int wildCardIndex = input.indexOf(WILDCARD_SYMBOL) + 1;
		return input.substring(wildCardIndex) + PERMUTERM_SYMBOL
				+ input.substring(0, wildCardIndex);
	}
//
//	public static void main(String[] args) {
//		String str = "Hello";
//
//		System.out.println(producePermutermList(str));
//		System.out.println(shiftWildCardToEnd("*las*edfgo"));
//	}
//	
	public static List<String> producePermutermList(String term) {

			List<String> result = new ArrayList<String>();
			term = PERMUTERM_SYMBOL + term;
			result.add(term);
			while (!term.endsWith(String.valueOf(PERMUTERM_SYMBOL))) {
				result.add(term = term.substring(term.length() - 1)
						+ term.substring(0, term.length() - 1));
			}
			return result;
		}
}
