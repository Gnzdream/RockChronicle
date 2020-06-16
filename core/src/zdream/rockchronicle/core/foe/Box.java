package zdream.rockchronicle.core.foe;

import com.badlogic.gdx.utils.IntIntMap;

import zdream.rockchronicle.core.world.LevelWorld;
import zdream.rockchronicle.core.world.Ticker;

/**
 * 从 06-08 开始, 角色的位置由 int 代替原来的 float.
 * 每一格的长宽为 65536. 绘画的单位仍然不变, 为 1.
 * 
 * 逻辑单位: 1p = 1/65536 格
 * 
 * @author Zdream
 * @date 2020-06-08
 */
public class Box {
	
//	private static Decimal
	
	public static final int P_PER_BLOCK = 65536;
	public static final int BLOCK_MASK = 0xFFFF;
	
	private static int idCount = 1;
	private LevelWorld world;
	
	/**
	 * @param parentId
	 *   该 Box 所属的角色的 id
	 */
	public Box(int parentId) {
		this.id = idCount++;
		this.parentId = parentId;
	}
	
	void setWorld(LevelWorld world) {
		this.world = world;
	}
	
	public final int id;
	public final int parentId;
	
	/**
	 * <p>原本每个盒子都是透明体. 这个参数
	 * <p>这里可以为每个盒子设置, 对哪些 camp 是强实体 (1), 弱实体 (2), 向上平台 (16), 向下平台 (32).
	 * 强实体在重合后可以判断其它角色 destroy, 弱实体在重合且无法恢复时不会让其它角色 destroy.
	 * 向上平台正着的角色可以站, 向下平台反着的角色可以站
	 * <p>key: camp, value: 地形体. 48 是既是向上平台又是向下平台. (16 & 32 = 48)
	 * </p>
	 */
	public IntIntMap terrain;
	
	/*
	 * 静态参数部分
	 */
	
	/**
	 * 锚点位置. 相对于整个房间的位置.<br>
	 * 单位 : p
	 */
	public int anchorX, anchorY;
	
	/**
	 * 上一步的锚点位置
	 */
	public int lastAnchorX, lastAnchorY;
	
	/**
	 * 整个方块相对于锚点的位置.<br>
	 * 一般而言, 除了状态改变使形状改变 (比如大小改变、摔倒等), 该数据才会变化<br>
	 * 单位 : p
	 */
	public int boxX, boxY, boxWidth, boxHeight;
	
	private boolean dirt = true;
	public int posX, posY, posWidth, posHeight;
	private final BoxOccupation occupation = new BoxOccupation();
	
	private void boxUpdated() {
		dirt = true;
		if (world != null) {
			world.notifyBoxUpdated(this);
		}
	}
	
	/**
	 * 设置锚点位置
	 * @param x
	 *   单位 : p
	 * @param y
	 *   单位 : p
	 */
	public void setAnchor(int x, int y) {
		anchorX = x;
		anchorY = y;
		boxUpdated();
	}
	
	/**
	 * 设置锚点横坐标位置
	 * @param x
	 *   单位 : p
	 */
	public void setAnchorX(int x) {
		anchorX = x;
		boxUpdated();
	}
	
	/**
	 * 设置锚点纵坐标位置
	 * @param y
	 *   单位 : p
	 */
	public void setAnchorY(int y) {
		anchorY = y;
		boxUpdated();
	}
	
	/**
	 * 在原有的位置上修改锚点位置, 让其坐标增加 (dx, dy)
	 * @param dx
	 *   单位 : p
	 * @param dy
	 *   单位 : p
	 */
	public void addAnchor(int dx, int dy) {
		anchorX += dx;
		anchorY += dy;
		boxUpdated();
	}
	
	/**
	 * 在原有的位置上修改锚点位置, 让其坐标增加 (dx, 0)
	 * @param dx
	 *   单位 : p
	 */
	public void addAnchorX(int dx) {
		anchorX += dx;
		boxUpdated();
	}
	
	/**
	 * 在原有的位置上修改锚点位置, 让其坐标增加 (0, dy)
	 * @param dy
	 *   单位 : p
	 */
	public void addAnchorY(int dy) {
		anchorY += dy;
		boxUpdated();
	}
	
	/**
	 * 设置的是相对 anchor 的位置
	 */
	public void setBox(int x, int y, int width, int height) {
		boxX = x;
		boxY = y;
		boxWidth = width;
		boxHeight = height;
		boxUpdated();
	}
	
	/**
	 * @return
	 *   中心点横坐标, 单位 p
	 */
	public int getCenterX() {
		flush();
		return posX + posWidth / 2;
	}

	/**
	 * @return
	 *   中心点纵坐标, 单位 p
	 */
	public int getCenterY() {
		flush();
		return posY + posHeight / 2;
	}
	
	public Box flush() {
		if (dirt) {
			recalc();
		}
		return this;
	}
	
	private void recalc() {
		posWidth = boxWidth;
		posHeight = boxHeight;
		posX = boxX + anchorX;
		posY = boxY + anchorY;
		
		occupation.xleftTightly = (posX & BLOCK_MASK) == 0;
		occupation.xleft = (posX >= 0) ? posX / P_PER_BLOCK : 
			(occupation.xleftTightly) ? (posX / P_PER_BLOCK) : (posX / P_PER_BLOCK) - 1;
			
		final int ixright = posX + posWidth;
		occupation.xrightTightly = (ixright & BLOCK_MASK) == 0;
		occupation.xright = (ixright >= 0) ? 
				((occupation.xrightTightly) ? (ixright / P_PER_BLOCK - 1) : ixright / P_PER_BLOCK) :
				(ixright / P_PER_BLOCK - 1);
		
		occupation.ybottomTightly = (posY & BLOCK_MASK) == 0;
		occupation.ybottom = (posY >= 0) ? posY / P_PER_BLOCK : 
			(occupation.ybottomTightly) ? (posY / P_PER_BLOCK) : (posY / P_PER_BLOCK) - 1;
			
		final int iytop = posY + posHeight;
		occupation.ytopTightly = (iytop & BLOCK_MASK) == 0;
		occupation.ytop = (iytop >= 0) ? 
				((occupation.ytopTightly) ? (iytop / P_PER_BLOCK - 1) : iytop / P_PER_BLOCK) :
				(iytop / P_PER_BLOCK - 1);
		
		dirt = false;
	}
	
	public BoxOccupation getOccupation() {
		flush();
		return occupation;
	}
	
	/*
	 * 动态参数部分
	 */
	
	/**
	 * <p>该步的时间内, 物体移动的长度或趋势.
	 * 单位: p / 步
	 * <p>注意, velocity 里面的值在有些情况下不等于本步物体的位置减去上一步物体的位置,
	 * 大概有以下几种情况:
	 * <li>在斜坡上行走的 (纵向的速度将不会写进 velocity)
	 * <li>在左右移动后撞墙的 (velocity 里面是不判断撞墙时的值)
	 * </li>
	 * </p>
	 */
	public int velocityX, velocityY;
	
	/**
	 * 上一步的速度
	 */
	public float lastVelocityX, lastVelocityY;
	
	/**
	 * 设置速度. 一秒大概 120 步, 在 {@link Ticker#WORLD_STEP} 定义
	 * @param vx
	 *   单位 : p / 步
	 * @param vy
	 *   单位 : p / 步
	 */
	public void setVelocity(int vx, int vy) {
		this.velocityX = vx;
		this.velocityY = vy;
	}
	public void setVelocityX(int vx) {
		this.velocityX = vx;
	}
	public void setVelocityY(int vy) {
		this.velocityY = vy;
	}
	
	/**
	 * 按照速度, 更新 anchor 的值到一步时间后的状态 (“步”是个时间单位)
	 */
	public void updateAnchor() {
		this.addAnchor(velocityX, velocityY);
	}
	
	public void updateAnchorX() {
		this.addAnchorX(velocityX);
	}
	
	public void updateAnchorY() {
		this.addAnchorY(velocityY);
	}
	
	/*
	 * 配置参数
	 */
	/**
	 * 受到的重力及其它环境力的合力, 是重力的几倍;
	 * 受到相当于一倍重力的合力影响, 方向同 gravityDown 时, 参数为 1; 不受合力影响时为 0
	 */
	public float gravityScale = 1;
	/**
	 * 受到的重力是否向下, 这里不计其它力.
	 * 比较实际的意义是判断角色的纹理是正放还是倒放
	 */
	public boolean gravityDown = true;
	/**
	 * 左右朝向. true 为右
	 */
	public boolean orientation;
	
	/*
	 * 状态参数
	 */
	/**
	 * 四面是否碰到边了
	 */
	public boolean leftTouched, rightTouched, topTouched, bottomTouched;
	
	/**
	 * 是否在空中, false 表示在地上 / 平台上
	 */
	public boolean inAir;
	
	/**
	 * <p>将 p 为单位的数值转成块.
	 * <p>为方块右边、上边规则, 如果点在边界处, 则归入左边 / 下边.
	 * <li> -1 p -> -1 block
	 * <li> 0 p -> -1 block
	 * <li> 1 p -> 0 block
	 * <li> 65536 p -> 0 block
	 * <li> 65537 p -> 1 block
	 * <li> -65535 p -> -1 block
	 * <li> -65536 p -> -2 block
	 * </li></p>
	 * 
	 * @param p
	 * @return
	 */
	public static int blockLeft(int p) {
		return (p >= 0) ? 
				((p & BLOCK_MASK) == 0) ? p / P_PER_BLOCK - 1 : p / P_PER_BLOCK :
				p / P_PER_BLOCK - 1 ;
	}
	
	/**
	 * <p>将 p 为单位的数值转成块.
	 * <p>为方块左边、下边规则, 如果点在边界处, 则归入右块 / 上块.
	 * <li> 0 p -> 0 block
	 * <li> 100 p -> 0 block
	 * <li> 65536 p -> 1 block
	 * <li> -100 p -> -1 block
	 * <li> -65536 p -> -1 block
	 * <li> -65537 p -> -2 block
	 * </li></p>
	 * 
	 * @param p
	 * @return
	 */
	public static int blockRight(int p) {
		return (p >= 0) ? p / P_PER_BLOCK :
			((p & BLOCK_MASK) == 0) ? p / P_PER_BLOCK : p / P_PER_BLOCK - 1;
	}
	
	public static float p2block(int p) {
		return ((float) p) / P_PER_BLOCK;
	}
	
	public static int block2P(float block) {
		return (int) (block * P_PER_BLOCK);
	}
	
	/**
	 * 将 p 为单位的数值向下取整
	 * @param p
	 * @return
	 */
	public static int pFloor(int p) {
		return p & 0xFFFF0000;
	}
	
	/**
	 * 将 p 为单位的数值向上取整
	 * @param p
	 * @return
	 */
	public static int pCeil(int p) {
		return ((p & BLOCK_MASK) == 0) ? p : (p & 0xFFFF0000) + P_PER_BLOCK;
	}
	
	@Override
	public String toString() {
		return String.format("Box#%d %4.2f,%4.2f", parentId,
				(float) (anchorX) / P_PER_BLOCK,
				(float) (anchorY) / P_PER_BLOCK);
	}
	
}
