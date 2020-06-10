package zdream.rockchronicle.core.module.collision;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.JsonValue;

import zdream.rockchronicle.core.character.CharacterEntry;
import zdream.rockchronicle.core.character.event.CharacterEvent;
import zdream.rockchronicle.core.module.AbstractModule;
import zdream.rockchronicle.platform.body.Box;
import zdream.rockchronicle.platform.world.LevelWorld;

/**
 * <p>碰撞模块
 * <p>产生伤害以及其它效果的源头. 优先级 -128
 * </p>
 * 
 * @author Zdream
 * @since v0.0.1
 * @date
 *   2019-05-08 (create)
 *   2019-06-08 (last modified)
 */
public abstract class CollisionModule extends AbstractModule {
	
	public static final String NAME = "collision";

	public CollisionModule(CharacterEntry parent, String desc) {
		super(parent, NAME, desc);
		
	}

	@Override
	public int priority() {
		return -0x80;
	}
	
	@Override
	public void init(FileHandle file, JsonValue value) {
		super.init(file, value);
		
		JsonValue ocollisionc = value.get("collision");
		executeType = ocollisionc.getString("execute", "repeat");
		setSituation("collision.execute", new JsonValue(executeType));
		
		parent.addSubscribe("health_exhausted", this);
	}
	
	@Override
	public void willDestroy() {
		parent.removeSubscribe("health_exhausted", this);
		super.willDestroy();
	}
	
	@Override
	public void determine(LevelWorld world, int index, boolean hasNext) {
		super.determine(world, index, hasNext);
		this.searchOverlapsBox(getSingleBox(), world);
	}
	
	/* **********
	 * 基本参数 *
	 ********** */
	/**
	 * 执行方式
	 */
	public String executeType;
	/**
	 * 角色是否需要删除
	 */
	public boolean parentWillDelete;
	
	/*
	 * 状态
	 */
	
	/**
	 * 是否能发挥效果
	 */
	public boolean isFunctioned = true;
	
	@Override
	public void stepPassed() {
		if (parentWillDelete) {
			parent.willDestroy();
		} else {
			super.stepPassed();
		}
	}

	/* **********
	 * 工具方法 *
	 ********** */
	
	/**
	 * 如果本角色只有一个碰撞盒子, 则调用该方法来获取其碰撞盒子
	 * @return
	 */
	protected Box getSingleBox() {
		return parent.getBoxModule().getBox();
	}
	
	protected void searchOverlapsBox(Box box, LevelWorld world) {
		if (!needCheckOverlaps(box, world)) {
			return;
		}
		if (isFunctioned) {
			world.overlaps(box, this::doForOverlapsBox);
		}
	}
	
	/**
	 * 是否进行碰撞、重合判断
	 * @param box
	 * @param world
	 * @return
	 */
	protected boolean needCheckOverlaps(Box box, LevelWorld world) {
		return box != null;
	}
	
	/**
	 * 对碰撞的、重合的其它角色的碰撞盒子进行判断.
	 * @param box
	 * @return
	 *   如果还需要判断其它的盒子, 则返回 true; 如果不再判断其它盒子, 返回 false
	 */
	protected abstract boolean doForOverlapsBox(Box box);
	
	@Override
	public void receiveEvent(CharacterEvent event) {
		if ("health_exhausted".equals(event.name)) {
			isFunctioned = false;
			return;
		}
		super.receiveEvent(event);
	}

}
