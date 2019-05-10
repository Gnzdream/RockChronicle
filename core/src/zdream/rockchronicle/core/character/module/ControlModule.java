package zdream.rockchronicle.core.character.module;

import zdream.rockchronicle.core.character.CharacterEntry;
import zdream.rockchronicle.core.input.IControlListener;
import zdream.rockchronicle.core.input.InputCenter;
import zdream.rockchronicle.core.input.PlayerInput;

public abstract class ControlModule extends AbstractModule implements IControlListener {
	
	public static final String NAME = "Control";
	
	public ControlModule(CharacterEntry ch) {
		super(ch);
	}

	@Override
	public String name() {
		return NAME;
	}
	
	protected PlayerInput in;
	
	/**
	 * 用键位控制来绑定它
	 */
	public void bind(PlayerInput in) {
		this.in = in;
		// TODO 我们先尝试绑定左右方向键
		in.addControlListener(new int[] {
				InputCenter.MAP_LEFT, InputCenter.MAP_RIGHT, InputCenter.MAP_JUMP, InputCenter.MAP_ATTACK,
				InputCenter.MAP_RUSH
		}, this);
	}
	
	/**
	 * 解绑
	 */
	public void unbind() {
		in.removeControlListener(this);
		this.in = null;
	}
	
	@Override
	public int priority() {
		return 999;
	}
	
}
