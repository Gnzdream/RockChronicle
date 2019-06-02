package zdream.rockchronicle.sprite.character.megaman;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.JsonValue;

import zdream.rockchronicle.core.character.CharacterEntry;
import zdream.rockchronicle.core.character.event.CharacterEvent;
import zdream.rockchronicle.core.module.motion.TerrainMotionModule;
import zdream.rockchronicle.core.move.IMovable;
import zdream.rockchronicle.platform.body.Box;
import zdream.rockchronicle.platform.body.BoxOccupation;
import zdream.rockchronicle.platform.region.Terrains;
import zdream.rockchronicle.platform.world.LevelWorld;

public class MegamanMotionModule extends TerrainMotionModule implements IMovable {
	
	MegamanInLevel parent;
	
	/**
	 * 是否向左或向右移动. 左和右不会同时为 true.
	 */
	boolean left, right;
	/**
	 * 上一次计算的横坐标速度
	 */
	protected float lastvx;
	
	/*
	 * 移动静态参数: 格子 / 秒
	 */
	public static final float
		HORIZONTAL_VELOCITY_DELTA = 50,
		HORIZONTAL_VELOCITY_MAX = 5.4f,
		PARRY_VELOCITY = 1;
	
	/*
	 * 原配置值
	 */
	/**
	 * 水平速度增量 (线性), 单位: 格子 / (步 ^ 2)
	 */
	public float phorizontalVelDelta;
	/**
	 * 水平速度最大值, 单位: 格子 / 步
	 */
	public float phorizontalVelMax;
	/**
	 * 击退时的速度, 单位: 格子 / 步
	 */
	public float pparryVel;
	/**
	 * 当玩家下达停止命令时, 角色是否立即停止
	 */
	public boolean pstopSlide;
	
	/*
	 * 移动参数 (当前值)
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
	/**
	 * 当玩家下达停止命令时, 角色是否立即停止
	 */
	public boolean stopSlide;
	
	public MegamanMotionModule(MegamanInLevel parent) {
		super(parent);
		this.parent = parent;
		
		this.phorizontalVelDelta =
				HORIZONTAL_VELOCITY_DELTA * LevelWorld.TIME_STEP * LevelWorld.TIME_STEP;
		this.phorizontalVelMax = HORIZONTAL_VELOCITY_MAX * LevelWorld.TIME_STEP;
		this.pparryVel = PARRY_VELOCITY * LevelWorld.TIME_STEP;
		this.pstopSlide = true;
		
		resetParam();
	}
	
	private void resetParam() {
		this.horizontalVelDelta = this.phorizontalVelDelta;
		this.horizontalVelMax = this.phorizontalVelMax;
		this.parryVel = this.pparryVel;
		this.stopSlide = this.pstopSlide;
	}
	
	private float calcVelocity(float velocity, float acceleration, float max) {
		if (acceleration > 0) {
			velocity += acceleration;
			if (velocity >= max) {
				return max;
			}
		} else { // (acceleration < 0)
			velocity += acceleration;
			if (velocity <= max) {
				return max;
			}
		}
		
		return velocity;
	}
	
	@Override
	public void init(FileHandle file, JsonValue value) {
		super.init(file, value);
		
		// 添加事件监听
		parent.addSubscribe("ctrl_axis", this);
		parent.getBoxModule().addMovable(this, 0);
	}
	
	@Override
	public void determine(LevelWorld world, int index, boolean hasNext) {
		super.determine(world, index, hasNext);
		
		Box box = getSingleBox();
		
		boolean onTheGround = onTheGround(world, box, box.bottomStop, box.topStop);
		setSituation("state.onTheGround", new JsonValue(onTheGround));
		
		boolean stiffness = getBoolean("state.stiffness", false);
		if (stiffness) { // 受伤时不转向
			return;
		} else {
			if (left) {
				setSituation("state.orientation", new JsonValue(false));
			} else if (right) {
				setSituation("state.orientation", new JsonValue(true));
			}
		}
		
		// state
		String motion = "stop";
		if (left || right) {
			motion = "walk";
		}
		setState("state.motion", new JsonValue(motion));
		
	}

	@Override
	public void move(LevelWorld world, Box box, CharacterEntry entry) {
		boolean climbing = getBoolean("climb.climbing", false);
		if (climbing) { // 1. 如果在攀爬状态, 所有的速度修改都不需要了
			return;
		}
		
		// 2. 修改速度
		boolean stiffness = getBoolean("state.stiffness", false);
		Vector2 vel = box.velocity; // 速度
		float vx = vel.x;
		
		// 3. 查看是否落地, 并将数据提交至 state 中
		boolean onTheGround = getBoolean("state.onTheGround", false);
		
		// 最终速度 Y, 需要等待 jump 来改
		
		// 4. 执行左右移动
		boolean orientation = getBoolean("state.orientation", true);
		if (stiffness) {
			// 在击退 / 硬直状态下
			if (stopSlide) {
				if (orientation) {
					vx = -parryVel;
				} else {
					vx = parryVel;
				}
			} else {
				if (orientation) {
					vx = calcVelocity(vx, -horizontalVelDelta, -parryVel);
				} else {
					vx = calcVelocity(vx, horizontalVelDelta, parryVel);
				}
			}
		} else {
			// 正常情况下, 每秒增加 horizontalVelDelta 的水平速度, horizontalVelMax 为最值.
			if (left) {
				if (onTheGround) { // 落地, 向左走
					if (lastvx > 0 && stopSlide || box.leftStop) {
						vx = 0;
					} else {
						vx = calcVelocity(lastvx, -horizontalVelDelta, -horizontalVelMax);
						if (vx > 0) {
							System.out.println("error");
						}
					}
				} else { // 空中
					vx = -horizontalVelMax;
				}
			} else if (right) {
				if (onTheGround) { // 落地, 向右走
					if (lastvx < 0 && stopSlide || box.rightStop) {
						vx = 0;
					} else {
						vx = calcVelocity(lastvx, horizontalVelDelta, horizontalVelMax);
						if (vx < 0) {
							System.out.println("error");
						}
					}
				} else { // 空中
					vx = horizontalVelMax;
				}
			} else {
				if (stopSlide) {
					vx = 0; // 不在打滑状态下, 立即停住
				} else {
					if (lastvx > 0) {
						vx = calcVelocity(lastvx, -horizontalVelDelta, 0);
					} else if (lastvx < 0) {
						vx = calcVelocity(lastvx, horizontalVelDelta, 0);
					} else {
						vx = 0;
					}
				}
			}
		}
		
		// 设置的最终速度 X
		box.setVelocityX(vx);
		lastvx = vx;
	}
	
	/**
	 * <p>判断角色是否站在某个物体上 (不悬在空中)
	 * <p>这里有一个额外的判断内容, 就是是否爬梯子
	 * </p>
	 */
	private boolean onTheGround(LevelWorld world, Box box,
			boolean bottomStop, boolean topStop) {
		if (bottomStop && box.gravityDown || topStop && !box.gravityDown) {
			return true;
		}
		if (!box.climbable) {
			return false;
		}
		
		// 判断梯子部分
		BoxOccupation occ = box.getOccupation();
		if (!occ.ybottomTightly && box.gravityDown || !occ.ytopTightly && !box.gravityDown) {
			return false;
		}
		
		if (box.gravityDown) {
			int ybottom = occ.ybottom - 1; // 角色底端再下面一格
			for (int x = occ.xleft; x <= occ.xright; x++) {
				if (Terrains.isLadder(world.getTerrain(x, ybottom))
						&& !Terrains.isLadder(world.getTerrain(x, ybottom + 1))) {
					return true;
				}
			}
		} else {
			int ybottom = occ.ytop + 1; // 角色顶端上面一格
			for (int x = occ.xleft; x <= occ.xright; x++) {
				if (Terrains.isLadder(world.getTerrain(x, ybottom))
						&& !Terrains.isLadder(world.getTerrain(x, ybottom - 1))) {
					return true;
				}
			}
		}
		
		return false;
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
	
	@Override
	public void stepPassed() {
		super.stepPassed();
		
		resetParam();
	}
	
	private void recvCtrlAxis(CharacterEvent event) {
		left = event.value.getBoolean("left");
		right = event.value.getBoolean("right");
	}
	
}
