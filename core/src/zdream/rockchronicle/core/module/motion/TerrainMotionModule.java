package zdream.rockchronicle.core.module.motion;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.JsonValue;

import zdream.rockchronicle.core.character.CharacterEntry;
import zdream.rockchronicle.platform.body.Box;
import zdream.rockchronicle.platform.world.LevelWorld;

/**
 * <p>在地形的约束中的角色的行动模块
 * </p>
 * 
 * @author Zdream
 * @since v0.0.1
 * @date
 *   2019-05-06 (created)
 *   2019-05-13 (last modified)
 */
public class TerrainMotionModule extends MotionModule {
	
	public TerrainMotionModule(CharacterEntry ch) {
		super(ch);
	}
	
	@Override
	public void init(FileHandle file, JsonValue value) {
		super.init(file, value);
	}

	@Override
	public void determine(LevelWorld world, int index, boolean hasNext) {
		super.determine(world, index, hasNext);
		
		Box box = getSingleBox();
		if (box != null) {
			if (box.inTerrain) {
				// 碰边检测
				world.bottomStop(box);
				world.topStop(box);
				world.leftStop(box);
				world.rightStop(box);
				
				// 位置重合修正
				boolean glitch = world.correctOverlapBox(box);
				if (!glitch) { // 已经卡在墙中了
					Gdx.app.error("SingleBoxMotionModule", String.format("%s 被卡在墙里了, 而且无法修正", parent));
				}
			}
		}
		
		setMotionState();
	}

	/* **********
	 * 资源事件 *
	 ********** */
	public void setMotionState() {
		Box box = getSingleBox();
		
		setState("motion.bottomStop", new JsonValue(box.bottomStop));
		setState("motion.topStop", new JsonValue(box.topStop));
		setState("motion.leftStop", new JsonValue(box.leftStop));
		setState("motion.rightStop", new JsonValue(box.rightStop));
	}

}
