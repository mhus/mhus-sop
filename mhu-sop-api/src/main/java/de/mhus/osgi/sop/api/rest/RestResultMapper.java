/**
 * Copyright 2018 Mike Hummel
 *
 * <p>Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
                JsonResult r = (JsonResult) result;
                JsonNode json = r.getJson();
                if (json == null) {
                    sb.append("null");
                } else {
                    try {
                        sb.append(
                                mapper.writer()
                                        .withDefaultPrettyPrinter()
                                        .writeValueAsString(json));
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
        if (in instanceof RestResult) return new RestResultStringifier((RestResult) in);
        return null;
    }
}
