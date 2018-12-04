package de.mhus.osgi.sop.api.rest;

import java.io.PrintWriter;

import de.mhus.lib.core.MString;
import de.mhus.lib.core.io.http.MHttp;

public class PlainTextResult implements RestResult {

	private String contentType;
	private String text;

	public PlainTextResult(String text, String contentType) {
		if (MString.isEmpty(contentType)) contentType = MHttp.CONTENT_TYPE_TEXT;
		this.contentType = contentType;
		this.text = text;
	}

	@Override
	public void write(PrintWriter writer) throws Exception {
		writer.write(text);
	}

	@Override
	public String getContentType() {
		return contentType;
	}

}
