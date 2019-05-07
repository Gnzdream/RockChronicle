package zdream.rockchronicle.character.megaman;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.JsonValue;

import zdream.rockchronicle.bullet.base.MMBuster;
import zdream.rockchronicle.core.character.event.CharacterEvent;
import zdream.rockchronicle.core.character.motion.SingleBoxMotionModule;
import zdream.rockchronicle.core.character.parameter.CharacterParameter;
import zdream.rockchronicle.desktop.RockChronicleDesktop;
import zdream.rockchronicle.platform.world.LevelWorld;

public class MegamanMotionModule extends SingleBoxMotionModule {
	
	Megaman parent;
	
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
		HORIZONTAL_VELOCITY_MAX = 5;
	
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
	
	public MegamanMotionModule(Megaman parent) {
		super(parent);
		this.parent = parent;
		
		this.horizontalVelDelta =
				HORIZONTAL_VELOCITY_DELTA * LevelWorld.TIME_STEP * LevelWorld.TIME_STEP;
		this.horizontalVelMax = HORIZONTAL_VELOCITY_MAX * LevelWorld.TIME_STEP;
	}
	
	@Override
	public void init(FileHandle file, JsonValue value) {
		super.init(file, value);
		
		// 添加事件监听
		parent.addSubscribe("ctrl_axis", this);
		parent.addSubscribe("ctrl_motion", this);
	}
	
	@Override
	public void step(LevelWorld world, int index, boolean hasNext) {
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
		
		// 设置的最终速度 X
		box.setVelocityX(vx);
		
		// 最后确定并更新位置
		world.execHorizontalMotion(box);
//		System.out.println(box.anchor);
		
		// 其它 : 是否攻击
		if (bulletCount > 0 && attackBegin) {
			float x = (orientation) ? box.anchor.x + 0.5f : box.anchor.x - 0.5f;
			MMBuster buster = (MMBuster) RockChronicleDesktop.INSTANCE.characterBuilder.create("megaman_buster",
					CharacterParameter.newInstance()
						.setBoxAnchor(x, box.anchor.y + 0.75f)
						.setMotionOrientation(orientation)
						.get());
			buster.setDisappearCallback((b) -> {this.bulletCount++;});
			RockChronicleDesktop.INSTANCE.runtime.addEntry(buster);
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
