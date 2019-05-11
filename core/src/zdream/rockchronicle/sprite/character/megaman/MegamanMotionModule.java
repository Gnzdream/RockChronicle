package zdream.rockchronicle.sprite.character.megaman;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonValue.ValueType;

import zdream.rockchronicle.RockChronicle;
import zdream.rockchronicle.core.character.event.CharacterEvent;
import zdream.rockchronicle.core.character.parameter.CharacterParameter;
import zdream.rockchronicle.core.module.motion.SingleBoxMotionModule;
import zdream.rockchronicle.platform.world.LevelWorld;
import zdream.rockchronicle.sprite.bullet.base.MMBuster;

public class MegamanMotionModule extends SingleBoxMotionModule {
	
	MegamanInLevel parent;
	
	/**
	 * 是否向左或向右移动. 左和右不会同时为 true.
	 */
	boolean left, right;
	/**
	 * 攻击暂存. attackBegin 为暂存, inAttack 为状态
	 */
	boolean attackBegin, inAttack;
	/**
	 * 子弹的剩余个数
	 */
	int bulletCount = 3;
	
	/*
	 * 移动静态参数: 格子 / 秒
	 */
	public static final float
		HORIZONTAL_VELOCITY_DELTA = 50,
		HORIZONTAL_VELOCITY_MAX = 5.4f,
		PARRY_VELOCITY = 1;
	
	/*
	 * 移动参数
	 */
	/**
	 * 水平速度增量 (线性), 单位: 格子 / (步 ^ 2)
	 */
	public float horizontalVelDelta;
	/**
	 * 水平速度最大值, 单位: 格子 / 步
	 */
	public float horizontalVelMax;
	/**
	 * 击退时的速度, 单位: 格子 / 步
	 */
	public float parryVel;
	
	public MegamanMotionModule(MegamanInLevel parent) {
		super(parent);
		this.parent = parent;
		
		this.horizontalVelDelta =
				HORIZONTAL_VELOCITY_DELTA * LevelWorld.TIME_STEP * LevelWorld.TIME_STEP;
		this.horizontalVelMax = HORIZONTAL_VELOCITY_MAX * LevelWorld.TIME_STEP;
		this.parryVel = PARRY_VELOCITY * LevelWorld.TIME_STEP;
		// boolean immune = parent.getBoolean(new String[] {"state", "immune"}, false);
	}
	
	@Override
	public void init(FileHandle file, JsonValue value) {
		super.init(file, value);
		
		// 添加事件监听
		parent.addSubscribe("ctrl_axis", this);
		parent.addSubscribe("ctrl_motion", this);
	}
	
	@Override
	public void determine(LevelWorld world, int index, boolean hasNext) {
		super.determine(world, index, hasNext);
		
		String motion = "stop";
		if (left) {
			motion = "left";
		} else if (right) {
			motion = "right";
		}
		JsonValue v = new JsonValue(ValueType.object);
		v.addChild("motion", new JsonValue(motion));
		
		parent.setJson("state", v);
	}
	
	@Override
	public void resetPosition(LevelWorld world, int index, boolean hasNext) {
		Vector2 vel = box.velocity; // 速度
		float vx = vel.x, vy = vel.y;
		
		// 1. 判断重合以及补救方法
		
		// 2. 判断角色状态
		
		// 3. 执行上下移动 TODO
		boolean bottomStop = box.onTheGround();
		
		// 设置的最终速度 Y
		box.setVelocityY(vy);
		world.execVerticalMotion(box);
		
		// 4. 执行左右移动
//		if (box.leftStop) {
//			System.out.println("l");
//		} else if (box.rightStop) {
//			System.out.println("r");
//		}
		boolean stiffness = parent.getBoolean(new String[] {"state", "stiffness"}, false);
		if (stiffness) {
			// 在击退 / 硬直状态下
			if (orientation) {
				vx = -parryVel;
			} else {
				vx = parryVel;
			}
		} else {
			// 正常情况下, 每秒增加 horizontalVelDelta 的水平速度, horizontalVelMax 为最值.
			if (left) {
				orientation = false;
				if (bottomStop) {
					vx -= horizontalVelDelta;
					if (vx > 0 || box.leftStop) {
						vx = 0;
					} else if (vx < -horizontalVelMax) {
						vx = -horizontalVelMax;
					}
				} else {
					vx = -horizontalVelMax;
				}
			} else if (right) {
				orientation = true;
				if (bottomStop) {
					vx += horizontalVelDelta;
					if (vx < 0 || box.rightStop) {
						vx = 0;
					} else if (vx > horizontalVelMax) {
						vx = horizontalVelMax;
					}
				} else {
					vx = horizontalVelMax;
				}
			} else {
				vx = 0; // 不在打滑状态下, 立即停住
			}
		}
		
		// 设置的最终速度 X
		box.setVelocityX(vx);
		
		// 最后确定并更新位置
		world.execHorizontalMotion(box);
		
		// 其它 : 是否攻击
		if (bulletCount > 0 && attackBegin && !stiffness) {
			float x = (orientation) ? box.anchor.x + 1 : box.anchor.x - 1;
			MMBuster buster = (MMBuster) RockChronicle.INSTANCE.runtime.characterBuilder.create("megaman_buster",
					CharacterParameter.newInstance()
						.setBoxAnchor(x, box.anchor.y + 0.75f)
						.setMotionOrientation(orientation)
						.setCamp(parent.getInt(new String[] {"camp", "camp"}, 0))
						.get());
			buster.setDisappearCallback((b) -> {this.bulletCount++;});
			RockChronicle.INSTANCE.runtime.addEntry(buster);
			-- bulletCount;
		}
		
		// 重置参数
		this.attackBegin = false;
	}
	
	@Override
	public void receiveEvent(CharacterEvent event) {
		switch (event.name) {
		case "ctrl_axis":
			recvCtrlAxis(event);
			break;
		case "ctrl_motion":
			recvCtrlMotion(event);
			break;

		default:
			super.receiveEvent(event);
			break;
		}
	}
	
	private void recvCtrlAxis(CharacterEvent event) {
		left = event.value.getBoolean("left");
		right = event.value.getBoolean("right");
	}
	
	private void recvCtrlMotion(CharacterEvent event) {
		inAttack = event.value.getBoolean("attack");
		boolean attackChange = event.value.getBoolean("attackChange");
//		boolean slide = event.value.getBoolean("slide");
//		boolean slideChange = event.value.getBoolean("slideChange");
		
		attackBegin = (inAttack && attackChange);
	}

}
