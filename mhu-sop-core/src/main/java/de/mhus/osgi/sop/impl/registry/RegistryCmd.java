package de.mhus.osgi.sop.impl.registry;

import java.util.Date;
import java.util.LinkedList;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import de.mhus.lib.core.MApi;
import de.mhus.lib.core.MDate;
import de.mhus.lib.core.console.ConsoleTable;
import de.mhus.osgi.sop.api.jms.JmsApi;
import de.mhus.osgi.sop.api.registry.RegistryApi;
import de.mhus.osgi.sop.api.registry.RegistryManager;
import de.mhus.osgi.sop.api.registry.RegistryValue;

@Command(scope = "sop", name = "registry", description = "Registry commands")
@Service
public class RegistryCmd implements Action {

	@Argument(index=0, name="cmd", required=true, description="Command list, action, info <path>, execute <path> [key=value]*, search", multiValued=false)
	String cmd;

	@Argument(index=1, name="path", required=false, description="Path to Node", multiValued=false)
    String path;
	
	@Argument(index=2, name="parameters", required=false, description="More Parameters", multiValued=true)
    String[] parameters;

	@Override
	public Object execute() throws Exception {
		RegistryApi api = MApi.lookup(RegistryApi.class);
		if (cmd.equals("list")) {
			
			if (path != null) {
				ConsoleTable out = new ConsoleTable();
				out.setHeaderValues("Path","Value","Source","Updated");
				for (String child : api.getNodeChildren(path))
					out.addRowValues(child,"[node]","","");
				for (RegistryValue value : api.getParameters(path) ) 
					out.addRowValues(value.getPath(), value.getValue(),value.getSource(),new Date(value.getUpdated()));
				out.print(System.out);
			} else {
				RegistryManager manager = MApi.lookup(RegistryManager.class);
				LinkedList<RegistryValue> list = new LinkedList<>(manager.getAll());
				list.sort((a,b) -> { return a.getPath().compareTo(b.getPath());});
				ConsoleTable out = new ConsoleTable();
				out.setHeaderValues("Path","Value","Source","Updated");
				for (RegistryValue value : list ) {
					out.addRowValues(value.getPath(), value.getValue(),value.getSource(),new Date(value.getUpdated()));
				}
				out.print(System.out);
			}
		} else
		if (cmd.equals("get")) {
			RegistryValue entry = api.getNodeParameter(path);
			System.out.println("Path   : " + entry.getPath());
			System.out.println("Value  : " + entry.getValue());
			System.out.println("Source : " + entry.getSource());
			System.out.println("Updated: " + MDate.toIsoDateTime(entry.getUpdated()));
		} else
		if (cmd.equals("set") || cmd.equals("add")) {
			if (api.setParameter(path, parameters[0]))
				System.out.println("SET");
			else
				System.out.println("NOT CHANGED");
		} else
		if (cmd.equals("remove")) {
			if (api.removeParameter(path))
				System.out.println("REMOVED");
			else
				System.out.println("UNKNOWN");
		} else
		if (cmd.equals("publish")) {
			api.publishAll();
		} else
		if (cmd.equals("request")) {
			api.requestAll();
		}
		return null;
	}

}
