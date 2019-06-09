package zdream.rockchronicle.core.module.buff;

import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonValue.ValueType;

import zdream.rockchronicle.core.character.CharacterEntry;
import zdream.rockchronicle.core.character.event.CharacterEvent;
import zdream.rockchronicle.platform.world.LevelWorld;

public class GradualRecoveryBuffModule extends BuffModule {
	
	public GradualRecoveryBuffModule(CharacterEntry parent) {
		super(parent, "gradualRecovery");
	}

	@Override
	public void determine(LevelWorld world, int index, boolean hasNext) {
		super.determine(world, index, hasNext);
		
		CharacterEvent event = new CharacterEvent("inside_recovery");
		JsonValue v = new JsonValue(ValueType.object);
		v.addChild("health", new JsonValue(1));
		event.value = v;
		parent.publish(event);
	}

}
