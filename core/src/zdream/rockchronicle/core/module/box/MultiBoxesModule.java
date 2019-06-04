package zdream.rockchronicle.core.module.box;

import com.badlogic.gdx.utils.Array;

import zdream.rockchronicle.core.character.CharacterEntry;
import zdream.rockchronicle.platform.body.Box;
import zdream.rockchronicle.platform.world.LevelWorld;

/**
 * <p>复合碰撞方块的盒子模块
 * <p>角色的盒子模块为复合盒子模块, 意味着一个角色拥有或可能拥有多于一个的碰撞盒子
 * </p>
 * 
 * @author Zdream
 * @since v0.0.1
 * @date
 *   2019-05-13 (created)
 *   2019-06-04 (last modified)
 */
public class MultiBoxesModule extends BoxModule {
	
	/**
	 * 主要的盒子
	 */
	public final Box mainBox;
	
	/**
	 * 除了主要的盒子以外, 其它的盒子.
	 * 当做添加、修改、删除操作时, 请操作后设置 boxArray = null
	 */
	public final Array<Box> boxes = new Array<Box>(4);
	
	/**
	 * 缓存
	 */
	private Box[] boxArray;

	public MultiBoxesModule(CharacterEntry parent) {
		super(parent);
		
		mainBox = new Box(parent.id);
	}
	
	@Override
	public String description() {
		return "multi";
	}

	@Override
	public Box getBox() {
		return mainBox;
	}

	@Override
	public Box[] getAllBoxes() {
		if (boxArray == null) {
			boxArray = new Box[1 + boxes.size];
			boxArray[0] = mainBox;
			final int len = boxes.size;
			for (int i = 0; i < len; i++) {
				boxArray[i + 1] = boxes.get(i);
			}
		}
		return boxArray;
	}

	@Override
	protected void createBody() {
		world.addBox(mainBox);
		for (int i = 0; i < boxes.size; i++) {
			world.addBox(boxes.get(i));
		}
	}

	@Override
	protected void destroyBody() {
		world.removeBox(mainBox);
		for (int i = 0; i < boxes.size; i++) {
			world.removeBox(boxes.get(i));
		}
	}

	@Override
	public void setNextPattern(String pattern) {
		// TODO Auto-generated method stub

	}

	@Override
	public void move(LevelWorld world) {
		// TODO Auto-generated method stub

	}

}
