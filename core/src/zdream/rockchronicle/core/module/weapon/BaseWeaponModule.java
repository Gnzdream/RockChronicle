package zdream.rockchronicle.core.module.weapon;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectMap;

import zdream.rockchronicle.core.character.CharacterEntry;
import zdream.rockchronicle.core.character.event.CharacterEvent;
import zdream.rockchronicle.platform.body.Box;

/**
 * <p>基础武器模块
 * </p>
 * 
 * @author Zdream
 * @since v0.0.1
 * @date
 *   2019-06-02 (created)
 *   2019-06-02 (last modified)
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
	
	ObjectMap<String, JsonValue> weapons = new ObjectMap<>();

	public BaseWeaponModule(CharacterEntry ch) {
		super(ch);
	}
	
	@Override
	public void init(FileHandle file, JsonValue value) {
		super.init(file, value);
		
		// 解析
		JsonValue oweapon = value.get("weapon");
		JsonValue ospawnPosition = oweapon.get("spawnPosition");
		if (ospawnPosition != null) {
			setSituation("weapon.spawnPosition.x",
					new JsonValue(ospawnPosition.getFloat("x", 0)));
			setSituation("weapon.spawnPosition.y",
					new JsonValue(ospawnPosition.getFloat("y", 0)));
		}
		
		// 监听器
		parent.addSubscribe("attack_attempt", this);
	}
	
	@Override
	public int priority() {
		return 6;
	}
	
	@Override
	public String description() {
		return "base";
	}
	
	@Override
	public void receiveEvent(CharacterEvent event) {
		if ("attack_attempt".equals(event.name)) {
			handleAttack(event.value);
		}
		super.receiveEvent(event);
	}
	
	public void handleAttack(JsonValue param) {
		System.out.println("开火！" + param);
		
		boolean orientation = getBoolean("state.orientation", true); // true : 向右
		float spawnx = getFloat("weapon.spawnPosition.x", 0);
		float spawny = getFloat("weapon.spawnPosition.y", 0);
		
		Box box = parent.getBoxModule().getBox();
		
	}

}
