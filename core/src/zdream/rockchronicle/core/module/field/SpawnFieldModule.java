package zdream.rockchronicle.core.module.field;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.JsonValue;

import zdream.rockchronicle.RockChronicle;
import zdream.rockchronicle.core.GameRuntime;
import zdream.rockchronicle.core.character.CharacterEntry;
import zdream.rockchronicle.platform.world.LevelWorld;

import static zdream.rockchronicle.platform.world.LevelWorld.STEPS_PER_SECOND;

/**
 * <p>生怪场的状态记录的模块
 * </p>
 * 
 * @author Zdream
 * @since v0.0.1
 * @date
 *   2019-05-12 (create)
 *   2019-05-13 (last modified)
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
	/**
	 * 第一只怪生成的时间间隔 (步), 配置项
	 */
	public int firstDuration;
	/**
	 * 生成的怪的名称, 配置项
	 */
	public String spawnFoesName;
	/**
	 * 生成的怪的参数, 配置项
	 */
	public JsonValue spawnFoesParam;

	public SpawnFieldModule(CharacterEntry parent) {
		super(parent);
	}
	
	@Override
	public void init(FileHandle file, JsonValue value) {
		super.init(file, value);
		
		initArguments(value);
	}
	
	private void initArguments(JsonValue value) {
		JsonValue oparam = value.get("fieldParam");
		if (oparam == null) {
			return;
		}
		
		spawnDuration = (int) (oparam.getFloat("spawnDuration", 60) * STEPS_PER_SECOND);
		if (spawnDuration < 0.1f * STEPS_PER_SECOND) {
			spawnDuration = 60 * STEPS_PER_SECOND;
		}
		
		firstDuration =
				(int) (oparam.getFloat("firstDuration", 0) * STEPS_PER_SECOND) + 1;
		spawnRemain = firstDuration;
		
		// 怪参数
		JsonValue ofoes = oparam.get("spawnFoes");
		spawnFoesName = ofoes.getString("name");
		spawnFoesParam = ofoes.get("param");
	}
	
	@Override
	public void determine(LevelWorld world, int index, boolean hasNext) {
		super.determine(world, index, hasNext);
		
		if (isActive()) {
			spawnRemain--;
		}
		if (spawnRemain == 0) {
			spawn(world);
			spawnRemain = spawnDuration;
		}
	}
	
	public void spawn(LevelWorld world) {
		Gdx.app.log("SpawnFieldModule", "spawn");
		
		GameRuntime runtime = RockChronicle.INSTANCE.runtime;
		CharacterEntry entry = runtime.characterBuilder.create(
				spawnFoesName, spawnFoesParam);
		runtime.addEntry(entry);
	}

}
