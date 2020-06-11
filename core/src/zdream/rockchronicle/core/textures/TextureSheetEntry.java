package zdream.rockchronicle.core.textures;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class TextureSheetEntry {
	
	public String name;
	
	/**
	 * 所选区域的长宽
	 */
	public int width, height;
	
	/**
	 * 在原来的纹理图片上, 这个所选区域左上角的点的位置.
	 */
	public int x, y;
	
	/**
	 * 引擎中对所选区域的定义数据内容
	 */
	public TextureRegion region;
	
	/**
	 * <p>游戏中纹理的位置和碰撞体的位置是不同的. 通常碰撞体图形在纹理图片区域的内部.<br>
	 * 系统先计算碰撞体图形的位置, 然后根据碰撞体的位置来确定纹理摆放的区域.<br>
	 * {@link #offsetx} 与 {@link #offsety} 两个参数就是确定这样位置关系的数据;<br>
	 * 它指示了纹理左下角和碰撞体左下角两个点的差值.
	 * <br>
	 * <p>这里举个例子, 坐标系是 y 轴向上的、以屏幕左下角为原点的:<br>
	 * 当确定了碰撞体的位置之后, 假设拿到的左下角点位置是 (x, y)
	 * 那么纹理左下角点就是 (x - offsetx, y - offsety).
	 */
	public int offsetx, offsety;

}
