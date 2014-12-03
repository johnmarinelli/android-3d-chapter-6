package com.robsexample.chapter6;

import android.content.Context;
import android.media.SoundPool;

public class Sound {
	private SoundPool mSoundPool;
	
	/* index to specific sound in sound pool */
	private int mSoundIndex = -1;
	
	float mLeftVolume = 1, mRightVolume = 1;
	
	int mPriority = 1;
	
	int mLoop = 0;
	
	/* speed to play sound */
	float mRate = 1.f;
	
	Sound(Context ctx, SoundPool pool, int resourceID) {
		mSoundPool = pool;
		mSoundIndex = mSoundPool.load(ctx, resourceID, 1);
	}
	
	void playSound() {
		mSoundPool.play(mSoundIndex, mLeftVolume, mRightVolume, mPriority, mLoop, mRate);
	}
}
