package zdream.rockchronicle.core.move;

import zdream.rockchronicle.core.character.CharacterEntry;
import zdream.rockchronicle.platform.body.Box;
import zdream.rockchronicle.platform.world.LevelWorld;

/**
 * <p>线性移动
 * <p>该类不直接执行移动, 只是设置速度
 * </p>
 * 
 * @author Zdream
 * @since v0.0.1
 * @date
 *   2019-05-27 (created)
 *   2019-05-27 (last modified)
 */
public class LinearMovement implements IMovable {
	
	public float vx, vy;
	public boolean enable = true;

	@Override
	public void action(LevelWorld world, Box box, CharacterEntry entry) {
		if (enable) {
			if (vx != 0) {
				box.setVelocityX(box.velocity.x + vx);
				if (vx > 0) {
					box.orientation = true;
				} else {
					box.orientation = false;
				}
			}
			if (vy != 0) {
				box.setVelocityY(box.velocity.y + vy);
			}
		}
	}

}
