package zdream.rockchronicle.core.module.box;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.JsonValue;

import zdream.rockchronicle.core.character.CharacterEntry;
import zdream.rockchronicle.core.module.AbstractModule;
import zdream.rockchronicle.platform.body.Box;
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

	protected LevelWorld world;

	public BoxModule(CharacterEntry parent) {
		super(parent);
	}
	
	@Override
	public void init(FileHandle file, JsonValue value) {
		super.init(file, value);
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
	
	/**
	 * 如果本角色只有一个碰撞盒子, 则调用该方法来获取其碰撞盒子
	 * @return
	 */
	public abstract Box getBox();
	
	/**
	 * 设置下一步的形态是什么
	 * @param pattern
	 */
	public abstract void setNextPattern(String pattern);

}
