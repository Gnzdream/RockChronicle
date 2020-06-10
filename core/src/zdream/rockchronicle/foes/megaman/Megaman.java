package zdream.rockchronicle.foes.megaman;

import static zdream.rockchronicle.core.world.Ticker.WORLD_STEP;

import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonValue.ValueType;
import com.badlogic.gdx.utils.ObjectMap;

import zdream.rockchronicle.core.GameRuntime;
import zdream.rockchronicle.core.foe.Box;
import zdream.rockchronicle.core.foe.Foe;
import zdream.rockchronicle.core.foe.FoeEvent;
import zdream.rockchronicle.core.foe.ShapePainter;
import zdream.rockchronicle.core.input.IInputBindable;
import zdream.rockchronicle.core.input.InputCenter;
import zdream.rockchronicle.core.input.PlayerInput;

public class Megaman extends Foe implements IInputBindable {

	public Megaman() {
		super("megaman");
		
		box = new Box(id);
		boxes = new Box[] {box};
		initBox();
	}
	
	public Megaman(JsonValue json) {
		this();
	}
	
	/**
	 * @param bAnchorX 单位: 块
	 * @param bAnchorY 单位: 块
	 */
	public Megaman(float bAnchorX, float bAnchorY) {
		this();

		box.setAnchor(Box.block2P(bAnchorX), Box.block2P(bAnchorY));
	}
	
	@Override
	public void init(GameRuntime runtime) {
		super.init(runtime);
		
		initMotion();
		
		putPainter(new ShapePainter(box));
	}
	
	/*
	 * 类似于两阶段提交.
	 * 
	 * 1. step
	 *  - 处理控制
	 *  - 自己状态更新 / state 数据寿命减一
	 *  - 重叠、攻击判定 (跨 Foe)
	 * 
	 * 2. submit (这个函数只管自己的状态)
	 *  - 移动部分的提交
	 * 
	 */
	
	@Override
	public void step(boolean pause) {
		if (pause) {
			return;
		}
		
		// 1. 从 state / props 中取数据 TODO
		
		// 2. 处理控制
		if (input != null) {
			handleInput();
		}
//		System.out.println(String.format("%s,%s", left, right));
		handleAxis();
		
		// 3. 查询洛克人状态, 含:
		// box 初始化、在地形中的情形、是否受伤僵直、移动情况
		runtime.world.freshBox(box, true);
		
		// TODO 是否受伤僵直. 僵直时移动情况被自动设置
		
		// 移动情况

		// 保存 state / props 数据 TODO
		
		
		
		
//		boolean onTheGround = onTheGround();
//		setState("state.onTheGround", new JsonValue(onTheGround));
//		
//		boolean stiffness = getBoolean("health.stiffness", false);
//		if (stiffness) { // 受伤时不转向
//			return;
//		} else {
//			if (left) {
//				box.orientation = false;
//			} else if (right) {
//				box.orientation = true;
//			}
//		}
//		
//		// state
//		String motion = "stop";
//		if (left || right) {
//			motion = "walk";
//		}
//		setState("state.motion", new JsonValue(motion));
	}
	
	@Override
	public void submit(boolean pause) {
		resetParam();
		
		// 处理左右移动 TODO
		box.addAnchorX(box.velocityX);
		box.addAnchorY(box.velocityY);
		box.velocityX = 0;
		box.velocityY = 0;
	}
	
	/* **********
	 *   盒子   *
	 ********** */
	
	Box box;
	Box[] boxes;
	
	private ObjectMap<String, int[]> patterns = new ObjectMap<>();
	public String currentPattern;

	@Override
	public Box[] getBoxes() {
		return boxes;
	}
	
	public void setCurrentPattern(String pattern) {
		int[] ps = patterns.get(pattern);
		
		if (ps != null) {
			currentPattern = pattern;
			
			box.setBox(ps[0], ps[1], ps[2], ps[3]);
		}
	}
	
	private void initBox() {
		patterns.put("normal", new int[] {-27313, 0, 54613, 98304});
		// 爬梯子到顶端有两个状态
		patterns.put("climb_top_0", new int[] {-27313, 0, 54613, 62805});
		patterns.put("climb_top_1", new int[] {-27313, 0, 54613, 46421});
		// 滑铲
		// TODO
		
		setCurrentPattern("normal");
	}
	
	/* **********
	 *   移动   *
	 ********** */
	
	/**
	 * 上一次计算的横坐标速度
	 */
	protected int lastvx;
	
	/**
	 * 移动静态参数. 下面的数据都转成 p 和 步 了.
	 * 
	 * <li>HORIZONTAL_VELOCITY_MAX : 左右移动最大速度: 5.4 块/秒
	 * <li>HORIZONTAL_VELOCITY_DELTA : 从静止到到达最快速度: 0.1 秒
	 * <li>PARRY_VELOCITY : 击退时左右移动速度: 1 块/秒
	 * </li>
	 */
	public static final int
		HORIZONTAL_VELOCITY_DELTA = 246,
		HORIZONTAL_VELOCITY_MAX = 2950, // 单位 p
		PARRY_VELOCITY = (int) (Box.P_PER_BLOCK * WORLD_STEP);
	
	/*
	 * 原配置值
	 */
	/**
	 * 水平速度增量 (线性), 单位: p / (步 ^ 2)
	 */
	public int phorizontalVelDelta;
	/**
	 * 水平速度最大值, 单位: p / 步
	 */
	public int phorizontalVelMax;
	/**
	 * 击退时的速度, 单位: p / 步
	 */
	public int pparryVel;
	/**
	 * 当玩家下达停止命令时, 角色是否立即停止
	 */
	public boolean pstopSlide;
	
	/*
	 * 移动参数 (当前值)
	 */
	/**
	 * 水平速度增量 (线性), 单位: p / (步 ^ 2)
	 */
	public int horizontalVelDelta;
	/**
	 * 水平速度最大值, 单位: p / 步
	 */
	public int horizontalVelMax;
	/**
	 * 击退时的速度, 单位: p / 步
	 */
	public int parryVel;
	/**
	 * 当玩家下达停止命令时, 角色是否立即停止.
	 * 在冰面上该参数为 false.
	 */
	public boolean stopSlide;
	
	/*
	 * 行动参数
	 */
	/**
	 * 是否在攀爬状态
	 */
	public boolean climbing;
	/**
	 * 是否在受伤僵直状态
	 */
	public boolean stiffness;
	
	public void initMotion() {
		this.phorizontalVelDelta = HORIZONTAL_VELOCITY_DELTA;
		this.phorizontalVelMax = HORIZONTAL_VELOCITY_MAX;
		this.pparryVel = PARRY_VELOCITY;
		this.pstopSlide = true;
		
		resetParam();
		
//		addSubscribe("ctrl_axis", axisEvent);
	}
	
	private void resetParam() {
		this.horizontalVelDelta = this.phorizontalVelDelta;
		this.horizontalVelMax = this.phorizontalVelMax;
		this.parryVel = this.pparryVel;
		this.stopSlide = this.pstopSlide;
	}
	
	private int calcVelocity(int velocity, int acceleration, int max) {
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
	
	private void handleAxis() {
//		if (climbing) { // 1. 如果在攀爬状态, 所有的速度修改都不需要了
//			return;
//		}
		
		// 2. 修改速度. 单位: p
		int vx = box.velocityX;
//		int vy = box.velocityY;
		
		// 3. 是否在空中
		boolean inAir = box.inAir;
		
		// 最终速度 Y, 需要等待 jump 来改
		
		// 4. 执行左右移动
		boolean orientation = box.orientation;
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
		} else if (!climbing) {
			// 正常情况下, 每秒增加 horizontalVelDelta 的水平速度, horizontalVelMax 为最值.
			if (left) {
				if (inAir) { // 空中
					vx = -horizontalVelMax;
				} else { // 落地, 向左走
					if (lastvx > 0 && stopSlide || box.leftTouched) {
						vx = 0;
					} else {
						vx = calcVelocity(lastvx, -horizontalVelDelta, -horizontalVelMax);
						if (vx > 0) {
							System.err.println("error");
						}
					}
				}
			} else if (right) {
				if (inAir) { // 空中
					vx = horizontalVelMax;
				} else { // 落地, 向右走
					if (lastvx < 0 && stopSlide || box.rightTouched) {
						vx = 0;
					} else {
						vx = calcVelocity(lastvx, horizontalVelDelta, horizontalVelMax);
						if (vx < 0) {
							System.err.println("error");
						}
					}
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
		} else { // 在爬梯子
			// TODO
		}
		
		// 设置的最终速度 X
		box.setVelocityX(vx);
		lastvx = vx;
	}
	
	/* **********
	 *   控制   *
	 ********** */

	/**
	 * 是否向左或向右移动. 左和右不会同时为 true.
	 */
	boolean left, right;
	boolean up, down;
	/**
	 * 两个键是否同时按下
	 */
	boolean leftAndRight, upAndDown;
	/**
	 * 上一步, 方向键是否按下
	 */
	boolean lastLeft, lastRight, lastUp, lastDown;
	/**
	 * 上一步, 行动键位是否按下
	 */
	boolean lastJump, lastAttack, lastSlide;
	
	private PlayerInput input;

	@Override
	public void bindController(PlayerInput input) {
		this.input = input;
	}

	@Override
	public void unbindController() {
		this.input = null;
	}
	
	private void handleInput() {
		// 横向
		left = input.isMapKeyDown(InputCenter.MAP_LEFT);
		right = input.isMapKeyDown(InputCenter.MAP_RIGHT);
		up = input.isMapKeyDown(InputCenter.MAP_UP);
		down = input.isMapKeyDown(InputCenter.MAP_DOWN);
		if (left && right) {
			if (!leftAndRight) {
				if (lastLeft && !lastRight) {
					left = false;
					right = true;
				} else if (!lastLeft && lastRight) {
					left = true;
					right = false;
				} else {
					left = right = false;
				}
			} else {
				left = lastLeft;
				right = lastRight;
			}
			leftAndRight = true;
		} else {
			leftAndRight = false;
		}
		if (up && down) {
			if (!upAndDown) {
				if (lastUp && !lastDown) {
					up = false;
					down = true;
				} else if (!lastUp && lastDown) {
					up = true;
					down = false;
				} else {
					up = down = false;
				}
			} else {
				up = lastUp;
				down = lastDown;
			}
			upAndDown = true;
		} else {
			upAndDown = false;
		}
		if (left != lastLeft || right != lastRight || up != lastUp || down != lastDown) {
			publish(axisInputEvent(left, right, up, down));
			lastLeft = left;
			lastRight = right;
			lastUp = up;
			lastDown = down;
		}
		
		// 行动状态
		boolean attack = input.isMapKeyDown(InputCenter.MAP_ATTACK),
				jump = input.isMapKeyDown(InputCenter.MAP_JUMP),
				slide = input.isMapKeyDown(InputCenter.MAP_RUSH);
		if (attack != lastAttack || jump != lastJump || slide != lastSlide) {
			publish(motionInputEvent(attack, attack != lastAttack,
					jump, jump != lastJump, slide, slide != lastSlide));
			lastAttack = attack;
			lastJump = jump;
			lastSlide = slide;
		}
	}
	
	private FoeEvent axisInputEvent(boolean left, boolean right, boolean up, boolean down) {
		FoeEvent event = new FoeEvent("ctrl_axis");
		JsonValue v = new JsonValue(ValueType.object);
		event.value = v;
		
		v.addChild("left", new JsonValue(left));
		v.addChild("right", new JsonValue(right));
		v.addChild("up", new JsonValue(up));
		v.addChild("down", new JsonValue(down));
		return event;
	}
	
	private FoeEvent motionInputEvent(
			boolean attack, boolean attackChange,
			boolean jump, boolean jumpChange,
			boolean slide, boolean slideChange) {
		FoeEvent event = new FoeEvent("ctrl_axis");
		JsonValue v = new JsonValue(ValueType.object);
		event.value = v;
		
		v.addChild("attack", new JsonValue(attack));
		v.addChild("attackChange", new JsonValue(attackChange));
		v.addChild("jump", new JsonValue(jump));
		v.addChild("jumpChange", new JsonValue(jumpChange));
		v.addChild("slide", new JsonValue(slide));
		v.addChild("slideChange", new JsonValue(slideChange));
		return event;
	}

}
