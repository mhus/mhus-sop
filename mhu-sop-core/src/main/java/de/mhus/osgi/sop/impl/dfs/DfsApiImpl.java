package de.mhus.osgi.sop.impl.dfs;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;

import aQute.bnd.annotation.component.Component;
import de.mhus.lib.core.MApi;
import de.mhus.lib.core.MLog;
import de.mhus.lib.core.util.MUri;
import de.mhus.lib.errors.MException;
import de.mhus.osgi.sop.api.dfs.DfsApi;
import de.mhus.osgi.sop.api.dfs.DfsProviderOperation;
import de.mhus.osgi.sop.api.dfs.FileInfo;
import de.mhus.osgi.sop.api.operation.OperationApi;
import de.mhus.osgi.sop.api.operation.OperationDescriptor;
import de.mhus.osgi.sop.api.operation.OperationUtil;

@Component
public class DfsApiImpl extends MLog implements DfsApi {

	@Override
	public FileInfo getFileInfo(MUri uri) {
		if ( DfsApi.SCHEME_DFQ.equals(uri.getScheme())) {
			try {
				return FileQueueApiImpl.instance.getFileInfo(uri);
			} catch (IOException | MException e) {
				log().w(uri,e);
				return null;
			}
		}
		OperationApi api = MApi.lookup(OperationApi.class);
		
		LinkedList<String> tags = new LinkedList<>();
		if (uri.getLocation() != null)
			tags.add(OperationDescriptor.TAG_IDENT + "=" + uri.getLocation());
		for (OperationDescriptor desc : api.findOperations(DfsProviderOperation.class.getCanonicalName(), null, tags)) {
			if (uri.getScheme().equals(desc.getParameter(DfsProviderOperation.PARAM_SCHEME))) {
				try {
					DfsProviderOperation provider = OperationUtil.createOpertionProxy(DfsProviderOperation.class, desc);
					FileInfo info = provider.getFileInfo(uri);
					if (info != null) return info;
				} catch (Throwable t) {
					log().w(desc,t);
				}
			}
		}
		return null;
	}

	@Override
	public MUri exportFile(MUri uri) {
		if ( DfsApi.SCHEME_DFQ.equals(uri.getScheme())) {
			return uri;
		}
		OperationApi api = MApi.lookup(OperationApi.class);
		
		LinkedList<String> tags = new LinkedList<>();
		if (uri.getLocation() != null)
			tags.add(OperationDescriptor.TAG_IDENT + "=" + uri.getLocation());
		for (OperationDescriptor desc : api.findOperations(DfsProviderOperation.class.getCanonicalName(), null, tags)) {
			if (uri.getScheme().equals(desc.getParameter(DfsProviderOperation.PARAM_SCHEME))) {
				try {
					DfsProviderOperation provider = OperationUtil.createOpertionProxy(DfsProviderOperation.class, desc);
					MUri out = provider.exportFile(uri);
					if (out != null) return out;
				} catch (Throwable t) {
					log().w(desc,t);
				}
			}
		}
		return null;
	}

	@Override
	public Map<String, MUri> getDirectoryList(MUri uri) {
		if ( DfsApi.SCHEME_DFQ.equals(uri.getScheme())) 
			return null;
		OperationApi api = MApi.lookup(OperationApi.class);
		LinkedList<String> tags = new LinkedList<>();
		if (uri.getLocation() != null)
			tags.add(OperationDescriptor.TAG_IDENT + "=" + uri.getLocation());
		for (OperationDescriptor desc : api.findOperations(DfsProviderOperation.class.getCanonicalName(), null, tags)) {
			if (uri.getScheme().equals(desc.getParameter(DfsProviderOperation.PARAM_SCHEME))) {
				try {
					DfsProviderOperation provider = OperationUtil.createOpertionProxy(DfsProviderOperation.class, desc);
					Map<String, MUri> out = provider.getDirectoryList(uri);
					if (out != null) return out;
				} catch (Throwable t) {
					log().w(desc,t);
				}
			}
		}
		return null;
	}

	@Override
	public Collection<String> listProviders() {
		LinkedList<String> out = new LinkedList<>();
		OperationApi api = MApi.lookup(OperationApi.class);
		for (OperationDescriptor desc : api.findOperations(DfsProviderOperation.class.getCanonicalName(), null, null)) {
			out.add(desc.getParameter(DfsProviderOperation.PARAM_SCHEME) + "://" + OperationUtil.getOption(desc.getTags(), OperationDescriptor.TAG_IDENT, "" ) );
		}
		return out;
	}

	@Override
	public void importFile(MUri queueUri, MUri target) throws IOException {
		if ( DfsApi.SCHEME_DFQ.equals(target.getScheme())) 
			return;
		OperationApi api = MApi.lookup(OperationApi.class);
		LinkedList<String> tags = new LinkedList<>();
		if (target.getLocation() != null)
			tags.add(OperationDescriptor.TAG_IDENT + "=" + target.getLocation());
		for (OperationDescriptor desc : api.findOperations(DfsProviderOperation.class.getCanonicalName(), null, tags)) {
			if (target.getScheme().equals(desc.getParameter(DfsProviderOperation.PARAM_SCHEME))) {
				try {
					DfsProviderOperation provider = OperationUtil.createOpertionProxy(DfsProviderOperation.class, desc);
					provider.importFile(queueUri, target);
					return;
				} catch (Throwable t) {
					log().w(desc,t);
				}
			}
		}
		
	}

	@Override
	public void deleteFile(MUri uri) throws IOException {
		if ( DfsApi.SCHEME_DFQ.equals(uri.getScheme())) 
			return;
		OperationApi api = MApi.lookup(OperationApi.class);
		LinkedList<String> tags = new LinkedList<>();
		if (uri.getLocation() != null)
			tags.add(OperationDescriptor.TAG_IDENT + "=" + uri.getLocation());
		for (OperationDescriptor desc : api.findOperations(DfsProviderOperation.class.getCanonicalName(), null, tags)) {
			if (uri.getScheme().equals(desc.getParameter(DfsProviderOperation.PARAM_SCHEME))) {
				try {
					DfsProviderOperation provider = OperationUtil.createOpertionProxy(DfsProviderOperation.class, desc);
					provider.deleteFile(uri);
					return;
				} catch (Throwable t) {
					log().w(desc,t);
				}
			}
		}
	}

	@Override
	public void createDirecories(MUri uri) throws IOException {
		if ( DfsApi.SCHEME_DFQ.equals(uri.getScheme())) 
			return;
		OperationApi api = MApi.lookup(OperationApi.class);
		LinkedList<String> tags = new LinkedList<>();
		if (uri.getLocation() != null)
			tags.add(OperationDescriptor.TAG_IDENT + "=" + uri.getLocation());
		for (OperationDescriptor desc : api.findOperations(DfsProviderOperation.class.getCanonicalName(), null, tags)) {
			if (uri.getScheme().equals(desc.getParameter(DfsProviderOperation.PARAM_SCHEME))) {
				try {
					DfsProviderOperation provider = OperationUtil.createOpertionProxy(DfsProviderOperation.class, desc);
					provider.createDirecories(uri);
					return;
				} catch (Throwable t) {
					log().w(desc,t);
				}
			}
		}
	}

}
