package zdream.rockchronicle.core.move;

import zdream.rockchronicle.core.character.CharacterEntry;
import zdream.rockchronicle.platform.body.Box;
import zdream.rockchronicle.platform.world.LevelWorld;

/**
 * <p>指导、执行移动, 或者设置速度让其它工具帮助其执行运动.
 * </p>
 * 
 * @author Zdream
 * @since v0.0.1
 * @date
 *   2019-05-27 (created)
 *   2019-05-27 (last modified)
 */
public interface IMovable {
	
	/**
	 * 为 box 执行相应的移动计划
	 */
	public void move(LevelWorld world, Box box, CharacterEntry entry);

}
