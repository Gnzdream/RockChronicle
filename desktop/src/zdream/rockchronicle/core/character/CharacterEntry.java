package zdream.rockchronicle.core.character;

import java.util.HashMap;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonValue;

import zdream.rockchronicle.platform.world.LevelWorld;

/**
 * 人物
 * @author Zdream
 */
public abstract class CharacterEntry {
	
	private boolean exists = true;
	private boolean inited = false;
	
	public boolean isExists() {
		return exists;
	}
	
	protected void init(FileHandle file, JsonValue json) {
		if (inited) {
			return;
		}
		
		sortModules();
		inited = true;
	}
	
	public Sprite getSprite() {
		if (moduleMap.containsKey(SpriteModule.NAME)) {
			return ((SpriteModule) moduleMap.get(SpriteModule.NAME)).sprite;
		}
		
		return null;
	}
	
	/**
	 * 获得该人物的碰撞单位
	 * @return
	 */
	public void createBody(LevelWorld world) {
		if (moduleMap.containsKey(MotionModule.NAME)) {
			((MotionModule) moduleMap.get(MotionModule.NAME)).doCreateModule(world);
		}
	}

	{
		
	}
	
	/* **********
	 * 模块管理 *
	 ********** */
	/*
	 * 各种模块的管理器. 有:
	 * 碰撞模块, 处理 2D 碰撞的;
	 * 动作模块, 判断它在走路、攻击, 并且持续了多久了. 这些数据将更新 texture (使每一帧的样子都可能不同)
	 * 能力模块, 储存 AT、HP、DF 以及 BUFF、技能 等这些数据
	 * 控制模块, 让 AI 或者用户的键盘或手柄控制它
	 * 等等
	 */
	protected final HashMap<String, AbstractModule> moduleMap = new HashMap<>();
	private Array<AbstractModule> modules;
	
	public void addModule(AbstractModule module) {
		moduleMap.put(module.name(), module);
		if (inited) {
			sortModules();
		}
	}
	
	public AbstractModule getModule(String name) {
		return moduleMap.get(name);
	}
	
	private void sortModules() {
		modules = new Array<>(moduleMap.size());
		moduleMap.values().forEach(m -> modules.add(m));
		
		modules.sort((a, b) -> {
			return b.priority() - a.priority();
		});
	}
	
	/**
	 * 状态确定
	 * @param world
	 *   世界实例
	 * @param index
	 *   本帧的第几次调用. 第一次为 0
	 * @param hasNext
	 *   本帧是否还会再调用
	 */
	public void determine(LevelWorld world, int index, boolean hasNext) {
		for (int i = 0; i < modules.size; i++) {
			modules.get(i).determine(world, index, hasNext);
		}
	}
	
	/**
	 * 行动. 只调用行动模块
	 * @param world
	 *   世界实例
	 * @param index
	 *   本帧的第几次调用. 第一次为 0
	 * @param hasNext
	 *   本帧是否还会再调用
	 */
	public void step(LevelWorld world, int index, boolean hasNext) {
		((MotionModule) moduleMap.get(MotionModule.NAME)).step(world, index, hasNext);
	}

	public void onStepFinished(LevelWorld levelWorld, boolean isPause) {
		// TODO Auto-generated method stub
		
	}
	
	/**
	 * 绘画. 在屏幕上画出这个人物.
	 * 绘画的位置是由碰撞块的位置和纹理的属性 (纹理的锚点位置与碰撞块的有一定的差值) 来决定的
	 */
	public abstract void draw(SpriteBatch batch, OrthographicCamera camera);
	
	public void dispose() {
		moduleMap.forEach((n, m) -> {
			try {
				m.dispose();
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}

}
