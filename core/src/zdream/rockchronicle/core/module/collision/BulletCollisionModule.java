package zdream.rockchronicle.core.module.collision;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonValue.ValueType;

import zdream.rockchronicle.RockChronicle;
import zdream.rockchronicle.core.character.CharacterEntry;
import zdream.rockchronicle.core.character.event.CharacterEvent;
import zdream.rockchronicle.platform.body.Box;
import zdream.rockchronicle.platform.world.LevelWorld;

public class BulletCollisionModule extends CollisionModule {

	public BulletCollisionModule(CharacterEntry parent) {
		super(parent);
		
	}
	
	@Override
	public void init(FileHandle file, JsonValue value) {
		super.init(file, value);
		
		JsonValue ocollisionc = value.get("collision");
		level = ocollisionc.getInt("level", 9);
		float fdamage = ocollisionc.getFloat("damage", -1);
		if (fdamage < 0) {
			damage = -1;
		} else {
			damage = (int) (fdamage * 256 + 0.1f);
		}
		
		setSituation("collision.level", new JsonValue(level));
		setSituation("collision.damage", new JsonValue(damage));
	}
	
	/* **********
	 * 基本参数 *
	 ********** */
	/**
	 * 碰撞等级
	 */
	public int level;
	/**
	 * 碰撞真实伤害. (显示伤害 * 256) 非负数. 没有伤害的碰撞体该值为 -1
	 */
	public int damage;
	
	/* **********
	 * 碰撞逻辑 *
	 ********** */
	
	@Override
	protected boolean needCheckOverlaps(Box box, LevelWorld world) {
		return super.needCheckOverlaps(box, world) && damage >= 0;
	}
	
	/**
	 * 对碰撞的、重合的其它角色的碰撞盒子进行判断.
	 * @param box
	 * @return
	 *   如果还需要判断其它的盒子, 则返回 true; 如果不再判断其它盒子, 返回 false
	 */
	protected boolean doForOverlapsBox(Box box) {
		// 阵营判断部分
		int camp = getInt("camp.camp", 0);
		int targetId = box.parentId;
		
		try {
			CharacterEntry target = RockChronicle.INSTANCE.runtime.findEntry(targetId);
			int targetCamp = target.getInt("camp.camp", 0);
			
			JsonValue jattackAccepted = getJson("camp.attackAccepted");
			boolean attackAccepted = jattackAccepted.getBoolean(Integer.toString(targetCamp), true);
			if (!attackAccepted) {
				return true; // 自己不能够攻击对方
			}
			
			JsonValue jdefenseAccepted = target.getJson("camp.defenseAccepted");
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
			
			// 结果比对部分, 现在有 4 种可能, accepted, ignore, absorbed, blocked
			String result = event.value.getString("result", "ignored");
			if ("ignored".equals(result)) {
				return true;
			}
		} catch (RuntimeException e) {
			e.printStackTrace();
			System.err.println(String.format("CollisionModule: 角色#%d, 对方#%d", parent.id, targetId));
			return true;
		}
		
		if ("once".equals(this.executeType)) {
			isFunctioned = false;
			willDelete = true;
		}
		return isFunctioned;
	}

}
