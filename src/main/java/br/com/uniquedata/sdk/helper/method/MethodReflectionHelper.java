package br.com.uniquedata.sdk.helper.method;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

import br.com.uniquedata.sdk.helper.pojo.ExtractMethod;

public class MethodReflectionHelper {

	public static ExtractMethod extract(final Object object, final String methodName, final Object... paramters){
		try {
			final Stream<Method> stream = Arrays.asList(object.getClass().getMethods()).stream();
			final Optional<Method> optionalMethod = stream.filter(filter -> filter.getName().equals(methodName)).findAny();
			
			if(optionalMethod.isPresent()) {
				final Method method = optionalMethod.get();
				
				if(paramters == null || paramters.length == 0) {
					return new ExtractMethod(methodName, method.invoke(object));
				}else {
					return new ExtractMethod(methodName, method.invoke(object, paramters));
				}
			}
			
			throw new RuntimeException("Method Not Found {"+methodName+"}");
		}catch (Exception e) {
			throw new RuntimeException(e);
		}
	}	
	
}
