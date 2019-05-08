package zdream.rockchronicle.core.character.module;

import zdream.rockchronicle.core.character.CharacterEntry;

/**
 * 管理跳跃参数的模块
 * 
 * @author Zdream
 * @since v0.0.1
 * @date 2019-05-05 (create)
 */
public abstract class JumpModule extends AbstractModule {
	
	public static final String NAME = "Jump";

	public JumpModule(CharacterEntry ch) {
		super(ch);
		// TODO Auto-generated constructor stub
	}

	@Override
	public String name() {
		return NAME;
	}
	
	@Override
	public int priority() {
		return 0x80;
	}

}
