package de.mhus.osgi.sop.api.rest;

import org.codehaus.jackson.node.ObjectNode;

import de.mhus.osgi.sop.api.rest.JsonResult;

public class ErrorJsonResult extends JsonResult {

    public ErrorJsonResult(int rc, String msg) {
        ObjectNode out = createObjectNode();
        out.put("successful", false);
        out.put("rc", rc);
        if (msg != null)
            out.put("msg", msg);
    }

}
