package framework;

import java.io.Serializable;

import mathLib.Complex;
import mathLib.Matrix;

public class SolderedGate implements Serializable{
	private static final long serialVersionUID = 2595030500395644473L;
	
	private AbstractGate abstractGate;
	
	public SolderedGate(AbstractGate abstractGate) {
		this.abstractGate = abstractGate;
	}

	public boolean isMultiQubit() {
		return abstractGate.isMultiQubitGate();
	}
	
	public AbstractGate getAbstractGate() {
		return abstractGate;
	}
	
	public int getExpectedNumberOfRegisters() {
		return abstractGate.getNumberOfRegisters();
	}
	
	
}