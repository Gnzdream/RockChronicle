package zdream.rockchronicle.platform.world;

import static java.lang.Math.floor;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Pool.Poolable;

import zdream.rockchronicle.platform.body.Box;

/**
 * 用于在检测碰撞盒子是否碰边时, 记录碰撞盒子数据的
 * 
 * @author Zdream
 * @since v0.0.1
 * @date
 *   2019-05-18 (created)
 *   2019-05-18 (last modified)
 */
public class BoxParam implements Poolable {
	
	Box box;
	
	/**
	 * 盒子左侧的横坐标, 向下取整.
	 * 
	 * 比如 0.5 -> 0
	 * 比如 -0.5 -> -1
	 * 比如 2 -> 2
	 */
	int xleft;
	
	/**
	 * 盒子的左侧横坐标是否是整数
	 */
	boolean xleftTightly;
	
	/**
	 * 盒子右侧的横坐标, 向下取整. 如果盒子右侧横坐标为整数, 则减一.
	 * 
	 * 比如 0.5 -> 0
	 * 比如 -0.5 -> -1
	 * 比如 2 -> 1
	 */
	int xright;
	
	/**
	 * 盒子的右侧横坐标是否是整数
	 */
	boolean xrightTightly;
	
	/**
	 * 盒子上侧的纵坐标, 向下取整. 如果盒子上侧纵坐标为整数, 则减一.
	 * 
	 * 比如 0.5 -> 0
	 * 比如 -0.5 -> -1
	 * 比如 2 -> 1
	 */
	int ytop;
	
	/**
	 * 盒子的上侧纵坐标, 是否是整数
	 */
	boolean ytopTightly;
	
	/**
	 * 盒子下侧的纵坐标, 向下取整.
	 * 
	 * 比如 0.5 -> 0
	 * 比如 -0.5 -> -1
	 * 比如 2 -> 2
	 */
	int ybottom;
	
	/**
	 * 盒子的下侧纵坐标是否是整数
	 */
	boolean ybottomTightly;
	
	
	public BoxParam() {}

	@Override
	public void reset() {
		box = null;
	}
	
	public void handleBox(Box box) {
		this.box = box;
		recalc();
	}
	
	public void recalc() {
		Rectangle rect = box.getPosition();
		
		float fxleft = rect.x;
		xleft = (int) floor(fxleft);
		xleftTightly = (fxleft == xleft);
		
		float fxright = fxleft + rect.width;
		xright = (int) floor(fxright);
		if (xrightTightly = (fxright == xright)) {
			xright--;
		}
		
		float fybottom = rect.y;
		ybottom = (int) floor(fybottom);
		ybottomTightly = (fybottom == ybottom);
		
		float fytop = fybottom + rect.height;
		ytop = (int) floor(fytop);
		if (ytopTightly = (fytop == ytop)) {
			ytop--;
		}
	}

}
