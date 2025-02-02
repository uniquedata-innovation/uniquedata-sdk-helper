package br.com.uniquedata.sdk.helper.pojo;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MapperExtractFields extends ArrayList<MapperExtractField> {

	private static final long serialVersionUID = 7919712175525421672L;

	public boolean contains(final Class<? extends Annotation> annotation) {
		return super.stream().anyMatch(filter -> filter.getAnnotation().equals(annotation));
	}
	
	public boolean add(final Class<? extends Annotation> annotation, final ExtractField field) {
		return super.add(new MapperExtractField(field, annotation));
	}
	
	public List<MapperExtractField> get(final Class<? extends Annotation> annotation) {
		return parse(annotation).collect(Collectors.toList());	
	}
	
	private Stream<MapperExtractField> parse(final Class<? extends Annotation> annotation) {
		return super.stream().filter(filter -> filter.getAnnotation().equals(annotation));
	}
	
}
