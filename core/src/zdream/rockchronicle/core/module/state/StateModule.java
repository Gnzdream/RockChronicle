package zdream.rockchronicle.core.module.state;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonValue.ValueType;

import zdream.rockchronicle.core.character.CharacterEntry;
import zdream.rockchronicle.core.character.parameter.JsonCollector;
import zdream.rockchronicle.core.module.AbstractModule;
import zdream.rockchronicle.platform.world.LevelWorld;

import static zdream.rockchronicle.utils.JsonUtils.*;

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

	protected JsonCollector statec, situationc;
	
	/**
	 * 长期性状态数据, 不会自动清除
	 */
	protected final JsonValue situation = new JsonValue(ValueType.object);
	/**
	 * 短期性状态数据, 下一步时间开始时自动清除
	 */
	protected final JsonValue state = new JsonValue(ValueType.object);

	public StateModule(CharacterEntry ch) {
		super(ch);
		
		statec = new JsonCollector(this::createStateJson, "state");
		situationc = new JsonCollector(this::createSituationJson, "situation");
	}

	@Override
	public String name() {
		return NAME;
	}
	
	@Override
	public int priority() {
		return 1000;
	}
	
	@Override
	public void determine(LevelWorld world, int index, boolean hasNext) {
		super.determine(world, index, hasNext);
		clear(state);
	}
	
	@Override
	public void init(FileHandle file, JsonValue value) {
		super.init(file, value);
		
		addCollector(statec);
		addCollector(situationc);
	}
	
	public JsonValue createStateJson() {
		JsonValue v = new JsonValue(ValueType.object);
		mergeJson(v, state);
		return v;
	}
	
	public JsonValue createSituationJson() {
		JsonValue v = new JsonValue(ValueType.object);
		mergeJson(v, situation);
		return v;
	}
	
	@Override
	protected boolean setJson(String first, JsonValue value) {
		if ("state".equals(first)) {
			return setStateJson(value);
		} else if ("situation".equals(first)) {
			return setSituationJson(value);
		}
		return super.setJson(first, value);
	}

	protected boolean setStateJson(JsonValue value) {
		mergeJson(state, value);
		return true;
	}

	protected boolean setSituationJson(JsonValue value) {
		mergeJson(situation, value);
		return true;
	}

}
