package zdream.rockchronicle.sprite.character.megaman;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonValue.ValueType;

import zdream.rockchronicle.core.character.event.CharacterEvent;
import zdream.rockchronicle.core.module.motion.TerrainMotionModule;
import zdream.rockchronicle.platform.body.Box;
import zdream.rockchronicle.platform.world.LevelWorld;

public class MegamanMotionModule extends TerrainMotionModule {
	
	MegamanInLevel parent;
	
	/**
	 * 是否向左或向右移动. 左和右不会同时为 true.
	 */
	boolean left, right;
	
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
	}
	
	@Override
	public void determine(LevelWorld world, int index, boolean hasNext) {
		super.determine(world, index, hasNext);
		
		boolean climbing = parent.getBoolean(new String[] {"climb", "climbing"}, false);
		if (!climbing) { // 如果在攀爬状态, 所有的速度修改都不需要了
			JsonValue v;
			
			// situation
			boolean stiffness = parent.getBoolean(new String[] {"state", "stiffness"}, false);
			if (!stiffness) { // 受伤时不转向
				if (left) {
					v = new JsonValue(ValueType.object);
					v.addChild("orientation", new JsonValue(false));
					parent.setJson("situation", v);
				} else if (right) {
					v = new JsonValue(ValueType.object);
					v.addChild("orientation", new JsonValue(true));
					parent.setJson("situation", v);
				}
			}
			
			// state
			String motion = "stop";
			if (left || right) {
				motion = "walk";
			}
			
			v = new JsonValue(ValueType.object);
			v.addChild("motion", new JsonValue(motion));
			parent.setJson("state", v);
			
			// 2. 修改速度
			Box box = getSingleBox();
			Vector2 vel = box.velocity; // 速度
			float vx = vel.x, vy = vel.y;
			
			// 3. 执行上下移动 TODO
			boolean bottomStop = box.bottomStop;
			
			// 设置的最终速度 Y
			box.setVelocityY(vy);
			
			// 4. 执行左右移动
			boolean orientation = parent.getBoolean(new String[] {"situation", "orientation"}, true);
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
		}
	}
	
	@Override
	public void receiveEvent(CharacterEvent event) {
		switch (event.name) {
		case "ctrl_axis":
			recvCtrlAxis(event);
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
	
}
