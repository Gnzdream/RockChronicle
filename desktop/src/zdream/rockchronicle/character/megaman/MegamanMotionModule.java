package zdream.rockchronicle.character.megaman;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.utils.JsonValue;

import zdream.rockchronicle.character.MotionModule;
import zdream.rockchronicle.core.Config;
import zdream.rockchronicle.platform.body.Box;
import zdream.rockchronicle.platform.world.LevelWorld;

public class MegamanMotionModule extends MotionModule {
	
	Megaman parent;
	
	/**
	 * 是否向左或向右移动. 左和右不会同时为 true.
	 */
	boolean left, right;
	/**
	 * 跳跃暂存
	 */
	boolean jump, jumpEnd;
	
	public final Box box = new Box();
	
	/**
	 * 在世界场上面控制的物体
	 */
	public Body body;
	public Fixture fixture;
	
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
	
	/**
	 * Megaman 中使用的消息
	 */
	public static final String
		INFO_LEFT = "left",
		INFO_RIGHT = "right",
		INFO_JUMP = "jump",
		INFO_JUMP_END = "jump_end";

	public MegamanMotionModule(Megaman parent) {
		super(parent);
		this.parent = parent;
		
		this.horizontalVelDelta =
				HORIZONTAL_VELOCITY_DELTA * LevelWorld.TIME_STEP * LevelWorld.TIME_STEP;
		this.horizontalVelMax = HORIZONTAL_VELOCITY_MAX * LevelWorld.TIME_STEP;
		
		this.box.jumpImpulse = 21.36f * LevelWorld.TIME_STEP;
		this.box.jumpDecay = -72 * LevelWorld.TIME_STEP * LevelWorld.TIME_STEP;
		this.box.maxDropVelocity = -28 * LevelWorld.TIME_STEP;
	}
	
	public void initCollideRect(JsonValue rectArray) {
		JsonValue value = rectArray.get(0);
		 // 单位: 像素 -> 格子
		box.box.width = (value.getInt("w") / (float) Config.INSTANCE.blockWidth);
		box.box.height = (value.getInt("h") / (float) Config.INSTANCE.blockHeight);
		box.box.x = (value.getInt("x") / (float) Config.INSTANCE.blockWidth);
		box.box.y = (value.getInt("y") / (float) Config.INSTANCE.blockHeight);
	}

	@Override
	public void step(LevelWorld world, int index, boolean hasNext) {
		Vector2 vel = box.velocity; // 速度
		float vx = vel.x, vy = vel.y;
		
		// 1. 判断重合以及补救方法
		
		// 2. 判断角色状态
		boolean bottomStop = world.bottomStop(box); // 无论速度是否向上, 开始的落地检测都不能少
		boolean topStop = world.topStop(box);
		
		// 3. 执行上下移动 TODO
		if (bottomStop) {
			vy = 0;
			if (jump) {
				// 执行跳跃
				vy = box.jumpImpulse;
			}
		} else if (!bottomStop) {
			vy += box.jumpDecay;
			if (vy < box.maxDropVelocity) {
				vy = box.maxDropVelocity;
			}
		}
		if (vy > 0 && (jumpEnd || topStop)) {
			vy = 0;
		}
		
		// 设置的最终速度 Y
		box.setVelocityY(vy);
		world.execVerticalMotion(box);
		
		// 4. 执行左右移动
		boolean leftStop = world.leftStop(box), rightStop = world.rightStop(box);
		if (leftStop) {
			System.out.println("l");
		} else if (rightStop) {
			System.out.println("r");
		}
		// 正常情况下, 每秒增加 horizontalVelDelta 的水平速度, horizontalVelMax 为最值.
		if (left) {
			orientation = false;
			if (bottomStop) {
				vx -= horizontalVelDelta;
				if (vx > 0 || leftStop) {
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
				if (vx < 0 || rightStop) {
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
		
		// 重置参数
		if (!hasNext) {
			resetControl();
		}
		this.jump = false;
		this.jumpEnd = false;
	}
	
	/**
	 * 将控制洛克人的键位、命令进行重置
	 */
	private void resetControl() {
		left = false;
		right = false;

	}
	
	/**
	 * 设置锚点位置, 单位: 格子
	 */
	public void setBlockPos(final int blockx, final int blocky) {
		box.setAnchor(blockx + 0.5f, blocky);
	}

	@Override
	public void recvControl(String[] infos) {
		for (int i = 0; i < infos.length; i++) {
			if (infos[i] == null) {
				continue;
			}
			
			switch (infos[i]) {
			case INFO_LEFT:
				left = true;
				break;
			case INFO_RIGHT:
				right = true;
				break;
			case INFO_JUMP:
				jump = true;
				break;
			case INFO_JUMP_END:
				jumpEnd = true;
				break;

			default:
				break;
			}
		}
		
	}

	@Override
	public void createBody(LevelWorld world) {
//		BodyDef bodyDef = new BodyDef();
//		bodyDef.type = BodyType.DynamicBody;
//		bodyDef.position.set(anchorPoint.x, anchorPoint.y); // 锚点位置
//		bodyDef.gravityScale = 0;
//		bodyDef.fixedRotation = true; // 不旋转
//		
//		body = world.world.createBody(bodyDef);
//		body.setUserData(OtherBodyParam.INSTANCE);
//		
//		PolygonShape shape = new PolygonShape();
//		Vector2 center = rect.getCenter(new Vector2()); // 相对于锚点的位置 (单位: 格子)
//		shape.setAsBox(rect.width / 2, rect.height / 2, center, 0);
//		
//		// Fixture 固定器将 shape 固定到 body 上
//		FixtureDef fixtureDef = new FixtureDef();
//		fixtureDef.shape = shape;
//		fixtureDef.density = 0.5f; 
//		fixtureDef.friction = 0.4f;
//		fixtureDef.restitution = 0.6f;
//		
//		fixture = body.createFixture(fixtureDef);
//		shape.dispose();
//		
//		// Filter 碰撞过滤
//		Filter filter = new Filter();
//		filter.categoryBits = 0x2;
//		filter.maskBits = 0x1;
//		fixture.setFilterData(filter);
	}

}
