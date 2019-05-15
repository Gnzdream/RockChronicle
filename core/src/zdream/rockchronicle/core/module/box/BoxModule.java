package zdream.rockchronicle.core.module.box;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.JsonValue;

import zdream.rockchronicle.core.character.CharacterEntry;
import zdream.rockchronicle.core.character.parameter.JsonCollector;
import zdream.rockchronicle.core.module.AbstractModule;
import zdream.rockchronicle.platform.world.LevelWorld;

/**
 * 单一碰撞方块的盒子模块
 * 
 * @author Zdream
 * @since v0.0.1
 * @date
 *   2019-05-13 (create)
 */
public abstract class BoxModule extends AbstractModule {
	
	public static final String NAME = "Box";

	protected JsonCollector boxc;
	protected LevelWorld world;

	public BoxModule(CharacterEntry parent) {
		super(parent);
		boxc = new JsonCollector(this::getBoxJson, "box");
	}
	
	@Override
	public void init(FileHandle file, JsonValue value) {
		super.init(file, value);
		
		addCollector(boxc);
	}

	@Override
	public String name() {
		return NAME;
	}
	
	@Override
	public int priority() {
		return -100;
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
	public abstract void resetPosition(LevelWorld world, int index, boolean hasNext);

	public final void doCreateBody(LevelWorld world) {
		this.world = world;
		this.createBody();
	}
	
	public final void doDestroyBody() {
		this.destroyBody();
	}

	protected abstract void createBody();
	protected abstract void destroyBody();
	protected abstract JsonValue getBoxJson();
	
	public void modified() {
		if (boxc != null)
			boxc.clear();
	}

}
