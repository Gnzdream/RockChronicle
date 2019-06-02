package zdream.rockchronicle.core.module.control;

import zdream.rockchronicle.core.character.CharacterEntry;
import zdream.rockchronicle.core.module.AbstractModule;

public abstract class ControlModule extends AbstractModule {
	
	public static final String NAME = "Control";
	
	public ControlModule(CharacterEntry ch) {
		super(ch);
	}

	@Override
	public String name() {
		return NAME;
	}
	
	@Override
	public int priority() {
		return 999;
	}
	
}
