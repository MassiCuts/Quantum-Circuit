package appFX.framework.exportGates;

import java.util.Arrays;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Set;

import appFX.framework.gateModels.BasicGateModel;
import appFX.framework.gateModels.GateModel;
import appFX.framework.gateModels.PresetGateType;
import appFX.framework.gateModels.PresetGateType.PresetGateModel;
import appFX.framework.gateModels.QuantumGateDefinition;
import appFX.framework.gateModels.QuantumGateDefinition.QuantumGateType;
import appFX.framework.utils.InputDefinitions.MatrixDefinition;
import mathLib.Complex;
import mathLib.Matrix;

public class ExportedGate {	
	private final GateModel gateModel;
	private final Hashtable<String, Complex> paramArgMap;
	private final int[] gateRegisters;
	private final Control[] quantumControls;
	private final Control[] classicalControls;
	private final Matrix<Complex>[] matrixes;
	private final boolean isClassical;
	
	
	@SuppressWarnings("unchecked")
	public static ExportedGate mkIdentAt(int register, boolean isClassical) {
		BasicGateModel gm = PresetGateType.IDENTITY.getModel();
		QuantumGateDefinition definition = gm.getQuantumGateDefinition();
		Matrix<Complex> m = ((MatrixDefinition) definition.getDefinitions().get(0)).getMatrix();
		
		return new ExportedGate(gm, new Hashtable<>(), new int[] {register}, isClassical, new Control[0], new Control[0], new Matrix[] { m });
	}
	
	
	public ExportedGate(GateModel gateModel, Hashtable<String, Complex> argParamMap, int[] gateRegisters, boolean isClassical, Control[] classicalControls, Control[] quantumControls, Matrix<Complex>[] matrixes) {
		this.gateModel = gateModel;
		this.gateRegisters = gateRegisters;
		this.classicalControls = classicalControls;
		this.quantumControls = quantumControls;
		this.isClassical = isClassical;
		this.matrixes = matrixes;
		this.paramArgMap = argParamMap;
	}
	
	public boolean isClassical() {
		return isClassical;
	}
	
	public boolean isQuantum() {
		return !isClassical;
	}
	
	public GateModel getGateModel() {
		return gateModel;
	}
	
	public Complex getArgument (String parameterName) {
		return paramArgMap.get(parameterName);
	}
	
	public Set<String> getArgumentSet() {
		return paramArgMap.keySet();
	}
	
	public Collection<Complex> getArguments() {
		return paramArgMap.values();
	}
	
	public int[] getGateRegisters() {
		return gateRegisters;
	}
	
	public boolean isPresetGate() {
		return gateModel.isPreset();
	}
	
	public PresetGateType getPresetGateType() {
		if(gateModel.isPreset()) 
			return  ((PresetGateModel) gateModel).getPresetGateType();
		else return null;
	}
	
	public QuantumGateType getQuantumGateType() {
		if(gateModel instanceof BasicGateModel) {
			if(gateModel.isQuantum()) {
				QuantumGateDefinition definition = ((BasicGateModel) gateModel).getQuantumGateDefinition();
				return definition.getQuantumGateType();
			}
		}
		return null;
	}
	
	
	public Control[] getQuantumControls(){
		return quantumControls;
	}
	
	public Control[] getClassicalControls(){
		return classicalControls;
	}
	
	public Matrix<Complex>[] getInputMatrixes() {
		return matrixes;
	}
	
	@Override
	public String toString() {
		String separator = "-------------";
		String gateTypeString = "["  + getGateModel().getName() + "]";
		String regsString = "Regs: " + Arrays.toString(getGateRegisters());
		String qcsString = "QCs: " + Arrays.toString(getQuantumControls());
		String ccsString = "CCs: " + Arrays.toString(getClassicalControls());
		
		String output = "";
		for (String s : new String[]{
				separator,
				gateTypeString,
				regsString,
				qcsString,
				ccsString,
				separator
				})
			output += s + '\n';
		return output;
	}
	
	
}
