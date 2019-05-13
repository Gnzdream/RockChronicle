package zdream.rockchronicle.core.module.state;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonValue.ValueType;

import zdream.rockchronicle.core.character.CharacterEntry;
import zdream.rockchronicle.core.character.parameter.JsonCollector;
import zdream.rockchronicle.core.module.AbstractModule;

/**
 * <p>抽象状态记录的模块
 * </p>
 * 
 * @author Zdream
 * @since v0.0.1
 * @date
 *   2019-05-10 (create)
 *   2019-05-10 (last modified)
 */
public abstract class StateModule extends AbstractModule {
	
	public static final String NAME = "State";

	protected JsonCollector statec;

	public StateModule(CharacterEntry ch) {
		super(ch);
		
		statec = new JsonCollector(this::createStateJson, "state");
	}

	@Override
	public String name() {
		return NAME;
	}
	
	@Override
	public int priority() {
		return 0xD0; // 228
	}
	
	@Override
	public void init(FileHandle file, JsonValue value) {
		super.init(file, value);
		
		addCollector(statec);
	}
	
	public JsonValue createStateJson() {
		JsonValue v = new JsonValue(ValueType.object);
		return v;
	}

}
