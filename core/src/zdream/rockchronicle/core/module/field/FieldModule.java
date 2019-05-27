package zdream.rockchronicle.core.module.field;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.JsonValue;

import zdream.rockchronicle.core.character.CharacterEntry;
import zdream.rockchronicle.core.module.AbstractModule;

/**
 * <p>一般场的状态记录的模块
 * </p>
 * 
 * @author Zdream
 * @since v0.0.1
 * @date
 *   2019-05-12 (create)
 *   2019-05-12 (last modified)
 */
public abstract class FieldModule extends AbstractModule {
	
	/**
	 * 配置项. 该场是否需要激活时才能够运行.
	 * 如果该项为 true, 则每次仅当 {@link #isActive()} 返回 true 时运行
	 */
	public boolean needTrigger = true;
	
	public static final String NAME = "Field";

	public FieldModule(CharacterEntry parent) {
		super(parent);
	}
	
	@Override
	public void init(FileHandle file, JsonValue value) {
		super.init(file, value);
		
		JsonValue ofield = value.get("fieldParam");
		if (ofield != null) {
			JsonValue v = ofield.get("needTrigger");
			if (v != null) needTrigger = v.asBoolean();
		}
	}

	@Override
	public String name() {
		return NAME;
	}
	
	@Override
	public int priority() {
		return 5;
	}
	
	/**
	 * 是否激活. 如果 needTrigger = true 时, 将始终保持激活状态
	 * @return
	 */
	public boolean isActive() {
		if (needTrigger) {
			return getBoolean("field.active", false);
		} else {
			return true;
		}
	}

}
