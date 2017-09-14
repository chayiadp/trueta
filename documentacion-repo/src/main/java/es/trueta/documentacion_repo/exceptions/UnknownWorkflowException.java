package es.trueta.documentacion_repo.exceptions;

public class UnknownWorkflowException extends Exception {

	private static final long serialVersionUID = 1L;
	public UnknownWorkflowException(){};

	public UnknownWorkflowException (String message){
		super(message);
	}
}
