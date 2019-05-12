package zdream.rockchronicle.core.module.collision;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonValue.ValueType;

import zdream.rockchronicle.RockChronicle;
import zdream.rockchronicle.core.character.CharacterEntry;
import zdream.rockchronicle.platform.body.Box;

/**
 * <p>场的碰撞模块
 * <p>一般不产生伤害.
 * 但是如果指定角色或指定角色的种类进入该区域, 将激活场;
 * 离开则使场待机. 激活的状态记录在状态模块中.
 * </p>
 * 
 * @author Zdream
 * @since v0.0.1
 * @date
 *   2019-05-12 (create)
 */
public class FieldCollisionModule extends CollisionModule {
	
	protected String[] targetTypes;
	protected int[] targetCamps;

	public FieldCollisionModule(CharacterEntry parent) {
		super(parent);
	}
	
	@Override
	public int priority() {
		return 20;
	}
	
	@Override
	public void init(FileHandle file, JsonValue value) {
		super.init(file, value);
		
		JsonValue ocollision = value.get("collision");
		
		// targetTypes
		JsonValue atargets = ocollision.get("targetTypes");
		targetTypes = new String[atargets.size];
		int i = 0;
		for (JsonValue entry = atargets.child; entry != null; entry = entry.next) {
			targetTypes[i++] = entry.asString();
		}
		
		// targetCamps
		atargets = ocollision.get("targetCamps");
		targetCamps = new int[atargets.size];
		i = 0;
		for (JsonValue entry = atargets.child; entry != null; entry = entry.next) {
			targetCamps[i++] = entry.asInt();
		}
	}
	
	@Override
	public JsonValue getCollisionJson() {
		JsonValue v = super.getCollisionJson();
		
		JsonValue array = new JsonValue(ValueType.array);
		v.addChild("targetTypes", array);
		for (int i = 0; i < targetTypes.length; i++) {
			array.addChild(new JsonValue(targetTypes[i]));
		}
		
		array = new JsonValue(ValueType.array);
		v.addChild("targetCamps", array);
		for (int i = 0; i < targetCamps.length; i++) {
			array.addChild(new JsonValue(targetCamps[i]));
		}
		
		return v;
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
		int targetId = box.parentId;
		
		CharacterEntry target = RockChronicle.INSTANCE.runtime.findEntry(targetId);
		int targetCamp = target.getInt(path, 0);
		
		boolean accepted = false;
		for (int i = 0; i < targetCamps.length; i++) {
			if (targetCamps[i] == targetCamp) {
				accepted = true;
				break;
			}
		}
		if (!accepted) {
			return true;
		}
		
		// 判断角色
		accepted = false;
		String targetType = target.type;
		
		for (int i = 0; i < targetTypes.length; i++) {
			if (targetTypes[i].equals(targetType)) {
				accepted = true;
				break;
			}
		}
		if (!accepted) {
			return true;
		}
		
		// 设置状态
		JsonValue v = new JsonValue(ValueType.object);
		v.addChild("active", new JsonValue(true));
		parent.setJson("state", v);
		
		if ("once".equals(this.executeType)) {
			isFunctioned = false;
			willDelete = true;
		}
		return isFunctioned;
	}

}
