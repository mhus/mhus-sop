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
package de.mhus.osgi.sop.rest;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map.Entry;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import de.mhus.lib.core.MApi;
import de.mhus.lib.core.console.ConsoleTable;
import de.mhus.osgi.sop.api.rest.AbstractNode;
import de.mhus.osgi.sop.api.rest.RestApi;
import de.mhus.osgi.sop.api.rest.RestNodeService;

@Command(scope = "sop", name = "rest", description = "REST Call")
@Service
public class RestCmd implements Action {

    @Argument(index=0, name="cmd", required=true, description="Command:\n"
            + " list\n"
            + "", multiValued=false)
    String cmd;
    
    @Argument(index=1, name="parameters", required=false, description="More Parameters", multiValued=true)
    String[] parameters;

    @Option(name="-x", description="Full Table Content",required=false, multiValued=false)
    boolean full = false;

	@Override
	public Object execute() throws Exception {

        RestApi restService = MApi.lookup(RestApi.class);

        if (cmd.equals("list")) {
            HashMap<RestNodeService, LinkedList<String>> list = new HashMap<RestNodeService, LinkedList<String>>();
            for (Entry<String, RestNodeService> entry : restService.getRestNodeRegistry().entrySet()) {
                LinkedList<String> item = list.get(entry.getValue());
                if (item == null) {
                    item = new LinkedList<String>();
                    list.put(entry.getValue(), item);
                }
                item.add(entry.getKey());
            }
            
            ConsoleTable table = new ConsoleTable(full);
            table.setLineSpacer(true);
            table.setHeaderValues("Class","Node Id","Parents","Managed","Registrations");
            for (Entry<RestNodeService, LinkedList<String>> entry : list.entrySet()) {
                String managed = "";
                if (entry.getKey() instanceof AbstractNode)
                    managed = ((AbstractNode)entry.getKey()).getManagedClassName();
                
            	table.addRowValues(
            	        entry.getKey().getClass().getCanonicalName(),
            	        entry.getKey().getNodeId(), 
            	        entry.getKey().getParentNodeCanonicalClassNames(),
            	        managed,
            	        entry.getValue()
            	      );
            }
            table.print();
        }
		return null;
	}

}
