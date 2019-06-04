package zdream.rockchronicle.core.module.action;

import static zdream.rockchronicle.platform.region.ITerrainStatic.TERRAIN_SOLID;
import static zdream.rockchronicle.platform.world.LevelWorld.TIME_STEP;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.JsonValue;

import zdream.rockchronicle.core.character.CharacterEntry;
import zdream.rockchronicle.core.character.event.CharacterEvent;
import zdream.rockchronicle.core.module.AbstractModule;
import zdream.rockchronicle.core.move.IMovable;
import zdream.rockchronicle.platform.body.Box;
import zdream.rockchronicle.platform.body.BoxOccupation;
import zdream.rockchronicle.platform.region.Terrains;
import zdream.rockchronicle.platform.world.LevelWorld;

/**
 * <p>攀爬模块, 指攀爬梯子
 * </p>
 * 
 * @author Zdream
 * @since v0.0.1
 * @date
 *   2019-05-20 (created)
 *   2019-05-26 (last modified)
 */
public class ClimbModule extends AbstractModule implements IMovable {

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
	 * 攀爬状态参数, 0 表示不攀爬, 1 表示攀爬中, [2-13] 表示在梯子顶端的特殊攀爬状态,
	 * 共 12 步时间 (0.1 s)
	 * 
	 * 如果上一帧不为 0, 后面每帧都需要检测, 直到将其设置为 0
	 */
	int climbing;
	
	/**
	 * 由于在攀爬状态下攻击将有 0.5 秒时间不能上下移动, 这里记录剩余的恢复速度
	 */
	int haltRemain;
	
	/**
	 * 补充参数, 本步时间内是否需要从梯子的顶端下降到梯子上
	 */
	boolean lower;
	/**
	 * 如果上面的 lower 为 true 时, 这里写出其横纵坐标
	 */
	float lowerX, lowerY;
	/**
	 * 指示本步时间内是否从 climbing = 0 的状态变成附到梯子上
	 */
	boolean adhere;
	
	public ClimbModule(CharacterEntry parent) {
		super(parent);
	}
	
	@Override
	public void init(FileHandle file, JsonValue value) {
		super.init(file, value);
		
		JsonValue v = value.get("climbParam");
		float fv = v.getFloat("velocity");
		climbVelocity = fv * TIME_STEP;
		setSituation("climb.param.velocity", new JsonValue(climbVelocity));
		
		parent.addSubscribe("ctrl_axis", this);
		parent.addSubscribe("ctrl_motion", this);
		parent.addSubscribe("open_fire", this);

		parent.getBoxModule().addMovable(this, 20);
	}

	@Override
	public String name() {
		return "Climb";
	}
	
	@Override
	public int priority() {
		return 0x81;
	}
	
	/**
	 * 临时参数部分
	 */
	public void setClimbState() {
		setState("climb.climbing", new JsonValue(climbing));
		if (climbing > 0)
			setState("climb.upOrDown", new JsonValue(upOrDown));
		setState("climb.haltRemain", new JsonValue(haltRemain));
	}
	
	@Override
	public void determine(LevelWorld world, int index, boolean hasNext) {
		super.determine(world, index, hasNext);
		
		if (climbing == 0 && upOrDown == 0) {
			return;
		}
		
		// 该方法需要处理的是:
		// 1. 判断角色是否还在梯子上, 如果不是, climbing 设置为 0;
		// 2. 判断角色是否将附到梯子上. 如果是, 将在 move 方法中修改 climbing, 该方法不作修改
		
		// 被攻击硬直时, 后面的判断都不用继续了
		boolean stiffness = getBoolean("state.stiffness", false);
		if (stiffness) { // 被攻击硬直时, 后面的判断都不用继续了
			climbing = 0;
			parent.getBoxModule().setNextPattern("normal");
			setClimbState(); return;
		}
		
		Box box = parent.getBoxModule().getBox();
		// 盒子的中心点
		Vector2 centerPoint = box.getPosition().getCenter(new Vector2());
		// 现在查看这个中心点映射到的是哪个地形
		
		if (climbing == 0) {
			setClimbState();
			BoxOccupation occ = box.getOccupation();
			
			byte terrain = world.getTerrain((int) centerPoint.x, (int) centerPoint.y);
			if (Terrains.isLadder(terrain)) {
				// 这个时候如果按了上或者下, 就应该到梯子上了, 除了一种情况: 你在落地时按下
				if (box.gravityDown && occ.ybottomTightly) {
					if (world.getTerrain((int) centerPoint.x, occ.ybottom - 1) == TERRAIN_SOLID
							&& upOrDown == 2) {
					} else {
						this.adhere = true;
					}
				} else if (!box.gravityDown && occ.ytopTightly) {
					if (world.getTerrain((int) centerPoint.x, occ.ytop + 1) == TERRAIN_SOLID
							&& upOrDown == 1) {
					} else {
						this.adhere = true;
					}
				} else {
					this.adhere = true;
				}
			} else {
				// 还有一种情况, 在梯子顶端按下将下降到下面的梯子上
				if (box.gravityDown && occ.ybottomTightly) {
					if (Terrains.isLadder(world.getTerrain((int) centerPoint.x, occ.ybottom - 1)) && upOrDown == 2) {
						// 可以向下降到梯子上
						lower = true;
						lowerX = ((int) centerPoint.x) + 0.5f;
						lowerY = occ.ybottom;
					}
				} else if (!box.gravityDown && occ.ytopTightly) {
					if (Terrains.isLadder(world.getTerrain((int) centerPoint.x, occ.ytop + 1)) && upOrDown == 1) {
						// 人是倒着的, 可以向上“降”到梯子上
						lower = true;
						lowerX = ((int) centerPoint.x) + 0.5f;
						lowerY = occ.ytop + 1;
					}
				}
			}
			return;
		}
		
		// 到达这里说明 climbing > 0
		
		// 检测是否在攀登状态. 取消攀登状态的可能有以下情况: 
		// 1. 梯子消失 (现在正在检查的) (需要检查中点和锚点, 一项满足即可)
		// 2. 被攻击 (前面已经过滤)
		// 3. 跳下来 (监听事件, 不在这里)
		// 4. 顺着梯子到底, 站到了平地上 (当 climbing <= 7 时需要检查锚点)
			
		if (climbing <= 7) {
			byte terrain = world.getTerrain((int) box.anchor.x, (int) box.anchor.y);
			// 顺着梯子到底, 站到了平地上 (以后会扩展到站到其它实体)
			if (Terrains.isLadder(terrain)) {
				BoxOccupation occ = box.getOccupation();
				
				if (box.gravityDown && occ.ybottomTightly) {
					// TODO 其它平地块
					if (world.getTerrain((int) centerPoint.x, occ.ybottom - 1) == TERRAIN_SOLID
							&& upOrDown == 2) {
						climbing = 0; // 变成站立
						parent.getBoxModule().setNextPattern("normal");
						setClimbState(); return;
					}
				} else if (!box.gravityDown && occ.ytopTightly) {
					// TODO 其它平地块
					if (world.getTerrain((int) centerPoint.x, occ.ytop + 1) == TERRAIN_SOLID
							&& upOrDown == 1) {
						climbing = 0; // 变成站立
						parent.getBoxModule().setNextPattern("normal");
						setClimbState(); return;
					}
				}
			} else {
				terrain = world.getTerrain((int) centerPoint.x, (int) centerPoint.y); // 检查中点
				if (!Terrains.isLadder(terrain)) {
					// 梯子消失 ?
					climbing = 0; // 变成站立
					parent.getBoxModule().setNextPattern("normal");
					setClimbState(); return;
				}
			}
		}
		
		// 这里附加判断:
		// 角色是不能够攀爬房间区域以外的梯子的. 否则切换房间的判定将出现问题
		if (!world.currentRoom.containInRoom(centerPoint.x, centerPoint.y)) {
			climbing = 0;
			parent.getBoxModule().setNextPattern("normal");
			setClimbState(); return;
		}
		
		setClimbState();
	}
	
	@Override
	public void action(LevelWorld world, Box box, CharacterEntry entry) {
		int ix = (int) box.anchor.x;
		
		if (climbing == 0) {
			if (adhere) {
				box.setAnchorX(ix + 0.5f);
				box.setVelocity(0, 0);
				climbing = 1;
				setClimbState(); return;
			} else if (lower) {
				BoxOccupation occ = box.getOccupation();
				
				if (box.gravityDown && occ.ybottomTightly &&
						Terrains.isLadder(world.getTerrain((int) lowerX, (int) lowerY - 1))) {
					parent.getBoxModule().setNextPattern("climb_top_1");
					box.setAnchorX(lowerX);
					// y 为梯子顶部, (整数), 即不动
					climbing = 13;
					setClimbState(); return;
				}
				if (!box.gravityDown && occ.ybottomTightly &&
						Terrains.isLadder(world.getTerrain((int) lowerX, (int) lowerY))) {
					parent.getBoxModule().setNextPattern("climb_top_1");
					box.setAnchorX(lowerX);
					// y 为梯子顶部 (人是倒立的, 可以视作底部), (整数), 即不动
					climbing = 13;
					setClimbState(); return;
				}
			}
			return;
		}
		
		// 特殊攀爬状态
		if (climbing >= 2) {
			int last = climbing;
			box.setVelocity(0, 0);
			
			if (this.upOrDown == 1) {
				climbing = (box.gravityDown) ? climbing + 1 : climbing - 1;
			} else if (this.upOrDown == 2) {
				climbing = (box.gravityDown) ? climbing - 1 : climbing + 1;
			}
			
			if (climbing > 13) {
				// 站在楼梯顶端
				climbing = 0;
				parent.getBoxModule().setNextPattern("normal");
			} else if (climbing == 1) {
				// 回到一般状态
				parent.getBoxModule().setNextPattern("normal");
				box.setAnchorY((float) Math.ceil(box.anchor.y) - 0.55f);
			} else {
				if (last == 7 && climbing == 8) {
					parent.getBoxModule().setNextPattern("climb_top_1");
					box.setAnchorY((float) Math.ceil(box.anchor.y));
				} else if (last == 8 && climbing == 7) {
					parent.getBoxModule().setNextPattern("climb_top_0");
					box.setAnchorY((float) Math.ceil(box.anchor.y) - 0.25f);
				}
			}
			
			setClimbState(); return;
		}
		
		// 角色将改变形状 (共 3 个形状, 存储在 StateModule 中的 motion 字段)、
		// 姿势 (爬梯子状态, Box 里面的参数)
		
		// 角色如果原本不在爬梯子状态, 需要对角色的位置进行调整,
		// 平移到梯子上;
		box.setAnchorX(ix + 0.5f);
		
		// 将根据角色与梯子顶端的距离来设置爬梯子状态;
		
		// 下面需要粗略计算离梯子顶端的距离 (这里的 y 以角色锚点为准)
		int iy = (int) Math.ceil(box.anchor.y);
		boolean yTightly = box.anchor.y == iy;
		float distance = (iy - box.anchor.y) +
				(yTightly ?
				(Terrains.isLadder(world.getTerrain(ix, iy + 1)) ? 1 : 0) :
				(Terrains.isLadder(world.getTerrain(ix, iy)) ? 1 : 0));
		
		float vy = 0;
		
		// vy
		if (distance < 0.5f) {
			// 两个快爬到顶端的状态, 速度和状态需要修改
			climbing = 2;
			box.setAnchorY(iy - 0.25f);
			
			// 改变形态
			parent.getBoxModule().setNextPattern("climb_top_0");
		} else {
			// 离顶端还很远
			// 当在攻击状态时, 将不移动
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
		
		box.setVelocityX(0);
		box.setVelocityY(vy);
		setClimbState();
	}
	
	@Override
	public void stepPassed() {
		lower = false;
		lowerX = 0;
		lowerY = 0;
		adhere = false;
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
		boolean jump = event.value.getBoolean("jump");
		
		if (jumpChange && jump) {
			climbing = 0;
			parent.getBoxModule().setNextPattern("normal");
			setClimbState();
		}
	}
	
	private void recvOpenFire(CharacterEvent event) {
		if (climbing != 0) {
			haltRemain = LevelWorld.STEPS_PER_SECOND / 2;
		}
	}

}
