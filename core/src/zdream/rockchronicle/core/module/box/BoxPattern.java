package zdream.rockchronicle.core.module.box;

/**
 * <p>碰撞方块预设的形状数据
 * <p>角色在不同形态下的方块的长宽与锚点相对位置均有差别, 每一个形态的一个盒子对应了一个 BoxShape
 * </p>
 * 
 * @author Zdream
 * @since v0.0.1
 * @date
 *   2019-05-26 (created)
 *   2019-05-26 (last modified)
 */
public class BoxPattern {

	public String name;
	public float width, height, x, y;
	
	@Override
	public String toString() {
		return "BoxShape: " + name;
	}

}
