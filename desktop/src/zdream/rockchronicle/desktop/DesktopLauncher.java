package zdream.rockchronicle.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		
		config.width = 600; // 25 * 24
		config.height = 336; // 14 * 24
		config.title = "Rock Chronicle";
		
		new LwjglApplication(new RockChronicleDesktop(), config);
	}
}
