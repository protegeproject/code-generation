package org.protege.owl.codegeneration;

public class CodeGenerationRuntimeException extends RuntimeException {
	private static final long serialVersionUID = 8315402587191641855L;

	public CodeGenerationRuntimeException(String message) {
		super(message);
	}
	
	public CodeGenerationRuntimeException(Throwable t) {
		super(t);
	}
}
