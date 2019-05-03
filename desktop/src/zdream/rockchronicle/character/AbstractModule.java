package zdream.rockchronicle.character;

import zdream.rockchronicle.platform.world.IPhysicsStep;
import zdream.rockchronicle.platform.world.LevelWorld;

public abstract class AbstractModule implements IPhysicsStep {
	
	public abstract String name();
	
	protected CharacterEntry ch;
	
	public AbstractModule(CharacterEntry ch) {
		this.ch = ch;
	}
	
	public void init() {
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
	@Override
	public void step(LevelWorld world, int index, boolean hasNext) {
		// do nothing
	}
	
	public void dispose() {
		
	}

}
