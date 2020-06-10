package zdream.rockchronicle.core.module.weapon;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectMap;

import zdream.rockchronicle.core.character.CharacterEntry;
import zdream.rockchronicle.core.character.event.CharacterEvent;
import zdream.rockchronicle.core.character.parameter.CharacterParameter;
import zdream.rockchronicle.platform.body.Box;
import zdream.rockchronicle.utils.JsonUtils;

/**
 * <p>基础武器模块
 * </p>
 * 
 * @author Zdream
 * @since v0.0.1
 * @date
 *   2019-06-02 (created)
 *   2019-06-06 (last modified)
 */
public class BaseWeaponModule extends WeaponModule {
	
	/*
	 * weapon.spawnPosition.x
	 * weapon.spawnPosition.y
	 * 子弹出来的位置
	 */
	
	
	/*
	 * 子弹列表
	 */
	
	ObjectMap<String, WeaponItem> weapons = new ObjectMap<>();
	
	class WeaponItem {
		/**
		 * 如果为 true, 子弹出来的位置以下面的 spawnx, spawny 为准;
		 * 如果为 false, 子弹出来的位置从 parent.state 里面取
		 */
		boolean spawnPosReset = false;
		/**
		 * 单位: p
		 */
		int spawnx, spawny;
		
		String name;
		String model;
		JsonValue param;
	}

	public BaseWeaponModule(CharacterEntry ch) {
		super(ch, "base");
	}
	
	@Override
	public void init(FileHandle file, JsonValue value) {
		super.init(file, value);
		
		// 解析
		parseJson(value);
		
		// 监听器
		parent.addSubscribe("attack_attempt", this);
	}
	
	private void parseJson(JsonValue value) {
		JsonValue oweapon = value.get("weapon");
		// 默认
		JsonValue ospawnPosition = oweapon.get("spawnPosition");
		if (ospawnPosition != null) {
			int px = (int) (ospawnPosition.getFloat("x", 0) * Box.P_PER_BLOCK);
			int py = (int) (ospawnPosition.getFloat("y", 0) * Box.P_PER_BLOCK);
			
			setSituation("weapon.spawnPosition.x", new JsonValue(px));
			setSituation("weapon.spawnPosition.y", new JsonValue(py));
		}
		
		// 每个武器的参数
		JsonValue aweapons = oweapon.get("weapons");
		for (JsonValue entry = aweapons.child; entry != null; entry = entry.next) {
			parseWeapon(entry);
		}
	}
	
	private void parseWeapon(JsonValue oweapon) {
		WeaponItem item = new WeaponItem();
		String name = oweapon.getString("name");
		item.name = name;
		item.model = oweapon.getString("model");
		
		JsonValue ospawnPosition = oweapon.get("spawnPosition");
		if (ospawnPosition != null) {
			item.spawnPosReset = true;
			item.spawnx = ospawnPosition.getInt("x", 0);
			item.spawny = ospawnPosition.getInt("y", 0);
		}
		
		item.param = oweapon.get("param");
		this.weapons.put(name, item);
	}
	
	@Override
	public int priority() {
		return 6;
	}
	
	@Override
	public void receiveEvent(CharacterEvent event) {
		if ("attack_attempt".equals(event.name)) {
			String weapon = event.value.getString("weapon");
			handleAttack(weapon, event.value.get("param"));
		}
		super.receiveEvent(event);
	}
	
	public void handleAttack(String weapon, JsonValue param) {
		WeaponItem item = weapons.get(weapon);
		
		boolean orientation = parent.getBoxModule().getBox().orientation; // true : 向右
		int spawnx;
		int spawny;
		if (item.spawnPosReset) {
			spawnx = item.spawnx;
			spawny = item.spawny;
		} else {
			spawnx = getInt("weapon.spawnPosition.x", 0);
			spawny = getInt("weapon.spawnPosition.y", 0);
		}
		
		Box box = parent.getBoxModule().getBox();
		
		int px = (orientation) ? box.anchorX + spawnx : box.anchorX - spawnx;
		int py = box.anchorY + spawny;
		setState("weapon.attacking", new JsonValue(true));
		
		parent.createEntry(item.model,
				CharacterParameter.newInstance(JsonUtils.mergeJson(item.param, param))
					.setBoxAnchorP(px, py)
					.setStateOrientation(orientation)
					.setMotionFlipX(!orientation)
					.setCamp(getInt("camp.camp", 0))
					.get());
		
		parent.publish(new CharacterEvent("open_fire")); // 暂时没有附加信息
	}

}
