package appFX.appUI.appViews;

import java.net.URI;
import java.net.URL;
import java.util.Iterator;
import java.util.ResourceBundle;

import appFX.appUI.MainScene;
import appFX.appUI.utils.AppFileIO;
import appFX.appUI.utils.GateModelContextMenu;
import appFX.framework.AppCommand;
import appFX.framework.AppStatus;
import appFX.framework.Project;
import appFX.framework.gateModels.BasicGateModel;
import appFX.framework.gateModels.CircuitBoardModel;
import appFX.framework.gateModels.GateModel;
import appFX.framework.gateModels.PresetGateType;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import utils.customCollections.Pair;

public class ProjectHierarchy extends AppView implements Initializable, EventHandler<MouseEvent> {
	
	@FXML private TreeView<String> projectView;
	private TreeItem<String> root, topLevel, subCircuits, customGates, presetGates;
	private ContextMenu cm = new ContextMenu();
	
	public ProjectHierarchy() {
		super("views/ProjectHierarchyView.fxml", "Project Hierarchy", ViewLayout.LEFT, true);
	}
	
	public void setFocusedProject(Project project) {
		root = new TreeItem<>(project.getProjectName() + "." + AppFileIO.QUANTUM_PROJECT_EXTENSION);
		topLevel = new TreeItem<>("Top Level Board");
		subCircuits = new TreeItem<>("Sub-Circuits");
		customGates = new TreeItem<>("Custom Gates");
		presetGates = new TreeItem<>("Preset Gates");
		
		String topLevelName = project.getTopLevelCircuitLocationString();
		boolean hasTopLevel = topLevelName != null;
		
		if(topLevelName != null) {
			topLevel.getChildren().add(new TreeItem<>(topLevelName));
		}
			
		for(String name : project.getCircuitBoardModels().getGateNameIterable())
			if(!hasTopLevel || !name.equals(topLevelName))
				subCircuits.getChildren().add(new TreeItem<String>(name));
		
		for(String name : project.getCustomGates().getGateNameIterable())
			customGates.getChildren().add(new TreeItem<String>(name));
		
		for(PresetGateType dg : PresetGateType.values())
			presetGates.getChildren().add(new TreeItem<String>(dg.getModel().getLocationString()));
		
		
		root.getChildren().add(topLevel);
		root.getChildren().add(subCircuits);
		root.getChildren().add(customGates);
		root.getChildren().add(presetGates);
		
		projectView.setRoot(root);
		cm.hide();
	}
	
	@Override
	public void handle(MouseEvent event) {
		TreeItem<String> item = projectView.getSelectionModel().getSelectedItem();
		
		if(item != null && event.getButton().equals(MouseButton.PRIMARY) && event.getClickCount() == 2){
			cm.hide();
			
			TreeItem<String> parent = item.getParent();
			
			if(parent == topLevel || parent == subCircuits || parent == presetGates
					|| parent == customGates)
				AppCommand.doAction(AppCommand.OPEN_GATE, item.getValue());
			
		} else if (item != null && event.getButton().equals(MouseButton.SECONDARY) && event.getClickCount() == 1) {
			cm.hide();
			cm = new CustomContextMenu(item);
			cm.show(projectView, event.getScreenX(), event.getScreenY());
		} else {
			cm.hide();
		}
	}
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		Project p = AppStatus.get().getFocusedProject();
		if( p != null )
			setFocusedProject(p);

		projectView.setOnMouseClicked(this);
	}

	@Override
	public void receive(Object source, String methodName, Object... args) {
		AppStatus status = AppStatus.get();
		Project p = status.getFocusedProject();
		
		if(source instanceof AppStatus && methodName.equals("setFocusedProject")) {
			setFocusedProject( (Project) args[0] );
		} else if(source instanceof Project && methodName.equals("setProjectFileLocation")) {
			root.setValue(Project.getProjectNameFromURI((URI) args[0]));
		} else if(source instanceof Project && methodName.equals("setTopLevelCircuitName")) {
			topLevel.getChildren().clear();
			if(args[0] != null) {
				topLevel.getChildren().add(new TreeItem<String>(args[0].toString()));
				Iterator<TreeItem<String>> iter = subCircuits.getChildren().iterator();
				while(iter.hasNext())
					if(iter.next().getValue().equals(args[0]))
						iter.remove();
			}
			
			String previous = p.getTopLevelCircuitLocationString();
			if(previous != null && p.getCircuitBoardModels().containsGateModel(previous)) {
				boolean containsItem = false;
				for(TreeItem<String> item: subCircuits.getChildren()) {
					if(item.getValue().equals(previous.toString())) {
						containsItem = true;
						break;
					}
				}
				if(!containsItem)
					subCircuits.getChildren().add(new TreeItem<String>(previous.toString()));
			}
			
		} else 	if(p != null &&(source == p.getCustomGates() || source == p.getCircuitBoardModels())) {
			Pair<TreeItem<String>, String> list = getListFromSource(p, source);
			
			
			if(methodName.equals("put")) {
				GateModel replacement = (GateModel) args[0];
				removeSolderableByName(list.first(), replacement.getLocationString());
				addSolderable(list.first(), replacement);
			} else if(methodName.equals("replace")) {
				String name = (String) args[0];
				GateModel replacement = (GateModel) args[1];
				removeSolderableByName(list.first(), name);
				removeSolderableByName(list.first(), replacement.getLocationString());
				addSolderable(list.first(), replacement);
			} else 	if(methodName.equals("remove")) {
				String name = (String) args[0];
				removeSolderableByName(list.first(), name);
			}
		}
		cm.hide();
	}
	
	private Pair<TreeItem<String>, String> getListFromSource(Project p, Object source) {
		if(source == p.getCustomGates()) {
			return new Pair<>(customGates, BasicGateModel.GATE_MODEL_EXTENSION);
		} else if (source == p.getCircuitBoardModels()) {
			return new Pair<>(subCircuits, CircuitBoardModel.CIRCUIT_BOARD_EXTENSION);
		}
		return null;
	}

	private void removeSolderableByName(TreeItem<String> treeItem, String name) {
		Iterator<TreeItem<String>> iter = treeItem.getChildren().iterator();
		while(iter.hasNext()) {
			TreeItem<String> next= iter.next();
			
			if(next.getValue().equals(name)) {
				iter.remove();
				return;
			}
		}
	}
	
	private void addSolderable(TreeItem<String> treeItem, GateModel solderable) {
		treeItem.getChildren().add(new TreeItem<String>(solderable.getLocationString()));
	}
	
	@SuppressWarnings("unused")
	private class CustomContextMenu extends ContextMenu {
		
		private CustomContextMenu(TreeItem<String> selected) {
			TreeItem<String> parent = selected.getParent();
			
			AppStatus as = AppStatus.get();
			
			MainScene ms = as.getMainScene();
			Project p = as.getFocusedProject();
			
			ObservableList<MenuItem> elements = getItems();
			
			if (parent == topLevel || parent == subCircuits || parent == presetGates || parent == customGates) {
				GateModel gm = p.getGateModel(selected.getValue());
				GateModelContextMenu.addToElements(gm, elements, p);
				
			} else if (selected == subCircuits) {
				MenuItem open = new MenuItem("Add Untitled Circuit Board");
				open.setOnAction((e) -> AppCommand.doAction(AppCommand.ADD_UNTITLED_CIRCUIT_BOARD));
				elements.add(open);
			} else if (selected == customGates) {
				MenuItem open = new MenuItem("Create Gate");
				open.setOnAction((e) -> AppCommand.doAction(AppCommand.CREATE_DEFAULT_GATE));
				elements.add(open);
			}
		}
		
		
	}
	
}
