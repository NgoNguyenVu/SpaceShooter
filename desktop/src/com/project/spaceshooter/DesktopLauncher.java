package com.project.spaceshooter;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.project.spaceshooter.SpaceShooterGame;

// Please note that on macOS your application needs to be started with the -XstartOnFirstThread JVM argument
public class DesktopLauncher {
	public static void main (String[] arg) {
		Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
		config.setWindowedMode(600, 900);
		config.setWindowIcon("assets/icons8-spaceship-32.png");

		config.setForegroundFPS(60);
		config.setTitle("SpaceShooter");
		new Lwjgl3Application(new SpaceShooterGame(), config);
	}
}
