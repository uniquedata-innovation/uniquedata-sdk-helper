package br.com.uniquedata.sdk.helper.object;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import javax.management.RuntimeErrorException;

/**
 * The ObjectReflectionHelper helps to manipulate and inspect class settings 
 * and values through reflection.
 * 
 * <p>
 * This class provides utility methods for instantiating objects, creating proxy 
 * instances, retrieving field values, and handling generic return types.
 * </p>
 * 
 * @author Jaderson Berti
 * @author Unique Data Inovatation (company)
 * @since 1.0
 */
public class ObjectReflectionHelper {
	
	public static Class<?> newArrayType(final Class<?> elementType) {
		return Array.newInstance(elementType, 0).getClass();
	}
	
	public static Class<?> newCollectionType(final Class<? extends Collection<?>> collectionClass) {
		return Array.newInstance(collectionClass, 0).getClass();
	}
	
	public static ArrayList<?> newInstanceArraList(final Object[] objects) {
		return new ArrayList<>(Arrays.asList(objects));
	}
	
	public static ArrayList<?> newInstanceArraList(final Object object) {
		return newInstanceArraList((Object[]) object);
	}
	
	@SuppressWarnings("unchecked")
	public static Collection<Object> newCollection(final Class<?> collectionType) {
	    try {
	        return (Collection<Object>) collectionType.getDeclaredConstructor().newInstance();
	    } catch (Exception e) {
	        return new ArrayList<>();
	    }
	}

	@SuppressWarnings("unchecked")
	public static Map<Object, Object> newMap(final Class<?> mapType) {
	    try {
	        return (Map<Object, Object>) mapType.getDeclaredConstructor().newInstance();
	    } catch (Exception e) {
	        return new HashMap<>();
	    }
	}
	
	public static Object newInstance(final Class<?> classType) {
		try {
			return classType.getDeclaredConstructor().newInstance();
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeErrorException(new Error(e), "Erro create new Instance!");
		}
	}
	
	public static Object newProxyInstance(final Class<?> interfaceType, final Object invocationHandlerImpl) {
		try {
			final InvocationHandler invocationHandler =  (InvocationHandler) invocationHandlerImpl;
			return Proxy.newProxyInstance(interfaceType.getClassLoader(),new Class<?>[]{ interfaceType }, invocationHandler);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeErrorException(new Error(e), "Erro create new Instance!");
		}
	}
	
	public static Collection<?> newInstanceCollection(final Class<? extends Collection<?>> collectionType, final Object object) {
        if (object == null) {
            throw new IllegalArgumentException("The object to convert cannot be null");
        }
        
        if (!object.getClass().isArray()) {
            throw new IllegalArgumentException("The object must be an array, but was: " + object.getClass().getName());
        }
        
        try {
            @SuppressWarnings("unchecked")
			final Collection<Object> collection = (Collection<Object>) collectionType.getDeclaredConstructor().newInstance();
            collection.addAll(Arrays.asList((Object[]) object));
            
            return collection;
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("Failed to cast the provided object to the expected type array", e);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("The collection type " + collectionType.getName() + " must have a no-argument constructor", e);
        } catch (Exception e) {
            throw new RuntimeException("Failed to instantiate collection type: " + collectionType.getName(), e);
        }
    }
	
	public static <T> Stream<T> toStream(final T[] array){
		return Arrays.asList(array).stream();
	}
	
	public static <T> List<T> toArrayList(final T[] array){
		return new ArrayList<>(Arrays.asList(array));
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T reflection(final Class<?> classType, final Object object) {
		return (T) classType.cast(object);
	}

}