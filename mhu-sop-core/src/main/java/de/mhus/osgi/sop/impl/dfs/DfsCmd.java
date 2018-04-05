package de.mhus.osgi.sop.impl.dfs;

import java.io.File;
import java.util.Date;
import java.util.Map.Entry;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import de.mhus.lib.core.MApi;
import de.mhus.lib.core.MDate;
import de.mhus.lib.core.MFile;
import de.mhus.lib.core.MString;
import de.mhus.lib.core.console.ConsoleTable;
import de.mhus.lib.core.util.MUri;
import de.mhus.osgi.sop.api.dfs.DfsApi;
import de.mhus.osgi.sop.api.dfs.FileInfo;
import de.mhus.osgi.sop.api.dfs.FileQueueApi;

@Command(scope = "sop", name = "dfs", description = "Distributed File System actions")
@Service
public class DfsCmd implements Action {

	@Argument(index=0, name="cmd", required=true, description=
			"Command:\n"
			+ "providers          - list all providers\n"
			+ "list <dir uri>     - list directory content\n"
			+ "info <file uri>    - list file infomation\n"
			+ "require <file uri> - create a file queue for the file\n"
			+ "print <file uri>   - create file queue entry and print content of file")
	String cmd; 

	@Argument(index=1, name="parameters", required=false, description="More Parameters", multiValued=true)
    String[] parameters;

	@Option(name="-f", aliases="--full", description="Full output",required=false)
	boolean full = false;

	@Override
	public Object execute() throws Exception {
		
		DfsApi api = MApi.lookup(DfsApi.class);

		switch (cmd) {
		case "providers" : {
			ConsoleTable table = new ConsoleTable(full);
			table.setHeaderValues("Scheme","Location","Uri");
			for (String uri : api.listProviders()) {
				MUri u = MUri.toUri(uri);
				table.addRowValues(u.getScheme(), u.getLocation(), uri);
			}
			table.print(System.out);
		} break;
		case "list": {
			MUri uri = MUri.toUri(parameters[0]);
			System.out.println("Directory Content of " + uri);
			ConsoleTable table = new ConsoleTable(full);
			table.setHeaderValues("Name","Uri");
			for (Entry<String, MUri> file : api.getDirectoryList(uri).entrySet()) {
				table.addRowValues(file.getKey(), file.getValue());
			}
			table.print(System.out);
		} break;
		case "info": {
			FileInfo info = api.getFileInfo(parameters[0]);
			System.out.println("Info for " + parameters[0]);
			System.out.println("Name    : " + info.getName());
			System.out.println("Size    : " + MString.toByteDisplayString(info.getSize()) + " (" + info.getSize() + ")");
			System.out.println("Modified: " + MDate.toIso8601(new Date(info.getModified())));
			System.out.println("URI     : " +info.getUri());
		} break;
		case "request": {
			MUri uri = api.requestFile(MUri.toUri(parameters[0]));
			System.out.println("Requested file: " + parameters[0]);
			System.out.println("Queue: " + uri);
		} break;
		case "print": {
			MUri uri = api.requestFile(MUri.toUri(parameters[0]));
			System.out.println("Requested file: " + parameters[0]);
			System.out.println("Queue: " + uri);
			System.out.println("------------ Content -------------");
			File file = MApi.lookup(FileQueueApi.class).loadFile(uri);
			System.out.println( MFile.readFile(file) );
			System.out.println("-------------- END ---------------");
		}
		}
		return null;
	}

	
}
