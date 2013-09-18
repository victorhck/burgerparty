package com.agateau.burgerparty.view;

import com.agateau.burgerparty.Kernel;
import com.agateau.burgerparty.model.LevelWorld;
import com.agateau.burgerparty.utils.Anchor;
import com.agateau.burgerparty.utils.AnchorGroup;
import com.agateau.burgerparty.utils.UiUtils;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.Scaling;

public class AbstractWorldView extends AnchorGroup {
	private static final float SLIDE_IN_ANIM_DURATION = 0.2f;

	public AbstractWorldView(LevelWorld world) {
		setFillParent(true);
		setSpacing(UiUtils.SPACING);
		setupLayers();
		setupDecor(world);
		setupAnchors();
	}

	@Override
	public void draw(SpriteBatch batch, float parentAlpha) {
		batch.setColor(1, 1, 1, parentAlpha);
		batch.draw(mBackgroundRegion, 0, 0, getWidth(), getHeight());
		super.draw(batch, parentAlpha);
	}

	@Override
	public void layout() {
		float width = getWidth();
		float height = getHeight();
		boolean resized = width != mWidth || height != mHeight;
		mWidth = width;
		mHeight = height;

		if (resized) {
			mInventoryView.setWidth(width);
			mWorkbench.setWidth(width);
			mWorkbench.invalidate();
		}

		super.layout();

		if (resized) {
			onResized();
		}
	}

	protected void onResized() {
	}

	private AnchorGroup createLayer() {
		AnchorGroup layer = new AnchorGroup();
		layer.setFillParent(true);
		layer.setSpacing(UiUtils.SPACING);
		layer.setTouchable(Touchable.childrenOnly);
		addActor(layer);
		return layer;
	}

	private void setupLayers() {
		mCustomersLayer = createLayer();
		mCounterLayer = createLayer();
		mInventoryLayer = createLayer();
		mHudLayer = createLayer();
	}

	private void setupDecor(LevelWorld world) {
		TextureAtlas atlas = Kernel.getTextureAtlas();
		String dirName = world.getDirName();

		mBackgroundRegion = atlas.findRegion(dirName + "background");

		TextureRegion region = atlas.findRegion(dirName + "workbench");
		mWorkbench = new Image(region);
		mWorkbench.setScaling(Scaling.stretch);

		mInventoryView = new InventoryView(dirName, atlas);
		mInventoryLayer.addActor(mInventoryView);
	}

	private void setupAnchors() {
		mCounterLayer.addRule(mWorkbench, Anchor.BOTTOM_LEFT, mInventoryView, Anchor.TOP_LEFT);
	}

	protected void slideInMealView(MealView view) {
		view.setPosition(-view.getWidth(), mWorkbench.getY() + 0.48f * UiUtils.SPACING);
		view.addAction(Actions.moveTo((getWidth() - view.getWidth()) / 2, view.getY(), SLIDE_IN_ANIM_DURATION, Interpolation.pow2Out));
		mCounterLayer.addActor(view);
	}

	protected AnchorGroup mCustomersLayer;
	protected AnchorGroup mCounterLayer;
	protected AnchorGroup mInventoryLayer;
	protected AnchorGroup mHudLayer;

	protected InventoryView mInventoryView;
	protected Image mWorkbench;
	protected float mWidth = -1;
	protected float mHeight = -1;

	private TextureRegion mBackgroundRegion;
}
