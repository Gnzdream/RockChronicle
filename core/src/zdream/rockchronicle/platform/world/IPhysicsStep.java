package zdream.rockchronicle.platform.world;

/**
 * 作为 {@link LevelWorld} 的回调函数接口, 每一次物理世界的变化将回调一次
 * 
 * @author Zdream
 * @date 2019-04-28
 */
public interface IPhysicsStep {
	
	/**
	 * 物理世界向前走一步的回调函数
	 * @param world
	 *   物理世界实例
	 * @param index
	 *   本帧的第几次调用. 第一次为 0
	 * @param hasNext
	 *   本帧是否还会再调用
	 */
	public void step(LevelWorld world, int index, boolean hasNext);
	
	/**
	 * 物理世界行动完成时, 每帧的最后会调用一次该函数
	 * @param world
	 *   物理世界实例
	 * @param isPause
	 *   是否在暂停中
	 */
	default public void onStepFinished(LevelWorld world, boolean isPause) {
		// do nothing
	}
	
}
