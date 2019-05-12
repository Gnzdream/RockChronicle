package zdream.rockchronicle.core.module.state;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.JsonValue;

import zdream.rockchronicle.core.character.CharacterEntry;

/**
 * <p>场状态记录的模块. 它的优先度为 10
 * </p>
 * 
 * @author Zdream
 * @since v0.0.1
 * @date
 *   2019-05-12 (create)
 *   2019-05-12 (last modified)
 */
public class FieldStateModule extends StateModule {
	
	public FieldStateModule(CharacterEntry ch) {
		super(ch);
	}
	
	@Override
	public void init(FileHandle file, JsonValue value) {
		super.init(file, value);
	}
	
	@Override
	public int priority() {
		return 10;
	}
	
	/**
	 * 是否激活, 状态项, 每步更新
	 */
	public boolean active;
	
	public JsonValue createStateJson() {
		JsonValue v = super.createStateJson();
		
		v.addChild("active", new JsonValue(active));
		
		return v;
	}
	
	@Override
	protected boolean setJson(String first, JsonValue value) {
		if ("state".equals(first)) {
			setStateJson(value);
			return true;
		}
		return super.setJson(first, value);
	}
	
	public void setStateJson(JsonValue value) {
		for (JsonValue entry = value.child; entry != null; entry = entry.next) {
			switch (entry.name) {
			case "active":
				this.active = entry.asBoolean();
				break;
			default:
				break;
			}
		}
	}
	
	@Override
	public void stepPassed() {
		active = false;
	}

}
