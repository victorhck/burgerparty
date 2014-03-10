package com.agateau.burgerparty.burgerjeweled;

import com.agateau.burgerparty.Assets;
import com.agateau.burgerparty.BurgerPartyGame;
import com.agateau.burgerparty.model.MiniGame;

public class BurgerjeweledMiniGame extends MiniGame {
    public BurgerjeweledMiniGame(Assets assets, BurgerPartyGame game) {
        super(assets, game);
    }

    @Override
    public void showStartScreen() {
        setScreenAndDispose(new BurgerjeweledStartScreen(this));
    }

    @Override
    public void showMainScreen() {
        setScreenAndDispose(new BurgerjeweledMainScreen(this));
    }
}
