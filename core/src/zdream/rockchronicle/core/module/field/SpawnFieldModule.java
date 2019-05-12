package zdream.rockchronicle.core.module.field;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.JsonValue;

import zdream.rockchronicle.core.character.CharacterEntry;
import zdream.rockchronicle.platform.world.LevelWorld;

/**
 * <p>生怪场的状态记录的模块
 * </p>
 * 
 * @author Zdream
 * @since v0.0.1
 * @date
 *   2019-05-12 (create)
 *   2019-05-12 (last modified)
 */
public class SpawnFieldModule extends FieldModule {
	
	/**
	 * 到下一只怪生成的剩余时间 (步), 状态项
	 */
	public int spawnRemain;
	/**
	 * 每只怪生成的时间间隔 (步), 配置项
	 */
	public int spawnDuration;

	public SpawnFieldModule(CharacterEntry parent) {
		super(parent);
	}
	
	@Override
	public void init(FileHandle file, JsonValue value) {
		super.init(file, value);
		
		initArguments(value);
	}
	
	private void initArguments(JsonValue value) {
		JsonValue ostate = value.get("field");
		if (ostate == null) {
			return;
		}
		JsonValue oparam = ostate.get("param");
		if (oparam == null) {
			return;
		}
		
		spawnDuration = (int) (oparam.getFloat("spawn", 60) * LevelWorld.STEPS_PER_SECOND);
		spawnRemain = spawnDuration;
	}
	
	@Override
	public void determine(LevelWorld world, int index, boolean hasNext) {
		super.determine(world, index, hasNext);
		
		if (isActive()) {
			spawnRemain--;
		}
		if (spawnRemain == 0) {
			Gdx.app.log("SpawnFieldModule", "spawn");
			spawnRemain = spawnDuration;
		}
	}

}
