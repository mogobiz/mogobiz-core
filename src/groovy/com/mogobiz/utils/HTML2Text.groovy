package com.mogobiz.utils;

import javax.swing.text.html.HTMLEditorKit
import javax.swing.text.html.parser.ParserDelegator


public class Html2Text extends HTMLEditorKit.ParserCallback {
	StringBuffer s;

	public Html2Text(String s) {
		StringReader reader = new StringReader(s);
		this.parse(reader);
		reader.close();
	}

	private void parse(Reader ir) throws IOException {
		s = new StringBuffer();
		ParserDelegator dlg = new ParserDelegator();
		// the third parameter is TRUE to ignore charset directive
		dlg.parse(ir, this, Boolean.TRUE);
	}

	public void handleText(char[] text, int pos) {
		s.append(text);
	}

	public String getText() {
		return s.toString();
	}
}