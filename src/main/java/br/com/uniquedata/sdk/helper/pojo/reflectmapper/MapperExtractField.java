package br.com.uniquedata.sdk.helper.pojo.reflectmapper;

import java.lang.annotation.Annotation;

import br.com.uniquedata.sdk.helper.pojo.extract.ExtractField;

public class MapperExtractField {
	
	private ExtractField extractField; 

	private Class<? extends Annotation> annotation;
	
	public MapperExtractField() {}

	public MapperExtractField(final ExtractField extractField, 
		final Class<? extends Annotation> annotation) {
		
		this.extractField = extractField;
		this.annotation = annotation;
	}

	public ExtractField getExtractField() {
		return extractField;
	}

	public void setExtractField(final ExtractField extractField) {
		this.extractField = extractField;
	}

	public Class<? extends Annotation> getAnnotation() {
		return annotation;
	}

	public void setAnnotation(final Class<? extends Annotation> annotation) {
		this.annotation = annotation;
	}
	
}