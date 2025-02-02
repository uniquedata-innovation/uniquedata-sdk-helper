package br.com.uniquedata.sdk.helper.annotation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import br.com.uniquedata.sdk.helper.method.MethodReflectionHelper;
import br.com.uniquedata.sdk.helper.object.ObjectReflectionHelper;
import br.com.uniquedata.sdk.helper.pojo.ExtractAnnotation;
import br.com.uniquedata.sdk.helper.pojo.ExtractMethod;

public class AnnotationReflectionHelper {
	
	public static boolean contains(final Class<?> classType, final Class<? extends Annotation> annotation){
		final Stream<Field> stream = Arrays.asList(classType.getDeclaredFields()).stream();
		return stream.anyMatch(filter -> filter.isAnnotationPresent(annotation));
	}
	
	public static ExtractAnnotation extractByMethodName(final Annotation annotation, final String methodName) {
		final ExtractAnnotation extractAnnotation = new ExtractAnnotation();
		extractAnnotation.setAnnotation(annotation);
		extractAnnotation.setAnnotationValue(MethodReflectionHelper.extract(annotation, methodName).getMethodValue());
		extractAnnotation.setAnnotationName(annotation.annotationType().getSimpleName());
		
		return extractAnnotation;
	}
	
	public static List<ExtractAnnotation> extract(final Annotation ... annotations) {
		return Arrays.asList(annotations).stream()
			.map(annotation -> {
				return ObjectReflectionHelper.toStream(annotation.annotationType().getDeclaredMethods())
					.map(method -> {
						final ExtractMethod extractMethod = MethodReflectionHelper.extract(annotation, method.getName());
	
						final ExtractAnnotation extractAnnotation = new ExtractAnnotation();
						extractAnnotation.setAnnotation(annotation);
						extractAnnotation.setAnnotationName(annotation.annotationType().getSimpleName());
						extractAnnotation.setAnnotationValue(extractMethod.getMethodValue());
	
						return extractAnnotation;
				}).findAny().orElse(null);
		}).collect(Collectors.toList());
	}
	
	public static ExtractAnnotation extractAnnotationByFieldAndMethodName(
			final Annotation annotation, final Field field, final String methodName) {
		
		return Arrays.asList(field.getAnnotations()).stream()
			.filter(filterAnnotation -> filterAnnotation.equals(annotation))
			.map(annotationFound -> {
				final Object objectAnnotation = field.getAnnotation(annotationFound.annotationType());
				final ExtractMethod extractMethod = MethodReflectionHelper.extract(objectAnnotation, methodName);

				final ExtractAnnotation extractAnnotation = new ExtractAnnotation();
				extractAnnotation.setAnnotation(annotationFound);
				extractAnnotation.setAnnotationValue(extractMethod.getMethodValue());
				extractAnnotation.setAnnotationName(annotationFound.annotationType().getSimpleName());

				return extractAnnotation;
		}).findAny().orElseThrow(() -> new RuntimeException("Method Not Found {"+methodName+"}"));
	}

}
