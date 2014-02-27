package com.agateau.burgerparty.model;

import java.util.HashSet;

import com.agateau.burgerparty.model.World.Score;
import com.agateau.burgerparty.utils.FileUtils;
import com.agateau.burgerparty.utils.Signal1;

public class BurgerPartyGameStats {
	private HashSet<Object> mHandlers = new HashSet<Object>();

	public final CounterGameStat mealServedCount;

	public final AchievementManager manager = new AchievementManager();

	public BurgerPartyGameStats() {
		mealServedCount = new CounterGameStat("mealServedCount");
		manager.addGameStat(mealServedCount);

		CounterAchievement achievement = new CounterAchievement("burger-master", "Burger Master", "Serve 10 burgers");
		achievement.init(mealServedCount, 10);
		manager.addAchievement(achievement);

		manager.setGameStatsFileHandle(FileUtils.getUserWritableFile("gamestats.xml"));
		manager.setAchievementsFileHandle(FileUtils.getUserWritableFile("achievements.xml"));
		manager.load();
	}
}
