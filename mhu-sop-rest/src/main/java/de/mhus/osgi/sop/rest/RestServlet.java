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

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.osgi.service.component.annotations.Component;

import de.mhus.lib.core.IReadProperties;
import de.mhus.lib.core.M;
import de.mhus.lib.core.MApi;
import de.mhus.lib.core.MProperties;
import de.mhus.lib.core.MString;
import de.mhus.lib.core.io.http.MHttp;
import de.mhus.lib.core.logging.LevelMapper;
import de.mhus.lib.core.logging.Log;
import de.mhus.lib.core.logging.MLogUtil;
import de.mhus.lib.core.logging.TrailLevelMapper;
import de.mhus.lib.core.util.Base64;
import de.mhus.lib.core.util.MNls;
import de.mhus.lib.core.util.MUri;
import de.mhus.lib.errors.AccessDeniedException;
import de.mhus.osgi.sop.api.aaa.AaaContext;
import de.mhus.osgi.sop.api.aaa.AccessApi;
import de.mhus.osgi.sop.api.aaa.Trust;
import de.mhus.osgi.sop.api.rest.CallContext;
import de.mhus.osgi.sop.api.rest.HttpRequest;
import de.mhus.osgi.sop.api.rest.Node;
import de.mhus.osgi.sop.api.rest.RestApi;
import de.mhus.osgi.sop.api.rest.RestResult;
import de.mhus.osgi.sop.api.util.SopFileLogger;
import de.mhus.osgi.sop.api.util.TicketUtil;

/*
 * Test: http://localhost:8182/rest/public/?_action=ping&_method=POST
 */
@Component(immediate=true,name="RestServlet",service=Servlet.class,property="alias=/rest/*")
public class RestServlet extends HttpServlet {

	static Log trace = new SopFileLogger("rest", "rest_trace");

    private static final String METHOD_DELETE = "DELETE";
//    private static final String METHOD_HEAD = "HEAD";
    private static final String METHOD_GET = "GET";
//    private static final String METHOD_OPTIONS = "OPTIONS";
    private static final String METHOD_POST = "POST";
    private static final String METHOD_PUT = "PUT";
    private static final String METHOD_TRACE = "TRACE";

    private static final String RESULT_TYPE_JSON = "json";
    private static final String RESULT_TYPE_HTTP = "http";
    
    private static final String PUBLIC_PATH = "/public/";
        
	/**
	 * 
	 */
    private static final Log log = Log.getLog(RestServlet.class);
	private static final long serialVersionUID = 1L;
	@SuppressWarnings("unused")
	private MNls nls = MNls.lookup(this);
	
	private int nextId = 0;

    @Override
	protected void service(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
    	// System.out.println(">>> " + req.getPathInfo());    	
    	resp.setHeader("Access-Control-Allow-Origin", "*");

    	boolean isTrailEnabled = false;
    	try {
	    	String trail = req.getParameter("_trace");
	    	if (trail != null) {
	    		LevelMapper lm = MApi.get().getLogFactory().getLevelMapper();
	    		if (lm != null && lm instanceof TrailLevelMapper) {
	    			isTrailEnabled = true;
	    			if (trail.length() == 0) trail = MLogUtil.MAP_LABEL;
	    			((TrailLevelMapper)lm).doConfigureTrail(MLogUtil.TRAIL_SOURCE_REST, trail);
	    		}
	    	}
	    	
	    	String errorResultType = req.getParameter("_errorResult");
	    	if (errorResultType == null) errorResultType = RESULT_TYPE_JSON;
	    	
	    	long id = newId();
	    	
	    	
	    	String path = req.getPathInfo();
	    	
	    	if (path == null || path.length() < 1) {
	    		resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
	    		return;
	    	}
	    	List<String> parts = new LinkedList<String>(Arrays.asList(path.split("/")));
	    	if (parts.size() == 0) return;
	    	parts.remove(0); // [empty]
	//    	parts.remove(0); // rest
	    	
	    	String ticket = req.getParameter("_ticket");
	    	if (MString.isEmpty(ticket)) {
		    	String auth = req.getHeader("Authorization");  
		        // Do we allow that user?
		    	ticket = getTicket(auth);
		        if (!path.startsWith(PUBLIC_PATH) && MString.isEmpty(ticket)) {  
		        		log.i("authorization required",id,auth,req.getRemoteAddr());
		            // Not allowed, so report he's unauthorized  
		            resp.setHeader("WWW-Authenticate", "BASIC realm=\"rest\"");  
		            sendError(errorResultType, id, resp, HttpServletResponse.SC_UNAUTHORIZED,"", null, null);
		            return;
		        }
	    	}
	        if ("true".equals(req.getParameter("_admin")))
	        	ticket = ticket + TicketUtil.SEP + "admin";
	        
	        MProperties context = new MProperties();
	        String method = req.getParameter("_method");
	        if (method == null) method = req.getMethod();
	
	        logAccess(id,req.getRemoteAddr(),req.getRemotePort(),ticket,method,req.getPathInfo(),req.getParameterMap());
	
			CallContext callContext = new CallContext(new HttpRequest(req.getParameterMap()), MHttp.toMethod(method), context);
	        
	        RestApi restService = M.l(RestApi.class);
	        
	        RestResult res = null;
	        
	        AccessApi access = M.l(AccessApi.class);
	        AaaContext user = null;
	        if (MString.isEmpty(ticket) && path.startsWith(PUBLIC_PATH)) {
	        		if (access != null)
	        			user = access.getGuestContext();
	        } else {
		        try {
			        	String localeStr = req.getHeader("Accept-Language");
			        	Locale locale = localeStr == null ? null : Locale.forLanguageTag(localeStr);
			        	user = access.process(ticket, locale);
		        } catch (AccessDeniedException e) {
		//	        	log.d("access denied",ticket,e);
		            resp.setHeader("WWW-Authenticate", "BASIC realm=\"rest\"");  
		            sendError(errorResultType, id, resp, HttpServletResponse.SC_UNAUTHORIZED,e.getMessage(), e, null);
		            return;
		        } catch (Throwable t) {
			        	sendError(errorResultType, id, resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, t.getMessage(), t, null );
			        	return;
		        }
		        
		        if (user == null) { // paranoia, should throw an exception in 'process()'
			        	resp.setHeader("WWW-Authenticate", "BASIC realm=\"rest\"");  
			        	sendError(errorResultType, id, resp, HttpServletResponse.SC_UNAUTHORIZED,"?", null, null);
			        	return;
		        }
	        }
	        if (user != null) {
		        	Trust trust = user.getTrust();
		        	if (trust != null) {
		        		IReadProperties trustProp = trust.getProperties();
		        		if (user.isAdminMode() && !trustProp.getBoolean("allowAdmin", true)) {
		    	            sendError(errorResultType, id, resp, HttpServletResponse.SC_UNAUTHORIZED,"admin", null, null);
		    	            return;
		        		}
		        		String hostsStr = trustProp.getString("allowedHosts",null);
		        		if (hostsStr != null) {
		        			String[] hosts = hostsStr.split(",");
		        			String remote = req.getRemoteHost();
		        			boolean allowed = false;
		        			for (String pattern : hosts) {
		        				if (pattern.matches(remote)) {
		        					allowed = true;
		        					break;
		        				}
		        			}
		        			if (!allowed) {
		        	            sendError(errorResultType, id, resp, HttpServletResponse.SC_UNAUTHORIZED,"Host " + remote, null, null);
		        	            return;
		        			}
		        		}
		        	}
	        }
	        
	        try {
		        Node item = restService.lookup(parts, null, callContext);
		        
		    	if (item == null) {
		            sendError(errorResultType, id, resp, HttpServletResponse.SC_NOT_FOUND,"Resource Not Found", null, user == null ? "?" : user.getAccountId());
		    		return;
		    	}
		    	
		        if (method.equals(METHOD_GET)) {
		        	res = item.doRead(callContext);
		        } else 
		        if (method.equals(METHOD_POST)) {

		        	if (callContext.hasAction())
		        		res = item.doAction(callContext);
		        	else
		        		res = item.doCreate(callContext);
		        } else
		        if (method.equals(METHOD_PUT)) {
		        	res = item.doUpdate(callContext);
		        } else 
		        if (method.equals(METHOD_DELETE)) {
		        	res = item.doDelete(callContext);
		        } else 
		        if (method.equals(METHOD_TRACE)) {
		        	
		        }
	        
	        if (res == null) {
	            sendError(errorResultType, id, resp, HttpServletResponse.SC_NOT_IMPLEMENTED, null, null, user == null ? "?" : user.getAccountId() );
	            return;
	        }
	        
	        try {
		        if (res != null) {
		        	log.d("result",id,res);
		        	trace.i("result",id,res);
		        	resp.setContentType(res.getContentType());
		        	res.write(resp.getWriter());
		        }
	        } catch (Throwable t) {
	        	log.d(t);
	            sendError(errorResultType, id, resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, t.getMessage(), t, user == null ? "?" : user.getAccountId() );
	        	return;
	        }
	        
	        } catch (Throwable t) {
	        	log.d(t);
	        	sendError(errorResultType, id, resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, t.getMessage(), t, user == null ? "?" : user.getAccountId() );
	        	return;
	        } finally {
	        	if (access != null)
	        		access.release(ticket);
	        }
	        
    	} finally {
    		if (isTrailEnabled) {
	    		LevelMapper lm = MApi.get().getLogFactory().getLevelMapper();
	    		if (lm != null && lm instanceof TrailLevelMapper)
	    			((TrailLevelMapper)lm).doResetTrail();
    		}
    	}
    }
    
    private void logAccess(long id, String remoteAddr, int remotePort,
			String ticket, String method, String pathInfo, @SuppressWarnings("rawtypes") Map parameterMap) {

    	String paramLog = getParameterLog(parameterMap);
    	trace.i("access",id,remoteAddr,remotePort,getTicketLog(ticket), method, pathInfo, paramLog);
    	log.d("access",id,
    	        "\n Remote: " + remoteAddr + ":" + remotePort + 
    	        "\n Ticket: " + getTicketLog(ticket) + 
    	        "\n Method: " + method + 
    	        "\n Request: " + pathInfo + 
    	        "\n Parameters: " + paramLog + "\n" );
	}

	private String getParameterLog(Map<?,?> parameterMap) {
		StringBuilder out = new StringBuilder().append('{');
		for (Map.Entry<?,?> entry : parameterMap.entrySet()) {
			out.append('\n').append(entry.getKey()).append("=[");
			Object val = entry.getValue();
			if (val == null) {
			} else
			if (val.getClass().isArray()) {
				boolean first = true;
				Object[] arr = (Object[])val;
				for (Object o : arr) {
					if (first) first = false; else out.append(',');
					out.append(o);
				}
			} else {
				out.append(val);
			}
			out.append("] ");
		}
		out.append('}');
		return out.toString();
	}

	private String getTicketLog(String ticket) {
//		if (ticket == null || !ticket.startsWith("acc,") || ticket.length() < 10) return ticket;
//		int p = ticket.indexOf(',', 9);
//		if (p < 0) return ticket;
//		return ticket.substring(0,p);
		return ticket;
	}

	private synchronized long newId() {
		return nextId ++;
	}

	private void sendError(String error, long id, HttpServletResponse resp,
			int errNr, String errMsg, Throwable t, String user) throws IOException {
		
		trace.e("error",id, errNr,errMsg, t);
		log.d("error",id, errNr,errMsg, t);
		
        if (error.equals(RESULT_TYPE_HTTP)) {
        	resp.sendError(errNr);  
        	resp.getWriter().print(errMsg);
        	return;
        }

        if (error.equals(RESULT_TYPE_JSON)) {

        	if (errNr == HttpServletResponse.SC_UNAUTHORIZED)
        		resp.setStatus(errNr);
        	else
        		resp.setStatus(HttpServletResponse.SC_OK);
        	
        	PrintWriter w = resp.getWriter();
        	ObjectMapper m = new ObjectMapper();

        	ObjectNode json = m.createObjectNode();
        	json.put("_sequence", id);
        	if (user != null)
        		json.put("_user",  user);
    		LevelMapper lm = MApi.get().getLogFactory().getLevelMapper();
    		if (lm != null && lm instanceof TrailLevelMapper)
    			json.put("_trail",((TrailLevelMapper)lm).getTrailId());
        	json.put("_error", errNr);
        	json.put("_errorMessage", errMsg);
        	resp.setContentType("application/json");
    		m.writeValue(w,json);

        	return;
        }
	}

	private String getTicket(String auth) {

		if (auth == null) return null;
        if (!auth.toUpperCase().startsWith("BASIC ")) {   
            return null;  // we only do BASIC  
        }  
        // Get encoded user and password, comes after "BASIC "  
        String userpassEncoded = auth.substring(6);  
        // Decode it, using any base 64 decoder  
        String userpassDecoded = new String( Base64.decode(userpassEncoded) );
        // Check our user list to see if that user and password are "allowed"
        String[] parts = userpassDecoded.split(":",2);
        
        String account = null;
        String pass = null;
        if (parts.length > 0) account = MUri.decode(parts[0]);
        if (parts.length > 1) pass = MUri.decode(parts[1]);
        	
        return TicketUtil.createTicket(account, pass);
        
	}
	

}
