package zdream.rockchronicle.core;

import zdream.rockchronicle.character.CharacterEntry;
import zdream.rockchronicle.platform.region.Region;
import zdream.rockchronicle.platform.region.Room;
import zdream.rockchronicle.platform.world.IPhysicsStep;
import zdream.rockchronicle.platform.world.LevelWorld;

public class GameRuntime {
	
	/**
	 * 现在正在显示的关卡 {@link Region}
	 */
	public Region curRegion;
	
	/**
	 * 现在显示的 {@link Room} 编号
	 */
	public int room;
	
	/**
	 * 玩家所控制的角色
	 */
	public CharacterEntry player1;
	
	/**
	 * 如果在关卡中, 关卡世界的参数
	 */
	public LevelWorld levelWorld;
	
	/**
	 * @return
	 *   正在显示的 {@link Room}
	 */
	public Room getCurrentRoom() {
		return curRegion.rooms[room];
	}
	
	/**
	 * 每一帧更新一下. 包括角色
	 * @param index
	 *   本帧的第几次调用. 第一次为 0
	 * @param hasNext
	 *   本帧是否还会再调用
	 */
	public void onWorldSteped(int index, boolean hasNext) {
		player1.step(levelWorld, index, hasNext);
	}

	/**
	 * 类似于 {@link IPhysicsStep#onStepFinished(LevelWorld, boolean)}
	 * @param isPause
	 *   本帧是否在暂停状态
	 */
	public void onStepFinished(boolean isPause) {
		player1.onStepFinished(levelWorld, isPause);
	}

}
