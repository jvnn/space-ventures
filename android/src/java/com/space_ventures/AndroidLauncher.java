package com.space_ventures;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.Game;

public class AndroidLauncher extends AndroidApplication {
	public void onCreate (android.os.Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		try {
			Game game = (Game) Class.forName("com.space-ventures.core.Game").newInstance();
			initialize(game, true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
