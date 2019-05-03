package zdream.rockchronicle.platform.body;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import zdream.rockchronicle.platform.world.LevelWorld;

public class Box {
	
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
			pos.width = box.width;
			pos.height = box.height;
			pos.x = box.x + anchor.x;
			pos.y = box.y + anchor.y;
			dirt = false;
		}
		return pos;
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
	 * 其它检测参数部分
	 */
	
	// ****** 左右碰撞检测
	
	/**
	 * 左边或右边是否碰到边了
	 */
	public boolean leftStop, rightStop;
	
	// ****** 落地检测
	
	/**
	 * 上边是否碰到边了
	 */
	public boolean topStop;
	/**
	 * 是否受地形的影响
	 */
	public boolean inTerrain = true;
	/**
	 * 踩在哪些地面上
	 */
	public Array<TerrainParam> grounds = new Array<>(4);
	
	/**
	 * @return
	 *   返回是否站在地面上
	 */
	public boolean onTheGround() {
		return grounds.size != 0;
	}
	
	// ****** 跳跃参数
	/*
	 * 向上跳跃是一个 [高度 - 时间] 二次函数
	 */
	
	/**
	 * 跳跃的向上的冲量, 即跳跃的第一帧, 每步向上的格子数
	 * 洛克人的值为: 0.178 (1/120 s) | 21.36 (1 s)
	 */
	public float jumpImpulse;
	/**
	 * 在跳跃时, 向上的速度会逐渐降下来 (负数)
	 * 每过一步的时间, 每步向上的格子数. （单位：格子 / (步^2)）.
	 * 如果碰到在水中的情况, 每步下降的值为这里的一半
	 * 洛克人的值为: -0.005 (1/120 s) | -72 (1 s)
	 */
	public float jumpDecay;
	/**
	 * 最大的掉落速度. (负数)
	 * 如果碰到在水中的情况, 最大的掉落速度为这里的一半
	 * 洛克人的值为: -0.233333 (1/120 s) | -28 (1 s)
	 */
	public float maxDropVelocity;
	
}
