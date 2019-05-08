package zdream.rockchronicle.core.character.motion;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonValue.ValueType;

import zdream.rockchronicle.core.character.CharacterEntry;
import zdream.rockchronicle.core.character.module.MotionModule;
import zdream.rockchronicle.core.character.parameter.JsonCollector;
import zdream.rockchronicle.platform.body.Box;
import zdream.rockchronicle.platform.world.LevelWorld;

/**
 * 单一碰撞方块的行动模块
 * 
 * @author Zdream
 * @since v0.0.1
 * @date 2019-05-06 (create)
 */
public class SingleBoxMotionModule extends MotionModule {
	
	public final Box box = new Box();
	protected JsonCollector boxc;

	public SingleBoxMotionModule(CharacterEntry ch) {
		super(ch);
	}
	
	@Override
	public void init(FileHandle file, JsonValue value) {
		super.init(file, value);
		
		initBox(value.get("box"));
		addCollector(boxc = new JsonCollector(this::getBoxJson, "box"));
	}

	public void initBox(JsonValue object) {
		box.inTerrain = object.getBoolean("inTerrain", true);
		
		JsonValue orect = object.get("rect");
		// TODO 暂时不考虑 def
		box.box.width = orect.getFloat("width");
		box.box.height = orect.getFloat("height");
		box.box.x = orect.getFloat("x");
		box.box.y = orect.getFloat("y");
		
		// 初始锚点位置
		JsonValue oanchor = object.get("anchor");
		if (oanchor != null) {
			box.anchor.x = oanchor.getFloat("x", 0f);
			box.anchor.y = oanchor.getFloat("y", 0f);
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
	public void determine(LevelWorld world, int index, boolean hasNext) {
		super.determine(world, index, hasNext);
		
		if (box.inTerrain) {
			// 碰边检测
			world.bottomStop(box);
			world.topStop(box);
			world.leftStop(box);
			world.rightStop(box);
			super.motionc.clear();
			
			// 位置重合修正
			boolean glitch = world.correctOverlapBox(box);
			if (!glitch) { // 已经卡在墙中了
				Gdx.app.error("SingleBoxMotionModule", String.format("%s 被卡在墙里了, 而且无法修正", parent));
			}
		}
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
	public JsonValue getMotionJson() {
		JsonValue v = super.getMotionJson();
		v.addChild("bottomStop", new JsonValue(box.onTheGround()));
		v.addChild("topStop", new JsonValue(box.topStop));
		v.addChild("leftStop", new JsonValue(box.leftStop));
		v.addChild("rightStop", new JsonValue(box.rightStop));
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

}
