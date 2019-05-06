package zdream.rockchronicle.core.character;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonValue;

import zdream.rockchronicle.core.character.parameter.JsonCollector;
import zdream.rockchronicle.platform.world.LevelWorld;

public abstract class AbstractModule {
	
	public abstract String name();
	
	public final CharacterEntry parent;
	
	public AbstractModule(CharacterEntry parent) {
		this.parent = parent;
	}
	
	public void init(FileHandle file, JsonValue value) {
		// do nothing
	}
	
	/**
	 * 每一帧来刷新一下状态
	 * @param world
	 *   关卡世界实体
	 * @param index
	 *   本帧的第几次调用. 第一次为 0
	 * @param hasNext
	 *   本帧是否还会再调用
	 */
	public void determine(LevelWorld world, int index, boolean hasNext) {
		// do nothing
	}

	public void willDestroy() {
		
	}
	
	public void dispose() {
		parent.unbindResource(this);
	}
	
	/**
	 * <p>优先级
	 * <p>优先级高的模块的 {@link #determine(LevelWorld, int, boolean)} 方法将先执行.
	 * </p>
	 * @return
	 */
	public int priority() {
		return 0;
	}
	
	/* **********
	 * 资源事件 *
	 ********** */
	protected final Array<JsonCollector> collectors = new Array<>();
	
	public int getInt(String[] path, int defValue) {
		for (int i = 0; i < collectors.size; i++) {
			JsonCollector c = collectors.get(i);
			if (path[0].equals(c.first)) {
				return c.getInt(path, defValue);
			}
		}
		return defValue;
	}
	public String getString(String[] path, String defValue) {
		for (int i = 0; i < collectors.size; i++) {
			JsonCollector c = collectors.get(i);
			if (path[0].equals(c.first)) {
				return c.getString(path, defValue);
			}
		}
		return defValue;
	}
	public float getFloat(String[] path, float defValue) {
		for (int i = 0; i < collectors.size; i++) {
			JsonCollector c = collectors.get(i);
			if (path[0].equals(c.first)) {
				return c.getFloat(path, defValue);
			}
		}
		return defValue;
	}
	public boolean getBoolean(String[] path, boolean defValue) {
		for (int i = 0; i < collectors.size; i++) {
			JsonCollector c = collectors.get(i);
			if (path[0].equals(c.first)) {
				return c.getBoolean(path, defValue);
			}
		}
		return defValue;
	}
	public JsonValue getJson(String[] path) {
		for (int i = 0; i < collectors.size; i++) {
			JsonCollector c = collectors.get(i);
			if (path[0].equals(c.first)) {
				return c.getJson(path);
			}
		}
		return null;
	}
	
	/*
	 * 返回值: 是否修改被允许 (accepted)
	 */
	public boolean setJson(String first, JsonValue value) {
		return false;
	}

}
