package de.mhus.osgi.sop.impl.sample;

import aQute.bnd.annotation.component.Component;
import de.mhus.lib.core.definition.DefRoot;
import de.mhus.lib.core.strategy.AbstractOperation;
import de.mhus.lib.core.strategy.Operation;
import de.mhus.lib.core.strategy.OperationDescription;
import de.mhus.lib.core.strategy.OperationResult;
import de.mhus.lib.core.strategy.Successful;
import de.mhus.lib.core.strategy.TaskContext;

@Component
public class TestOperation extends AbstractOperation implements Operation {

	@Override
	protected OperationResult doExecute2(TaskContext context) throws Exception {
		System.out.println("Execute");
		return new Successful(this);
	}

	@Override
	protected OperationDescription createDescription() {
		return new OperationDescription(this,"Test", new DefRoot(
		
				));
	}

}
