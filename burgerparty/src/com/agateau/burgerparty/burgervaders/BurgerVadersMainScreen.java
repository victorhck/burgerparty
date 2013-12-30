package com.agateau.burgerparty.burgervaders;

import com.agateau.burgerparty.utils.SpriteImage;
import com.agateau.burgerparty.utils.StageScreen;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Array;

public class BurgerVadersMainScreen extends StageScreen {
	public BurgerVadersMainScreen(BurgerVadersMiniGame miniGame) {
		super(miniGame.getAssets().getSkin());
		mMiniGame = miniGame;
		createBg();
		createEnemies();
		createPlayer();
		createHud();
	}

	@Override
	public void onBackPressed() {
		mMiniGame.showStartScreen();
	}

	private void createPlayer() {
		TextureRegion region = mMiniGame.getAssets().getTextureAtlas().findRegion("burgervaders/cannon");
		assert(region != null);
		mCannon = new Cannon(region);
		mCannon.setX((Gdx.graphics.getWidth() - region.getRegionWidth()) / 2);
		mCannon.setY(0);
		getStage().addActor(mCannon);
	}

	private void createBg() {
		TextureRegion region = mMiniGame.getAssets().getTextureAtlas().findRegion("levels/2/background");
		assert(region != null);
		setBackgroundActor(new Image(region));
	}

	private void createEnemies() {
	}

	private void createHud() {
		mScoreLabel = new Label("0", mMiniGame.getAssets().getSkin(), "lock-star-text");
		getStage().addActor(mScoreLabel);
		mScoreLabel.setX(0);
		mScoreLabel.setY(Gdx.graphics.getHeight() - mScoreLabel.getPrefHeight());
	}

	private void updateHud() {
		mScoreLabel.setText(String.valueOf((mScore / 10) * 10));
	}

	private BurgerVadersMiniGame mMiniGame;
	private SpriteImage mCannon;
	private Array<SpriteImage> mEnemies = new Array<SpriteImage>();
	private Array<SpriteImage> mBullets = new Array<SpriteImage>();
	private int mScore = 0;
	private Label mScoreLabel;
}
