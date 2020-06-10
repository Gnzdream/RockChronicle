package zdream.rockchronicle.platform.world;

import static zdream.rockchronicle.platform.body.Box.P_PER_BLOCK;
import static zdream.rockchronicle.platform.body.Box.blockLeft;
import static zdream.rockchronicle.platform.body.Box.blockRight;

import java.util.function.Predicate;

import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.utils.Array;

import zdream.rockchronicle.platform.body.Box;
import zdream.rockchronicle.platform.body.BoxOccupation;
import zdream.rockchronicle.platform.body.TerrainParam;
import zdream.rockchronicle.platform.region.Gate;
import zdream.rockchronicle.platform.region.ITerrainStatic;
import zdream.rockchronicle.platform.region.Room;
import zdream.rockchronicle.platform.region.Terrains;

/**
 * 关卡, 含物理世界
 * @author Zdream
 */
public class LevelWorld implements ITerrainStatic {

	public LevelWorld() {
		terrainCollisionFilter = new Filter();
		terrainCollisionFilter.groupIndex = -1;
	}
	
	public Room currentRoom;
	
	public void doCreate() {
		// 世界重力向下
		this.pause = 2;
	}
	
	/**
	 * 强暂停
	 */
	public void doPause() {
		this.pause = 2;
	}
	
	/**
	 * 形式暂停
	 */
	public void doWait() {
		this.pause = 1;
	}
	
	/**
	 * 从暂停中恢复
	 */
	public void doResume() {
		if (this.pause == 0) return;
		this.pause = 0;
		this.frameTime = 0;
		this.accumulator = 0;
	}
	
	/**
	 * <p>是否在暂停中.
	 * <p>进行中为 0, 形式暂停为 1, 强暂停为 2.
	 * 其中, 形式暂停指切换房间时世界的暂停, 以及其它重要的事件触发时.
	 * 此时画面不能完全暂停;
	 * 强暂停时世界里的所有的动作全部暂停
	 * </p>
	 */
	public byte pause;
	
	/**
	 * 该帧距离上一帧时, 过去的物理世界的时间. 单位秒<br>
	 * 外部不允许修改, 只允许访问
	 */
	public float frameTime; // readonly for outsider
	
	/**
	 * 该值用于在动态帧率时, 调整世界的更新频率.
	 */
	private float accumulator = 0;
	
	/**
	 * 现在的步数
	 */
	public int step;

	/**
	 * 世界的更新频率为每秒 120 步
	 */
	public static final int STEPS_PER_SECOND = 120;
	public static final float TIME_STEP = 1.0f / STEPS_PER_SECOND;
	
	public final Array<Box> boxs = new Array<>();
	
	/**
	 * 向世界放置物体
	 * @param box
	 */
	public void addBox(Box box) {
		boxs.add(box);
	}
	
	/**
	 * 从世界中删除物体
	 * @param box
	 */
	public void removeBox(Box box) {
		boxs.removeValue(box, true);
	}

	public int count() {
		return boxs.size;
	}
	
	/**
	 * 清空世界中的物体
	 */
	public void clearBox() {
		boxs.clear();
	}
	
	/**
	 * 每帧的回调函数
	 */
	private IPhysicsStep stepCallBack;
	
	/**
	 * 设置每帧的回调函数. 设置为 null 即清除
	 */
	public void setStepCallBack(IPhysicsStep stepCallBack) {
		this.stepCallBack = stepCallBack;
	}
	
	/**
	 * 用两帧相隔的时间处理世界的变化率. 不超过 0.1s
	 * @param deltaTime
	 *   该帧距离上一帧的时间. 单位秒
	 */
	public void doPhysicsStep(float deltaTime) {
		this.frameTime = deltaTime = Math.min(deltaTime, 0.1f);
	    accumulator += deltaTime;
	    int index = 0;
	    while (accumulator >= TIME_STEP) {
	        accumulator -= TIME_STEP;
	        
	        if (this.pause == 0) {
	        	step++;
		        if (stepCallBack != null) {
		        	stepCallBack.step(this, index++, accumulator >= TIME_STEP);
		        }
	        } else if (this.pause == 1) {
	        	stepCallBack.stepPaused(this);
	        }
	    }
	}
	
	public void setCurrentRoom(Room currentRoom) {
		this.currentRoom = currentRoom;
	}
	
	private Filter terrainCollisionFilter;
	
	/**
	 * 查看与指定的盒子发生碰撞的盒子, 并逐个进行判断.
	 * @param box
	 *   指定的碰撞盒子
	 * @param test
	 *   对碰撞的盒子进行判断. 返回 true 则继续判断后面的盒子, 否则停止
	 */
	public void overlaps(Box box, Predicate<Box> test) {
		int ixleft = box.posX;
		int ixright = ixleft + box.posWidth;
		int iybottom = box.posY;
		int iytop = iybottom + box.posHeight;
		
		for (int i = 0; i < boxs.size; i++) {
			Box other = boxs.get(i);
			if (other != box) {
				int oxleft = other.posX;
				int oxright = ixleft + other.posWidth;
				int oybottom = other.posY;
				int oytop = iybottom + other.posHeight;
				
				boolean b = oxleft <= ixright &&
						oxright >= ixleft &&
						oybottom <= iytop &&
						oytop >= iybottom; // pos.overlaps(otherPos) 加强版
				if (!b) {
					continue;
				}
				if (!test.test(other)) {
					return;
				}
			}
		}
	}
	
	/**
	 * <p>判断一个盒子是否下侧是否碰到了地形块、或其它允许踩的其它盒子
	 * <p>当确定物体“嵌”在地里面, 现在不进行抬升操作
	 * 
	 * <p>注意: <br>
	 * 该方法现在只处理单个直立方块组成的物体<br>
	 * 所有的地形方块长宽都不等于 1, 有一些预留的缝隙,
	 *   防止由于引擎误判造成摩擦力太大的情况
	 * </p>
	 * @param box
	 *   地形数据. 单位 : 格子
	 */
	public boolean bottomStop(Box box) {
		if (!box.inTerrain) {
			return box.bottomStop = false;
		}
		
		// TODO 先跳过判断踩在别的盒子的判断
		
		BoxOccupation occ = box.getOccupation();
		// 底边左端所在的格子 (x)
		int xleft = Math.max(occ.xleft, 0);
		int xright = Math.min(occ.xright, currentRoom.width - 1);
		
		// TODO 下面暂时不判断斜坡
		
		if (!occ.ybottomTightly) {
			// 肯定不在地面上
			return box.bottomStop = false;
		}
		
		int ybottom = occ.ybottom;
		// 落地检测部分
		for (int x = xleft; x <= xright; x++) {
			int terrain = getTerrain(x, ybottom - 1);
			
			if (terrain == TERRAIN_SOLID) { // TODO 其它实体块
				return box.bottomStop = true;
			}
		}
		return box.bottomStop = false;
	}
	
	/**
	 * <p>判断一个物体的上侧是否碰到了地形块
	 * </p>
	 * @param box
	 *   物体实例
	 */
	public boolean topStop(Box box) {
		if (!box.inTerrain) {
			return box.topStop = false;
		}
		
		BoxOccupation occ = box.getOccupation();
		if (!occ.ytopTightly) {
			// 肯定上方没有碰边 (不考虑斜坡与其它地图块时)
			return box.topStop = false;
		}
		
		int xleft = occ.xleft;
		int xright = occ.xright;
		int ytop = occ.ytop + 1;
		
		// 落地检测部分
		for (int x = xleft; x <= xright; x++) {
			int terrain = getTerrain(x, ytop);
			
			if (terrain == TERRAIN_SOLID) { // TODO 其它实体块
				return box.topStop = true;
			}
		}
		return box.topStop = false;
	}
	
	/**
	 * <p>判断一个物体的左侧是否碰到了地形块
	 * </p>
	 * @param box
	 *   物体实例
	 */
	public boolean leftStop(Box box) {
		if (!box.inTerrain) {
			return box.leftStop = false;
		}
		
		BoxOccupation occ = box.getOccupation();
		if (!occ.xleftTightly) {
			return box.leftStop = false;
		}
		
		int ybottom = occ.ybottom;
		int ytop = occ.ytop;
		int xleft = occ.xleft - 1;
		
		for (int y = ybottom; y <= ytop; y++) {
			int terrain = getTerrain(xleft, y);
			
			if (terrain == TERRAIN_SOLID) { // TODO 其它实体块
				return box.leftStop = true;
			}
		}
		
		return box.leftStop = false;
	}
	
	/**
	 * <p>判断一个物体的右侧是否碰到了地形块
	 * </p>
	 * @param box
	 *   物体实例
	 */
	public boolean rightStop(Box box) {
		if (!box.inTerrain) {
			return box.rightStop = false;
		}
		
		BoxOccupation occ = box.getOccupation();
		if (!occ.xrightTightly) {
			return box.rightStop = false;
		}
		
		int ybottom = occ.ybottom;
		int ytop = occ.ytop;
		int xright = occ.xright + 1;
		
		for (int y = ybottom; y <= ytop; y++) {
			int terrain = getTerrain(xright, y);
			
			if (terrain == TERRAIN_SOLID) { // TODO 其它实体块
				return box.rightStop = true;
			}
		}
		
		return box.rightStop = false;
	}
	
	/**
	 * <p>执行左右移动.
	 * <p>根据 box 给出的位置和速度, 执行水平移动, 并将新的位置赋值到 box 中
	 * <p>TODO: 暂时不考虑斜坡、其它能动的刚体
	 * </p>
	 * @param box
	 *   物体实例
	 */
	public void execHorizontalMotion(Box box) {
		if (!box.inTerrain) {
			box.updateAnchorX(); // TODO 还缺屏幕两边的隐形的限制
			return;
		}
		
		int vx = box.velocityX;
		if (vx == 0) {
			return;
		}
		
		BoxOccupation occ = box.getOccupation();
		if (vx < 0) {
			int pxsleft = box.posX; // src 单位：p
			int xsleft = occ.xleft; // 单位：块
			int pxdleft = pxsleft + vx; // dest 单位：p
			int xdleft = blockRight(pxdleft); // 单位：块
			
			if (xsleft == xdleft) { // 向左移的过程中, 没有跨格子
				box.addAnchorX(vx);
				return;
			}
			
			// 出现了跨格子 (只判断跨一个格子的情况)
			int ytop = occ.ytop;
			int ybottom = occ.ybottom;
			
			for (int y = ybottom; y <= ytop; y++) {
				int terrain = getTerrain(xdleft, y);
				if (terrain == TERRAIN_SOLID) { // TODO 其它实体块
					// 最后向左移动的结果就是撞到该格子
					box.addAnchorX(xsleft * P_PER_BLOCK - pxsleft);
					return;
				}
			}
			
			box.addAnchorX(vx);
		} else { // vx > 0
			int pxsright = box.posX + box.posWidth; // src
			int xsright = occ.xright;
			int pxdright = pxsright + vx; // dest
			int xdright = blockLeft(pxdright);
			
			if (xsright == xdright) { // 向左移的过程中, 没有跨格子
				box.addAnchorX(vx);
				return;
			}
			
			// 出现了跨格子 (只判断跨一个格子的情况)
			int ytop = occ.ytop;
			int ybottom = occ.ybottom;
			
			for (int y = ybottom; y <= ytop; y++) {
				int terrain = getTerrain(xsright, y);
				
				if (terrain == TERRAIN_SOLID) { // TODO 其它实体块
					// 最后向左移动的结果就是撞到该格子
					box.addAnchorX(xsright * P_PER_BLOCK - pxsright);
					return;
				}
			}
			box.addAnchorX(vx);
		}
	}
	
	/**
	 * <p>执行上下移动.
	 * <p>根据 box 给出的位置和速度, 执行水平移动, 并将新的位置赋值到 box 中
	 * <p>如果能够攀爬的角色, 允许站在梯子的顶端.
	 * <p>TODO: 暂时不考虑斜坡、其它能动的刚体
	 * </p>
	 * @param box
	 *   物体实例
	 */
	public void execVerticalMotion(Box box) {
		if (!box.inTerrain) {
			box.updateAnchorY(); // TODO 还缺屏幕上下两边的隐形的限制 (或落坑判定)
			return;
		}
		
		int vy = box.velocityY;
		if (vy == 0) {
			return;
		}

		BoxOccupation occ = box.getOccupation();
		if (vy < 0) { // 向下
			int pysbottom = box.posY; // src
			int ysbottom = occ.ybottom;
			int fydbottom = box.posY + vy; // dest
			int ydbottom = blockRight(fydbottom);
			
			if (ysbottom == ydbottom) { // 向左移的过程中, 没有跨格子
				box.addAnchorY(vy);
				return;
			}
			
			// 出现了跨格子 (只判断跨一个格子的情况)
			int xleft = Math.max(occ.xleft, 0);
			int xright = Math.min(occ.xright, currentRoom.width - 1);
			
			for (int x = xleft; x <= xright; x++) {
				byte terrain = getTerrain(x, ydbottom);
				
				if (terrain == TERRAIN_SOLID) { // TODO 其它实体块
					// 最后向左移动的结果就是撞到该格子
					box.addAnchorY((ydbottom + 1) * P_PER_BLOCK - pysbottom);
					return;
				} else if (Terrains.isLadder(terrain)) {
					if (!box.climbable || !box.gravityDown) {
						continue;
					}
					// 只有顶端的梯子才有效
					if (!Terrains.isLadder(getTerrain(x, ydbottom + 1))) {
						box.addAnchorY((ydbottom + 1) * P_PER_BLOCK - pysbottom);
						return; // 可能有浮点精度问题
					}
				}
			}
			box.addAnchorY(vy);
		} else { // 向上
			int pystop = box.posY + box.posHeight; // src
			int ystop = occ.ytop;
			int pydtop = pystop + vy; // dest
			int ydtop = blockLeft(pydtop);
			
			if (ystop == ydtop) { // 向左移的过程中, 没有跨格子
				box.addAnchorY(vy);
				return;
			}
			
			// 出现了跨格子 (只判断跨一个格子的情况)
			int xleft = Math.max(occ.xleft, 0);
			int xright = Math.min(occ.xright, currentRoom.width - 1);
			
			for (int x = xleft; x <= xright; x++) {
				byte terrain = getTerrain(x, ystop); // 注意因为前面是 Math.ceil 所以用 ystop 而不是 fydtop
				
				if (terrain == TERRAIN_SOLID) { // TODO 其它实体块
					// 最后向上移动的结果就是撞到该格子
					box.addAnchorY(ystop * P_PER_BLOCK - pystop); // ystop = ydtop - 1
					return;
				} else if (Terrains.isLadder(terrain)) {
					if (!box.climbable || box.gravityDown) {
						continue;
					}
					// 只有底端的梯子才有效
					if (!Terrains.isLadder(getTerrain(x, ystop - 1))) {
						box.addAnchorY(ystop * P_PER_BLOCK - pystop);
						return; // 可能有浮点精度问题
					}
				}
			}
			box.addAnchorY(vy);
		}
	}
	
	/**
	 * 判断是否重合
	 */
//	private boolean isBoxOverlap(Rectangle rect) {
//		// 判断重合
//		float fxstart = rect.x;
//		float fxend = fxstart + rect.width;
//		float fystart = rect.y;
//		float fyend = fystart + rect.height;
//		int xstart = (int) Math.floor(fxstart);
//		int xend = (int) Math.ceil(fxend);
//		int ystart = (int) Math.floor(fystart);
//		int yend = (int) Math.ceil(fyend);
//		
//		for (int x = xstart; x < xend; x++) {
//			for (int y = ystart; y < yend; y++) {
//				int terrain = getTerrain(x, y);
//				
//				if (terrain == TERRAIN_SOLID) { // TODO 其它实体块
//					// 最后向左移动的结果就是撞到该格子
//					return true;
//				}
//			}
//		}
//		return false;
//	}
	
	
	/**
	 * <p>盒子的位置重合修正
	 * <p>由于一系列原因导致盒子与不能重合的物体重合了 (比如地形、不穿透刚体等).
	 * 造成该类原因有些是陷阱、不穿透怪物等自身运动导致和其它物体重合,
	 * 有些是浮点数的计算精度缺陷 (比如 16 计算成 15.999999)
	 * <p>修正的方式是将该盒子移出重合区. 本函数将尝试向上下左右四个方向将物体移出重合区,
	 * 但最大移动距离设置为 1 格 (以下). 如果盒子无法移出重合区, 将会返回 false,
	 * 让盒子的持有者自行决定如何处理.
	 * </p>
	 */
	public boolean correctOverlapBox(Box box) {
		if (!box.inTerrain) {
			return true; // 不用管
		}
		
		// 判断重合
		BoxOccupation occ = box.getOccupation();
		int xstart = occ.xleft;
		int xend = occ.xright;
		int ystart = occ.ybottom;
		int yend = occ.ytop;
		Array<TerrainParam> overlaps = new Array<>();
		
		for (int x = xstart; x < xend; x++) {
			for (int y = ystart; y < yend; y++) {
				int terrain = getTerrain(x, y);
				
				if (terrain == TERRAIN_SOLID) { // TODO 其它实体块
					// 最后向左移动的结果就是撞到该格子
					overlaps.add(new TerrainParam(x, y, terrain));
				}
			}
		}
		
		// TODO 除了地形以外的重合判定
		
		if (overlaps.size == 0) {
			return true;
		}
		
		// 修正部分
		
//		Rectangle rn = new Rectangle(rect);
//		// 如果采用左右上下移动, 将最少使用的偏移量
//		float ldelta, rdelta, udelta, ddelta;
//		float rightMax = Float.MIN_VALUE, leftMin = Float.MAX_VALUE,
//				topMax = Float.MIN_VALUE, bottomMin = Float.MAX_VALUE;
//		for (int i = 0; i < overlaps.size; i++) {
//			TerrainParam t = overlaps.get(i);
//			
//			if (t.terrain == TERRAIN_SOLID) { // TODO 其它实体块
//				rightMax = (t.x + 1 > rightMax) ? t.x + 1 : rightMax;
//				leftMin = (t.x < leftMin) ? t.x : leftMin;
//				topMax = (t.y + 1 > topMax) ? t.y + 1 : topMax;
//				bottomMin = (t.y < bottomMin) ? t.y : bottomMin;
//			}
//		}
//		ldelta = Math.abs(fxend - leftMin);
//		rdelta = Math.abs(fxstart - rightMax);
//		udelta = Math.abs(fyend - topMax);
//		ddelta = Math.abs(fystart - bottomMin);
//		// 按左右上下取最小值
//		boolean l = ldelta < 1, r = rdelta < 1, u = udelta < 1, d = ddelta < 1;
//		int loopCount = l ? 1 : 0;
//		loopCount = r ? loopCount + 1 : loopCount;
//		loopCount = u ? loopCount + 1 : loopCount;
//		loopCount = d ? loopCount + 1 : loopCount;
//		
//		float delta ; // 记录移动的数量
//		for (int i = 0; i < loopCount; i++) {
//			int choose = 0; // 1:左, 2:右, 3:上, 4:下
//			delta = 1;
//			
//			if (l && ldelta < delta) {
//				choose = 1;
//				delta = ldelta;
//			}
//			if (r && rdelta < delta) {
//				choose = 2;
//				delta = rdelta;
//			}
//			if (u && udelta < delta) {
//				choose = 3;
//				delta = udelta;
//			}
//			if (d && ddelta < delta) {
//				choose = 4;
//				delta = ddelta;
//			}
//			
//			// 执行
//			switch (choose) {
//			case 1:
//				rn.x = rect.x - delta;
//				l = false;
//				break;
//			case 2:
//				rn.x = rect.x + delta;
//				r = false;
//				break;
//			case 3:
//				rn.y = rect.y + delta;
//				u = false;
//				break;
//			case 4:
//				rn.y = rect.y - delta;
//				d = false;
//				break;
//			default:
//				return false;
//			}
//			
//			if (isBoxOverlap(rn)) {
//				continue;
//			}
//			
//			// 修正成功
//			switch (choose) {
//			case 1:
//				box.addAnchorX(-delta);
//				return true;
//			case 2:
//				box.addAnchorX(delta);
//				return true;
//			case 3:
//				box.addAnchorY(delta);
//				return true;
//			case 4:
//				box.addAnchorY(-delta);
//				return true;
//			}
//			
//		}
		
		return true;
	}
	
	/**
	 * <p>获得地形数据.
	 * <li>当 x 出界时, 返回 TERRAIN_SOLID
	 * <li>当 y 向上出界时, 返回 room.terrains[room.height - 1]
	 * <li>当 y 向下出界时, 返回 TERRAIN_EMPTY
	 * </li></p>
	 * @param x
	 *   相对于房间的横坐标, 单位: 格子
	 * @param y
	 *   相对于房间的纵坐标, 单位: 格子
	 * @return
	 *   地形码
	 */
	public byte getTerrain(int x, int y) {
		if (x < 0 || x >= currentRoom.width) {
			return TERRAIN_SOLID;
		}
		if (y >= currentRoom.height) {
			return currentRoom.terrains[x][currentRoom.height - 1];
		}
		if (y < 0) {
			return TERRAIN_EMPTY;
		}
		return currentRoom.terrains[x][y];
	}
	
	public byte getTerrainForP(int px, int py) {
		return getTerrain(Box.blockRight(px), Box.blockRight(py));
	}

	/* **********
	 * 房间切换 *
	 ********** */
	
	/**
	 * 房间切换是否允许. 如果像打 BOSS 这类限制房间切换的情况发生, 请设置为 false
	 */
	public boolean roomShiftEnable = true;
	
	/**
	 * <p>检查指定角色的盒子是否到达房间边缘, 要进行房间的切换.
	 * <p>切换的第一个判断是碰断角色是否碰到了房间的边缘,
	 * 而该方法仅判断位置是否满足, 此外仍然需要附加判定, 比如向上切房间需要爬墙等姿势,
	 * 这些在该方法均不判定.
	 * </p>
	 * @param box
	 *   指定角色的盒子, 一般是玩家控制角色的
	 * @return
	 */
	public Gate checkRoomShift(Box box) {
		if (!roomShiftEnable) {
			return null;
		}
		
		BoxOccupation p = box.getOccupation();
		
		// 向左
		LEFT: {
			if (p.xleft != 0) {
				break LEFT;
			}
			if (!p.xleftTightly) {
				break LEFT;
			}
			Gate g = null;
			for (int y = p.ybottom; y <= p.ytop; y++) {
				Gate g2 = findGate(currentRoom, Gate.DIRECTION_LEFT, y);
				if (g2 == null) { break LEFT; }
				if (g == null) { g = g2; }
				if (g != g2) { break LEFT; }
			}
			return g;
		}

		// 向右
		RIGHT: {
			if (p.xright != currentRoom.width - 1) { // 门的坐标是房间宽度 - 1
				break RIGHT;
			}
			if (!p.xrightTightly) {
				break RIGHT;
			}
			Gate g = null;
			for (int y = p.ybottom; y <= p.ytop; y++) {
				Gate g2 = findGate(currentRoom, Gate.DIRECTION_RIGHT, y);
				if (g2 == null) { break RIGHT; }
				if (g == null) { g = g2; }
				if (g != g2) { break RIGHT; }
			}
			return g;
		}
		
		// 向下
		BOTTOM: {
			if (p.ybottom >= 0) {
				break BOTTOM;
			}
			Gate g = null;
			for (int x = p.xleft; x <= p.xright; x++) {
				Gate g2 = findGate(currentRoom, Gate.DIRECTION_BOTTOM, x);
				if (g2 == null) { break BOTTOM; }
				if (g == null) { g = g2; }
				if (g != g2) { break BOTTOM; }
			}
			return g;
		}
		
		// 向上
		TOP: {
			if (p.ytop < currentRoom.height) {
				break TOP;
			}
			Gate g = null;
			for (int x = p.xleft; x <= p.xright; x++) {
				Gate g2 = findGate(currentRoom, Gate.DIRECTION_TOP, x);
				if (g2 == null) { break TOP; }
				if (g == null) { g = g2; }
				if (g != g2) { break TOP; }
			}
			return g;
		}
		
		return null;
	}
	
	/**
	 * 根据规则在指定房间中查找大门
	 * @param room
	 * @param direction
	 * @param xory
	 * @return
	 */
	public Gate findGate(Room room, byte direction, int xory) {
		Array<Gate> gates = room.gates;
		for (int i = 0; i < gates.size; i++) {
			Gate g = gates.get(i);
			
			if (g.direction != direction) {
				continue;
			}
			int[] exits = g.exits;
			for (int j = 0; j < exits.length; j++) {
				if (xory == exits[j]) {
					return g;
				}
			}
		}
		
		return null;
	}

}