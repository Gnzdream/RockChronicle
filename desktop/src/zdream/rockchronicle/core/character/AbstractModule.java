package zdream.rockchronicle.core.character;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.JsonValue;

import zdream.rockchronicle.platform.world.LevelWorld;

public abstract class AbstractModule {
	
	public abstract String name();
	
	protected CharacterEntry ch;
	
	public AbstractModule(CharacterEntry ch) {
		this.ch = ch;
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
	
	public void dispose() {
		
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

}
