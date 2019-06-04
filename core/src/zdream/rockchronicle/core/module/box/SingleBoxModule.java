package zdream.rockchronicle.core.module.box;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectMap;

import zdream.rockchronicle.core.character.CharacterEntry;
import zdream.rockchronicle.platform.body.Box;
import zdream.rockchronicle.platform.world.LevelWorld;

/**
 * <p>单一碰撞方块的盒子模块
 * <p>原类名为 SingleBoxMotionModule, 现在行动模块与盒子模块拆成两个模块
 * </p>
 * 
 * @author Zdream
 * @since v0.0.1
 * @date
 *   2019-05-06 (created)
 *   2019-05-13 (last modified)
 */
public class SingleBoxModule extends BoxModule {
	
	/**
	 * 碰撞盒子
	 */
	public final Box box;
	
	/**
	 * 盒子列表
	 */
	public final Box[] boxes;
	
	/**
	 * 所有的形态的集合
	 */
	public final ObjectMap<String, BoxPattern> patterns = new ObjectMap<>(10);
	
	/**
	 * 当前的形态
	 */
	public String curPattern;
	
	/**
	 * 下一步的形态
	 */
	public String nextPattern;

	public SingleBoxModule(CharacterEntry ch) {
		super(ch);
		box = new Box(ch.id);
		boxes = new Box[] {box};
	}
	
	@Override
	public void init(FileHandle file, JsonValue value) {
		super.init(file, value);
		
		initBox(value.get("box"));
	}

	public void initBox(JsonValue object) {
		box.inTerrain = object.getBoolean("inTerrain", true);
		
		// 初始锚点位置
		JsonValue oanchor = object.get("anchor");
		if (oanchor != null) {
			box.anchor.x = oanchor.getFloat("x", 0f);
			box.anchor.y = oanchor.getFloat("y", 0f);
		}
		
		box.gravityDown = true;
		for (JsonValue entry = object.child; entry != null; entry = entry.next) {
			switch (entry.name) {
			case "gravityScale": box.gravityScale = entry.asFloat(); break;
			case "gravityDown": box.gravityDown = entry.asBoolean(); break;
			case "climbable": box.climbable = entry.asBoolean(); break;
			}
		}
		
		// 形态
		JsonValue apatterns = object.get("patterns");
		if (apatterns != null) { // array
			for (JsonValue opattern = apatterns.child; opattern != null; opattern = opattern.next) {
				BoxPattern pattern = new BoxPattern();
				pattern.name = opattern.getString("name");
				pattern.width = opattern.getFloat("width");
				pattern.height = opattern.getFloat("height");
				pattern.x = opattern.getFloat("x");
				pattern.y = opattern.getFloat("y");
				
				patterns.put(pattern.name, pattern);
			}
		}
		
		// 最后初始化形状
		JsonValue orect = object.get("rect");
		JsonValue def = orect.get("def");
		
		if (orect.has("width")) {
			BoxPattern pattern = new BoxPattern();
			pattern.name = "default";
			pattern.width = orect.getFloat("width");
			pattern.height = orect.getFloat("height");
			pattern.x = orect.getFloat("x");
			pattern.y = orect.getFloat("y");
			patterns.put(pattern.name, pattern);
			
		}
		
		if (def == null) {
			BoxPattern pattern = patterns.get("default");
			curPattern = "default";
			
			box.box.width = pattern.width;
			box.box.height = pattern.height;
			box.box.x = pattern.x;
			box.box.y = pattern.y;
		} else {
			curPattern = def.asString();
			BoxPattern pattern = patterns.get(curPattern);

			box.box.width = pattern.width;
			box.box.height = pattern.height;
			box.box.x = pattern.x;
			box.box.y = pattern.y;
		}
	}
	
	@Override
	public String description() {
		return "single";
	}

	@Override
	public void createBody() {
		world.addBox(box);
	}
	
	@Override
	protected void destroyBody() {
		world.removeBox(box);
	}
	
	@Override
	public void move(LevelWorld world) {
		box.lastAnchorX = box.anchor.x;
		box.lastAnchorY = box.anchor.y;
		
		// 处理变换形态
		if (nextPattern != null) {
			BoxPattern pattern = patterns.get(nextPattern);
			box.box.width = pattern.width;
			box.box.height = pattern.height;
			box.box.x = pattern.x;
			box.box.y = pattern.y;
			nextPattern = null;
		}
		
		// 执行移动实例
		for (int i = 0; i < movables.size; i++) {
			movables.get(i).movable.move(world, box, parent);
		}
		
		// 处理移动位置
		world.execVerticalMotion(box);
		world.execHorizontalMotion(box);
		box.velocity.x = 0;
		box.velocity.y = 0;
	}
	
	/* **********
	 * 资源事件 *
	 ********** */
	
	@Override
	public Box getBox() {
		return box;
	}
	
	@Override
	public Box[] getAllBoxes() {
		return boxes;
	}

	@Override
	public void setNextPattern(String pattern) {
		this.nextPattern = pattern;
	}

}
