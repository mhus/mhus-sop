/**
 * Copyright 2018 Mike Hummel
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
