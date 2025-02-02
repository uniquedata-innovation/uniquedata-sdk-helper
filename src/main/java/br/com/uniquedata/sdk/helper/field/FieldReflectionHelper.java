package br.com.uniquedata.sdk.helper.field;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import br.com.uniquedata.sdk.helper.annotation.AnnotationReflectionHelper;
import br.com.uniquedata.sdk.helper.pojo.extract.ExtractAnnotation;
import br.com.uniquedata.sdk.helper.pojo.extract.ExtractField;

public class FieldReflectionHelper {

	public static ExtractField extract(final Object object, final Field field){
		try {
			field.setAccessible(true);
			
			final ExtractField extractField = new ExtractField();
			extractField.setField(field);
			extractField.setFieldName(field.getName());
			extractField.setFieldValue(field.get(object));

			if(field.getAnnotations() != null) {
				extractField.setFieldAnnotations(AnnotationReflectionHelper.extract(field.getAnnotations()));
			}
			
			return extractField;
		}catch (Exception e) {
 			throw new RuntimeException(e);
		}
	}
	
	public static Map<String, Object> getFieldNameAndValue(final Object object, final Class<? extends Annotation> annotation){
		return Arrays.asList(object.getClass().getDeclaredFields()).stream()
			.map(field -> {
				if(field.isAnnotationPresent(annotation)) {
					final ExtractField extractField = extract(object, field);
					final Optional<ExtractAnnotation> annotationOptional = extractField.getExtractAnnotationBy(annotation);
					
					if(annotationOptional.isPresent()) {
						final ExtractAnnotation extractAnnotation = annotationOptional.get();
						final Object annotationValue = extractAnnotation.getAnnotationValue();
						return new ExtractField(field, annotationValue.toString(), extractField.getFieldValue());
					}
				}	
			
			return extract(object, field);
		}).collect(Collectors.toMap(ExtractField::getFieldName, ExtractField::getFieldValue));
	}
	
	public static Map<Field, Object> getFieldAndValue(final Object object){
		final Map<Field, Object> fields = new HashMap<>();

		toArrayList(object.getClass()).forEach(field -> {
			try {
				field.setAccessible(true);
				fields.put(field, field.get(object));
			}catch (Exception e) {
				throw new RuntimeException(e);
			}
		});
		
		return fields;
	}
	
	public static Field transferField(final Object objectIn, final Field fieldIn, 
			final Object objectOut, final Field fieldOut){
		
		try {
			fieldIn.setAccessible(true);
			fieldOut.setAccessible(true);
			
			fieldOut.set(objectOut, fieldIn.get(objectIn));
		}catch (Exception e) {
 			throw new RuntimeException(e);
		}
		
		return fieldOut;
	}
	
	public static Field addValueField(final Object newValueField, final Object objectField, final Field field){
		try {
			field.setAccessible(true);
			field.set(objectField, newValueField);
		}catch (Exception e) {
 			throw new RuntimeException(e);
		}
		
		return field;
	}
	
	@SuppressWarnings("unchecked")
	public static Class<? extends Collection<?>> toCollectionType(final Class<?> arrayType) {
		if (!Collection.class.equals(arrayType) && !Collection.class.isAssignableFrom(arrayType)) {
            throw new IllegalArgumentException("The object must be an collection, but was: " + arrayType.getClass().getName());
        }
		
		return (Class<? extends Collection<?>>) arrayType;
	}
	
	public static ExtractField extractFieldByName(final Object object, final String fieldName){
		final Stream<Field> stream = Arrays.asList(object.getClass().getDeclaredFields()).stream();
		return extract(object, stream.filter(field -> field.getName().equals(fieldName)).findAny().get());
	}
	
	public static ExtractField extractFieldByAnnotation(final Object object, final Class<? extends Annotation> annotation){
		final Stream<Field> stream = Arrays.asList(object.getClass().getDeclaredFields()).stream();
		return extract(object, stream.filter(field -> field.isAnnotationPresent(annotation)).findAny().get());
	}
	
	public static Object getValueByFieldName(final String fieldName, final Object object){
		final Stream<Field> stream = Arrays.asList(object.getClass().getDeclaredFields()).stream();
 		return stream.filter(filter -> filter.getName().equals(fieldName)).findAny().orElse(null);
	}
	
	public static Map<String, Object> getFieldNameAndValue(final Object object, final Annotation annotation){
		return getFieldNameAndValue(object, annotation.annotationType());
	}
	
	public static Stream<Field> toStream(final Class<?> classType){
		return Arrays.asList(classType.getDeclaredFields()).stream();
	}
	
	public static List<Field> toArrayList(final Class<?> classType){
		return new ArrayList<>(Arrays.asList(classType.getDeclaredFields()));
	}
	
}
