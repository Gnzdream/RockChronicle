package zdream.rockchronicle.core.module.action;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.JsonValue;

import zdream.rockchronicle.core.character.CharacterEntry;
import zdream.rockchronicle.core.character.event.CharacterEvent;
import zdream.rockchronicle.core.move.IMovable;
import zdream.rockchronicle.platform.body.Box;
import zdream.rockchronicle.platform.world.LevelWorld;

/**
 * <p>默认的单段跳的行动模块
 * <p>允许角色在不同重力、合力影响的情况下计算纵向的速度
 * </p>
 * 
 * @author Zdream
 * @since v0.0.1
 * @date
 *   2019-05-07 (create)
 *   2019-05-20 (last modified)
 */
public class BaseJumpModule extends JumpModule implements IMovable {
	
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
	 * 本帧跳跃键是否在按下状态
	 */
	public boolean inJump;
	
	/**
	 * 从起跳开始为 1, 时间每过一步, 数值加 1. 如果现在没有在跳跃, 数值为 0
	 */
	public int duration;
	
	public BaseJumpModule(CharacterEntry ch) {
		super(ch, "base");
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
		parent.getBoxModule().addMovable(this, 10);
		setJumpParam();
	}
	
	@Override
	public void willDestroy() {
		parent.removeSubscribe("ctrl_motion", this);
		parent.getBoxModule().removeMovable(this);
		super.willDestroy();
	}
	
	@Override
	public void stepPassed() {
		jumpEnd = false;
		jumpStart = false;
		
		super.stepPassed();
	}
	
	/**
	 * 长期参数部分
	 */
	public void setJumpParam() {
		setSituation("jump.param.impulse", new JsonValue(impulse));
		setSituation("jump.param.decay", new JsonValue(decay));
		setSituation("jump.param.maxDropVelocity", new JsonValue(maxDropVelocity));
	}
	
	/**
	 * 临时参数部分
	 */
	public void setJumpState() {
		setState("jump.duration", new JsonValue(duration));
		setState("jump.inJump", new JsonValue(inJump));
		setState("jump.jumpStart", new JsonValue(jumpStart));
		setState("jump.jumpEnd", new JsonValue(jumpEnd));
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
		setJumpState();
	}
	
	/*
	 * 暂时数据
	 */
	float lastvy = 0;
	
	@Override
	public void determine(LevelWorld world, int index, boolean hasNext) {
		super.determine(world, index, hasNext);
		
		Box box = parent.getBoxModule().getBox();
		
		// 攀爬 (楼梯) 状态、悬挂状态、附着状态
		// TODO 如果在以上状态时, 下面的一切都不需要判断.
		
		boolean climbing = getBoolean("climb.climbing", false);
		if (climbing) {
			lastvy = 0;
			return;
		}
		
		boolean stiffness = getBoolean("health.stiffness", false);
		
		float vy = lastvy;
		float gravityScale = box.gravityScale;
		boolean onTheGround = getBoolean("state.onTheGround", false);
		
		if (gravityScale > 0) {
			if (onTheGround) {
				vy = 0;
				if (jumpStart && !stiffness) {
					// 执行跳跃
					vy = (box.gravityDown) ? impulse : -impulse;
				}
			}
		}
		
		// 下面判断落体运动
		if (!onTheGround && gravityScale != 0) {
			float delta = decay * gravityScale;
			float maxDropVelocity = this.maxDropVelocity * gravityScale;
			if (box.gravityDown) {
				boolean topStop = getBoolean("motion.topStop", false);
				if (vy > 0 && (jumpEnd || topStop || stiffness)) {
					delta = 4 * decay;
					if (vy >= -delta) {
						delta = -vy;
					}
				}
				vy += delta;
				if (vy < maxDropVelocity) {
					vy = maxDropVelocity;
				}
			} else {
				boolean bottomStop = getBoolean("motion.bottomStop", false);
				if (vy < 0 && (jumpEnd || bottomStop || stiffness)) {
					delta = 4 * decay;
					if (vy <= delta) {
						delta = vy;
					}
				}
				vy -= delta;
				maxDropVelocity *= -1;
				if (vy > maxDropVelocity) {
					vy = maxDropVelocity;
				}
			}
		}
		
		if (lastvy != vy)
			lastvy = vy;
		setState("jump.direction", vy > 0 ? new JsonValue(1) : (vy == 0 ? new JsonValue(0) : new JsonValue(-1)));
		setJumpState();
	}
	
	@Override
	public void action(LevelWorld world, Box box, CharacterEntry entry) {
		boolean climbing = getBoolean("climb.climbing", false);
		if (climbing) {
			lastvy = 0;
			return;
		}
		
		box.setVelocityY(lastvy);
	}
	
}
