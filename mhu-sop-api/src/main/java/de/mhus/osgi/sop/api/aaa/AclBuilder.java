package de.mhus.osgi.sop.api.aaa;

import java.util.LinkedList;

import de.mhus.lib.basics.Ace;
import de.mhus.lib.core.MString;

public class AclBuilder {
	
	private LinkedList<String> list = new LinkedList<>();

	public AclBuilder addGroup(String name, String right) {
		list.add(normalize(name) + AccessApi.ACCESS_IS + normalize(right));
		return this;
	}

	public AclBuilder addNotGroup(String name, String right) {
		list.add(AccessApi.ACCESS_NOT + normalize(name) + AccessApi.ACCESS_IS + normalize(right));
		return this;
	}

	public AclBuilder addUser(String name, String right) {
		list.add(AccessApi.ACCESS_USER + normalize(name) + AccessApi.ACCESS_IS + normalize(right));
		return this;
	}

	public AclBuilder addNotUser(String name, String right) {
		list.add(AccessApi.ACCESS_NOT_USER + normalize(name) + AccessApi.ACCESS_IS + normalize(right));
		return this;
	}

	// --
	
	public AclBuilder addGroup(String name, Ace right) {
		list.add(normalize(name) + AccessApi.ACCESS_IS + right);
		return this;
	}

	public AclBuilder addNotGroup(String name, Ace right) {
		list.add(AccessApi.ACCESS_NOT + normalize(name) + AccessApi.ACCESS_IS + right);
		return this;
	}

	public AclBuilder addUser(String name, Ace right) {
		list.add(AccessApi.ACCESS_USER + normalize(name) + AccessApi.ACCESS_IS + right);
		return this;
	}

	public AclBuilder addNotUser(String name, Ace right) {
		list.add(AccessApi.ACCESS_NOT_USER + normalize(name) + AccessApi.ACCESS_IS + right);
		return this;
	}

	@Override
	public String toString() {
		return MString.join(list.iterator(), AccessApi.ACCESS_SEPARATOR);
	}
	
	private String normalize(String in) {
		if (in.indexOf('=') >= 0)
			in = in.replace('=', '_');
		if (in.indexOf(':') >= 0)
			in = in.replace(':', '_');
		if (in.indexOf(',') >= 0)
			in = in.replace(',', '_');
		return in;
	}
}
