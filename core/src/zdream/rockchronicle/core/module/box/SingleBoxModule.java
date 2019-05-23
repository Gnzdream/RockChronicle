package zdream.rockchronicle.core.module.box;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonValue.ValueType;

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
	
	public final Box box;

	public SingleBoxModule(CharacterEntry ch) {
		super(ch);
		box = new Box(ch.id);
	}
	
	@Override
	public void init(FileHandle file, JsonValue value) {
		super.init(file, value);
		
		initBox(value.get("box"));
	}

	public void initBox(JsonValue object) {
		box.inTerrain = object.getBoolean("inTerrain", true);
		
		JsonValue orect = object.get("rect");
		// TODO 暂时不考虑 def
		box.box.width = orect.getFloat("width", 0);
		box.box.height = orect.getFloat("height", 0);
		box.box.x = orect.getFloat("x", 0);
		box.box.y = orect.getFloat("y", 0);
		
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
	public void resetPosition(LevelWorld world, int index, boolean hasNext) {
		world.execVerticalMotion(box);
		world.execHorizontalMotion(box);
	}
	
	/* **********
	 * 资源事件 *
	 ********** */
	/*
	 * 允许获取与修改:
	 * motion.orientation
	 */
	public JsonValue getBoxJson() {
		JsonValue v = new JsonValue(ValueType.object);
		
		v.addChild("inTerrain", new JsonValue(box.inTerrain));
		
		JsonValue orect = new JsonValue(ValueType.object);
		v.addChild("rect", orect);
		// TODO 暂时不考虑 def
		orect.addChild("width", new JsonValue(box.box.width));
		orect.addChild("height", new JsonValue(box.box.height));
		orect.addChild("x", new JsonValue(box.box.x));
		orect.addChild("y", new JsonValue(box.box.y));
		
		// 初始锚点位置
		JsonValue oanchor = new JsonValue(ValueType.object);
		v.addChild("anchor", oanchor);
		oanchor.addChild("x", new JsonValue(box.anchor.x));
		oanchor.addChild("y", new JsonValue(box.anchor.y));

		// 初始速度
		JsonValue ovelocity = new JsonValue(ValueType.object);
		v.addChild("velocity", ovelocity);
		ovelocity.addChild("x", new JsonValue(box.velocity.x));
		ovelocity.addChild("y", new JsonValue(box.velocity.y));
		
		return v;
	}
	
	@Override
	public boolean setJson(String first, JsonValue value) {
		if ("box".equals(first)) {
			
			boolean modified = false;
			for (JsonValue entry = value.child; entry != null; entry = entry.next) {
				switch (entry.name) {
				case "anchor": {
					JsonValue ax = entry.get("x");
					JsonValue ay = entry.get("y");
					if (ax != null) {
						box.anchor.x = ax.asFloat(); modified = true;
					}
					if (ay != null) {
						box.anchor.y = ay.asFloat(); modified = true;
					}
				} break;
				case "velocity": {
					JsonValue vx = entry.get("x");
					JsonValue vy = entry.get("y");
					if (vx != null) {
						box.anchor.x = vx.asFloat(); modified = true;
					}
					if (vy != null) {
						box.velocity.y = vy.asFloat(); modified = true;
					}
				} break;

				default:
					break;
				}
			}
			
			return modified;
		}
		return super.setJson(first, value);
	}

	@Override
	public Box getBox() {
		return box;
	}

}
