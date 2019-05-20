package zdream.rockchronicle.platform.body;

import static java.lang.Math.floor;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import zdream.rockchronicle.platform.world.LevelWorld;

public class Box {
	
	private static int idCount = 1;
	
	/**
	 * @param parentId
	 *   该 Box 所属的角色的 id
	 */
	public Box(int parentId) {
		this.id = idCount++;
		this.parentId = parentId;
	}
	
	public final int id;
	public final int parentId;
	
	/*
	 * 静态参数部分
	 */
	
	/**
	 * 锚点位置. 相对于整个房间的位置.<br>
	 * 单位 : 格子
	 */
	public final Vector2 anchor = new Vector2();
	
	/**
	 * 整个方块相对于锚点的位置.<br>
	 * 一般而言, 除了状态改变使形状改变 (比如大小改变、摔倒等), 该数据才会变化<br>
	 * 单位 : 格子
	 */
	public final Rectangle box = new Rectangle();
	
	private boolean dirt = true;
	private final Rectangle pos = new Rectangle();
	private final BoxOccupation occupation = new BoxOccupation();
	
	/**
	 * 设置锚点位置
	 * @param x
	 *   单位 : 格子
	 * @param y
	 *   单位 : 格子
	 */
	public void setAnchor(float x, float y) {
		anchor.x = x;
		anchor.y = y;
		dirt = true;
	}
	
	/**
	 * 设置锚点横坐标位置
	 * @param x
	 *   单位 : 格子
	 */
	public void setAnchorX(float x) {
		anchor.x = x;
		dirt = true;
	}
	
	/**
	 * 设置锚点纵坐标位置
	 * @param y
	 *   单位 : 格子
	 */
	public void setAnchorY(float y) {
		anchor.y = y;
		dirt = true;
	}
	
	/**
	 * 在原有的位置上修改锚点位置, 让其坐标增加 (dx, dy)
	 * @param dx
	 *   单位 : 格子
	 * @param dy
	 *   单位 : 格子
	 */
	public void addAnchor(float dx, float dy) {
		anchor.x += dx;
		anchor.y += dy;
		dirt = true;
	}
	
	/**
	 * 在原有的位置上修改锚点位置, 让其坐标增加 (dx, 0)
	 * @param dx
	 *   单位 : 格子
	 */
	public void addAnchorX(float dx) {
		anchor.x += dx;
		dirt = true;
	}
	
	/**
	 * 在原有的位置上修改锚点位置, 让其坐标增加 (0, dy)
	 * @param dy
	 *   单位 : 格子
	 */
	public void addAnchorY(float dy) {
		anchor.y += dy;
		dirt = true;
	}
	
	public void setBox(float x, float y, float width, float height) {
		box.x = x;
		box.y = y;
		box.width = width;
		box.height = height;
		dirt = true;
	}
	
	public Rectangle getPosition() {
		if (dirt) {
			recalc();
		}
		return pos;
	}
	
	private void recalc() {
		pos.width = box.width;
		pos.height = box.height;
		pos.x = box.x + anchor.x;
		pos.y = box.y + anchor.y;
		
		float fxleft = pos.x;
		occupation.xleft = (int) floor(fxleft);
		occupation.xleftTightly = (fxleft == occupation.xleft);
		
		float fxright = fxleft + pos.width;
		occupation.xright = (int) floor(fxright);
		if (occupation.xrightTightly = (fxright == occupation.xright)) {
			occupation.xright--;
		}
		
		float fybottom = pos.y;
		occupation.ybottom = (int) floor(fybottom);
		occupation.ybottomTightly = (fybottom == occupation.ybottom);
		
		float fytop = fybottom + pos.height;
		occupation.ytop = (int) floor(fytop);
		if (occupation.ytopTightly = (fytop == occupation.ytop)) {
			occupation.ytop--;
		}
		
		dirt = false;
	}
	
	public BoxOccupation getOccupation() {
		if (dirt) {
			recalc();
		}
		return occupation;
	}
	
	/*
	 * 动态参数部分
	 */
	
	/**
	 * <p>该步的时间内, 物体移动的格子个数或趋势.
	 * 单位: 格子 / 步
	 * <p>注意, velocity 里面的值在有些情况下不等于本步物体的位置减去上一步物体的位置,
	 * 大概有以下几种情况:
	 * <li>在斜坡上行走的 (纵向的速度将不会写进 velocity)
	 * <li>在左右移动后撞墙的 (velocity 里面是不判断撞墙时的值)
	 * </li>
	 * </p>
	 */
	public final Vector2 velocity = new Vector2();
	
	/**
	 * 设置速度. 一秒大概 120 步, 在 {@link LevelWorld#TIME_STEP} 定义
	 * @param vx
	 *   单位 : 格子 / 步
	 * @param vy
	 *   单位 : 格子 / 步
	 */
	public void setVelocity(float vx, float vy) {
		this.velocity.x = vx;
		this.velocity.y = vy;
	}
	public void setVelocityX(float vx) {
		this.velocity.x = vx;
	}
	public void setVelocityY(float vy) {
		this.velocity.y = vy;
	}
	
	/**
	 * 按照速度, 更新 anchor 的值到一步时间后的状态 (“步”是个时间单位)
	 */
	public void updateAnchor() {
		this.addAnchor(velocity.x, velocity.y);
	}
	
	public void updateAnchorX() {
		this.addAnchorX(velocity.x);
	}
	
	public void updateAnchorY() {
		this.addAnchorY(velocity.y);
	}
	
	/*
	 * 配置参数
	 */
	/**
	 * 受到的重力及其它环境力的合力, 是重力的几倍;
	 * 受到相当于一倍重力的合力影响, 方向同 gravityDown 时, 参数为 1; 不受合力影响时为 0
	 */
	public float gravityScale;
	/**
	 * 受到的重力是否向下, 这里不计其它力.
	 * 比较实际的意义是判断角色的纹理是正放还是倒放
	 */
	public boolean gravityDown;
	/**
	 * 是否受地形的影响
	 */
	public boolean inTerrain = true;
	/**
	 * 特殊配置, 描述该角色是否能爬梯子
	 */
	public boolean climbable;
	
	/*
	 * 状态参数
	 */
	/**
	 * 四面是否碰到边了
	 */
	public boolean leftStop, rightStop, topStop, bottomStop;
	/**
	 * 标准姿势
	 */
	public byte posture;
	
	/** 普通姿势 */
	public static final byte POSTURE_NORMAL = 0;
	/** 攀爬姿势 */
	public static final byte POSTURE_CLIMB = 1;
	/** 悬挂、附着姿势 */
	public static final byte POSTURE_HANG = 2;
	
	@Override
	public String toString() {
		return String.format("Box#%d %s", parentId, this.anchor);
	}
	
}
