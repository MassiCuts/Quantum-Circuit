package appFX.appUI.appViews.gateChooser;

import java.net.URL;
import java.util.ResourceBundle;

import appFX.framework.gateModels.PresetGateType;

public class PresetGatesChooserView extends AbstractGateChooserView {
	
	public PresetGatesChooserView() {
		super("Preset Gates");
	}

	public void initializeGates() {
		for(PresetGateType dg : PresetGateType.values())
			addGateModel(dg.getModel());
	}
	
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {		
		setButtonVisible(false);
		initializeGates();
	}

	@Override
	public void buttonAction() {}


	@Override
	public void receive(Object source, String methodName, Object... args) {
		
	}
	
}
