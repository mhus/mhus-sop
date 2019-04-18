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
package de.mhus.osgi.sop.api.util;

import java.io.File;

import de.mhus.lib.core.M;
import de.mhus.lib.core.cfg.CfgString;
import de.mhus.lib.logging.FileLogger;
import de.mhus.osgi.sop.api.aaa.AaaContext;
import de.mhus.osgi.sop.api.aaa.AccessApi;

// TODO implement asynchrony logging
public class SopFileLogger extends FileLogger {

	private static CfgString logDir = new CfgString(SopFileLogger.class, "logDirectory", null) {
		@Override
		protected void onPreUpdate(String newValue) {
			if (newValue == null) return;
					new File(newValue).mkdirs();
		}
	};
	private String logName;
	
	public SopFileLogger(String name, String logName) {
		super(name, null );
		this.logName = logName;
	}

	@Override
	protected String getInfo() {
		StringBuilder out = new StringBuilder();
		out.append(Thread.currentThread().getId()).append(',');
		out.append(Thread.currentThread().getName()).append(',');
		
		AaaContext context = M.l(AccessApi.class).getCurrentOrGuest();
		out.append(context.getAccountId());
		
		return out.toString();
	}

	@Override
    protected void prepare(StringBuilder sb) {
	}

	@Override
	protected void doUpdateFile() {
		file = logDir.value() == null ? null : new File(logDir.value(), logName + ".log");
	}

}
