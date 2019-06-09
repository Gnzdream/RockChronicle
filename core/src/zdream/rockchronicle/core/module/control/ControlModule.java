package zdream.rockchronicle.core.module.control;

import zdream.rockchronicle.core.character.CharacterEntry;
import zdream.rockchronicle.core.module.AbstractModule;

public abstract class ControlModule extends AbstractModule {
	
	public static final String NAME = "control";
	
	public ControlModule(CharacterEntry ch, String desc) {
		super(ch, NAME, desc);
	}

	@Override
	public int priority() {
		return 999;
	}
	
}
