package com.agateau.burgerparty.model;

import com.badlogic.gdx.utils.Array;

public class LevelWorld {
	public LevelWorld(int index, String dirName) {
		mIndex = index;
		mDirName = dirName;
	}

	public String getDirName() {
		return mDirName;
	}

	public void addLevel(Level level) {
		mLevels.add(level);
	}

	public Level getLevel(int index) {
		return mLevels.get(index);
	}

	public int getLevelCount() {
		return mLevels.size;
	}

	public int getWonStarCount() {
		int count = 0;
		for (Level level: mLevels) {
			count += level.getStars();
		}
		return count;
	}

	public int getTotalStarCount() {
		return mLevels.size * 3;
	}

	public int getIndex() {
		return mIndex;
	}

	private int mIndex;
	private String mDirName;
	private Array<Level> mLevels = new Array<Level>();
}
