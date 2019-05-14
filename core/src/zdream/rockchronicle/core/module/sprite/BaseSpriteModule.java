package zdream.rockchronicle.core.module.sprite;

import zdream.rockchronicle.core.character.CharacterEntry;
import zdream.rockchronicle.platform.world.LevelWorld;

/**
 * <p>默认的的绘画模块
 * </p>
 * 
 * @author Zdream
 * @since v0.0.1
 * @date
 *   2019-05-13 (created)
 *   2019-05-13 (last modified)
 */
public class BaseSpriteModule extends SpriteModule {

	public BaseSpriteModule(CharacterEntry ch) {
		super(ch);
	}
	
	@Override
	public void determine(LevelWorld world, int index, boolean hasNext) {
		super.determine(world, index, hasNext);
		steps++;
	}

}
