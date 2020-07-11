package zdream.rockchronicle.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

import zdream.rockchronicle.core.Config;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		
		config.width = 25 * Config.INSTANCE.blockWidth; // 25 * 48
		config.height = 14 * Config.INSTANCE.blockHeight; // 14 * 48
		config.title = "Rock Chronicle";
		
		new LwjglApplication(new RockChronicleDesktop(), config);
	}
}
