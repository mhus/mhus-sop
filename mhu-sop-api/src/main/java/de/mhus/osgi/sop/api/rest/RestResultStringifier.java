package de.mhus.osgi.sop.api.rest;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;

public class RestResultStringifier {

    private RestResult result;

    private static ObjectMapper mapper = new ObjectMapper();
    static {
        DefaultPrettyPrinter.Indenter indenter = 
                new DefaultIndenter("    ", DefaultIndenter.SYS_LF);
        DefaultPrettyPrinter printer = new DefaultPrettyPrinter();
        printer.indentObjectsWith(indenter);
        printer.indentArraysWith(indenter);
    }
    
    public RestResultStringifier(RestResult result) {
        this.result = result;
    }
    
    @Override
    public String toString() {
        if (result == null) return "null";
        StringBuilder sb = new StringBuilder();
        
        sb.append("=== REST Result === ").append(result.getContentType()).append("\n");
        
        if (result instanceof JsonResult) {
            JsonResult r = (JsonResult)result;
            JsonNode json = r.getJson();
            if (json == null) {
                sb.append("null");
            } else {
                try {
                    sb.append(mapper.writeValueAsString(json));
                } catch (Exception e) {
                    sb.append(e.toString());
                }
            }
        } else {
            sb.append(result.getClass().getCanonicalName());
        }
        
        
        return sb.toString();
    }

}
