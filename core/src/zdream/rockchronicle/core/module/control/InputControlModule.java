package zdream.rockchronicle.core.module.control;

import zdream.rockchronicle.core.character.CharacterEntry;
import zdream.rockchronicle.core.input.IControlListener;
import zdream.rockchronicle.core.input.InputCenter;
import zdream.rockchronicle.core.input.PlayerInput;

/**
 * 用键盘、手柄等外设进行控制的控制模块
 * 
 * @author Zdream
 * @since v0.0.1
 * @date
 *   2019-06-02 (created)
 *   2019-06-02 (last modified)
 */
public abstract class InputControlModule extends ControlModule implements IControlListener {

	public InputControlModule(CharacterEntry ch, String desc) {
		super(ch, desc);
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
