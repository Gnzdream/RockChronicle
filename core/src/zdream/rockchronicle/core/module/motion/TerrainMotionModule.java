package zdream.rockchronicle.core.module.motion;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.JsonValue;

import zdream.rockchronicle.core.character.CharacterEntry;
import zdream.rockchronicle.core.character.event.CharacterEvent;
import zdream.rockchronicle.core.move.IMovable;
import zdream.rockchronicle.platform.body.Box;
import zdream.rockchronicle.platform.world.LevelWorld;

/**
 * <p>在地形的约束中的角色的行动模块
 * <p>每步判断盒子的四面碰壁的情况;
 *   在行动阶段的后半程进行矫正, 处理盒子在墙壁内的情况.
 * </p>
 * 
 * @author Zdream
 * @since v0.0.1
 * @date
 *   2019-05-06 (created)
 *   2019-06-09 (last modified)
 */
public class TerrainMotionModule extends MotionModule {
	
	public TerrainMotionModule(CharacterEntry ch) {
		this(ch, "terrain");
	}
	
	protected TerrainMotionModule(CharacterEntry ch, String desc) {
		super(ch, desc);
	}
	
	@Override
	public void init(FileHandle file, JsonValue value) {
		super.init(file, value);
		
		parent.getBoxModule().addMovable(glitchMb, -25);
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
			}
		}
		
		setMotionState();
	}
	
	@Override
	public void willDestroy() {
		parent.getBoxModule().removeMovable(glitchMb);
		
		super.willDestroy();
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

	/* **********
	 * 位置矫正 *
	 ********** */
	
	/**
	 * 用于位置重合修正. 如果位置无法矫正, 将发出 glitch_found 信息
	 * @author Zdream
	 */
	class TerrainMotionMovable implements IMovable {

		@Override
		public void action(LevelWorld world, Box box, CharacterEntry entry) {
			boolean glitch = world.correctOverlapBox(box);
			if (!glitch) { // 已经卡在墙中了
				Gdx.app.error("SingleBoxMotionModule", String.format("%s 被卡在墙里了, 而且无法修正", parent));
				CharacterEvent event = new CharacterEvent("glitch_found");
				parent.publishNow(event);
			}
		}
		
	}
	TerrainMotionMovable glitchMb = new TerrainMotionMovable();

}
