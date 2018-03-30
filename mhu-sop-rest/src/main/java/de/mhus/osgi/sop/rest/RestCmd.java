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

import java.util.Arrays;
import java.util.Map.Entry;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import de.mhus.lib.core.MApi;
import de.mhus.lib.core.console.ConsoleTable;
import de.mhus.osgi.sop.api.rest.RestApi;
import de.mhus.osgi.sop.api.rest.RestNodeService;

@Command(scope = "sop", name = "rest", description = "REST Call")
@Service
public class RestCmd implements Action {

	@Override
	public Object execute() throws Exception {

        RestApi restService = MApi.lookup(RestApi.class);

        ConsoleTable table = new ConsoleTable();
        table.setHeaderValues("Registered","Node Id","Parents","Class");
        for (Entry<String, RestNodeService> entry : restService.getRestNodeRegistry().entrySet()) {
        	table.addRowValues(entry.getKey(),entry.getValue().getNodeId(), Arrays.toString( entry.getValue().getParentNodeIds() ),entry.getValue().getClass().getCanonicalName() );
        }
        table.print(System.out);
		return null;
	}

}
