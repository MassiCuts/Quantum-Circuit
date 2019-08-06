package appFX.framework;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.stream.Stream;

import appFX.appUI.MainScene;
import appFX.appUI.appViews.BasicGateModelView;
import appFX.appUI.appViews.circuitBoardView.CircuitBoardView;
import appFX.appUI.utils.AppAlerts;
import appFX.appUI.utils.AppFXMLComponent;
import appFX.appUI.utils.AppFileIO;
import appFX.appUI.utils.AppTab;
import appFX.appUI.wizards.BasicGateModelEditWizard;
import appFX.appUI.wizards.CircuitBoardPropertiesEditWizard;
import appFX.appUI.wizards.CircuitBoardToPNGWizard;
import appFX.appUI.wizards.Wizard;
import appFX.framework.exportGates.ExportedGate;
import appFX.framework.exportGates.GateManager;
import appFX.framework.exportGates.GateManager.ExportException;
import appFX.framework.gateModels.BasicGateModel;
import appFX.framework.gateModels.CircuitBoardModel;
import appFX.framework.gateModels.CircuitBoardModel.RowType;
import appFX.framework.gateModels.GateModel;
import appFX.framework.gateModels.GateModel.GateComputingType;
import appFX.framework.gateModels.PresetGateType;
import appFX.framework.solderedGates.SolderedControlPin;
import appFX.framework.solderedGates.SolderedGate;
import appFX.framework.solderedGates.SolderedPin;
import appFX.framework.solderedGates.SolderedRegister;
import appFX.framework.solderedGates.SpacePin;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import utils.PrintStream;
import utils.PrintStream.SystemPrintStream;
import utils.customCollections.CommandParameterList;
import utils.customCollections.Pair;

/**
 * All application Commands are listed here.
 * @author quantumresearch
 *
 */
public enum AppCommand {
	HELP,
	
	OPEN_USER_PREFERENCES,
	
	EXPORT_TO_PNG_IMAGE,
	EXPORT_TO_QUIL,
	EXPORT_TO_QASM,
	EXPORT_TO_QUIPPER,
	
	IMPORT_FROM_QUIL,
	IMPORT_FROM_QASM,
	IMPORT_FROM_QUIPPER,
	
	OPEN_NEW_PROJECT,
	OPEN_PROJECT,
	SAVE_PROJECT_TO_FILESYSTEM,
	SAVE_PROJECT,
	
	RESET_CURRENT_TOOL,
	ADD_ROW_TO_FOCUSED_CB,
	ADD_COLUMN_TO_FOCUSED_CB,
	REMOVE_ROW_FROM_FOCUSED_CB,
	REMOVE_COLUMN_FROM_FOCUSED_CB,
	UNDO_FOCUSED_CB,
	REDO_FOCUSED_CB,
	
	RUN_QUIL,
	RUN_QASM,
	RUN_SIMULATION,
	
	REMOVE_GATE,
	EDIT_GATE,
	EDIT_AS_NEW_GATE,
	CREATE_GATE,
	RENAME_GATE,
	CREATE_CIRCUIT_BOARD,
	CREATE_DEFAULT_GATE,
	CREATE_ORACLE_GATE,
	OPEN_GATE,
	OPEN_CIRCUIT_BOARD_AND_FOCUS,
	OPEN_CIRCUIT_BOARD_AND_SHOW_ERROR,
	SET_AS_TOP_LEVEL,
	REMOVE_TOP_LEVEL,
	LIST_USER_GATES,
	SHOW_RENDERED_LATEX_FOR_GATE,
	SHOW_LATEX_STRING_FOR_GATE,
	
	ADD_UNTITLED_CIRCUIT_BOARD,
	
	DEBUG_CIRCUIT_BOARD,
	
	GET_GATE,
	GET_GATE_INSTANCES,
	
	TEST_SIMULATION
	
	;
	
	
	public static AppCommand getbyName(String command) { 
		for(AppCommand ac : AppCommand.values())
			if(ac.name().equalsIgnoreCase(command))
				return ac;
		return null;
	}
	
	public static Object doAction(AppCommand actionCommand, Object ... parameters) {
		return doAction(SystemPrintStream.get(), actionCommand, new CommandParameterList(parameters));
	}
	
	
	public static Object doAction(PrintStream commandResponse, AppCommand actionCommand, CommandParameterList parameters) {
		AppStatus status = AppStatus.get();
		MainScene ms = status.getMainScene();
		Stage primaryStage = status.getPrimaryStage();
		Project currentProject = status.getFocusedProject();
		PrintStream console = status.getConsole();
		
		
		switch(actionCommand) {
		case HELP:
			commandResponse.println("Command List (Commands are not Case Sensitive): \n", Color.BLUE);			
			for(AppCommand command : AppCommand.values())
				commandResponse.println(command.name(), Color.GREEN);
			
			
			break;
		
		case OPEN_USER_PREFERENCES:
			break;
		
		
		case EXPORT_TO_PNG_IMAGE:
			Wizard<Pair<BufferedImage, File>> wizard = new CircuitBoardToPNGWizard();
			CircuitBoardView  cbv = getFocusedCircuitBoardView(ms, commandResponse);
			String circuitBoardName = "";
			if(cbv != null)
				circuitBoardName = cbv.getCircuitBoardModel().getLocationString();
			wizard.openWizardAndGetElement(circuitBoardName);
			break;
		case EXPORT_TO_QASM:
			break;
		case EXPORT_TO_QUIL:
			String code = Translator.exportToQUIL(currentProject);
			console.println(code);
			break;
		case EXPORT_TO_QUIPPER:
			break;
			
			
			
		case IMPORT_FROM_QASM:
			break;
		case IMPORT_FROM_QUIL:
			break;
		case IMPORT_FROM_QUIPPER:
			break;
			
			
			
			
		case OPEN_NEW_PROJECT:
			status.setFocusedProject(Project.createNewTemplateProject());
			break;
		case OPEN_PROJECT:
			Project newProject = AppFileIO.openProject(primaryStage);
			if(newProject != null)
				status.setFocusedProject(newProject);
			break;
		case SAVE_PROJECT:
			if(AppFileIO.saveProject(currentProject, primaryStage) == AppFileIO.SUCCESSFUL)
				status.setProjectSavedFlag();
			else 
				return false;
			break;
		case SAVE_PROJECT_TO_FILESYSTEM:
			if(AppFileIO.saveProjectAs(currentProject, primaryStage) == AppFileIO.SUCCESSFUL)
				status.setProjectSavedFlag();
			break;

			
		case RESET_CURRENT_TOOL:
			cbv = getFocusedCircuitBoardView(ms, commandResponse);
			
			if(cbv == null)
				return null;
			
			cbv.getToolActionManager().resetCurrentTool();
			
			break;
			
			
		case REMOVE_COLUMN_FROM_FOCUSED_CB:
			cbv = getFocusedCircuitBoardView(ms, commandResponse);
			
			if(cbv == null)
				return null;
			
			
			CircuitBoardModel cb = cbv.getCircuitBoardModel();
			
			try {
				cb.removeColumns(cb.getColumns() - 1, cb.getColumns());
			} catch (IllegalArgumentException e) {
				AppAlerts.showMessage(primaryStage, "Could not remove Column", e.getMessage(), AlertType.ERROR);
			}
			
			
			break;
		case REMOVE_ROW_FROM_FOCUSED_CB:
			cbv = getFocusedCircuitBoardView(ms, commandResponse);
			
			if(cbv == null)
				return null;
			
			cb = cbv.getCircuitBoardModel();
			
			try {
				cb.removeRows(cb.getRows() - 1, cb.getRows());
			} catch (IllegalArgumentException e) {
				AppAlerts.showMessage(primaryStage, "Could not remove Row", e.getMessage(), AlertType.ERROR);
			}
			
			break;
		case ADD_COLUMN_TO_FOCUSED_CB:
			cbv = getFocusedCircuitBoardView(ms, commandResponse);
			
			if(cbv == null)
				return null;
			
			cb = cbv.getCircuitBoardModel();
			
			try {
				cb.addColumns(cb.getColumns(), 1);
			} catch (IllegalArgumentException e) {
				AppAlerts.showMessage(primaryStage, "Could not add Column", e.getMessage(), AlertType.ERROR);
			}
			
			break;
		case ADD_ROW_TO_FOCUSED_CB:
			cbv = getFocusedCircuitBoardView(ms, commandResponse);
			
			if(cbv == null)
				return null;
			
			cb = cbv.getCircuitBoardModel();
			
			RowType rowType;

			if(cb.getComputingType() == GateComputingType.CLASSICAL) {
				rowType = RowType.CLASSICAL;
			} else {
				rowType = RowType.QUANTUM;
			}
			
			try {
				cb.addRows(cb.getRows(), 1, rowType);
			} catch (IllegalArgumentException e) {
				AppAlerts.showMessage(primaryStage, "Could not add Row", e.getMessage(), AlertType.ERROR);
			}
			
			break;
		case UNDO_FOCUSED_CB:
			cbv = getFocusedCircuitBoardView(ms, commandResponse);
			
			if(cbv == null)
				return null;
			
			cb = cbv.getCircuitBoardModel();
			cb.undo();
			break;
		case REDO_FOCUSED_CB:
			cbv = getFocusedCircuitBoardView(ms, commandResponse);
			
			if(cbv == null)
				return null;
			
			cb = cbv.getCircuitBoardModel();
			cb.redo();
			break;	
			
			
		
		case RUN_QASM:
			console.println("Running QASM", Color.BLUE);
			break;
		case RUN_QUIL:
			console.println("Running QUIL", Color.BLUE);
			String quil = Translator.exportToQUIL(currentProject);
			console.println(Executor.execute(quil));
			break;
		case RUN_SIMULATION:
			console.println("Running Simulation", Color.BLUE);
			String output = Executor.executeInternal(currentProject);
			console.println(output);
			System.out.println(output);
			break;
		
		
			
		case RENAME_GATE:
			String oldGateLocationString = parameters.getString(0);
			String newGateLocationString = parameters.getString(1);
			
			if(oldGateLocationString.equals(newGateLocationString)) {
				commandResponse.println("The old name and new name are the same. No refactoring took place."); 
				return null;
			}
			
			GateModel gmOld = currentProject.getGateModel(oldGateLocationString);
			
			if(!assertExists(oldGateLocationString, gmOld, commandResponse))
				return null;
			
			GateModel gmNew = currentProject.getGateModel(newGateLocationString);
			
			if(gmNew != null) {
				if(gmNew.isPreset()) {
					commandResponse.printErrln("Gate \"" + newGateLocationString +  "\" is a preset gate and cannot be renamed");
					AppAlerts.showMessage(primaryStage, "Cannot rename Gate", 
							"The gate is renamed to preset Gate which cannot be modified", AlertType.ERROR);
					return null;
				}
				
				ButtonType buttonType = AppAlerts.showMessage(primaryStage, "Override Gate Model " + gmNew.getName(), 
						"A Gate Model with the name \"" + gmNew.getName() + "\" already exists, "
								+ "do you want to override this gate model?"
								+ " All instances of the previous implementation of \""
								+ gmNew.getName() + "\" in this project will be removed.", AlertType.WARNING);
				if(buttonType != ButtonType.OK)
					return null;
			}
			
			String newGateName = gmOld.getName();
			String newGateSymbol = gmOld.getSymbol();
			String newGateDescription = gmOld.getDescription();
			
			if(parameters.size() > 2) {
				newGateSymbol = parameters.getString(2);
				if(parameters.size() > 3)
					newGateDescription = parameters.getString(3);
			}

			
			GateModel replacement = gmOld.shallowCopyToNewName(newGateLocationString, newGateName, newGateSymbol, newGateDescription);
			
			if(gmOld instanceof CircuitBoardModel) {
				currentProject.getCircuitBoardModels().replace(oldGateLocationString, replacement);
				CircuitBoardView.openCircuitBoard(newGateLocationString);
			} else if (gmOld instanceof BasicGateModel) {
				currentProject.getCustomGates().replace(oldGateLocationString, replacement);
				ms.getViewManager().addView(new BasicGateModelView((BasicGateModel)replacement));
			} else {
				return null;
			}

			break;
		case CREATE_GATE:
			
			for(String param : parameters.stringIterable()) {
				String[] parts = param.split("\\.");
				
				String name = parts[0];
				String ext = parts[1]; 
				if(parts.length != 2) {
					commandResponse.printErrln("The formal name \"" + param +  "\" is not a valid name. It must have a proper extension.");
					continue;
				}
				if(ext.equals(BasicGateModel.GATE_MODEL_EXTENSION)) {
					BasicGateModelEditWizard.createNewGate(name);
				} else if (ext.equals(CircuitBoardModel.CIRCUIT_BOARD_EXTENSION)) {
					CircuitBoardPropertiesEditWizard.createNewGate(name);
				}
			}
			break;
		case CREATE_CIRCUIT_BOARD:
			CircuitBoardPropertiesEditWizard.createNewGate();
			break;
		case CREATE_DEFAULT_GATE:
			BasicGateModelEditWizard.createNewGate();
			break;
		case CREATE_ORACLE_GATE:
			
			break;
		
		case EDIT_AS_NEW_GATE:
			for(String param : parameters.stringIterable()) {
				GateModel gm = currentProject.getGateModel(param);
				if(!assertExists(param, gm, commandResponse))
					continue;
				
				if(gm instanceof BasicGateModel) {
					BasicGateModelEditWizard.editAsNewGate(gm.getLocationString());
				} else if (gm instanceof CircuitBoardModel) {
					CircuitBoardPropertiesEditWizard.editAsNewGate(gm.getLocationString());
				}
			}
			break;
		case EDIT_GATE:
			for(String param : parameters.stringIterable()) {
				GateModel gm = currentProject.getGateModel(param);
				if(!assertExists(param, gm, commandResponse))
					continue;
				
				if(gm.isPreset()) {
					commandResponse.println("Gate \"" + param +  "\" is a preset gate and cannot be edited");
					continue;
				}
				
				if(gm instanceof BasicGateModel) {
					BasicGateModelEditWizard.editGate(gm.getLocationString());
				} else if (gm instanceof CircuitBoardModel) {
					CircuitBoardPropertiesEditWizard.editGate(gm.getLocationString());
				}
			}
			break;
		case REMOVE_GATE:
			for(String param : parameters.stringIterable()) {
				GateModel gm = currentProject.getGateModel(param);
				
				if(!assertExists(param, gm, commandResponse))
					continue;
				
				if (gm.isPreset()) {
					commandResponse.printErrln("Gate \"" + param +  "\" is a preset gate and cannot be removed");
					continue;
				}

				ButtonType buttonType = AppAlerts.showMessage(primaryStage, "Remove Gate?", "Are you sure you want to remove this gate? "
						+ "All instances of this gate will be removed.", AlertType.CONFIRMATION);
				
				if(buttonType != ButtonType.OK)
					return null;
				
				if(gm instanceof BasicGateModel) {
					currentProject.getCustomGates().remove(gm.getLocationString());
				} else if (gm instanceof CircuitBoardModel) {
					currentProject.getCircuitBoardModels().remove(gm.getLocationString());
				}
			}
			break;
			
		case OPEN_GATE:
			for(String param : parameters.stringIterable()) {
				GateModel gm = currentProject.getGateModel(param);
				if(!assertExists(param, gm, commandResponse))
					continue;
				
				if(gm instanceof BasicGateModel) {
					ms.getViewManager().addView(new BasicGateModelView((BasicGateModel) gm));
				} else if (gm instanceof CircuitBoardModel) {
					CircuitBoardView.openCircuitBoard(gm.getLocationString());
				}
			}
			break;
		case OPEN_CIRCUIT_BOARD_AND_FOCUS:
			String gateName = parameters.getString(0);
			cbv = CircuitBoardView.openCircuitBoard(gateName);
			double row = getDoubleFromObject(parameters.get(1));
			double column = getDoubleFromObject(parameters.get(2));
			cbv.getRenderer().scrollToGrid(row, column);
			break;
		case OPEN_CIRCUIT_BOARD_AND_SHOW_ERROR:
			gateName = parameters.getString(0);
			cbv = CircuitBoardView.openCircuitBoard(gateName);
			int rowStart = getIntFromObject(parameters.get(1));
			int rowEnd 	=  getIntFromObject(parameters.get(2));
			int columnInt = getIntFromObject(parameters.get(3));
			cbv.renderErrorAt(rowStart, rowEnd, columnInt);
			break;
		case SET_AS_TOP_LEVEL:
			GateModel gm = currentProject.getGateModel(parameters.getString(0));
			
			if(!assertExists(parameters.getString(0), gm, commandResponse))
				return null;
			
			if(gm instanceof CircuitBoardModel) {
				if(gm.getLocationString().equals(currentProject.getTopLevelCircuitLocationString())) {
					commandResponse.println("Circuit Board \"" + parameters.get(0) + "\" is already top level");
					return null;
				} else {
					currentProject.setTopLevelCircuitName(parameters.getString(0));
				}
			} else {
				commandResponse.println("Gate \"" + parameters.get(0) + "\" is not a circuit board");
			}
			break;
			
		case REMOVE_TOP_LEVEL:
			if(currentProject.getTopLevelCircuitLocationString() == null) {
				commandResponse.println("There is no circuit board set as top level");
				return null;
			}
			currentProject.setTopLevelCircuitName(null);
			break;
			
		case ADD_UNTITLED_CIRCUIT_BOARD:
			CircuitBoardView.openCircuitBoard(currentProject.addUntitledSubCircuit());
			break;
			
		case GET_GATE:
			gm = currentProject.getGateModel(parameters.getString(0));
			if(!assertExists(parameters.getString(0), gm, commandResponse))
				return null;
			return gm;
			
		case GET_GATE_INSTANCES:
			gm = currentProject.getGateModel(parameters.getString(0));
			
			if(!assertExists(parameters.getString(0), gm, commandResponse))
				return null;
			
			if(!(gm instanceof CircuitBoardModel)) {
				commandResponse.printErrln("Gate \"" + parameters.get(0) +  "\" must be a circuit board");
				return null;
			}
			
			
			if(!currentProject.containsGateModel(parameters.getString(1))) {
				commandResponse.printErrln("Gate \"" + parameters.get(1) +  "\" does not exist");
				return null;
			}
			
			int insts = ((CircuitBoardModel) gm).getOccurrences(parameters.getString(1));
			
			commandResponse.println(Integer.toString(insts));
			
			return insts;
			
		case LIST_USER_GATES:
			commandResponse.println("Project Circuit Boards:", Color.BLUE);
			for(String modelName : currentProject.getCircuitBoardModels().getGateNameIterable())
				commandResponse.println(modelName);

			commandResponse.println("\nProject Custom Gates:", Color.BLUE);
			for(String modelName : currentProject.getCustomGates().getGateNameIterable())
				commandResponse.println(modelName);
			break;
			
		case SHOW_RENDERED_LATEX_FOR_GATE:
			break;
			
		case SHOW_LATEX_STRING_FOR_GATE:
			break;
			
		case DEBUG_CIRCUIT_BOARD:
			CircuitBoardModel model = (CircuitBoardModel) currentProject.getCircuitBoardModels().get(parameters.getString(0));
			if(!assertExists(parameters.getString(0), model, commandResponse))
				return null;
				
			int solderID = -1;
			SolderedGate sg = null;
			
			String[][] debugS = new String[model.getColumns()][model.getRows()];
			
			int[] largestSpaces = new int[model.getColumns()];
			
			for(int c = 0; c < model.getColumns(); c++) {
				for(int r = 0; r < model.getRows(); r++) {
					SolderedPin sp = model.getSolderedPinAt(r, c);
					String type = "";
					if(sp instanceof SolderedRegister) {
						type = Integer.toString(((SolderedRegister)sp).getSolderedGatePinNumber());
					} else if(sp instanceof SpacePin) {
						SpacePin spacePin = (SpacePin) sp;
						type = "(";
						if(spacePin.isInputLinked())
							type += spacePin.getInputReg();
						if(sp instanceof SolderedControlPin) {
							type += ((SolderedControlPin)sp).getControlStatus() ? "T" : "F";
						} else {
							type += "_";
						}
						if(spacePin.isOutputLinked())
							type += spacePin.getOutputReg();
						type += ")";
					}
					SolderedGate next  = sp.getSolderedGate();
					if(next != sg) {
						sg = next;
						solderID++;
					}
					
					String inBodyString = sp.isWithinBody()? "I" : "O";
					
					String firstCharName = sg.getGateModelLocationString().substring(0, 1);
					gm = AppStatus.get().getFocusedProject().getGateModel(sg.getGateModelLocationString());
					if(gm != null)
						firstCharName = gm.getSymbol();
					
					debugS[c][r] = firstCharName + type + "->(" + inBodyString + solderID + ")";
					
					largestSpaces[c] = largestSpaces[c] > debugS[c][r].length() ? largestSpaces[c] : debugS[c][r].length();
				}
			}
			
			for(int r = 0; r < model.getRows(); r++) {
				for(int c = 0; c < model.getColumns(); c++) {
					commandResponse.print(debugS[c][r]);
					String offsetFix = "  ";
					for(int i = debugS[c][r].length(); i < largestSpaces[c]; i++)
						offsetFix += " ";
					commandResponse.print(offsetFix);
				}
				commandResponse.println("");
			}
			
			break;
		case TEST_SIMULATION:
			try {
				Stream<ExportedGate> exportedGates = GateManager.exportGates(currentProject);
				exportedGates = exportedGates.filter((gate) -> {
					if(gate.isPresetGate()) {
						PresetGateType type = gate.getPresetGateType();
						if(type == PresetGateType.IDENTITY)
							return false;
					}
					return true;
				});
				exportedGates.forEach((gate) -> {
					System.out.println("New Gate");
					int[] registers = gate.getGateRegister();
					for(int i = 0; i < registers.length; i++)
						System.out.print(registers[i] + " ");
					System.out.println("");
				});
				exportedGates.close();
			} catch (ExportException e) {
				e.printStackTrace();
			}
			
			break;
		}
		
		return null;
	}
	
	private static int getIntFromObject(Object o) {
		int i;
		if(o instanceof String)
			i = Integer.parseInt((String)o);
		else
			i = (Integer) o;
		return i;
	}
	
	private static double getDoubleFromObject(Object o) {
		double d;
		if(o instanceof String)
			d = Double.parseDouble((String)o);
		else
			d = (Double) o;
		return d;
	}
	
	private static boolean assertExists(String gateModel, GateModel gm, PrintStream commandResponse) {
		if(gm == null) {
			commandResponse.println("Gate \"" + gateModel +  "\" does not exist");
			return false;
		} else {
			return true;
		}
	}
	
	private static CircuitBoardView getFocusedCircuitBoardView(MainScene ms, PrintStream commandResponse) {
		AppTab t = ms.getViewManager().getCenterFocusedView();
		if(t == null) {
			commandResponse.printErrln("No circuit board is opened and focused");
			return null;
		}
		
		AppFXMLComponent component = t.getAppFXMLComponent();
		
		if(!(component instanceof CircuitBoardView)) {
			commandResponse.printErrln("No circuit board is opened and focused");
			return null;
		}
			
		return (CircuitBoardView) component;
	}
	
}
