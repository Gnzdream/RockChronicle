package zdream.rockchronicle.core.module.camp;

import java.util.Iterator;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.IntIntMap;
import com.badlogic.gdx.utils.IntIntMap.Entry;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonValue.ValueType;

import zdream.rockchronicle.core.character.CharacterEntry;
import zdream.rockchronicle.core.module.AbstractModule;

/**
 * <p>管理阵营参数的模块
 * <p>它将指导判断子弹、其它怪物、陷阱等的伤害与效果施加是否生效.
 * <p>如果存在角色无该模块, 则认为该角色可能为中立的
 * </p>
 * 
 * @author Zdream
 * @since v0.0.1
 * @date 2019-05-06 (create)
 */
public abstract class CampModule extends AbstractModule {
	
	public static final String NAME = "camp";
	
	/**
	 * 自己属于哪个阵营
	 */
	public int camp;
	/**
	 * <p>自己作为攻击方, 能否攻击到阵营号为 key 的角色.
	 * <p>自己的阵营为 a, 对方的阵营为 b,
	 * 如果自己的 attackAccepted[a] == 1 而且对方的 defenseAccepted[b] == 1
	 * 则攻击生效
	 * </p>
	 * @see #defenseTable
	 */
	public IntIntMap attackTable = new IntIntMap(8);
	/**
	 * <p>自己作为防御方, 能否被角色阵营号为 key 的敌人攻击到.
	 * </p>
	 * @see #attackTable
	 */
	public IntIntMap defenseTable = new IntIntMap(8);
	
	public CampModule(CharacterEntry ch, String desc) {
		super(ch, NAME, desc);
	}
	
	@Override
	public void init(FileHandle file, JsonValue value) {
		super.init(file, value);
		
		// init camp 部分
		JsonValue ocamp = value.get("camp");
		if (ocamp.has("camp")) {
			setCamp(ocamp.getInt("camp"));
		}
		JsonValue attArray = value.get("attackAccepted");
		if (attArray != null) {
			for (JsonValue entry = attArray.child; entry != null; entry = entry.next) {
				int key = Integer.parseInt(attArray.name);
				attackTable.put(key, entry.asInt());
			}
		}
		JsonValue defArray = value.get("defenseAccepted");
		if (defArray != null) {
			for (JsonValue entry = defArray.child; entry != null; entry = entry.next) {
				int key = Integer.parseInt(attArray.name);
				defenseTable.put(key, entry.asInt());
			}
		}
		
		setCampState();
	}
	
	public void setCamp(int camp) {
		this.camp = camp;
	}
	
	public void setCampState() {
		setSituation("camp.camp", new JsonValue(camp));
		
		JsonValue attArray = new JsonValue(ValueType.object);
		setSituation("camp.attackAccepted", attArray);
		for (Iterator<Entry> iterator = attackTable.iterator(); iterator.hasNext();) {
			Entry e = iterator.next();
			attArray.addChild(Integer.toString(e.key), new JsonValue(e.value));
		}
		
		JsonValue defArray = new JsonValue(ValueType.object);
		setSituation("camp.defenseAccepted", defArray);
		for (Iterator<Entry> iterator = defenseTable.iterator(); iterator.hasNext();) {
			Entry e = iterator.next();
			defArray.addChild(Integer.toString(e.key), new JsonValue(e.value));
		}
	}
	
}
