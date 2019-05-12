package zdream.rockchronicle.core.module.collision;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonValue.ValueType;

import zdream.rockchronicle.RockChronicle;
import zdream.rockchronicle.core.character.CharacterEntry;
import zdream.rockchronicle.core.character.event.CharacterEvent;
import zdream.rockchronicle.platform.body.Box;

public class BulletCollisionModule extends CollisionModule {

	public BulletCollisionModule(CharacterEntry parent) {
		super(parent);
		
	}
	
	@Override
	public void init(FileHandle file, JsonValue value) {
		super.init(file, value);
		
		JsonValue ocollisionc = value.get("collision");
		level = ocollisionc.getInt("level", 9);
		damage = (int) (ocollisionc.getFloat("damage", 0) * 256);
	}
	
	/* **********
	 * 基本参数 *
	 ********** */
	/**
	 * 碰撞等级
	 */
	public int level;
	/**
	 * 碰撞真实伤害. (显示伤害 * 256) 非负数. 没有伤害的碰撞体该值为 0
	 */
	public int damage;
	
	public JsonValue getCollisionJson() {
		JsonValue v = super.getCollisionJson();
		
		v.addChild("level", new JsonValue(level));
		v.addChild("damage", new JsonValue(damage));
		
		return v;
	}
	
	/* **********
	 * 碰撞逻辑 *
	 ********** */
	
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
			if ("ignored".equals(result)) {
				return false;
			}
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

}
