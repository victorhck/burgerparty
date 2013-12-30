package com.agateau.burgerparty.burgercopter;

import com.agateau.burgerparty.utils.AnchorGroup;
import com.agateau.burgerparty.utils.FileUtils;
import com.agateau.burgerparty.utils.StageScreen;
import com.agateau.burgerparty.view.BurgerPartyUiBuilder;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

public class BurgerCopterStartScreen extends StageScreen {
	public BurgerCopterStartScreen(BurgerCopterMiniGame miniGame) {
		super(miniGame.getAssets().getSkin());
		mMiniGame = miniGame;
		setupWidgets();
	}

	@Override
	public void onBackPressed() {
		mMiniGame.exiting.emit();
	}

	private void setupWidgets() {
		BurgerPartyUiBuilder builder = new BurgerPartyUiBuilder(mMiniGame.getAssets());
		builder.build(FileUtils.assets("screens/burgercopter/start.gdxui"));
		AnchorGroup root = builder.getActor("root");
		getStage().addActor(root);
		root.setFillParent(true);

		builder.<ImageButton>getActor("backButton").addListener(new ChangeListener() {
			public void changed(ChangeListener.ChangeEvent Event, Actor actor) {
				onBackPressed();
			}
		});
		builder.<ImageButton>getActor("startButton").addListener(new ChangeListener() {
			public void changed(ChangeListener.ChangeEvent Event, Actor actor) {
				mMiniGame.showMainScreen();
			}
		});
	}

	private BurgerCopterMiniGame mMiniGame;
}