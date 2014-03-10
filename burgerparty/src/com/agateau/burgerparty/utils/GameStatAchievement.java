package com.agateau.burgerparty.utils;

import java.util.HashSet;


/**
 * An Achievement which depends on one or more GameStat
 *
 * @author aurelien
 *
 */
public abstract class GameStatAchievement extends Achievement {
    private HashSet<Object> mHandlers = new HashSet<Object>();

    public GameStatAchievement(String id, String title, String description) {
        super(id, title, description);
    }

    public abstract void update();

    public void addDependentGameStat(GameStat stat) {
        stat.changed.connect(mHandlers, new Signal0.Handler() {
            @Override
            public void handle() {
                update();
            }
        });
    }
}
