package org.protege.owl.codegeneration;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.WildcardType;
import java.net.URI;

import javax.xml.datatype.XMLGregorianCalendar;

import org.protege.owl.codegeneration.inferred.generate03.A;
import org.protege.owl.codegeneration.inferred.generate03.B;
import org.testng.Assert;
import org.testng.annotations.Test;



public class TestDateAndURI {

	@Test
	public void testURIDeclarations() throws NoSuchMethodException, SecurityException {
		Assert.assertNotNull(A.class.getMethod("addA", Object.class));
		Assert.assertNotNull(B.class.getMethod("addA", Object.class));
		Method uriMethod = B.class.getMethod("getA");
		Assert.assertTrue(uriMethod.getGenericReturnType() instanceof ParameterizedType);
		ParameterizedType returnType = (ParameterizedType) uriMethod.getGenericReturnType();
		Assert.assertEquals(returnType.getActualTypeArguments().length, 1);
		Assert.assertTrue(returnType.getActualTypeArguments()[0] instanceof WildcardType);
		Assert.assertEquals(((WildcardType) returnType.getActualTypeArguments()[0]).getUpperBounds()[0], URI.class);
	}

	
	@Test
	public void testDateDeclarations() throws NoSuchMethodException, SecurityException {
		Assert.assertNotNull(A.class.getMethod("addB", Object.class));
		Assert.assertNotNull(B.class.getMethod("addB", Object.class));
		Method uriMethod = B.class.getMethod("getB");
		Assert.assertTrue(uriMethod.getGenericReturnType() instanceof ParameterizedType);
		ParameterizedType returnType = (ParameterizedType) uriMethod.getGenericReturnType();
		Assert.assertEquals(returnType.getActualTypeArguments().length, 1);
		Assert.assertTrue(returnType.getActualTypeArguments()[0] instanceof WildcardType);
		Assert.assertEquals(((WildcardType) returnType.getActualTypeArguments()[0]).getUpperBounds()[0], XMLGregorianCalendar.class);
	}
}