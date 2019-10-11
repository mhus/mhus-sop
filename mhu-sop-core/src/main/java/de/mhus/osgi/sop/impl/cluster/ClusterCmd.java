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
package de.mhus.osgi.sop.impl.cluster;

import java.util.function.Consumer;

import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import de.mhus.lib.core.M;
import de.mhus.lib.core.concurrent.Lock;
import de.mhus.osgi.api.karaf.AbstractCmd;
import de.mhus.osgi.sop.api.cluster.ClusterApi;
import de.mhus.osgi.sop.api.registry.RegistryUtil;

@Command(scope = "sop", name = "cluster", description = "Cluster commands")
@Service
public class ClusterCmd extends AbstractCmd {

	@Argument(index=0, name="cmd", required=true, description="Command:\n"
	        + " lock <path>\n"
	        + " stacklock <path>\n"
	        + " master <path>\n"
	        + "", multiValued=false)
	String cmd;

	@Argument(index=1, name="path", required=false, description="Path to Node", multiValued=false)
    String path;
	
	@Argument(index=2, name="parameters", required=false, description="More Parameters", multiValued=true)
    String[] parameters;

    @Option(name="-t", aliases="--timeout", description="Set timeout for new entries",required=false)
    long timeout = 0;

    @Override
	public Object execute2() throws Exception {
	    ClusterApi api = M.l(ClusterApi.class);
		
	    if (cmd.equals("fire")) {
	        api.fireEvent(path, parameters[0]);
	        System.out.println("ok");
	    } else
	    if (cmd.equals("register")) {
            Consumer<String> c = v -> System.out.println("Event Value: " + v);
            api.registerListener(path, c );
	    } else
	    if (cmd.equals("listen")) {
	        Consumer<String> c = v -> System.out.println("Event Value: " + v);
	        api.registerListener(path, c );
	        System.out.println("Press Ctrl+C to exit");
	        try {
    	        while (true)
    	            Thread.sleep(1000);
	        } catch (InterruptedException e) {}
	        api.unregisterListener( c );
	    } else
        if (cmd.equals("master")) {
            System.out.println( RegistryUtil.master(path, timeout) );
        } else
        if (cmd.equals("stacklock")) {
            System.out.println("Wait for lock");
            try (Lock lock = api.getStackLock(path).lock()) {
                System.out.println("Locked " + lock.getLocker());
                System.out.println("Press ctrl+c to stop locking");
                try {
                    int cnt = 0;
                    while (true) {
                        System.out.print(".");
                        Thread.sleep(1000);
                        cnt++;
                        if (cnt >= 60 * 10) {
                            System.out.println();
                            System.out.println("Refresh");
                            lock.refresh();
                            cnt = 0;
                        }
                    }
                } catch (InterruptedException e) {}
            }
            System.out.println();
            System.out.println("Released");
        } else
		if (cmd.equals("lock")) {
		    System.out.println("Wait for lock");
		    try (Lock lock = api.getLock(path).lock()) {
                System.out.println("Locked " + lock.getLocker());
		        try {
		            int cnt = 0;
    		        while (true) {
    		            System.out.print(".");
    		            Thread.sleep(1000);
                        cnt++;
                        if (cnt >= 60 * 10) {
                            System.out.println();
                            System.out.println("Refresh");
                            lock.refresh();
                            cnt = 0;
                        }
    		        }
		        } catch (InterruptedException e) {}
		    }
            System.out.println();
		    System.out.println("Released");
		}
		return null;
	}

}
