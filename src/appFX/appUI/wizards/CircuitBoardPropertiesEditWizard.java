package appFX.appUI.wizards;

import java.util.HashSet;
import java.util.Iterator;

import appFX.appUI.utils.AppAlerts;
import appFX.appUI.utils.RearrangableParameterListPaneWrapper;
import appFX.appUI.utils.SequencePaneElement;
import appFX.appUI.utils.SequencePaneElement.SequencePaneFinish;
import appFX.framework.AppCommand;
import appFX.framework.AppStatus;
import appFX.framework.Project;
import appFX.framework.Project.ProjectHashtable;
import appFX.framework.gateModels.CircuitBoardModel;
import appFX.framework.gateModels.GateModel;
import appFX.framework.gateModels.GateModel.GateComputingType;
import appFX.framework.gateModels.GateModel.NameTakenException;
import appFX.framework.gateModels.PresetGateType;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import utils.customCollections.immutableLists.ImmutableArray;

public class CircuitBoardPropertiesEditWizard extends Wizard<CircuitBoardModel> {

	
	
	private boolean editAsNew;
	private CircuitBoardModel referencedCb;
	private CircuitBoardModel newCb;
	
	private FirstElementPane first;
	
	
	
	public static CircuitBoardModel createNewGate() {
		return openGateEditableView(null, true);
	}
	
	
	public static CircuitBoardModel createNewGate(String name) {
		Wizard<CircuitBoardModel> wizard = new CircuitBoardPropertiesEditWizard();
		return wizard.openWizardAndGetElement(name);
	}
	
	
	public static CircuitBoardModel editGate(String name) {
		return openGateEditableView(name, false);
	}
	
	public static CircuitBoardModel editAsNewGate(String name) {
		return openGateEditableView(name, true);
	}
	
	
	
	private static CircuitBoardModel openGateEditableView(String name, boolean editAsNewModel) {
		Project p = AppStatus.get().getFocusedProject();
		CircuitBoardModel gm = name == null ? null : (CircuitBoardModel) p.getCircuitBoardModels().get(name);

		Wizard<CircuitBoardModel> wizard = new CircuitBoardPropertiesEditWizard(gm, editAsNewModel);
		
		if(gm == null)
			wizard.setTitle("Create new Gate");
		else if(editAsNewModel)
			wizard.setTitle("Coping " + gm.getName() + " to new Gate");
		else
			wizard.setTitle("Changing " + gm.getName() + " Gate");
		
		return wizard.openWizardAndGetElement();
	}
	
	
	public CircuitBoardPropertiesEditWizard() {
		this(null, true);
	}
	
	public CircuitBoardPropertiesEditWizard(CircuitBoardModel referencedCb, boolean editAsNew) {
		super(650, 700);
		this.referencedCb = referencedCb;
		this.editAsNew = editAsNew;
		this.first = new FirstElementPane();
	}
	
	
	@Override
	protected SequencePaneElement getFirstSeqPaneElem() {
		return first;
	}
	
	@Override
	protected boolean onWizardCloseRequest() {
		return true;
	}
	
	private class FirstElementPane extends SequencePaneElement implements SequencePaneFinish<CircuitBoardModel> {
		
		@FXML private TextField fileLocation, name, symbol;
		@FXML private TextArea description;
		@FXML private VBox parameters;
		@FXML private ComboBox<GateComputingType> gateType;
		
		
		private RearrangableParameterListPaneWrapper parameterSelection;
		
		public FirstElementPane() {
			super("wizards/circuitBoardPropertiesWizard/CircuitBoardProperties.fxml");
		}

		@Override
		public void initialize() {
			parameterSelection = new RearrangableParameterListPaneWrapper(parameters);
			ObservableList<GateComputingType> types = gateType.getItems();
			for(GateComputingType type : GateComputingType.values())
				types.add(type);
		}
		
		@Override
		public void setStartingFieldData(PaneFieldDataList fieldData) {
			String fileLocationString = "";
			String nameString = "";
			String symbolString = "";
			String descriptionString = "";
			String[] args = {};
			GateComputingType chosenType = GateComputingType.QUANTUM;
			
			if(referencedCb != null) {
				fileLocationString = referencedCb.getLocationString();
				nameString = referencedCb.getName();
				symbolString = referencedCb.getSymbol();
				descriptionString = referencedCb.getDescription();
				chosenType = referencedCb.getComputingType();
				ImmutableArray<String> argsList = referencedCb.getParameters();
				args = new String[argsList.size()];
				argsList.toArray(args);
			}
			
			fieldData.add(fileLocation, fileLocationString);
			fieldData.add(name, nameString);
			fieldData.add(symbol, symbolString);
			fieldData.add(description, descriptionString);
			fieldData.add(parameterSelection, (Object[]) args);
			fieldData.add(gateType, chosenType);
		}
		
		@Override
		public boolean checkFinish() {
			String fileNameErrorMsg = null;
			try {
				GateModel.checkLocationString(fileLocation.getText(), false, CircuitBoardModel.CIRCUIT_BOARD_EXTENSION);
			} catch (Exception e) {
				fileNameErrorMsg = e.getMessage();
			}
			
			if(checkTextFieldError(getStage(), fileLocation, fileNameErrorMsg != null, "Cannot used the specified file location", fileNameErrorMsg)) return false;
			
			if(checkTextFieldError(getStage(), name, name.getText() == null, "Unfilled prompts", "Name must be defined")) return false;
			if(checkTextFieldError(getStage(), name, name.getText().matches("\\s+"), "Inproper name scheme", "Name should not be empty spaces")) return false;

			if(checkTextFieldError(getStage(), symbol, symbol.getText() == null, "Unfilled prompts", "Symbol must be defined")) return false;
			if(checkTextFieldError(getStage(), symbol, symbol.getText().matches("\\s+"), "Inproper symbol scheme", "Symbol should not be empty spaces")) return false;
			
			HashSet<String> paramSet = new HashSet<>();
			Iterator<Node> nodeIterator = parameterSelection.nodeIterator();
			String[] params = new String[parameterSelection.size()];
			int i = 0;
			for(Object[] nodeArgs : parameterSelection) {
				TextField tf = (TextField) nodeIterator.next();
				String param = (String) nodeArgs[0];
				
				if(checkTextFieldError(getStage(), tf, param.equals(""), "Unfilled prompts", "Parameter name can not be blank")) return false;
				if(checkTextFieldError(getStage(), tf, !param.matches(GateModel.PARAMETER_REGEX), "Inproper parameter scheme", GateModel.IMPROPER_PARAMETER_SCHEME_MSG)) return false;
				if(checkTextFieldError(getStage(), tf, paramSet.contains(param), "Duplicate Parameters", "There are more than one parameter with the same name")) return false;
				
				paramSet.add(param);
				params[i++] = param;
			}
			
			try {
				if(referencedCb == null)
					newCb = new CircuitBoardModel(fileLocation.getText() ,name.getText(), symbol.getText(),
							description.getText(), gateType.getValue(), 5, 5, params);
				else
					newCb = referencedCb.createDeepCopyToNewName(fileLocation.getText(), name.getText(), symbol.getText(),
							description.getText(), gateType.getValue(), params);
			} catch (Exception e) {
				AppAlerts.showMessage(getStage(), "Could not make board", "Please check fields for and error", AlertType.ERROR);
				return false;
			}
			return addCircuitBoardToProject(getStage(), referencedCb, newCb, editAsNew);
		}

		@Override
		public CircuitBoardModel getFinish() {
			return newCb;
		}
		
		@FXML private void addParameter(ActionEvent ae) {
			parameterSelection.addElementToEnd("");
		}

		@Override
		public boolean hasFinish() {
			return true;
		}
	}
	
	
	private static boolean addCircuitBoardToProject(Stage stage, GateModel referencedCb, GateModel newCb, boolean editAsNew) {
		Project p = AppStatus.get().getFocusedProject();
		ProjectHashtable pht = p.getCircuitBoardModels();
		
		if (!editAsNew && referencedCb != null) {
			ButtonType buttonType = AppAlerts.showMessage(stage, "Apply changes to " + referencedCb.getName(), 
					"Are you sure you want to change circuit board model \"" + referencedCb.getName() + "\"? "
							+ "All instances of the previous circuit board model in this project will be changed to the new implementation.", AlertType.WARNING);
			if(buttonType == ButtonType.CANCEL)
				return false;
			
			if(referencedCb.getComputingType() == GateComputingType.QUANTUM && newCb.getComputingType() == GateComputingType.CLASSICAL) {
				buttonType = AppAlerts.showMessage(stage, "Gate Type set to be changed", "The circuit board is changing from a quantum board to a classical board."
						+ " All quantum registers will be removed with this action. Do you wish to continue?", AlertType.WARNING);
				if(buttonType == ButtonType.CANCEL)
					return false;
			}
			
			if(!newCb.getName().equals(referencedCb.getName()) && p.containsGateModel(newCb.getLocationString())) {
				buttonType = AppAlerts.showMessage(stage, "Override circuit board model " + newCb.getName(), 
						"A circuit board model with the name \"" + newCb.getName() + "\" already exists, "
								+ "do you want to override this circuit board model?"
								+ " All instances of the previous implementation of \""
								+ newCb.getName() + "\" in this project will be removed.", AlertType.WARNING);
				if(buttonType == ButtonType.CANCEL)
					return false;
			}
			
			pht.replace(referencedCb.getLocationString(), newCb);
		} else {
			if(referencedCb != null) {
				if(referencedCb.getComputingType() == GateComputingType.QUANTUM && newCb.getComputingType() == GateComputingType.CLASSICAL) {
					ButtonType buttonType = AppAlerts.showMessage(stage, "Gate Type set to be changed", "The circuit board is changing from a quantum board to a classical board."
							+ " All quantum registers will be removed with this action. Do you wish to continue?", AlertType.WARNING);
					if(buttonType == ButtonType.CANCEL)
						return false;
				}
			}
			
			if(pht.containsGateModel(newCb.getLocationString())) {
				ButtonType buttonType = AppAlerts.showMessage(stage, "Override circuit board model " + newCb.getName(), 
						"A circuit board model with the name \"" + newCb.getName() + "\" already exists, "
								+ "do you want to override this circuit board model?"
								+ " All instances of the previous implementation of \""
								+ newCb.getName() + "\" in this project will be removed.", AlertType.WARNING);
				if(buttonType == ButtonType.CANCEL)
					return false;
			}
			
			pht.put(newCb);
		}
		
		AppCommand.doAction(AppCommand.OPEN_GATE, newCb.getLocationString());
		
		return true;
	}
	
}
