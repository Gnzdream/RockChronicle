package zdream.rockchronicle.core.module;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.JsonValue;

import zdream.rockchronicle.core.character.CharacterEntry;
import zdream.rockchronicle.core.character.event.CharacterEvent;
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
	 * 每一步来刷新一下状态
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
	
	/**
	 * 用于每一步结束时的工作，包括重置临时参数、删除模块
	 */
	public void stepPassed() {
		
	}

	/**
	 * 子类如果要继承, 请在方法最后加上 super.onStepFinished
	 * @param world
	 * @param isPause
	 */
	public void onStepFinished(LevelWorld world, boolean isPause) {
		
	}

	public void willDestroy() {
		
	}
	
	public void dispose() {
		// parent.unbindResource(this);
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
	
	public int getInt(String key, int defValue) {
		return parent.getInt(key, defValue);
	}

	public String getString(String key, String defValue) {
		return parent.getString(key, defValue);
	}

	public float getFloat(String key, float defValue) {
		return parent.getFloat(key, defValue);
	}

	public boolean getBoolean(String key, boolean defValue) {
		return parent.getBoolean(key, defValue);
	}

	public JsonValue getJson(String key) {
		return parent.getJson(key);
	}

	public void setState(String key, JsonValue value) {
		parent.setState(key, value);
	}

	public void setSituation(String key, JsonValue value) {
		parent.setSituation(key, value);
	}

	public void removeState(String key) {
		parent.removeState(key);
	}

	public void removeSituation(String key) {
		parent.removeSituation(key);
	}

	public void receiveEvent(CharacterEvent event) {
		
	}
	
	/* **********
	 *   其它   *
	 ********** */
	
	public String description() {
		return null;
	}
	
	@Override
	public String toString() {
		String desc = description();
		if (desc != null) {
			return String.format("{M:%s-%s}", name(), desc);
		}
		return String.format("{M:%s}", name());
	}

}
