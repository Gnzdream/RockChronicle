package zdream.rockchronicle.character;

import java.util.Iterator;
import java.util.LinkedHashMap;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

import zdream.rockchronicle.platform.world.IPhysicsStep;
import zdream.rockchronicle.platform.world.LevelWorld;

/**
 * 人物
 * @author Zdream
 */
public abstract class CharacterEntry implements IPhysicsStep {
	
	public Sprite getSprite() {
		if (modules.containsKey(SpriteModule.NAME)) {
			return ((SpriteModule) modules.get(SpriteModule.NAME)).sprite;
		}
		
		return null;
	}
	
	/**
	 * 获得该人物的碰撞单位
	 * @return
	 */
	public void createBody(LevelWorld world) {
		if (modules.containsKey(MotionModule.NAME)) {
			((MotionModule) modules.get(MotionModule.NAME)).createBody(world);
		}
	}

	public CharacterEntry() {
		
	}
	
	/*
	 * 各种模块的管理器. 有:
	 * 碰撞模块, 处理 2D 碰撞的;
	 * 动作模块, 判断它在走路、攻击, 并且持续了多久了. 这些数据将更新 texture (使每一帧的样子都可能不同)
	 * 能力模块, 储存 AT、HP、DF 以及 BUFF、技能 等这些数据
	 * 控制模块, 让 AI 或者用户的键盘或手柄控制它
	 * 等等
	 */
	protected final LinkedHashMap<String, AbstractModule> modules = new LinkedHashMap<>();
	
	public void addModule(AbstractModule module) {
		modules.put(module.name(), module);
	}
	
	public AbstractModule getModule(String name) {
		return modules.get(name);
	}
	
	public void load(FileHandle file) {
		JsonReader r = new JsonReader();
		JsonValue json = r.parse(file);
		
		onLoadJson(file, json);
	}
	
	protected abstract void onLoadJson(FileHandle file, JsonValue json);
	
	/**
	 * @param index
	 *   本帧的第几次调用. 第一次为 0
	 * @param hasNext
	 *   本帧是否还会再调用
	 */
	public void step(LevelWorld world, int index, boolean hasNext) {
		Iterator<AbstractModule> it = modules.values().iterator();
		while (it.hasNext()) {
			it.next().step(world, index, hasNext);
		}
	}
	
	/**
	 * 绘画. 在屏幕上画出这个人物.
	 * 绘画的位置是由碰撞块的位置和纹理的属性 (纹理的锚点位置与碰撞块的有一定的差值) 来决定的
	 */
	public abstract void draw(SpriteBatch batch);
	
	public void dispose() {
		modules.forEach((n, m) -> {
			try {
				m.dispose();
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}

}
