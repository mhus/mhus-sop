package de.mhus.osgi.sop.api.rest;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

import de.mhus.lib.core.logging.ParameterEntryMapper;

public class RestResultMapper implements ParameterEntryMapper {


    private static ObjectMapper mapper = new ObjectMapper();
    
    static class RestResultStringifier {
        
        private RestResult result;
        
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
                        sb.append(mapper.writer().withDefaultPrettyPrinter().writeValueAsString(json));
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

    @Override
    public Object map(Object in) {
        if (in instanceof RestResult)
            return new RestResultStringifier((RestResult)in);
        return null;
    }
    
}
