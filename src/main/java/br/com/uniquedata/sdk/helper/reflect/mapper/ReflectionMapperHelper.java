package br.com.uniquedata.sdk.helper.reflect.mapper;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import br.com.uniquedata.sdk.helper.field.FieldReflectionHelper;
import br.com.uniquedata.sdk.helper.object.ObjectReflectionHelper;
import br.com.uniquedata.sdk.helper.pojo.extract.ExtractField;
import br.com.uniquedata.sdk.helper.pojo.reflectmapper.MapperExtractFields;
import br.com.uniquedata.sdk.helper.pojo.reflectmapper.ReflectMapperField;
import br.com.uniquedata.sdk.helper.pojo.reflectmapper.ReflectMapperFilter;

/**
 * A utility class that uses reflection to:
 * <ul>
 *   <li>Map (clone/transform) the fields of an input object into a new instance 
 *       of another class, respecting field names and data types.</li>
 *   <li>Extract fields based on specified annotations. This is especially useful 
 *       if you need to log, validate, or otherwise process annotated fields.</li>
 * </ul>
 *
 * <p>
 * Typical usage involves:
 * </p>
 * <pre>{@code
 * // 1) Convert an object 'cliente' to another type 'SmallCliente'
 * SmallCliente small = ReflectionMapperHelper
 *     .refletc(cliente)
 *     .to(SmallCliente.class);
 *
 * // 2) Extract fields that have the annotation 'SimplesAnnotetion'
 * MapperExtractFields extractedFields = ReflectionMapperHelper
 *     .refletc(cliente)
 *     .addScanBy(SimplesAnnotetionOne.class)
 *     .addScanBy(SimplesAnnotetionTwo.class)
 *     .toExtractFields();
 * }</pre>
 *
 * <p>
 * Internally, it manages recursive copying of fields (including collections and maps), 
 * handles different data types, and can capture fields for later inspection through 
 * annotation-based filtering.
 * </p>
 *
 * @author Jaderson Berti
 * @author Unique Data Inovatation (company)
 * @since 1.0
 */
public class ReflectionMapperHelper {

	private AtomicReference<Object> objectInReference;
	
	private AtomicReference<ReflectMapperFilter> reflectMapperFilterReference;
	
	public ReflectionMapperHelper(final Object objectIn) {
		this.objectInReference = new AtomicReference<Object>(objectIn);
		this.reflectMapperFilterReference = new AtomicReference<>(new ReflectMapperFilter());
	}
	
    /**
     * Creates and returns a new {@code ReflectionMapperHelper} for the given object.
     * This is a static convenience method for initializing the helper with an input object
     * that will be reflected upon for mapping or field extraction operations.
     *
     * @param objectIn
     *        The source object to be reflected upon.
     * @return A new instance of {@code ReflectionMapperHelper} bound to the specified object.
     */
	public static ReflectionMapperHelper refletc(final Object objectIn) {
		return new ReflectionMapperHelper(objectIn);
	}
	
	/**
     * Maps the fields of the current source object to a new instance of the specified class type.
     * Fields of the same name and compatible types are copied over. This method supports 
     * recursive mapping for nested objects, collections, and maps.
     *
     * @param <T>
     *        The type to which the object should be mapped.
     * @param classTypeOut
     *        The target class to create and populate.
     * @return A new instance of {@code classTypeOut} populated with data from the source object.
     */
	public <T> T to(final Class<T> classTypeOut) {
	    return classTypeOut.cast(reflectMapper(objectInReference.get(), classTypeOut));
	}
	
	/**
     * Specifies an annotation to be scanned for during field extraction.
     * By calling this method, you enable filtering or extraction of only
     * those fields in the source object(s) that are marked with the provided annotation.
     *
     * @param annotation
     *        The annotation class to be added to the scan/filter list.
     * @return This {@code ReflectionMapperHelper} instance for fluent chaining.
     */
	public ReflectionMapperHelper addScanBy(final Class<? extends Annotation> annotation) {
		return this.reflectMapperFilterReference.get().addAnntotation(this, annotation);
	}
	
	/**
     * Extracts fields from the current source object (and its nested structures) 
     * into a {@link MapperExtractFields} instance. If annotations have been specified via
     * {@link #addScanBy(Class)}, only fields annotated with those annotations will be processed.
     *
     * @return A {@code MapperExtractFields} object containing information about the extracted fields.
     */
	public MapperExtractFields toExtractFields() {
		return toExtractFields(objectInReference.get(), new MapperExtractFields());
	}	
	
	private Object reflectMapper(final Object objectIn, final Class<?> classTypeOut) {
		final Object objectOut = ObjectReflectionHelper.newInstance(classTypeOut);

		try {
			final List<ExtractField> extractFieldsIn = toFields(objectIn.getClass()).stream().map(fieldIn -> {
				return FieldReflectionHelper.extract(objectIn, fieldIn);
			}).collect(Collectors.toList());

			toFields(objectOut.getClass()).stream().map(fieldOut  -> {
				return new ReflectMapperField(extractFieldsIn.stream().filter(extractFieldIn -> {
					final String fieldNameOut = fieldOut.getName();
					final String fieldNameIn = extractFieldIn.getFieldName();

					return fieldNameIn.equalsIgnoreCase(fieldNameOut);
				}).findAny().map(ExtractField::getField).orElse(null), fieldOut);
			}).filter(mapperField -> mapperField.isMatch()).forEach(mapperField -> {
				final Class<?> fieldType = mapperField.getFieldIn().getType();
				
				if (fieldType.isPrimitive() || isWrapperType(fieldType)) {
					FieldReflectionHelper.transferField(objectIn, mapperField.getFieldIn(), objectOut, mapperField.getFieldOut());
				} else if (Collection.class.isAssignableFrom(fieldType)) {
				    final Object fieldValue = FieldReflectionHelper.extract(objectIn, mapperField.getFieldIn()).getFieldValue();
				    FieldReflectionHelper.addValueField(reflectMapperCollection(fieldValue, fieldType), objectOut, mapperField.getFieldOut());
				} else if (Map.class.isAssignableFrom(fieldType)) {
				    final Object fieldValue = FieldReflectionHelper.extract(objectIn, mapperField.getFieldIn()).getFieldValue();
				    FieldReflectionHelper.addValueField(reflectMapperMap(fieldValue, fieldType), objectOut, mapperField.getFieldOut());
	            } else {
	            	final Object fieldValue = FieldReflectionHelper.extract(objectIn, mapperField.getFieldIn()).getFieldValue();
	            	
	            	if(fieldValue != null) {
	            		final Object reflectMapper = reflectMapper(fieldValue, mapperField.getFieldOut().getType());
	            		FieldReflectionHelper.addValueField(reflectMapper, objectOut, mapperField.getFieldOut());
	            	}else {
	            		FieldReflectionHelper.addValueField(null, objectOut, mapperField.getFieldOut());
	            	}
	            }
			});
		}catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		return objectOut;
	}
	
	private MapperExtractFields toExtractFields(final Object recursiveObject, 
			final MapperExtractFields reflectMapperExtractFields){

		try {
			final ReflectMapperFilter reflectMapperFilter = reflectMapperFilterReference.get();
			
			if(reflectMapperFilter.isEnable() == false) {
				throw new RuntimeException("You need first method scanBy");
			}
			
			toFields(recursiveObject.getClass()).stream().forEach(fieldIn -> {
				final Optional<Class<? extends Annotation>> annotationOptional = getAnnotationByMatch(fieldIn);
				final ExtractField extractField = FieldReflectionHelper.extract(recursiveObject, fieldIn);
				
				if (fieldIn.getType().isPrimitive() || isWrapperType(fieldIn.getType())) {
					if(annotationOptional.isPresent()) {
						reflectMapperExtractFields.add(annotationOptional.get(), extractField);
					}
				} else if (Collection.class.isAssignableFrom(fieldIn.getType())) {
					extractToCollection(extractField.getFieldValue(), reflectMapperExtractFields);
				} else {
					toExtractFields(extractField.getFieldValue(), reflectMapperExtractFields);
	            }
			});
			
		}catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		return reflectMapperExtractFields;
	}
	
	private Collection<?> reflectMapperCollection(final Object object, final Class<?> fieldType) {
		final Collection<?> sourceCollection = (Collection<?>) object;
		final Collection<Object> targetCollection = ObjectReflectionHelper.newCollection(fieldType);
		    
	    if (sourceCollection != null) {
	        for (final Object item : sourceCollection) {
	            if (item != null) {
	            	final Class<?> itemClass = item.getClass();
	                
	                if (itemClass.isPrimitive() || isWrapperType(itemClass)) {
	                    targetCollection.add(item);
	                } else if (Collection.class.isAssignableFrom(itemClass)) {
	                    targetCollection.add(reflectMapperCollection(item, itemClass));
	                }else if(Map.class.isAssignableFrom(itemClass)) {
	                	targetCollection.add(reflectMapperMap(item, itemClass));
	                } else {
	                    targetCollection.add(reflectMapper(item, itemClass));
	                }
	            }
	        }
	    }
	    
	    return targetCollection;
	}
	
	private void extractToCollection(final Object object, 
		final MapperExtractFields reflectMapperExtractFields) {
		    
	    if (object != null) {
	    	for (final Object item : ((Collection<?>) object)) {
	            if (item != null) {
	                if (Collection.class.isAssignableFrom(item.getClass())) {
	                	extractToCollection(item, reflectMapperExtractFields);
	                } else {
	                	toExtractFields(item, reflectMapperExtractFields);
	                	
	                }
	            }
	        }
	    }
	}
	
	private Map<Object, Object> reflectMapperMap(final Object object, final Class<?> fieldType) {
		final Map<?, ?> sourceMap = (Map<?, ?>) object;
		final Map<Object, Object> targetMap = ObjectReflectionHelper.newMap(fieldType);

		if (sourceMap != null) {
	        for (final Map.Entry<?, ?> entry : sourceMap.entrySet()) {
	            final Object mappedKey = (entry.getKey() == null) ? null : reflectMapperKeyValue(entry.getKey());
	            final Object mappedValue = (entry.getValue() == null) ? null : reflectMapperKeyValue(entry.getValue());
	            
	            targetMap.put(mappedKey, mappedValue);
	        }
	    }
		
		return targetMap;
	}
	
	private Object reflectMapperKeyValue(final Object input) {
	    final Class<?> type = input.getClass();

	    if (type.isPrimitive() || isWrapperType(type) || String.class.equals(type)) {
	        return input;
	    } else if (Collection.class.isAssignableFrom(type) || Map.class.isAssignableFrom(type)) {
	        return reflectMapper(input, type);
	    } else {
	        return reflectMapper(input, type);
	    }
	}
	
	public static boolean isWrapperType(final Class<?> clazz) {
	    return clazz.equals(Boolean.class) || clazz.equals(Byte.class) || clazz.equals(Character.class) || 
	    	clazz.equals(Double.class) || clazz.equals(Float.class) || clazz.equals(Integer.class) || 
	    	clazz.equals(Long.class) || clazz.equals(Short.class) || clazz.equals(String.class) ||  
	    	clazz.equals(Date.class) || clazz.equals(LocalDate.class) || clazz.equals(LocalDateTime.class) ||
	    	clazz.equals(Void.class);
	}
	
	private Optional<Class<? extends Annotation>> getAnnotationByMatch(final Field field){
		final Stream<Class<? extends Annotation>> stream = reflectMapperFilterReference.get().getAnnotations().stream();
		return stream.filter(annot -> field.isAnnotationPresent(annot)).findAny();
	}
	
	private List<Field> toFields(final Class<?> type){
		return new ArrayList<>(Arrays.asList(type.getDeclaredFields()));
	}

}