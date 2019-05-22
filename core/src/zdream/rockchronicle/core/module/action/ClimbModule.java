package zdream.rockchronicle.core.module.action;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonValue.ValueType;

import zdream.rockchronicle.core.character.CharacterEntry;
import zdream.rockchronicle.core.character.event.CharacterEvent;
import zdream.rockchronicle.core.character.parameter.JsonCollector;
import zdream.rockchronicle.core.module.AbstractModule;
import zdream.rockchronicle.platform.body.Box;
import zdream.rockchronicle.platform.region.Terrains;
import zdream.rockchronicle.platform.world.LevelWorld;

import static zdream.rockchronicle.platform.world.LevelWorld.*;

/**
 * <p>攀爬模块, 指攀爬梯子
 * </p>
 * 
 * @author Zdream
 * @since v0.0.1
 * @date
 *   2019-05-20 (created)
 *   2019-05-20 (last modified)
 */
public class ClimbModule extends AbstractModule {

	protected JsonCollector climbc, climbpc;
	protected JsonValue param;
	
	/**
	 * 攀爬速度, 每步攀爬的格子数. 配置项
	 */
	float climbVelocity;
	
	/*
	 * 以下参数不在 JsonCollector 中
	 */
	/**
	 * 0: 无, 1: 上, 2: 下,
	 * 每步不重置
	 */
	byte upOrDown;
	
	/**
	 * 是否在攀爬状态.
	 * 如果上一帧为 true, 后面每帧都需要检测, 直到将其设置为 false
	 */
	boolean climbing;
	
	/**
	 * 由于在攀爬状态下攻击将有 0.5 秒时间不能上下移动, 这里记录剩余的恢复速度
	 */
	int haltRemain;
	
	public ClimbModule(CharacterEntry parent) {
		super(parent);
	}
	
	@Override
	public void init(FileHandle file, JsonValue value) {
		super.init(file, value);
		
		JsonValue v = value.get("climbParam");
		float fv = v.getFloat("velocity");
		climbVelocity = fv * TIME_STEP;
		
		param = new JsonValue(ValueType.object);
		param.addChild("velocity", new JsonValue(fv));
		
		parent.addSubscribe("ctrl_axis", this);
		parent.addSubscribe("ctrl_motion", this);
		parent.addSubscribe("open_fire", this);
		addCollector(climbc = new JsonCollector(this::createClimbJson, "climb"));
		addCollector(climbpc = new JsonCollector(() -> param, "climbParam"));
	}

	@Override
	public String name() {
		return "Climb";
	}
	
	@Override
	public int priority() {
		return 0x81;
	}
	
	private JsonValue createClimbJson() {
		JsonValue v = new JsonValue(ValueType.object);
		v.addChild("climbing", new JsonValue(climbing));
		v.addChild("haltRemain", new JsonValue(haltRemain));
		return v;
	}
	
	@Override
	public void determine(LevelWorld world, int index, boolean hasNext) {
		super.determine(world, index, hasNext);
		
		if (!climbing && upOrDown == 0) {
			return;
		}
		
		boolean stiffness = parent.getBoolean(new String[] {"state", "stiffness"}, false);
		if (stiffness) { // 被攻击硬直时, 后面的判断都不用继续了
			climbing = false;
			climbc.clear(); return;
		}
		
		// 检测是否在攀登状态. 取消攀登状态的可能有以下情况: 
		// 1. 梯子消失 (现在正在检查的)
		// 2. 被攻击 (前面已经过滤)
		// 3. 跳下来 (监听事件, 不在这里)
		
		Box box = parent.getBoxModule().getBox();
		// 盒子的中心点
		Vector2 centerPoint = box.getPosition().getCenter(new Vector2());
		// 现在查看这个中心点映射到的是哪个地形
		byte terrain = world.getTerrain((int) centerPoint.x, (int) centerPoint.y);
		if (!Terrains.isLadder(terrain)) {
			climbing = false;
			climbc.clear(); return;
		}
		// 这里附加判断:
		// 角色是不能够攀爬房间区域以外的梯子的. 否则切换房间的判定将出现问题
		if (!world.currentRoom.containInRoom(centerPoint.x, centerPoint.y)) {
			climbing = false;
			climbc.clear(); return;
		}
		
		if (!climbing) {
			// 到了这里说明: 中心点是梯子的地形块, 不在房间外
			// 原先还不是攀爬状态, 不在硬直状态, 按了上或者下
			climbc.clear(); climbing = true;
		}
		
		// 角色将改变形状 (共 3 个形状, 存储在 StateModule 中的 motion 字段)、
		// 姿势 (爬梯子状态, Box 里面的参数)
		
		// 角色如果原本不在爬梯子状态, 需要对角色的位置进行调整,
		// 平移到梯子上;
		
		// 将根据角色与梯子顶端的距离来设置爬梯子状态;
		
		// 下面需要粗略计算离梯子顶端的距离
		int iy = (int) Math.ceil(centerPoint.y);
//		boolean yTightly = centerPoint.y == iy;
		float distance = (iy - centerPoint.y) +
				(Terrains.isLadder(world.getTerrain((int) centerPoint.x, iy + 1)) ? 1 : 0);
		
		float vx, vy;
		// vx
		vx = ((int) centerPoint.x) + 0.5f - centerPoint.x;
		
		// vy
		if (distance < 0.5f) {
			// 两个快爬到顶端的状态, 速度和状态需要修改
			System.out.println("distance < 0.5");
			vy = 0;
		} else {
			// 离顶端还很远
			// 当在攻击状态时, 将不移动
			vy = 0;
			if (haltRemain == 0) {
				if (upOrDown == 1) {
					vy = climbVelocity;
				} else if (upOrDown == 2) { // upOrDown == 2
					vy = -climbVelocity;
				}
			}
		}
		
		if (haltRemain > 0) {
			haltRemain --;
		}
		
		box.setVelocityX(vx);
		box.setVelocityY(vy);
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
		case "open_fire":
			recvOpenFire(event);
			break;
		default:
			super.receiveEvent(event);
			break;
		}
	}

	private void recvCtrlAxis(CharacterEvent event) {
		boolean up = event.value.getBoolean("up"),
				down = event.value.getBoolean("down");
		
		if (up) {
			upOrDown = 1;
		} else if (down) {
			upOrDown = 2;
		} else {
			upOrDown = 0;
		}
	}
	
	private void recvCtrlMotion(CharacterEvent event) {
		boolean jumpChange = event.value.getBoolean("jumpChange");
		
		if (jumpChange) {
			climbing = false;
			climbc.clear();
		}
	}
	
	private void recvOpenFire(CharacterEvent event) {
		if (climbing) {
			haltRemain = LevelWorld.STEPS_PER_SECOND / 2;
		}
	}

}
