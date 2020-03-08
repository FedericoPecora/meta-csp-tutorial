/*******************************************************************************
 * Copyright (c) 2010-2020 Federico Pecora <federico.pecora@oru.se>
 * 
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 * 
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package util;

import java.util.ArrayList;
import java.util.List;

public class MultiListParser {
	
	private String s = null;
	
	public MultiListParser(String toParse) {
		this.s = toParse;
	}
	
	private ArrayList<Object> parse() {
		ArrayList<Object> newList = new ArrayList<Object>();
		while (s.length() > 0) {
			int indexOfSpace = (s.indexOf(" ") == -1 ? Integer.MAX_VALUE : s.indexOf(" "));
			int indexOfParClose = (s.indexOf(")") == -1 ? Integer.MAX_VALUE : s.indexOf(")"));
			int indexOfParOpen = (s.indexOf("(") == -1 ? Integer.MAX_VALUE : s.indexOf("("));
			String piece = s.substring(0,Math.max(1, Math.min(Math.min(indexOfSpace,indexOfParOpen),indexOfParClose)));
			s = s.substring(piece.length());
			if (piece.equals("(")) newList.add(parse());
			else if (piece.equals(")")) break;
			else if (piece.equals(" ")) { }
			else newList.add(piece);
		}
		return newList;
	}
	
	@SuppressWarnings("unchecked")
	public List<Object> parseObjects() {
		List<Object> ret = this.parse();
		if (ret.isEmpty() || ret.size() > 1) return ret;
		return (List<Object>)ret.get(0);
	}
	
	public static void main(String[] args) {
		//String st = "  ( vs ( a..].l b )  998c  )";
		String st = "(((a b) (c)) ())";
		MultiListParser parser = new MultiListParser(st);
		List<Object> objs = parser.parseObjects();
		System.out.println(objs);
	}

}
