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
package de.mhus.osgi.sop.api.operation;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import de.mhus.lib.core.MApi;
import de.mhus.lib.core.MCast;
import de.mhus.lib.core.MProperties;
import de.mhus.lib.core.MString;
import de.mhus.lib.core.strategy.OperationResult;
import de.mhus.lib.core.strategy.OperationToIfcProxy;
import de.mhus.lib.core.util.VersionRange;
import de.mhus.lib.errors.MException;
import de.mhus.lib.errors.MRuntimeException;

public class OperationUtil {

	public static boolean matches(OperationDescriptor desc, String filter, VersionRange version, Collection<String> providedTags) {
		return  (filter == null || MString.compareFsLikePattern(desc.getPath(), filter)) 
				&& 
				(version == null || version.includes(desc.getVersion())) 
				&& 
				(providedTags == null || desc.compareTags(providedTags));
	}
	
	public static boolean isOption(String[] options, String opt) {
		if (options == null || opt == null) return false;
		for (String o : options)
			if (opt.equals(o)) return true;
		return false;
	}

	public static int getOption(String[] options, String opt, int def) {
		if (options == null || opt == null) return def;
		opt = opt + "=";
		for (String o : options)
			if (o != null && o.startsWith(opt)) return MCast.toint(o.substring(opt.length()), def);
		return def;
	}

	public static long getOption(String[] options, String opt, long def) {
		if (options == null || opt == null) return def;
		opt = opt + "=";
		for (String o : options)
			if (o != null && o.startsWith(opt)) return MCast.tolong(o.substring(opt.length()), def);
		return def;
	}

	public static String getOption(String[] options, String opt, String def) {
		if (options == null || opt == null) return def;
		opt = opt + "=";
		for (String o : options)
			if (o != null && o.startsWith(opt)) return o.substring(opt.length());
		return def;
	}

	public static String getOption(Collection<String> options, String opt, String def) {
		if (options == null || opt == null) return def;
		opt = opt + "=";
		for (String o : options)
			if (o != null && o.startsWith(opt)) return o.substring(opt.length());
		return def;
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T createOpertionProxy(Class<T> ifc, OperationDescriptor desc) throws MException {
		if (!desc.getPath().equals(ifc.getName()))
			throw new MException("Interface and operation do not match", ifc.getName(), desc.getName() );
		
		return (T)Proxy.newProxyInstance(ifc.getClassLoader(), new Class[] {ifc}, new OperationInvocationHandler(ifc,desc));
		
	}
	
	private static class OperationInvocationHandler implements InvocationHandler {

		@SuppressWarnings("unused")
		private Class<?> ifc;
		private OperationDescriptor desc;

		public OperationInvocationHandler(Class<?> ifc, OperationDescriptor desc) {
			this.ifc = ifc;
			this.desc = desc;
		}

		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			
			OperationApi api = MApi.lookup(OperationApi.class);

			MProperties properties = new MProperties();
			properties.setString(OperationToIfcProxy.METHOD, method.getName());
			int pcount = method.getParameterCount();
			for (int i = 0; i < pcount; i++) {
				if (args[i] != null) {
					properties.put(OperationToIfcProxy.PARAMETER + i, MCast.serializeToString(args[i]));
//					properties.put(OperationToIfcProxy.TYPE + i, method.getParameters()[i].getType().getCanonicalName() );
					properties.put(OperationToIfcProxy.TYPE + i, OperationToIfcProxy.SERIALISED );
					properties.put(OperationToIfcProxy.PARAMETERTYPE + i, args[i].getClass().getCanonicalName() );
				}
			}
			
			OperationResult res = api.doExecute(desc, properties);
			
			if (res == null)
				throw new NullPointerException();
			
			if (!res.isSuccessful())
				throw new MRuntimeException(res.getMsg());
			
			return res.getResult();
		}
		
	}

	public static Map<String,String> getParameters(OperationDescriptor desc) {
		TreeMap<String,String> out = new TreeMap<>();
		for (String key : desc.getParameterKeys())
			out.put(key, desc.getParameter(key));
		return out;
	}

	public static <T> T getOperationIfc(Class<T> ifc) throws MException {
		OperationApi api = MApi.lookup(OperationApi.class);
		OperationDescriptor desc = api.findOperation(ifc.getCanonicalName(), null, null);
		return createOpertionProxy(ifc, desc);
	}
	
}
