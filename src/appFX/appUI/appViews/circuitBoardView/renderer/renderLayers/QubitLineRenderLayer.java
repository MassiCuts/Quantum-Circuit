package appFX.appUI.appViews.circuitBoardView.renderer.renderLayers;

import appFX.appUI.appViews.circuitBoardView.renderer.GateRenderer;
import appFX.appUI.utils.CustomFXGraphics;
import appFX.framework.gateModels.CircuitBoardModel.RowTypeList;
import graphicsWrapper.FocusData;
import graphicsWrapper.Graphics;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import utils.customCollections.immutableLists.ImmutableTree;

public class QubitLineRenderLayer extends RenderLayer {

	public QubitLineRenderLayer(double width, double height) {
		super(width, height);
	}

	@Override
	@SuppressWarnings("unchecked")
	public void onDraw(Graphics<Image, Font, Color> graphics, Object... userArgs) {
		ImmutableTree<FocusData> fd = (ImmutableTree<FocusData>) userArgs[0];
		RowTypeList rowTypeList = (RowTypeList) userArgs[1];
		FocusData gridData = fd.getElement();
		GateRenderer.renderQubitLines(graphics, CustomFXGraphics.RENDER_PALETTE, gridData, rowTypeList);
	}

}
