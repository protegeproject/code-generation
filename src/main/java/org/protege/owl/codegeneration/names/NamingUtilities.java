package org.protege.owl.codegeneration.names;

public class NamingUtilities {

	private NamingUtilities() {
	}

	public static String convertToJavaIdentifier(String s) {
		s = s.replace(' ', '_').replace('-', '_').replaceAll("\'","");
		return s;
	}
	
    /** Returns the provided string with initial letter as capital
     * @param name
     */
    public static String convertInitialLetterToUpperCase(String name) {
        if (name == null) {
            return null;
        }
        if (name.length() > 1) {
            return Character.toUpperCase(name.charAt(0)) + name.substring(1);
        } else {
            return name.toUpperCase();
        }
    }
}
