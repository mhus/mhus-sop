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

import org.osgi.service.component.annotations.Component;
import de.mhus.lib.core.M;
import de.mhus.lib.core.MCast;
import de.mhus.lib.core.MThread;
import de.mhus.lib.core.crypt.MRandom;
import de.mhus.osgi.sop.api.registry.RegistryManager;
import de.mhus.osgi.sop.api.registry.RegistryPathControl;
import de.mhus.osgi.sop.api.registry.RegistryUtil;
import de.mhus.osgi.sop.api.registry.RegistryValue;

@Component(property="path=/system/mutex/")
public class RegistryMutex implements RegistryPathControl {

//    private ThreadLocal<String> working = new ThreadLocal<>();
    
	@Override
	public RegistryValue checkSetParameter(RegistryManager manager, RegistryValue value) {
        if (value.getPath().endsWith(RegistryUtil.VALUE_VARNAME)) {
            System.out.println("### Path: " + value.getPath());
            MThread.asynchron(new Runnable() {
                
                @Override
                public void run() {
                    String name = value.getPath();
                    name = name.substring(14, name.length() - RegistryUtil.VALUE_VARNAME.length());
                    System.out.println("### Fire: " + name);
                    RegistryClusterApiImpl.instance().fireEventLocal(name, value.getValue());
                }
            });
            return value;
        }
		if (value.getPath().endsWith(RegistryUtil.MASTER_VARNAME)) {
		    
//		    if (working.get() != null)
//		        return value;
		    
			RegistryValue cur = manager.getParameter(value.getPath());
			if (cur != null && !cur.getSource().equals(manager.getServerIdent()))
				return null; // can't overwrite other locally
    		long mySeed = M.l(MRandom.class).getLong();
    		if (cur != null)
    		    mySeed = MCast.tolong(cur.getValue(), Long.MIN_VALUE);
    		return new RegistryValue(String.valueOf(mySeed), value.getSource(), value.getUpdated(), value.getPath(), Math.max(60000, value.getTimeout()), false, false);
		}
		return value;
	}

	@Override
	public boolean checkRemoveParameter(RegistryManager manager, RegistryValue value) {
	    if (value.getPath().endsWith(RegistryUtil.MASTER_VARNAME)) {
	        if (value.getSource().equals(manager.getServerIdent())) {
	            return true;
	        }
	    }
		return false;
	}

	@Override
	public RegistryValue checkSetParameterFromRemote(RegistryManager manager, RegistryValue value) {
		if (value.getPath().endsWith(RegistryUtil.MASTER_VARNAME)) {
			RegistryValue cur = manager.getParameter(value.getPath());
			long theirSeed = MCast.tolong(value.getValue(), Long.MIN_VALUE);
			if (cur == null) {
//				// create my own seed
//				long mySeed = M.l(MRandom.class).getLong();
//				if (theirSeed < mySeed) {
//				    working.set("");
//				    try {
//				        manager.setParameter(value.getPath(), String.valueOf(mySeed), value.getTimeout(), false, false, false);
//				    } finally {
//				        working.remove();
//                    }
//					return null;
//				}
			    // accept their seed by default
			    return value;
			} else {
				long mySeed = MCast.tolong(cur.getValue(), Long.MIN_VALUE);
				if (theirSeed < mySeed) return null;
			}
		}
		return value;
	}

	@Override
	public boolean checkRemoveParameterFromRemote(RegistryManager manager, RegistryValue value) {
		return true;
	}

	@Override
	public boolean isTakeControl(String path) {
		return true;
	}

}