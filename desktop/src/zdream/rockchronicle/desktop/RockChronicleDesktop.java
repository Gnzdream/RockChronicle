package zdream.rockchronicle.desktop;

import com.badlogic.gdx.Game;

import zdream.rockchronicle.RockChronicle;

public class RockChronicleDesktop extends Game {
	
	{
		RockChronicle.pushGame(this);
	}

	@Override
	public void create() {
		RockChronicle.INSTANCE.create();
	}

}
