package zdream.rockchronicle.core.character.collision;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonValue.ValueType;

import zdream.rockchronicle.RockChronicle;
import zdream.rockchronicle.core.character.CharacterEntry;
import zdream.rockchronicle.core.character.event.CharacterEvent;
import zdream.rockchronicle.core.character.module.AbstractModule;
import zdream.rockchronicle.core.character.module.MotionModule;
import zdream.rockchronicle.core.character.motion.IBoxHolder;
import zdream.rockchronicle.core.character.parameter.JsonCollector;
import zdream.rockchronicle.platform.body.Box;
import zdream.rockchronicle.platform.world.LevelWorld;

/**
 * <p>碰撞模块
 * <p>产生伤害以及其它效果的源头. 优先级 -128
 * </p>
 * 
 * @author Zdream
 * @since v0.0.1
 * @date
 *   2019-05-08 (create)
 */
public abstract class CollisionModule extends AbstractModule {
	
	public static final String NAME = "Collision";

	public CollisionModule(CharacterEntry parent) {
		super(parent);
		
	}

	@Override
	public String name() {
		return NAME;
	}
	
	@Override
	public int priority() {
		return -0x80;
	}
	
	protected JsonCollector collisionc;
	
	@Override
	public void init(FileHandle file, JsonValue value) {
		super.init(file, value);
		
		JsonValue ocollisionc = value.get("collision");
		level = ocollisionc.getInt("level", 9);
		damage = ocollisionc.getFloat("damage", 0);
		executeType = ocollisionc.getString("execute", "repeat");
		
		addCollector(collisionc = new JsonCollector(this::getCollisionJson, "collision"));
		parent.addSubscribe("health_exhausted", this);
	}
	
	@Override
	public void willDestroy() {
		parent.removeSubscribe("health_exhausted", this);
		super.willDestroy();
	}
	
	@Override
	public void determine(LevelWorld world, int index, boolean hasNext) {
		super.determine(world, index, hasNext);
		this.searchOverlapsBox(getSingleBox(), world);
	}
	
	@Override
	public void onStepFinished(LevelWorld world, boolean isPause) {
		if (willDelete) {
			parent.willDestroy();
		}
		
		super.onStepFinished(world, isPause);
	}
	
	/* **********
	 * 基本参数 *
	 ********** */
	/**
	 * 碰撞等级
	 */
	public int level;
	/**
	 * 碰撞伤害. 非负数. 没有伤害的碰撞体该值为 0
	 */
	public float damage;
	/**
	 * 执行方式
	 */
	public String executeType;
	
	public JsonValue getCollisionJson() {
		JsonValue v = new JsonValue(ValueType.object);
		
		v.addChild("level", new JsonValue(level));
		v.addChild("damage", new JsonValue(damage));
		v.addChild("execute", new JsonValue(executeType));
		
		return v;
	}
	
	/*
	 * 状态
	 */
	
	/**
	 * 本帧结束时是否删除该帧
	 */
	public boolean willDelete;
	/**
	 * 是否能发挥效果
	 */
	public boolean isFunctioned = true;

	/* **********
	 * 工具方法 *
	 ********** */
	
	/**
	 * 如果本角色只有一个碰撞盒子, 则调用该方法来获取其碰撞盒子
	 * @return
	 */
	protected Box getSingleBox() {
		MotionModule mm = parent.getMotion();
		if (mm instanceof IBoxHolder) {
			return ((IBoxHolder) mm).getBox();
		}
		return null;
	}
	
	protected void searchOverlapsBox(Box box, LevelWorld world) {
		if (box == null) {
			return;
		}
		if (isFunctioned) {
			world.overlaps(box, this::doForOverlapsBox);
		}
	}
	
	/**
	 * 对碰撞的、重合的其它角色的碰撞盒子进行判断.
	 * @param box
	 * @return
	 *   如果还需要判断其它的盒子, 则返回 true; 如果不再判断其它盒子, 返回 false
	 */
	protected boolean doForOverlapsBox(Box box) {
		final String[] path = new String[] {"camp", "camp"};
		
		// 阵营判断部分
		int camp = parent.getInt(path, 0);
		int targetId = box.parentId;
		
		try {
			CharacterEntry target = RockChronicle.INSTANCE.runtime.findEntry(targetId);
			int targetCamp = target.getInt(path, 0);
			
			JsonValue jattackAccepted = parent.getJson(new String[] {"camp", "attackAccepted"});
			boolean attackAccepted = jattackAccepted.getBoolean(Integer.toString(targetCamp), true);
			if (!attackAccepted) {
				return true; // 自己不能够攻击对方
			}
			
			JsonValue jdefenseAccepted = target.getJson(new String[] {"camp", "defenseAccepted"});
			boolean defenseAccepted = jdefenseAccepted.getBoolean(Integer.toString(camp), true);
			if (!defenseAccepted) {
				return true; // 对方不接受这次攻击
			}
			
			// 攻击实施部分
			CharacterEvent event = new CharacterEvent("outside_collision");
			JsonValue v = new JsonValue(ValueType.object);
			event.value = v;
			v.addChild("attackId", new JsonValue(parent.id));
			v.addChild("attackCamp", new JsonValue(camp));
			v.addChild("damage", new JsonValue(damage));
			target.publishNow(event);
			
			// 结果比对部分
			String result = event.value.getString("result", "ignored");
			System.out.println(String.format("CollisionModule: 碰撞结果为 %s", result));
//			if ("ignored".equals(result)) {
//				return false;
//			}
		} catch (RuntimeException e) {
			e.printStackTrace();
			System.err.println(String.format("CollisionModule: 角色#%d, 对方#%d", parent.id, targetId));
			return false;
		}
		
		if ("once".equals(this.executeType)) {
			isFunctioned = false;
			willDelete = true;
		}
		return isFunctioned;
	}
	
	@Override
	public void receiveEvent(CharacterEvent event) {
		if ("health_exhausted".equals(event.name)) {
			isFunctioned = false;
			return;
		}
		super.receiveEvent(event);
	}

}
