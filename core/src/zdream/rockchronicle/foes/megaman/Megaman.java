package zdream.rockchronicle.foes.megaman;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonValue.ValueType;
import com.badlogic.gdx.utils.ObjectMap;

import zdream.rockchronicle.core.GameRuntime;
import zdream.rockchronicle.core.foe.Box;
import zdream.rockchronicle.core.foe.BoxOccupation;
import zdream.rockchronicle.core.foe.Foe;
import zdream.rockchronicle.core.foe.FoeEvent;
import zdream.rockchronicle.core.foe.ShapePainter;
import zdream.rockchronicle.core.input.IInputBindable;
import zdream.rockchronicle.core.input.InputCenter;
import zdream.rockchronicle.core.input.PlayerInput;
import zdream.rockchronicle.core.region.Terrains;
import zdream.rockchronicle.core.world.Ticker;

import static zdream.rockchronicle.core.world.Ticker.*;
import static zdream.rockchronicle.core.foe.Box.*;

import static zdream.rockchronicle.core.region.ITerrainStatic.*;

public class Megaman extends Foe implements IInputBindable {

	public Megaman() {
		super("megaman");
		camp = 1;
		type = "leader";
		
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
		
		addSubscribe("applyDamage", this::recieveDamage);
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
	public void step(byte pause) {
		super.step(pause);
		if (pause == WORLD_PAUSE) {
			return;
		} else if (pause == ROOM_SHIFTING) {
			// 房间切换中
			mPainter.tick();
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
		handleImmuse();
		handleClimb();
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
		runtime.world.submitMotion(box, true, true);
		
		// 处理 glitch
		runtime.world.glitchFix(box);
		
		// 设置属性
		pushParam();
		
		// 绘画处理
		mPainter.tick();
	}
	
	@Override
	public void submit(byte pause) {
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
	 * 攀爬参数
	 * 
	 * 从正常状态到攀爬状态 (不是从顶端下来):
	 * climbing: 0 -> 1~7
	 * climbDirection: 0
	 */
	/**
	 * 攀爬速度, 每步攀爬的格子数. 配置项
	 * 单位: p / 步, 默认 5块 / 秒, 换算后为 2731 p / 步
	 */
	int climbVelocity;
	/**
	 * 攀爬状态参数, 0 表示不攀爬, 1 表示攀爬中, [2-13] 表示在梯子顶端的特殊攀爬状态,
	 * 共 12 步时间 (0.1 s)
	 * 
	 * 如果上一帧不为 0, 后面每帧都需要检测, 直到将其设置为 0
	 */
	int climbing;
	/**
	 * 当前帧攀爬的方向, 0: 无, 1: 上, -1: 下,
	 * 每步不重置 / upOrDown
	 */
	byte climbDirection;
	/**
	 * 由于在攀爬状态下攻击将有 0.25 秒时间不能上下移动, 这里记录剩余的恢复速度
	 */
	int climbHaltRemain;
	
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
		
		this.climbVelocity = 2731;
	}
	
	private void pushParam() {
		// 房间切换要用到
		setState("climbing", new JsonValue(climbing));
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
	
	private void handleClimb() {
		final int pCenterX = box.getCenterX();
		final int pCenterY = box.getCenterY();
		final int centerX = Box.blockLeft(pCenterX);
		final int centerY = Box.blockLeft(pCenterY);
		BoxOccupation occ = box.getOccupation();
		// 现在查看这个中心点映射到的是哪个地形
		
		// 表示当前帧, 角色从 climbing = 0 的状态变成附到梯子上
		boolean adhere = false;
		
		// 表示在梯子顶端按下将下降到下面的梯子上
		boolean lower = true;
		int lowerX = 0, lowerY = 0; // 单位: 块
		
		if (climbing > 0) {
			this.climbDirection = (byte) (up ? 1 : down ? -1 : 0);
		} else {
			this.climbDirection = 0;
		}
		
		STEP1: {
			if (climbing == 0) {
				
				byte terrain = runtime.world.getTerrain(centerX, centerY);
				if (Terrains.isLadder(terrain)) {
					// 这个时候如果按了上或者下, 就应该到梯子上了, 除了一种情况: 你在落地时按下
					if (up || down) {
						if (box.gravityDown && occ.ybottomTightly) {
							if (runtime.world.getTerrain(centerX, occ.ybottom - 1) == TERRAIN_SOLID && down) {
								// do-nothing
							} else {
								adhere = true;
							}
						} else if (!box.gravityDown && occ.ytopTightly) {
							if (runtime.world.getTerrain(centerX, occ.ytop + 1) == TERRAIN_SOLID && up) {
								// do-nothing
							} else {
								adhere = true;
							}
						} else {
							adhere = true;
						}
					}
				} else {
					// 还有一种情况, 在梯子顶端按下将下降到下面的梯子上
					if (box.gravityDown && occ.ybottomTightly) {
						if (Terrains.isLadder(runtime.world.getTerrain(centerX, occ.ybottom - 1)) && down) {
							// 可以向下降到梯子上
							lower = true;
							lowerX = centerX;
							lowerY = occ.ybottom;
						}
					} else if (!box.gravityDown && occ.ytopTightly) {
						if (Terrains.isLadder(runtime.world.getTerrain(centerX, occ.ytop + 1)) && up) {
							// 人是倒着的, 可以向上“降”到梯子上
							lower = true;
							lowerX = centerX;
							lowerY = occ.ytop + 1;
						}
					}
				}
				break STEP1;
			}
			
			// 到达这里说明 climbing > 0
			
			// 检测是否在攀登状态. 取消攀登状态的可能有以下情况: 
			// 1. 梯子消失 (现在正在检查的) (需要检查中点和锚点, 一项满足即可)
			// 2. 被攻击 (前面已经过滤)
			// 3. 跳下来 (监听事件, 不在这里)
			// 4. 顺着梯子到底, 站到了平地上 (当 climbing <= 7 时需要检查锚点)
			int anchorX = Box.blockRight(box.anchorX);
			int anchorY = Box.blockRight(box.anchorY);
			
			if (climbing <= 7) {
				byte terrain = runtime.world.getTerrain(anchorX, anchorY);
				// 顺着梯子到底, 站到了平地上 (以后会扩展到站到其它实体)
				if (Terrains.isLadder(terrain)) {
					if (box.gravityDown && occ.ybottomTightly) {
						// TODO 其它平地块
						if (runtime.world.getTerrain(centerX, occ.ybottom - 1) == TERRAIN_SOLID && down) {
							climbing = 0; // 变成站立
							setCurrentPattern("normal");
							break STEP1;
						}
					} else if (!box.gravityDown && occ.ytopTightly) {
						// TODO 其它平地块
						if (runtime.world.getTerrain(centerX, occ.ytop + 1) == TERRAIN_SOLID && up) {
							climbing = 0; // 变成站立
							setCurrentPattern("normal");
							break STEP1;
						}
					}
				} else {
					terrain = runtime.world.getTerrain(centerX, centerY); // 检查中点
					if (!Terrains.isLadder(terrain)) {
						// 梯子消失 ?
						climbing = 0; // 变成站立
						setCurrentPattern("normal");
						break STEP1;
					}
				}
			}
			
			// 这里附加判断:
			// 角色是不能够攀爬房间区域以外的梯子的. 否则切换房间的判定将出现问题
			if (runtime.world.getCurrentRoom().containInRoomForBlock(centerX, centerY) == -1) {
				climbing = 0;
				setCurrentPattern("normal");
				break STEP1;
			}
		}
		
		// 阶段 2
		STEP2: {
			int px = box.anchorX;
			if (climbing == 0) {
				if (adhere) {
					box.setAnchorX((int) ((blockRight(px) + 0.5f) * P_PER_BLOCK));
					box.setVelocity(0, 0);
					climbing = 1;
					break STEP2;
				} else if (lower) {
					if (box.gravityDown && occ.ybottomTightly &&
							Terrains.isLadder(runtime.world.getTerrain(lowerX, lowerY - 1))) {
						setCurrentPattern("climb_top_1");
						box.setAnchorX(block2P((lowerX + 0.5f)));
						// y 为梯子顶部, (整数), 即不动
						climbing = 13;
						break STEP2;
					}
					if (!box.gravityDown && occ.ybottomTightly &&
							Terrains.isLadder(runtime.world.getTerrain(lowerX, lowerY))) {
						setCurrentPattern("climb_top_1");
						box.setAnchorX(block2P((lowerX + 0.5f)));
						// y 为梯子顶部 (人是倒立的, 可以视作底部), (整数), 即不动
						climbing = 13;
						break STEP2;
					}
				}
				break STEP2;
			}
			
			// 特殊攀爬状态
			if (climbing >= 2) {
				int last = climbing;
				box.setVelocity(0, 0);
				
				if (up) {
					climbing = (box.gravityDown) ? climbing + 1 : climbing - 1;
				} else if (down) {
					climbing = (box.gravityDown) ? climbing - 1 : climbing + 1;
				}
				
				if (climbing > 13) {
					// 站在楼梯顶端
					climbing = 0;
					setCurrentPattern("normal");
				} else if (climbing == 1) {
					// 回到一般状态
					setCurrentPattern("normal");
					box.setAnchorY(pCeil(box.anchorY) - (int) (P_PER_BLOCK * 0.55f));
				} else {
					if (last == 7 && climbing == 8) {
						setCurrentPattern("climb_top_1");
						box.setAnchorY(pCeil(box.anchorY));
					} else if (last == 8 && climbing == 7) {
						setCurrentPattern("climb_top_0");
						box.setAnchorY(pCeil(box.anchorY) - (int) (P_PER_BLOCK * 0.25f));
					}
				}

				break STEP2;
			}
			
			// 角色将改变形状 (共 3 个形状, 存储在 StateModule 中的 motion 字段)、
			// 姿势 (爬梯子状态, Box 里面的参数)
			
			// 角色如果原本不在爬梯子状态, 需要对角色的位置进行调整,
			// 平移到梯子上;
			box.setAnchorX((int) ((blockRight(px) + 0.5) * P_PER_BLOCK));
			
			// 将根据角色与梯子顶端的距离来设置爬梯子状态;
			
			// 下面需要粗略计算离梯子顶端的距离 (这里的 y 以角色锚点为准)
			int iy = Box.pCeil(box.anchorY); // 单位: p
			boolean yTightly = box.anchorY == iy;
			int distance = (iy - box.anchorY) +
					(yTightly ?
					(Terrains.isLadder(runtime.world.getTerrainForP(px, iy + P_PER_BLOCK)) ? P_PER_BLOCK : 0) :
					(Terrains.isLadder(runtime.world.getTerrainForP(px, iy)) ? P_PER_BLOCK : 0));  // 单位: p
			
			int vy = 0;
			
			// vy
			if (distance < P_PER_BLOCK / 2) {
				// 两个快爬到顶端的状态, 速度和状态需要修改
				climbing = 2;
				box.setAnchorY(iy - P_PER_BLOCK / 4);
				
				// 改变形态
				setCurrentPattern("climb_top_0");
			} else {
				// 离顶端还很远
				// 当在攻击状态时, 将不移动
				if (climbHaltRemain == 0) {
					if (up) {
						vy = climbVelocity;
					} else if (down) { // upOrDown == 2
						vy = -climbVelocity;
					}
				}
			}
			
			if (climbHaltRemain > 0) {
				climbHaltRemain --;
			}
			
			// 如果在底端按下, 也会退出攀爬状态
			if (climbing == 1) {
				if (box.gravityDown && box.bottomTouched && down &&
						runtime.world.getTerrain(centerX, occ.ybottom - 1) == TERRAIN_SOLID ||
						!box.gravityDown && box.topTouched && up &&
						runtime.world.getTerrain(centerX, occ.ytop + 1) == TERRAIN_SOLID) {
					climbing = 0;
					setCurrentPattern("normal");
				}
			}
			
			box.setVelocityX(0);
			box.setVelocityY(vy);
			break STEP2;
		}
		
		if (climbing > 0) {
			slideDuration = -1;
			jumpVel = 0;
		}
		
		if (climbing != lastClimbing) {
			System.out.println(runtime.ticker.count + ":" + climbing);
			lastClimbing = climbing;
		}
	}
	
	int lastClimbing = 0;
	
	private void handleMotion() {
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
			jumpStart = !lastJump && jump && stiffness == 0 && !inAir;
			jumpEnd = (lastJump && !jump) || stiffness > 0 && inAir;
			
			// 攀爬中起跳问题
			if (climbing == 1) { // 不会僵直
				if (!lastJump && jump) {
					climbing = 0;
				}
			} else if (climbing > 1) { // 不会僵直
				jumpStart = !lastJump && jump;
				if (jumpStart) {
					climbing = 0;
	 				box.setAnchorY(pCeil(box.anchorY));
					runtime.world.freshBox(box, true);
					inAir = box.inAir;
				}
			}
			
			// 缓解跳不起来的情况
			if (jumpPressDuration == 10) {
				jumpPressDuration = -1;
			}
			if (jumpPressDuration >= 0) {
				jumpPressDuration ++;
			}
			if (jump && !lastJump && stiffness == 0 && inAir) {
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
		
		if (climbing > 0) {
			return;
		}
		
		// 5. 执行左右移动
		if (stiffness != 0) {
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
		
		if (attacking && climbing > 0) {
			climbHaltRemain = STEPS_PER_SECOND / 4; // 0.25s
		}
	}
	
	/* **********
	 *   健康   *
	 ********** */
	
	protected int hpMax = 256 * 28;
	public int hp = hpMax; // 需要初始化
	
	public int stiffnessDuration = (int) (0.45f * Ticker.STEPS_PER_SECOND);
	public int immuneDuration = (int) (1.5f * Ticker.STEPS_PER_SECOND);
	
	/**
	 * 是否在受伤僵直状态. 处于僵直状态, 参数大于 0, 为剩余时间 (步)
	 */
	public int stiffness;
	/**
	 * 剩余无敌时间
	 */
	public int immuneRemain;
	/**
	 * 防御等级
	 */
	public int defenseLevel = 0;
	
	private void handleImmuse() {
		if (stiffness > 0) {
			stiffness--;
		}
		if (immuneRemain > 0) {
			immuneRemain--;
		}
		if (immuneRemain == 0) {
			defenseLevel = 0;
			// debug
			hp = hpMax;
		}
	}
	
	private void recieveDamage(FoeEvent eve) {
		int camp = eve.value.getInt("camp");
		if (camp != this.camp) {
			// 确定受伤啦
			int damage = eve.value.getInt("damage");
			int level = eve.value.getInt("level");
			
			if (defenseLevel < level) {
				eve.value.get("recieved").set(true);
				
				hp -= damage;
				stiffness = stiffnessDuration;
				immuneRemain = immuneDuration;
				defenseLevel = 10;
				slideDuration = -1;
				climbing = 0;
				setCurrentPattern("normal"); // TODO 不计滑铲时无法恢复的情况
				
				if (hp <= 0) {
					System.out.println("Megaman -- destroy");
//					this.desaktroy();
				}
			}
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
