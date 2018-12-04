package de.mhus.osgi.sop.api.rest;

import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Reader;

import de.mhus.lib.core.MFile;

public class BinaryResult implements RestResult {

	private InputStream is;
	private String contentType;
	private Reader reader;

	public BinaryResult(InputStream is, String contentType) {
		this.contentType = contentType;
		this.is = is;
	}

	public BinaryResult(Reader reader, String contentType) {
		this.contentType = contentType;
		this.reader = reader;
	}

	@Override
	public void write(PrintWriter writer) throws Exception {
		if (is != null) {
			while (true) {
				int b = is.read();
				if (b < 0) break;
				writer.write(b);
			}
			is.close();
			is = null;
		} else
		if (reader != null) {
			MFile.copyFile(reader, writer);
			reader.close();
			reader = null;
		}
	}

	@Override
	public String getContentType() {
		return contentType;
	}

}
