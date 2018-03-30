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
package de.mhus.osgi.sop.api.action;

import de.mhus.lib.core.strategy.AbstractOperation;
import de.mhus.lib.core.strategy.OperationResult;
import de.mhus.lib.core.strategy.TaskContext;
import de.mhus.osgi.sop.api.operation.OperationBpmDefinition;
import de.mhus.osgi.sop.api.operation.OperationService;

public abstract class BpmOperation extends AbstractOperation implements OperationService {

	@Override
	public boolean hasAccess() {
		return true;
	}

	@Override
	public final boolean canExecute(TaskContext context) {
		boolean ret = canExecuteBpm(context);
		if (!ret) BpmUtil.appendComment(context, this, "can't execute " + getClass().getCanonicalName());
		return ret;
	}

	protected abstract boolean canExecuteBpm(TaskContext context);

	@Override
	protected final OperationResult doExecute2(TaskContext context) throws Exception {
		try {
			OperationResult ret = doExecuteBpm(context);
			BpmUtil.appendComment(context, ret);
			return ret;
		} catch (Throwable t) {
			BpmUtil.appendComment(context, this, "Exception in " + getClass().getCanonicalName() +": " + t);
			throw t;
		}
	}

	protected abstract OperationResult doExecuteBpm(TaskContext context) throws Exception;

	protected void addComment(TaskContext context, String msg) {
		BpmUtil.appendComment(context, this, msg);
	}

	@Override
	public OperationBpmDefinition getBpmDefinition() {
		return null;
	}

}
