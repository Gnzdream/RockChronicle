package zdream.rockchronicle.core.module.scout;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonValue.ValueType;

import zdream.rockchronicle.core.character.CharacterEntry;
import zdream.rockchronicle.core.character.event.CharacterEvent;
import zdream.rockchronicle.core.module.AbstractModule;
import zdream.rockchronicle.platform.body.Box;
import zdream.rockchronicle.platform.world.LevelWorld;

/**
 * <p>搜寻敌人的侦查模块
 * <p>搜寻离自己最近的敌人, 并将敌人的 id 和位置信息打包成消息发送出去
 * <p>判断敌人的依据是: 与自己的阵营不同; 角色类型属于 "leader" 或 "foe"
 * </p>
 * 
 * @author Zdream
 * @since v0.0.1
 * @date
 *   2019-06-06 (created)
 *   2019-06-09 (last modified)
 */
public class EnemySearchingScoutModule extends AbstractModule {

	public EnemySearchingScoutModule(CharacterEntry parent) {
		super(parent, "scout", "enemySearching");
	}

	@Override
	public int priority() {
		return -30;
	}
	
	@Override
	public void init(FileHandle file, JsonValue value) {
		super.init(file, value);
		
		boolean enable = true;
		
		JsonValue oscout = value.get("scout");
		JsonValue oes = oscout.get("enemySearching");
		if (oes != null && !oes.isNull()) {
			executeType = oes.getString("execute", "repeat");
			enable = oes.getBoolean("enable", true);
		}
		
		setSituation("scout.enemySearching.active", new JsonValue(enable));
	}
	
	/* **********
	 *   参数   *
	 ********** */
	
	/**
	 * 执行方式, 只执行一次 "once", 每步都执行 "repeat" (默认), 还有其它
	 */
	String executeType;
	
	/* **********
	 *   执行   *
	 ********** */

	@Override
	public void determine(LevelWorld world, int index, boolean hasNext) {
		super.determine(world, index, hasNext);
		
		boolean active = getBoolean("scout.enemySearching.active", false);
		// 自己的阵营
		int camp = getInt("camp.camp", 0);
		
		if (active) {
			// 缓存最近的敌人
			CharacterEntry c0 = null;
			// 最近敌人的距离
			float distance0 = Float.MAX_VALUE;
			
			Box box = parent.getBoxModule().getBox();
			float anchorX = box.anchor.x;
			float anchorY = box.anchor.y;
			
			Array<CharacterEntry> entries = world.entries;
			for (int i = 0; i < entries.size; i++) {
				CharacterEntry c = entries.get(i);
				
				if (!c.type.equals("leader") && !c.type.equals("foe")) {
					continue;
				}
				int targetCamp = c.getInt("camp.camp", 0);
				if (targetCamp == camp) {
					continue; // TODO 是否需要添加友好关系矩阵
				}
				
				Box targetBox = c.getBoxModule().getBox();
				float targetX = targetBox.anchor.x;
				float targetY = targetBox.anchor.y;
				
				float distance = Math.abs(anchorX - targetX) + Math.abs(anchorY - targetY);
				if (distance < distance0) {
					c0 = c;
					distance0 = distance;
				}
			}
			
			if (c0 != null) {
				// 发布
				CharacterEvent event = new CharacterEvent("find_enemy");
				JsonValue v = new JsonValue(ValueType.object);
				v.addChild("id", new JsonValue(c0.id));
				
				Box targetBox = c0.getBoxModule().getBox();
				v.addChild("x", new JsonValue(targetBox.anchor.x));
				v.addChild("y", new JsonValue(targetBox.anchor.y));
				
				event.value = v;
				parent.publish(event);
				
				System.out.println("EnemySearchingScoutModule  find_enemy: " + c0);
				
				if ("once".equals(executeType)) {
					setSituation("scout.enemySearching.active", new JsonValue(false));
				}
			}
		}
	}
	
}
