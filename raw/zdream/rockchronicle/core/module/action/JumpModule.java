package zdream.rockchronicle.core.module.action;

import zdream.rockchronicle.core.character.CharacterEntry;
import zdream.rockchronicle.core.module.AbstractModule;

/**
 * 管理跳跃参数的模块
 * 
 * @author Zdream
 * @since v0.0.1
 * @date 2019-05-05 (create)
 */
public abstract class JumpModule extends AbstractModule {
	
	public static final String NAME = "jump";

	public JumpModule(CharacterEntry ch, String description) {
		super(ch, NAME, description);
	}

	@Override
	public int priority() {
		return 0x80;
	}

}
