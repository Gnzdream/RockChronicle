package zdream.rockchronicle.foes.megaman;

import static zdream.rockchronicle.core.world.Ticker.WORLD_STEP;

import com.badlogic.gdx.utils.Array;
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
		camp = 1;
		
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
	
	ShapePainter painter;
	MegamanPainter mPainter;
	
	@Override
	public void init(GameRuntime runtime) {
		super.init(runtime);

		initMotion();
		initWeapon();
		
		putPainter(painter = new ShapePainter(box));
		putPainter(mPainter = new MegamanPainter(this));
	}
	
	@Override
	public void onDispose() {
		removePainter(painter);
		super.onDispose();
	}
	
	/*
	 * 类似于两阶段提交.
	 * 
	 * 1. step
	 *  - 处理控制
	 *  - 自己状态更新 / state 数据寿命减一
	 *  - 重叠、攻击判定 (跨 Foe)
	 *  - 移动部分的提交
	 * 
	 * 2. submit (这个函数只管自己的状态)
	 *  - 各种收尾事项. 其实一般不用
	 */
	
	@Override
	public void step(boolean pause) {
		super.step(pause);
		if (pause) {
			return;
		}

		// 清空数据
		box.velocityX = 0;
		box.velocityY = 0;
		jumpEnd = false;
		jumpStart = false;
		
		// 1. 从 state / props 中取数据 TODO
		
		// 2. 处理控制
		if (input != null) {
			recieveInput();
		}
		
		// 3. 查询洛克人状态, 含:
		// box 初始化、在地形中的情形、是否受伤僵直、移动情况
		runtime.world.freshBox(box, true);
		handleMotion();
		
		// 处理攻击情况.
		handleFire();
		
		
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
		
		// 最后……
		// 处理左右移动
		runtime.world.submitMotion(box, true);
		
		// 处理 glitch
		runtime.world.glitchFix(box);
		
		// 绘画处理
		mPainter.tick();
	}
	
	@Override
	public void submit(boolean pause) {
		super.submit(pause);
		resetParam();
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
		patterns.put("slide", new int[] {-38229, 0, 76458, 49152}); // 28 x 18
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
	public boolean stopInstant;
	/**
	 * 是否在行走. 向右行走:1, 左:-1, 无:0
	 */
	public int walking;
	
	/*
	 * 跳跃参数
	 */
	/**
	 * 跳跃向上的初始速度, 配置值. 单位: p / 步.
	 * 每秒 21 块, 换算后为 11469 p / 步.
	 */
	public int impulse = 11469;
	/**
	 * 跳跃向上速度在每一步时间之后的速度衰减值 (delta), 配置值. 单位: p / 步
	 * 在水中会影响该衰减值导致跳跃变高.
	 * 换算后为 -304 p / 步.
	 */
	public int decay = -304;
	/**
	 * 下落时每步最快速度 (负数方向向下), 配置值. 单位: p / 步
	 * 在水中会影响该值导致向下速度变慢.
	 * 每秒 28 块, 换算后为 -15292 p / 步.
	 */
	public int maxDropVelocity = -15292;
	/**
	 * 本帧是否起跳
	 */
	public boolean jumpStart;
	/**
	 * 本帧是否结束跳跃
	 */
	public boolean jumpEnd;
	/**
	 * 由跳跃 / 在空中产生的纵向速度改变值. 默认情况为 0
	 */
	public int jumpVel;
	/**
	 * 跳跃按下的时长. 按下的一瞬间为 0, 每步 +1. 到 9 后, 下 1 步为 -1.
	 * 当 jumpPressDuration 为正数时, 都可以起跳, 来缓解跳不起来的情况.
	 */
	public byte jumpPressDuration = -1;
	
	/*
	 * 滑铲参数
	 */
	/**
	 * 滑铲速度是水平方向. 滑铲一旦触发, 如果不取消则默认持续 72 步时间.
	 * 速度 8.33 格/秒, 换算后为 4552 p / 步.
	 */
	public int slideVelocity = 4552;
	/**
	 * 滑铲的时长.
	 * 值域: [-1, 72]. -1 表示没有滑铲, 大于等于 0 表示滑铲中.
	 * 滑铲第一步为 0, 每步 + 1, 到 72 时就不再加, 滑铲结束后为 -1.
	 */
	public byte slideDuration = -1;
	
	/*
	 * 行动状态参数
	 */
	/**
	 * 是否在攀爬状态.
	 * 0 : 不在攀爬
	 * 其它大于 0: 都是不同的状态
	 */
	public int climbing;
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
		this.stopInstant = this.pstopSlide;
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
	
	private void handleMotion() {
//		if (climbing) { // 1. 如果在攀爬状态, 所有的速度修改都不需要了
//			return;
//		}
		
		// 2. 修改速度. 单位: p
		box.flush();
		int vx = box.velocityX;
		
		// 3. 是否在空中
		boolean inAir = box.inAir;
//		BoxOccupation occ = box.getOccupation();
		
		// 4. 跳跃 / 滑铲判定
		boolean startSlide = (!lastSlide && slide && !inAir && slideDuration == -1);
		if (jump && !lastJump && down) {
			// 判断成滑铲
			startSlide = true;
//			System.out.println(String.format("%d : slide", runtime.ticker.count));
		} else {
			jumpStart = !lastJump && jump && !stiffness && !inAir;
			jumpEnd = (lastJump && !jump) || stiffness && inAir;
			// 缓解跳不起来的情况
			if (jumpPressDuration == 10) {
				jumpPressDuration = -1;
			}
			if (jumpPressDuration >= 0) {
				jumpPressDuration ++;
			}
			if (jump && !lastJump && !stiffness && inAir) {
				jumpPressDuration = 0;
			}
			if (jumpPressDuration > 0 && !inAir) {
				jumpPressDuration = -1;
				jumpStart = true;
			}
			
//			if (jumpStart) {
//				System.out.println(String.format("%d : jump", runtime.ticker.count));
//			} else if (!lastJump && jump && inAir) {
//				System.out.println(String.format("%d : inAir", runtime.ticker.count));
//			}
		}
		
		// 5. 执行左右移动
		if (stiffness) {
			// 在击退 / 硬直状态下
			if (stopInstant) {
				if (box.orientation) {
					vx = -parryVel;
				} else {
					vx = parryVel;
				}
			} else {
				if (box.orientation) {
					vx = calcVelocity(vx, -horizontalVelDelta, -parryVel);
				} else {
					vx = calcVelocity(vx, horizontalVelDelta, parryVel);
				}
			}
			walking = 0;
		} else if (climbing > 0) {
			// 在爬梯子
			// TODO
			walking = 0;
		} else {
			// 处理滑铲
			if (startSlide) {
				setCurrentPattern("slide");
				this.slideDuration = 0;
			} else if (this.slideDuration == 72 || inAir ||
					((box.orientation && box.rightTouched || !box.orientation && box.leftTouched)) && this.slideDuration >= 24) {
				// TODO 有什么情况阻止滑铲呢
				
				setCurrentPattern("normal");
				this.slideDuration = -1;
			} else if (this.slideDuration >= 0) {
				this.slideDuration ++;
			}
			
			// 正常情况下, 每秒增加 horizontalVelDelta 的水平速度, horizontalVelMax 为最值.
			if (left) {
				walking = -1;
				box.orientation = false;
				if (inAir) { // 空中
					vx = -horizontalVelMax;
				} else if (slideDuration >= 0) {
					vx = -slideVelocity;
				} else { // 落地, 向左走
					if (lastvx > 0 && stopInstant || box.leftTouched) {
						vx = 0;
					} else {
						vx = calcVelocity(lastvx, -horizontalVelDelta, -horizontalVelMax);
						if (vx > 0) {
							System.err.println("error");
						}
					}
				}
			} else if (right) {
				walking = 1;
				box.orientation = true;
				if (inAir) { // 空中
					vx = horizontalVelMax;
				} else if (slideDuration >= 0) {
					vx = slideVelocity;
				} else { // 落地, 向右走
					if (lastvx < 0 && stopInstant || box.rightTouched) {
						vx = 0;
					} else {
						vx = calcVelocity(lastvx, horizontalVelDelta, horizontalVelMax);
						if (vx < 0) {
							System.err.println("error");
						}
					}
				}
			} else {
				walking = 0;
				if (slideDuration >= 0) {
					vx = (box.orientation) ? slideVelocity : -slideVelocity;
				} else if (stopInstant) {
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
		
		// 竖直方向上的判断
//		int vy = 0;
		if (climbing > 0) {
			jumpVel = 0;
		} else {
			float gravityScale = box.gravityScale;
			// 后面不允许使用 jump 和 lastJump
			
			if (gravityScale > 0) {
				// 下面判断落体运动
				if (inAir) {
					int delta = (int) (decay * gravityScale);
					int maxDropVel = (int) (maxDropVelocity * gravityScale);
					jumpVel += delta;
					
					if (box.gravityDown) {
						// 是否磕脑袋
						if ((box.topTouched || jumpEnd || !jump) && jumpVel > 0) {
							if (jumpVel >= -4 * delta) {
								jumpVel = -4 * delta;
							}
						}
						if (jumpVel < maxDropVel) {
							jumpVel = maxDropVel;
						}
					} else { // 重力方向向上
						// 是否磕脑袋
						if ((box.bottomTouched || jumpEnd || !jump) && jumpVel < 0) {
							if (jumpVel <= -4 * delta) { // TODO 没测试过
								jumpVel = -4 * delta;
							}
						}
						if (jumpVel > maxDropVel) {
							jumpVel = maxDropVel;
						}
					}
				} else if (jumpStart) { // 刚起跳
//					System.out.println(String.format("%d:  jumpStart", runtime.ticker.count));
					jumpVel = (box.gravityDown) ? impulse : -impulse;
				} else {
					jumpVel = 0;
				}
			} else {
				System.err.println("gravityScale < 0, Megaman.handleInput()");
			}
		}
		box.setVelocityY(jumpVel);
		
	}
	
	/* **********
	 *   武器   *
	 ********** */
	
	public Array<IMegamanWeapon> weapons;
	public int currentWeapon = 0;
	
	/**
	 * 在成功攻击的一步时间内, 为 true
	 */
	public boolean attacking;
	
	private void initWeapon() {
		weapons = new Array<>();
		weapons.add(new BusterWeapon(runtime));
	}
	
	public IMegamanWeapon getCurrentWeapon() {
		return weapons.get(currentWeapon);
	}
	
	private void handleFire() {
		/*
		 * 以下情况可以攻击:
		 * 
		 * 1. 当前配置武器时
		 * 2. 当前武器有能量时 (如果有能量限制);
		 * 3. 当前武器在蓄力完成、子弹个数等条件均满足时;
		 * 4. 洛克人不在受伤硬直状态
		 * 5. 洛克人不在滑铲状态 (注, 这里特殊, 是 slideDuration > 0 不让攻击)
		 * 
		 * 如果武器能蓄力:
		 * 
		 * 1. 当受伤硬直时, 蓄力消失 ?
		 * 2. 滑铲状态可以蓄力
		 */
		
		for (int i = 0; i < weapons.size; i++) {
			IMegamanWeapon weapon = weapons.get(i);
			if (weapon != null) {
				weapon.tick(this);
			}
		}
		
		if (!lastAttack && attack) {
			IMegamanWeapon weapon = getCurrentWeapon();
			attacking = weapon.onAttackPressed(this);
		} else if (lastAttack && !attack) {
			IMegamanWeapon weapon = getCurrentWeapon();
			attacking = weapon.onAttackReleased(this);
		} else {
			attacking = false;
		}
		
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
	boolean jump, attack, slide;
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
	
	private void recieveInput() {
		// 横向
		lastLeft = left;
		lastRight = right;
		lastUp = up;
		lastDown = down;
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
		}
		
		// 行动状态
		lastAttack = attack;
		lastJump = jump;
		lastSlide = slide;
		attack = input.isMapKeyDown(InputCenter.MAP_ATTACK);
		jump = input.isMapKeyDown(InputCenter.MAP_JUMP);
		slide = input.isMapKeyDown(InputCenter.MAP_RUSH);
		if (attack != lastAttack || jump != lastJump || slide != lastSlide) {
			publish(motionInputEvent(attack, attack != lastAttack,
					jump, jump != lastJump, slide, slide != lastSlide));
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
