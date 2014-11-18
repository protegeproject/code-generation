package org.protege.owl.codegeneration;
import org.testng.Assert;
import org.testng.annotations.Test; 

public class TestCustomNames {
	public static String PREFIX = "org.protege.owl.codegeneration.custom.names";
	public static String FACTORY_SUBPATH = "test.factory.path";
	
	
	@Test
	public void testFactoryAndVocabularyLocations() throws ClassNotFoundException {
		Assert.assertNotNull(Class.forName(PREFIX + "." + FACTORY_SUBPATH + "." + "CustomFactory"));
		Assert.assertNotNull(Class.forName(PREFIX + "." + FACTORY_SUBPATH + "." + "Vocabulary"));
	}
	
	@Test
	public void testClassJavaName() throws ClassNotFoundException {
		Assert.assertNotNull(Class.forName(PREFIX + "." + "TimsA"));
	}
	
	@Test
	public void testObjectPropertyJavaName() throws ClassNotFoundException, NoSuchMethodException, SecurityException {
		Class<?> TIMSA = Class.forName(PREFIX + "." + "TimsA");
		Assert.assertNotNull(TIMSA.getMethod("hasTimsP"));
	}

}
