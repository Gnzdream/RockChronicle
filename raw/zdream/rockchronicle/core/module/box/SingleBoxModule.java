package zdream.rockchronicle.core.module.box;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectMap;

import zdream.rockchronicle.core.character.CharacterEntry;
import zdream.rockchronicle.core.move.IMovable;
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
		this(ch, "single");
	}

	public SingleBoxModule(CharacterEntry ch, String desc) {
		super(ch, desc);
		box = new Box(ch.id);
		boxes = new Box[] {box};
	}
	
	@Override
	public void init(FileHandle file, JsonValue value) {
		super.init(file, value);
		
		initBox(value.get("box"));
		addMovable(m0, 100);
		addMovable(m1, 0);
	}

	public void initBox(JsonValue object) {
		box.inTerrain = object.getBoolean("inTerrain", true);
		box.orientation = object.getBoolean("orientation", true);
		
		// 初始锚点位置
		JsonValue oanchor = object.get("anchor");
		if (oanchor != null) {
			if (oanchor.has("px")) {
				box.anchorX = oanchor.getInt("px");
			} else {
				box.anchorX = (int) (oanchor.getFloat("x", 0f) * Box.P_PER_BLOCK);
			}
			
			if (oanchor.has("py")) {
				box.anchorY = oanchor.getInt("py");
			} else {
				box.anchorY = (int) (oanchor.getFloat("y", 0f) * Box.P_PER_BLOCK);
			}
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
				pattern.width = opattern.getInt("width");
				pattern.height = opattern.getInt("height");
				pattern.x = opattern.getInt("x");
				pattern.y = opattern.getInt("y");
				
				patterns.put(pattern.name, pattern);
			}
		}
		
		// 最后初始化形状
		JsonValue orect = object.get("rect");
		JsonValue def = orect.get("def");
		
		if (orect.has("width")) {
			BoxPattern pattern = new BoxPattern();
			pattern.name = "default";
			pattern.width = orect.getInt("width");
			pattern.height = orect.getInt("height");
			pattern.x = orect.getInt("x");
			pattern.y = orect.getInt("y");
			patterns.put(pattern.name, pattern);
			
		}
		
		if (def == null) {
			BoxPattern pattern = patterns.get("default");
			curPattern = "default";
			
			box.boxWidth = pattern.width;
			box.boxHeight = pattern.height;
			box.boxX = pattern.x;
			box.boxY = pattern.y;
		} else {
			curPattern = def.asString();
			BoxPattern pattern = patterns.get(curPattern);

			box.boxWidth = pattern.width;
			box.boxHeight = pattern.height;
			box.boxX = pattern.x;
			box.boxY = pattern.y;
		}
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
	public void action(LevelWorld world) {
		// 执行移动实例
		for (int i = 0; i < movables.size; i++) {
			movables.get(i).movable.action(world, box, parent);
		}
		
		actionFinished = true;
	}
	
	/**
	 * 处理变换形态
	 */
	IMovable m0 = new IMovable() {
		@Override
		public void action(LevelWorld world, Box box, CharacterEntry entry) {
			box.lastAnchorX = box.anchorX;
			box.lastAnchorY = box.anchorY;
			
			// 处理变换形态
			if (nextPattern != null) {
				BoxPattern pattern = patterns.get(nextPattern);
				box.boxWidth = pattern.width;
				box.boxHeight = pattern.height;
				box.boxX = pattern.x;
				box.boxY = pattern.y;
				nextPattern = null;
			}
		}
	};
	
	/**
	 * 处理移动位置
	 */
	IMovable m1 = new IMovable() {
		@Override
		public void action(LevelWorld world, Box box, CharacterEntry entry) {
			world.execVerticalMotion(box);
			world.execHorizontalMotion(box);
			box.lastVelocityX = box.velocityX;
			box.lastVelocityY = box.velocityY;
			box.velocityX = 0;
			box.velocityY = 0;
		}
	};
	
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
