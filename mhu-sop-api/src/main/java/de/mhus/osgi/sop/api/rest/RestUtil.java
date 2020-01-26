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

import java.io.IOException;
import java.util.LinkedList;
import java.util.UUID;

import de.mhus.lib.adb.DbCollection;
import de.mhus.lib.adb.DbMetadata;
import de.mhus.lib.adb.query.AQuery;
import de.mhus.lib.annotations.generic.Public;
import de.mhus.lib.core.IProperties;
import de.mhus.lib.core.M;
import de.mhus.lib.core.pojo.PojoAttribute;
import de.mhus.lib.core.pojo.PojoModel;
import de.mhus.lib.core.pojo.PojoModelFactory;
import de.mhus.lib.core.pojo.PojoParser;
import de.mhus.lib.errors.MException;
import de.mhus.lib.xdb.XdbService;
import de.mhus.osgi.sop.api.adb.AdbApi;

public class RestUtil {

    private static final int PAGE_SIZE = 1000;

    public static final int MAX_RETURN_SIZE = 1000;

    private static final PojoModelFactory POJO_FACTORY =
            new PojoModelFactory() {

                @SuppressWarnings("unchecked")
                @Override
                public PojoModel createPojoModel(Class<?> clazz) {
                    return new PojoParser()
                            .parse(clazz, "_", new Class[] {Public.class})
                            .filter(true, false, true, false, true)
                            .getModel();
                }
            };

    //	private static Log log = Log.getLog(RestUtil.class);

    public static void updateObject(CallContext callContext, Object obj, boolean publicOnly)
            throws IOException {
        PojoModelFactory schema = M.l(AdbApi.class).getManager().getPojoModelFactory();

        PojoModel model = schema.createPojoModel(obj.getClass());
        for (String name : callContext.getParameterNames()) {
            @SuppressWarnings("unchecked")
            PojoAttribute<Object> attr = model.getAttribute(name);
            if (attr != null) {
                Public p = attr.getAnnotation(Public.class);
                if (!publicOnly || p != null && p.readable() && p.writable()) {
                    // set
                    attr.set(obj, callContext.getParameter(name));
                }
            }
        }
    }

    public static void updateObject(IProperties props, Object obj, boolean publicOnly)
            throws IOException {
        PojoModelFactory schema = M.l(AdbApi.class).getManager().getPojoModelFactory();

        PojoModel model = schema.createPojoModel(obj.getClass());
        for (String name : props.keys()) {
            @SuppressWarnings("unchecked")
            PojoAttribute<Object> attr = model.getAttribute(name);
            if (attr != null) {
                Public p = attr.getAnnotation(Public.class);
                if (!publicOnly || p != null && p.readable() && p.writable()) {
                    // set
                    attr.set(obj, props.get(name));
                }
            }
        }
    }

    public static String getObjectIdParameterName(Class<? extends DbMetadata> clazz) {
        return clazz.getSimpleName().toLowerCase() + "Id";
    }

    public static UUID getObjectUuid(CallContext callContext, Class<? extends DbMetadata> clazz) {
        return UUID.fromString(callContext.getParameter(getObjectIdParameterName(clazz)));
    }

    public static String getObjectId(CallContext callContext, Class<? extends DbMetadata> clazz) {
        return callContext.getParameter(getObjectIdParameterName(clazz));
    }

    //	public static RestResult doExecuteRestAction(CallContext callContext, OperationDescriptor
    // descriptor, String source) {
    //		// TODO Auto-generated method stub
    //		return null;
    //	}

    //	public static int getPageFromSearch(String search) {
    //		if (MString.isEmpty(search) || !search.startsWith("page:"))
    //			return 0;
    //		search = search.substring(5);
    //		if (search.indexOf(',') >= 0)
    //			return MCast.toint(MString.beforeIndex(search, ','), 0);
    //		return MCast.toint(search, 0);
    //	}
    //
    //	public static String getFilterFromSearch(String search) {
    //		if (MString.isEmpty(search))
    //			return null;
    //		if (search.startsWith("page:")) {
    //			if (search.indexOf(',') >= 0)
    //				return MString.afterIndex(search, ',');
    //			return null;
    //		}
    //		return search;
    //	}

    public static <T> LinkedList<T> collectResults(
            XdbService service, AQuery<T> query, int page, int size) throws MException {
        LinkedList<T> list = new LinkedList<T>();
        if (page < 0) return list;
        DbCollection<T> res = service.getByQualification(query);
        if (size <= 0) size = PAGE_SIZE;
        else size = Math.min(PAGE_SIZE, size);
        if (!res.skip(page * size)) return list;
        while (res.hasNext()) {
            list.add(res.next());
            if (list.size() >= size) break;
        }
        res.close();
        return list;
    }

    public static PojoModelFactory getPojoModelFactory() {
        return POJO_FACTORY;
    }
}
