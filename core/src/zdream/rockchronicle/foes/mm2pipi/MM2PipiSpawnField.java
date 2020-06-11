package zdream.rockchronicle.foes.mm2pipi;

import static zdream.rockchronicle.core.foe.Box.block2P;

import java.util.Objects;
import java.util.function.Predicate;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.JsonValue;

import zdream.rockchronicle.core.GameRuntime;
import zdream.rockchronicle.core.foe.Box;
import zdream.rockchronicle.core.foe.Foe;
import zdream.rockchronicle.core.foe.ShapePainter;
import zdream.rockchronicle.core.world.Ticker;

/**
 * 这个生怪场可能有很多 box. 但是由 tmx 生成的只有一个 box
 * 
 * @author Zdream
 * @date 2020-06-11
 */
public class MM2PipiSpawnField extends Foe {

	/**
	 * @param json
	 *   说明位置和生成的怪、生怪规则
	 * {
	 *   box : {
	 *     rect : { x:bX, y:bY, width:bWidth, height:bHeight },
	 *     top : ? // 如果是"no_border" 则影响范围为上无边界
	 *   }
	 * }
	 */
	public MM2PipiSpawnField(JsonValue json) {
		super("mm2pipi_spawnfield");
		type = "field";
		camp = 2;
		
		reset(json);
	}
	
	@Override
	public void init(GameRuntime runtime) {
		super.init(runtime);
		putPainter(new ShapePainter(boxes[0], Color.BLUE));
		initTrigger();
	}

	private void reset(JsonValue json) {
		JsonValue orect = Objects.requireNonNull(json.get("box").get("rect")); // 不应该为 null
		
		Box box = new Box(id);
		box.setBox(0, 0, block2P(orect.getFloat("width")), block2P(orect.getFloat("height")));
		box.setAnchor(block2P(orect.getFloat("x")), block2P(orect.getFloat("y")));
		box.flush();
		
		boxes = new Box[] { box };
	}
	
	Box[] boxes;

	@Override
	public Box[] getBoxes() {
		return boxes;
	}
	
	/* **********
	 *   触发   *
	 ********** */
	/*
	 * 需要指定 type、camp 的角色操作才能触发的
	 */
	String targetTypes = "leader";
	int targetCamps = 1;
	/*
	 * 生成策略
	 */
	int firstDuration = Ticker.STEPS_PER_SECOND;
	int spawnDuration = Ticker.STEPS_PER_SECOND * 6;
	
	/**
	 * 当前步是否触发
	 */
	public boolean active;
	
	/*
	 * 状态数据
	 */
	/**
	 * 到下一只怪生成的剩余时间 (步), 状态项
	 */
	public int spawnRemain;
	
	private void initTrigger() {
		spawnRemain = firstDuration;
		
//		spawnFoesParam = 
	}
	
	@Override
	public void step(boolean pause) {
		super.step(pause);
		if (pause) {
			return;
		}

		// 检查是否触发
		checkActive();
		
		if (active) {
			spawnRemain--;
		}
		if (spawnRemain == 0) {
			spawn();
			spawnRemain = spawnDuration;
		}
	}

	private void spawn() {
		MM2Pipi pipi = new MM2Pipi();
		runtime.addFoe(pipi);
	}

	private void checkActive() {
		active = false;
		// 阵营判断部分
		runtime.world.overlaps(boxes[0], ch, false);
	}
	
	class Check implements Predicate<Box> {
		@Override
		public boolean test(Box box) {
			int targetId = box.parentId;
			
			Foe target = runtime.findEntry(targetId);
			if (target.camp == targetCamps && targetTypes.equals(target.type)) {
				active = true;
				// 停止判断后面的盒子
				return false;
			}
			
			// 继续判断后面的盒子
			return true;
		}
		
	}
	Check ch = new Check();

}
