package de.mhus.osgi.sop.impl.registry;

import aQute.bnd.annotation.component.Component;
import de.mhus.lib.core.MApi;
import de.mhus.lib.core.MCast;
import de.mhus.lib.core.crypt.MRandom;
import de.mhus.osgi.sop.api.registry.RegistryManager;
import de.mhus.osgi.sop.api.registry.RegistryPathControl;
import de.mhus.osgi.sop.api.registry.RegistryValue;

@Component(properties="path=/system/master/")
public class RegistryUnique implements RegistryPathControl {

	@Override
	public RegistryValue checkSetParameter(RegistryManager manager, RegistryValue value) {
		if (value.getPath().endsWith("@seed")) {
			RegistryValue cur = manager.getNodeParameter(value.getPath());
			if (cur != null)
				return null; // can't overwrite locally
		}
		long mySeed = MApi.lookup(MRandom.class).getLong();
		return new RegistryValue(String.valueOf(mySeed), value.getSource(), value.getUpdated(), value.getPath(), Math.max(60000, value.getTimeout()), false, false);
	}

	@Override
	public boolean checkRemoveParameter(RegistryManager manager, RegistryValue value) {
		return false;
	}

	@Override
	public RegistryValue checkSetLocalParameter(RegistryManager manager, RegistryValue value) {
		if (value.getPath().endsWith("@seed")) {
			RegistryValue cur = manager.getNodeParameter(value.getPath());
			long theirSeed = MCast.tolong(value.getValue(), Long.MIN_VALUE);
			if (cur == null) {
				// create my own seed
				long mySeed = MApi.lookup(MRandom.class).getLong();
				if (theirSeed < mySeed) {
					manager.setParameter(value.getPath(), String.valueOf(mySeed), value.getTimeout(), false, false, false);
					return null;
				}
			} else {
				long mySeed = MCast.tolong(cur.getValue(), Long.MIN_VALUE);
				if (theirSeed < mySeed) return null;
			}
		}
		return value;
	}

	@Override
	public boolean checkRemoveLocalParameter(RegistryManager manager, RegistryValue value) {
		return true;
	}

}
