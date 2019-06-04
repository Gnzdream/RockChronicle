package zdream.rockchronicle.core.module.box;

import java.util.Objects;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonValue;

import zdream.rockchronicle.core.character.CharacterEntry;
import zdream.rockchronicle.core.module.AbstractModule;
import zdream.rockchronicle.core.move.IMovable;
import zdream.rockchronicle.platform.body.Box;
import zdream.rockchronicle.platform.world.LevelWorld;

/**
 * 单一碰撞方块的盒子模块
 * 
 * @author Zdream
 * @since v0.0.1
 * @date
 *   2019-05-13 (created)
 *   2019-06-04 (last modified)
 */
public abstract class BoxModule extends AbstractModule {
	
	public static final String NAME = "Box";

	protected LevelWorld world;
	
	/**
	 * 本次的行动是否已经完成了
	 */
	public boolean actionFinished;

	public BoxModule(CharacterEntry parent) {
		super(parent);
	}
	
	@Override
	public void init(FileHandle file, JsonValue value) {
		super.init(file, value);
	}

	@Override
	public String name() {
		return NAME;
	}
	
	@Override
	public int priority() {
		return -100;
	}

	public final void doCreateBody(LevelWorld world) {
		this.world = world;
		this.createBody();
	}
	
	public final void doDestroyBody() {
		this.destroyBody();
	}

	protected abstract void createBody();
	protected abstract void destroyBody();
	
	/**
	 * 如果本角色只有一个碰撞盒子, 则调用该方法来获取其碰撞盒子
	 * @return
	 */
	public abstract Box getBox();
	
	/**
	 * 获取角色的所有的碰撞盒子, 以列表的形式返回.
	 */
	public abstract Box[] getAllBoxes();
	
	/**
	 * 设置下一步的形态是什么
	 * @param pattern
	 */
	public abstract void setNextPattern(String pattern);
	
	/* **********
	 * 移动执行 *
	 ********** */
	
	class MovableNode implements Comparable<MovableNode> {
		IMovable movable;
		int priority;
		public MovableNode(IMovable movable, int priority) {
			super();
			this.movable = movable;
			this.priority = priority;
		}
		@Override
		public int compareTo(MovableNode o) {
			// 优先级高的排前面
			return (priority < o.priority) ? 1 : ((priority == o.priority) ? 0 : -1);
		}
	}
	
	protected final Array<MovableNode> movables = new Array<>(8);
	
	/**
	 * 添加行动执行实例
	 * @param movable
	 * @param priority
	 *   优先度
	 */
	public void addMovable(IMovable movable, int priority) {
		Objects.requireNonNull(movable);
		movables.add(new MovableNode(movable, priority));
	}
	
	/**
	 * 删除行动执行实例
	 * @param movable
	 */
	public void removeMovable(IMovable movable) {
		for (int i = 0; i < movables.size; i++) {
			if (movables.get(i).movable == movable) {
				movables.removeIndex(i);
				i--;
			}
		}
	}
	
	/**
	 * 世界每走一步, 角色进行一次行动
	 * @param world
	 *   关卡世界实体
	 */
	public abstract void action(LevelWorld world);
	
	@Override
	public void stepPassed() {
		super.stepPassed();
		this.actionFinished = false;
	}

}
