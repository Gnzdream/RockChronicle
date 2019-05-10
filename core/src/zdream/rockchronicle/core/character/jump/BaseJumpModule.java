package zdream.rockchronicle.core.character.jump;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonValue.ValueType;

import zdream.rockchronicle.core.character.CharacterEntry;
import zdream.rockchronicle.core.character.event.CharacterEvent;
import zdream.rockchronicle.core.character.module.JumpModule;
import zdream.rockchronicle.core.character.motion.BoxSetter;
import zdream.rockchronicle.core.character.parameter.JsonCollector;
import zdream.rockchronicle.platform.world.LevelWorld;

/**
 * 默认的单段跳的行动模块
 * 
 * @author Zdream
 * @since v0.0.1
 * @date
 *   2019-05-07 (create)
 */
public class BaseJumpModule extends JumpModule {
	
	public float impulse; // param.impulse
	public float decay; // param.decay
	public float maxDropVelocity; // param.maxDropVelocity
	
	/**
	 * 本帧是否起跳
	 */
	public boolean jumpStart;
	
	/**
	 * 本帧是否结束跳跃
	 */
	public boolean jumpEnd;
	
	/**
	 * 本帧跳跃键是否按下
	 */
	public boolean inJump;
	
	/**
	 * 从起跳开始为 1, 时间每过一步, 数值加 1. 如果现在没有在跳跃, 数值为 0
	 */
	public int duration;
	
	protected JsonCollector jumpc;

	public BaseJumpModule(CharacterEntry ch) {
		super(ch);
	}

	@Override
	public void init(FileHandle file, JsonValue value) {
		super.init(file, value);
		
		// init jump 部分
		JsonValue ojump = value.get("jump");
		JsonValue oparam = ojump.get("param");
		this.impulse = oparam.getFloat("impulse") * LevelWorld.TIME_STEP;
		this.decay = oparam.getFloat("decay") * LevelWorld.TIME_STEP * LevelWorld.TIME_STEP;
		this.maxDropVelocity = oparam.getFloat("maxDropVelocity") * LevelWorld.TIME_STEP;
		
		parent.addSubscribe("ctrl_motion", this);
		addCollector(jumpc = new JsonCollector(this::getJumpJson, "jump"));
	}
	
	@Override
	public void willDestroy() {
		parent.removeSubscribe("ctrl_motion", this);
		super.willDestroy();
	}
	
	@Override
	public void determine(LevelWorld world, int index, boolean hasNext) {
		super.determine(world, index, hasNext);
		
		boolean bottomStop = parent.getBoolean(new String[] {"motion", "bottomStop"}, false);
		boolean topStop = parent.getBoolean(new String[] {"motion", "topStop"}, false);
		float ovy = parent.getFloat(new String[] {"box", "velocity", "y"}, 0);
		boolean stiffness = parent.getBoolean(new String[] {"state", "stiffness"}, false);
		
		float vy = ovy;
		if (bottomStop) {
			vy = 0;
			if (jumpStart && !stiffness) {
				// 执行跳跃
				vy = impulse;
			}
		} else if (!bottomStop) {
			vy += decay;
			if (vy < maxDropVelocity) {
				vy = maxDropVelocity;
			}
		}
		if (vy > 0 && (jumpEnd || topStop || stiffness)) {
			vy = 0;
		}
		
		parent.setJson("box", BoxSetter.newInstance().setVelocityY(vy).get());
	}
	
	@Override
	public void stepPassed() {
		jumpEnd = false;
		jumpStart = false;
		
		super.stepPassed();
	}
	
	public JsonValue getJumpJson() {
		JsonValue v = new JsonValue(ValueType.object);
		
		// 现在的参数部分
		v.addChild("duration", new JsonValue(duration));
		v.addChild("inJump", new JsonValue(inJump));
		v.addChild("jumpStart", new JsonValue(jumpStart));
		v.addChild("jumpEnd", new JsonValue(jumpEnd));
		
		// param 部分
		JsonValue oparam = new JsonValue(ValueType.object);
		v.addChild(oparam);
		oparam.addChild("impulse", new JsonValue(impulse));
		oparam.addChild("decay", new JsonValue(decay));
		oparam.addChild("maxDropVelocity", new JsonValue(maxDropVelocity));
		
		return v;
	}
	
	@Override
	public void receiveEvent(CharacterEvent event) {
		switch (event.name) {
		case "ctrl_motion":
			recvCtrlMotion(event);
			break;

		default:
			super.receiveEvent(event);
			break;
		}
	}
	
	private void recvCtrlMotion(CharacterEvent event) {
		inJump = event.value.getBoolean("jump");
		boolean jumpChange = event.value.getBoolean("jumpChange");
		
		jumpEnd = (!inJump && jumpChange);
		jumpStart = (inJump && jumpChange);
	}
	
}
