package de.mhus.osgi.sop.api;

import java.util.List;

import de.mhus.lib.core.IProperties;
import de.mhus.lib.core.strategy.OperationResult;
import de.mhus.lib.core.util.MCallback2;
import de.mhus.lib.core.util.VersionRange;
import de.mhus.lib.errors.MException;
import de.mhus.lib.errors.NotFoundException;
import de.mhus.osgi.sop.api.operation.OperationDescriptor;
import de.mhus.osgi.sop.api.operation.OperationUtil;
import de.mhus.osgi.sop.api.operation.OperationsSelector;
import de.mhus.osgi.sop.api.operation.Selector;

/**
 * This is a short cut utilitiy class to provide Sop tools.
 *  
 * @author mikehummel
 *
 */
public class S {

    /**
     * Execute a operation
     * @param selector 
     * @param properties 
     * @return results
     * @throws NotFoundException 
     */
    public static OperationResult e(OperationsSelector selector, IProperties properties) throws NotFoundException {
        return OperationUtil.doExecute(selector, properties);
    }

    /**
     * Execute a operation
     * @param filter 
     * @param properties 
     * @param providedTags 
     * @return results
     * @throws NotFoundException 
     */
    public static OperationResult e(Class<?> filter, IProperties properties, String ... providedTags) throws NotFoundException {
        return OperationUtil.doExecute(filter, properties, providedTags);
    }
    
    /**
     * Execute a operation
     * @param filter 
     * @param properties 
     * @param selectorAlgo 
     * @param providedTags 
     * @return results
     * @throws NotFoundException 
     */
    public static OperationResult e(Class<?> filter, IProperties properties, Selector selectorAlgo, String ... providedTags) throws NotFoundException {
        return OperationUtil.doExecute(filter, properties, selectorAlgo, providedTags);
    }
    
    /**
     * Execute all found operations
     * @param filter 
     * @param properties 
     * @param providedTags 
     * @return results
     * @throws NotFoundException 
     */
    public static List<OperationResult> eAll(Class<?> filter, IProperties properties, String ... providedTags) throws NotFoundException {
        return OperationUtil.doExecuteAll(filter, properties, providedTags);
    }
    
    /**
     * Execute all found operations
     * @param filter 
     * @param properties 
     * @param selectorAlgo 
     * @param providedTags 
     * @return results
     * @throws NotFoundException 
     */
    public static List<OperationResult> eAll(Class<?> filter, IProperties properties, Selector selectorAlgo, String ... providedTags) throws NotFoundException {
        return OperationUtil.doExecuteAll(filter, properties, selectorAlgo, providedTags);
    }
    
    /**
     * Execute a operation
     * @param filter 
     * @param executor 
     * @param providedTags 
     * @return results
     * @throws MException 
     */
    public static <T> boolean e(Class<T> filter, MCallback2<OperationDescriptor,T> executor, String ... providedTags) throws MException {
        return OperationUtil.doExecute(filter, executor, providedTags);
    }
    
    /**
     * Execute a operation
     * @param filter 
     * @param executor 
     * @param selectorAlgo 
     * @param providedTags 
     * @return results
     * @throws MException 
     */
    public static <T> boolean e(Class<T> filter, MCallback2<OperationDescriptor,T> executor, Selector selectorAlgo, String ... providedTags) throws MException {
        return OperationUtil.doExecute(filter, executor, selectorAlgo, providedTags);
    }
    
    /**
     * Execute all found operations
     * @param filter 
     * @param executor 
     * @param providedTags 
     * @return results
     * @throws MException 
     */
    public static <T> boolean eAll(Class<T> filter, MCallback2<OperationDescriptor,T> executor, String ... providedTags) throws MException {
        return OperationUtil.doExecuteAll(filter, executor, providedTags);
    }
    
    /**
     * Execute all found operations
     * @param filter 
     * @param executor 
     * @param selectorAlgo 
     * @param providedTags 
     * @return results
     * @throws MException 
     */
    public static <T> boolean eAll(Class<T> filter, MCallback2<OperationDescriptor,T> executor, Selector selectorAlgo, String ... providedTags) throws MException {
        return OperationUtil.doExecute(filter, executor, selectorAlgo, providedTags);
    }
    
    /**
     * Execute a operation
     * @param filter 
     * @param range 
     * @param properties 
     * @param providedTags 
     * @return results
     * @throws NotFoundException 
     */
    public static OperationResult e(Class<?> filter, VersionRange range, IProperties properties, String ... providedTags) throws NotFoundException {
        return OperationUtil.doExecute(filter, range, properties, providedTags);
    }
    
    /**
     * Execute a operation
     * @param filter 
     * @param range 
     * @param properties 
     * @param selectorAlgo 
     * @param providedTags 
     * @return results
     * @throws NotFoundException 
     */
    public static OperationResult e(Class<?> filter, VersionRange range, IProperties properties, Selector selectorAlgo, String ... providedTags) throws NotFoundException {
        return OperationUtil.doExecute(filter, range, properties, selectorAlgo, providedTags);
    }
    
    /**
     * Execute all found operations
     * @param selector 
     * @param properties 
     * @return results
     * @throws NotFoundException 
     */
    public static List<OperationResult> eAll(OperationsSelector selector, IProperties properties) throws NotFoundException {
        return OperationUtil.doExecuteAll(selector, properties);
    }
    
    /**
     * Execute all found operations
     * @param filter 
     * @param range 
     * @param properties 
     * @param providedTags 
     * @return results
     * @throws NotFoundException 
     */
    public static List<OperationResult> eAll(Class<?> filter, VersionRange range, IProperties properties, String ... providedTags) throws NotFoundException {
        return OperationUtil.doExecuteAll(filter, range, properties, providedTags);
    }
    
    /**
     * Execute all found operations
     * @param filter 
     * @param range 
     * @param properties 
     * @param selectorAlgo 
     * @param providedTags 
     * @return results
     * @throws NotFoundException 
     */
    public static List<OperationResult> eAll(Class<?> filter, VersionRange range, IProperties properties, Selector selectorAlgo, String ... providedTags) throws NotFoundException {
        return OperationUtil.doExecuteAll(filter, range, properties, selectorAlgo, providedTags);
    }
    
    /**
     * Execute a operation
     * @param filter 
     * @param range 
     * @param executor 
     * @param providedTags 
     * @return results
     * @throws MException 
     */
    public static <T> boolean e(Class<T> filter, VersionRange range, MCallback2<OperationDescriptor,T> executor, String ... providedTags) throws MException {
        return OperationUtil.doExecute(filter, range, executor, providedTags);
    }
    
    /**
     * Execute a operation
     * @param filter 
     * @param range 
     * @param executor 
     * @param selectorAlgo 
     * @param providedTags 
     * @return results
     * @throws MException 
     */
    public static <T> boolean e(Class<T> filter, VersionRange range, MCallback2<OperationDescriptor,T> executor, Selector selectorAlgo, String ... providedTags) throws MException {
        return OperationUtil.doExecute(filter, range, executor, selectorAlgo, providedTags);
    }
    
    /**
     * Execute all found operations
     * @param filter 
     * @param range 
     * @param executor 
     * @param providedTags 
     * @return results
     * @throws MException 
     */
    public static <T> boolean eAll(Class<T> filter, VersionRange range, MCallback2<OperationDescriptor,T> executor, String ... providedTags) throws MException {
        return OperationUtil.doExecuteAll(filter, range, executor, providedTags);
    }
    
    /**
     * Execute all found operations
     * @param filter 
     * @param range 
     * @param executor 
     * @param selectorAlgo 
     * @param providedTags 
     * @return results
     * @throws MException 
     */
    public static <T> boolean eAll(Class<T> filter, VersionRange range, MCallback2<OperationDescriptor,T> executor, Selector selectorAlgo, String ... providedTags) throws MException {
        return OperationUtil.doExecuteAll(filter, range, executor, selectorAlgo, providedTags);
    }
    
}
