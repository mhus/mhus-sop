package de.mhus.osgi.sop.impl.operation;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import de.mhus.lib.core.IProperties;
import de.mhus.lib.core.MApi;
import de.mhus.lib.core.MProperties;
import de.mhus.lib.core.MString;
import de.mhus.lib.core.console.ConsoleTable;
import de.mhus.lib.core.strategy.Operation;
import de.mhus.lib.core.strategy.OperationDescription;
import de.mhus.lib.core.strategy.OperationResult;
import de.mhus.lib.core.util.VersionRange;
import de.mhus.lib.jms.JmsConnection;
import de.mhus.lib.karaf.jms.JmsUtil;
import de.mhus.osgi.sop.api.Sop;
import de.mhus.osgi.sop.api.jms.JmsApi;
import de.mhus.osgi.sop.api.aaa.AaaContext;
import de.mhus.osgi.sop.api.aaa.AccessApi;
import de.mhus.osgi.sop.api.operation.OperationAddress;
import de.mhus.osgi.sop.api.operation.OperationApi;
import de.mhus.osgi.sop.api.operation.OperationDescriptor;

@Command(scope = "sop", name = "operation", description = "Operation commands")
@Service
public class OperationCmd implements Action {

	@Argument(index=0, name="cmd", required=true, description="Command list, action, info <path>, execute <path> [key=value]*, search", multiValued=false)
	String cmd;
	
	@Argument(index=1, name="path", required=false, description="Path to Operation", multiValued=false)
    String path;
	
	@Argument(index=2, name="parameters", required=false, description="More Parameters", multiValued=true)
    String[] parameters;

	@Option(name="-c", aliases="--connection", description="JMS Connection Name",required=false)
	String conName = null;

	@Option(name="-q", aliases="--queue", description="JMS Connection Queue OperationChannel",required=false)
	String queueName = null;
	
	@Option(name="-v", aliases="--version", description="Version Range [1.2.3,2.0.0)",required=false)
	String version = null;
	
	@Option(name="-o", aliases="--options", description="Execute Options separated by pipe",required=false)
	String options = null;
	
	@Override
	public Object execute() throws Exception {

		if (conName == null)
			conName = MApi.lookup(JmsApi.class).getDefaultConnectionName();
		
		OperationApi api = MApi.lookup(OperationApi.class);
		
		if (cmd.equals("list")) {
			ConsoleTable out = new ConsoleTable();
			out.setHeaderValues("address","title","tags");
			for (OperationDescriptor desc : api.findOperations(path,version == null ? null : new VersionRange(version),null)) {
				out.addRowValues(desc.getAddress(),desc.getTitle(),desc.getTags());
			}
			out.print(System.out);
		} else
		if (cmd.equals("info")) {
			OperationDescriptor des = api.findOperation(path, version == null ? null : new VersionRange(version),null);
			System.out.println("Title  : " + des.getTitle());
			System.out.println("Form   : " + des.getForm());
		} else
		if (cmd.equals("execute")) {
			String[] executeOptions = null;
			if (options != null) executeOptions = options.split("\\|");
			
			MProperties properties = MProperties.explodeToMProperties(parameters);
			OperationResult res = null;
			if (path.indexOf("://") >= 0) {
				OperationAddress addr = new OperationAddress(path);
				OperationDescriptor desc = api.getOperation(addr);
				res = api.doExecute(desc, properties, executeOptions);
			} else
				res = api.doExecute(path, version == null ? null : new VersionRange(version), null, properties, executeOptions);
			System.out.println("Result: "+res);
			if (res != null) {
				System.out.println("RC: " + res.getReturnCode());
				System.out.println("Object: " + res.getResult());
			}
		} else
		if (cmd.equals("request")) {
			MApi.lookup(JmsApi.class).requestOperationRegistry();
			System.out.println("ok");
		} else
		if (cmd.equals("send")) {
			MApi.lookup(JmsApi.class).sendLocalOperations();
			System.out.println("ok");
		} else
		if (cmd.equals("sync")) {
			api.synchronize();
			System.out.println("ok");
		} else {
			System.out.println("Command not found");
		}
		return null;
	}

	
}
