package zdream.rockchronicle.core.world;

import static zdream.rockchronicle.core.foe.Box.*;

import com.badlogic.gdx.utils.Array;

import zdream.rockchronicle.core.GameRuntime;
import zdream.rockchronicle.core.foe.Box;
import zdream.rockchronicle.core.foe.BoxOccupation;
import zdream.rockchronicle.core.region.ITerrainStatic;
import zdream.rockchronicle.core.region.Region;
import zdream.rockchronicle.core.region.RegionBuilder;
import zdream.rockchronicle.core.region.Room;

public class LevelWorld implements ITerrainStatic {
	
	private final GameRuntime runtime;
	
	public LevelWorld(GameRuntime runtime) {
		this.runtime = runtime;
	}
	
	public void init() {
		regionBuilder.init();
	}
	
	/* **********
	 *   地图   *
	 ********** */
	
	/**
	 * 现在正在显示的关卡 {@link Region}
	 */
	public Region curRegion;
	
	/**
	 * 现在显示的 {@link Room} 编号
	 */
	public int room;
	
	public final RegionBuilder regionBuilder = new RegionBuilder();

	/**
	 * 创建一个 {@link Region}, 设置为当前活动的地区, 并且当前房间为默认的初始房间
	 * @param name
	 *   这个初始的 {@link Region} 的名字
	 */
	public void setCurrentRegion(String name) {
		this.curRegion = regionBuilder.build(name);
		runtime.setCurrentRoom(curRegion.spawnRoom);
	}

	public Room getCurrentRoom() {
		return curRegion.rooms[room];
	}
	
	/**
	* 当前房间发生替换
	* @param room
	*/
	public void setCurrentRoom(int room) {
		this.room = room;
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
		Room currentRoom = curRegion.rooms[room];
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
		return getTerrain(blockRight(px), blockRight(py));
	}
	
	/* **********
	 *   盒子   *
	 ********** */
	
	/**
	 * <p>计算 Box 的数值. 适用于单一盒子. 不要将不受地形影响的 Foe 的盒子调用该方法.
	 * 
	 * <p>以下数值将重新计算:
	 * <li>四面是否碰到边: leftTouched, rightTouched, topTouched, bottomTouched
	 * <li>是否在空中: inAir
	 * </li></p>
	 * 
	 * @param climbable
	 *   这个角色会不会爬梯子
	 */
	public Box freshBox(Box box, boolean climbable) {
		box.flush();
		BoxOccupation occ = box.getOccupation();
		Room room = getCurrentRoom();
		
		// 四边, 单位: p
//		int pxLeft = box.posX;
//		int pxRight = pxLeft + box.posWidth;
//		int pyBottom = box.posY;
//		int pyTop = pyBottom + box.posHeight;
		
		// 盒子左右边所在的格子, 单位: 块
		int ibxLeft = Math.max(occ.xleft, 0);
		int ibxRight = Math.min(occ.xright, room.width - 1);
		// 盒子上下边所在的格子, 单位: 块
		int ibyBottom = Math.max(occ.ybottom, 0);
		int ibyTop = Math.min(occ.ytop, room.height - 1);
		
		// TODO 这里还是不考虑斜边
		
		// 左右
		box.leftTouched = false;
		box.rightTouched = false;
		if (occ.xleftTightly) {
			for (int y = ibyBottom; y <= ibyTop; y++) {
				int terrain = getTerrain(ibxLeft - 1, y);
				if (terrain == TERRAIN_SOLID) { // TODO 其它实体块
					box.leftTouched = true; break;
				}
			}
		}
		if (occ.xrightTightly) {
			for (int y = ibyBottom; y <= ibyTop; y++) {
				int terrain = getTerrain(ibxRight + 1, y);
				if (terrain == TERRAIN_SOLID) { // TODO 其它实体块
					box.rightTouched = true; break;
				}
			}
		}
		
		// 下上
		box.bottomTouched = false;
		box.topTouched = false;
		if (occ.ybottomTightly) {
			for (int x = ibxLeft; x <= ibxRight; x++) {
				int terrain = getTerrain(x, ibyBottom - 1);
				if (terrain == TERRAIN_SOLID) { // TODO 其它实体块
					box.bottomTouched = true; break;
				} else if (terrain == TERRAIN_STAB_LADDER) {
					// 梯子只有最上面那一块能站
					if (getTerrain(x, ibyBottom) != TERRAIN_STAB_LADDER && box.gravityDown && climbable) {
						box.bottomTouched = true; break;
					}
				}
			}
		}
		if (occ.ytopTightly) {
			for (int x = ibxLeft; x <= ibxRight; x++) {
				int terrain = getTerrain(x, ibyTop + 1);
				if (terrain == TERRAIN_SOLID) { // TODO 其它实体块
					box.topTouched = true; break;
				} else if (terrain == TERRAIN_STAB_LADDER) {
					// 梯子只有最上面那一块能站
					if (getTerrain(x, ibyTop) != TERRAIN_STAB_LADDER && !box.gravityDown && climbable) {
						box.topTouched = true; break;
					}
				}
			}
		}
		
		// 是否在空中
		box.inAir = !(box.bottomTouched && box.gravityDown || box.topTouched && !box.gravityDown);
		
		return box;
	}
	
	/**
	 * 处理 Foe 的移动, 按照盒子中的速度来更新盒子中的 anchor 位置.
	 * @param box
	 * @param inTerrain
	 *   是否受地形影响.
	 */
	public void submitMotion(Box box, boolean inTerrain) {
		box.flush();
		
		if (inTerrain) {
			int vx = box.velocityX;
			int vy = box.velocityY;
			BoxOccupation occ = box.getOccupation();
			
			// 水平移动
			if (vx < 0) {
				int pxsLeft = box.posX; // src 单位：p
				int bxsLeft = occ.xleft; // 单位：块
				int pxdLeft = pxsLeft + vx; // dest 单位：p
				int bxdLeft = blockRight(pxdLeft); // 单位：块
				
				if (bxsLeft != bxdLeft) { // 出现了跨格子 (只判断跨一个格子的情况)
					int byTop = occ.ytop;
					int byBottom = occ.ybottom;
					
					for (int y = byBottom; y <= byTop; y++) {
						int terrain = getTerrain(bxdLeft, y);
						if (terrain == TERRAIN_SOLID) { // TODO 其它实体块
							// 最后向左移动的结果就是撞到该格子
							vx = block2P(bxsLeft) - pxsLeft;
							break;
						}
					}
				} else { // 向左移的过程中, 没有跨格子
//					box.addAnchorX(vx);
				}
				
			} else if (vx > 0) {
				int pxsRight = box.posX + box.posWidth; // src
				int bxsRight = occ.xright;
				int pxdRight = pxsRight + vx; // dest
				int bxdRight = blockLeft(pxdRight);
				
				if (bxsRight != bxdRight) { // 出现了跨格子 (只判断跨一个格子的情况)
					int byTop = occ.ytop;
					int byBottom = occ.ybottom;
					
					for (int y = byBottom; y <= byTop; y++) {
						int terrain = getTerrain(bxdRight, y);
						
						if (terrain == TERRAIN_SOLID) { // TODO 其它实体块
							// 最后向左移动的结果就是撞到该格子
							vx = block2P(bxdRight) - pxsRight;
							break;
						}
					}
				}
				
			} // 不处理 vx == 0 的情况
			if (vx != 0) {
				box.addAnchorX(vx);
				box.flush();
			}
			
			if (vy < 0) {
				int pysBottom = box.posY; // src
				int bysBottom = occ.ybottom;
				int pydBottom = box.posY + vy; // dest
				int bydBottom = blockRight(pydBottom);
				
				if (bysBottom != bydBottom) { // 跨格子
					int bxLeft = Math.max(occ.xleft, 0);
					int bxRight = Math.min(occ.xright, getCurrentRoom().width - 1);
					
					for (int x = bxLeft; x <= bxRight; x++) {
						int terrain = getTerrain(x, bydBottom);
						
						if (terrain == TERRAIN_SOLID) { // TODO 其它实体块
							// 最后向左移动的结果就是撞到该格子
							vy = block2P(bysBottom) - pysBottom;
							break;
						}
					}
				}
			} else if (vy > 0) {
				int pysTop = box.posY + box.posHeight; // src
				int bysTop = occ.ytop;
				int pydTop = pysTop + vy; // dest
				int bydTop = blockLeft(pydTop);
				
				if (bysTop != bydTop) { // 跨格子
					int bxLeft = Math.max(occ.xleft, 0);
					int bxRight = Math.min(occ.xright, getCurrentRoom().width - 1);
					
					for (int x = bxLeft; x <= bxRight; x++) {
						int terrain = getTerrain(x, bydTop);
						
						if (terrain == TERRAIN_SOLID) { // TODO 其它实体块
							// 最后向左移动的结果就是撞到该格子
							vy = block2P(bydTop) - pysTop;
							break;
						}
					}
				}
			} // 不处理 vy == 0 的情况
			
			if (vy != 0) {
				box.addAnchorY(vy);
				box.flush();
			}
			
		} else {
			box.addAnchorX(box.velocityX);
			box.addAnchorY(box.velocityY);
			box.flush();
		}
	}
	
	/**
	 * 判断是否重合
	 * @return
	 *   重合了返回 false
	 */
	public boolean isBoxOverlap(int pxStart, int pyStart, int pWidth, int pHeight) {
		int pxEnd = pxStart + pWidth;
		int pyEnd = pyStart + pHeight;
		
		int bxStart = blockRight(pxStart);
		int bxEnd = blockRight(pxEnd);
		int byStart = blockRight(pyStart);
		int byEnd = blockRight(pyEnd);
		
		for (int x = bxStart; x < bxEnd; x++) {
			for (int y = byStart; y < byEnd; y++) {
				int terrain = getTerrain(x, y);
				
				if (terrain == TERRAIN_SOLID) { // TODO 其它实体块
					// 最后向左移动的结果就是撞到该格子
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * 判断是否在房间外. 贴线的返回 false
	 */
	public boolean isOutside(int pxStart, int pyStart, int pWidth, int pHeight) {
		// return x < r.x + r.width && x + width > r.x && y < r.y + r.height && y + height > r.y;
		Room room = getCurrentRoom();
		int pRoomWidth = block2P(room.width);
		int pRoomHeight = block2P(room.height);
		
		return 0 > pxStart + pWidth
				|| pRoomWidth < pxStart
				|| 0 > pyStart + pHeight
				|| pRoomHeight < pyStart;
	}
	
	/**
	 * <p>盒子的位置重合修正.
	 * <p>由于一系列原因导致盒子与不能重合的物体重合了 (比如地形、不穿透刚体等).
	 * 造成该类原因有些是陷阱、不穿透怪物等自身运动导致和其它物体重合,
	 * 有些是浮点数的计算精度缺陷 (比如 16 计算成 15.999999)
	 * <p>修正的方式是将该盒子移出重合区. 本函数将尝试向上下左右四个方向将物体移出重合区,
	 * 但最大移动距离设置为 1 格 (以下). 如果盒子无法移出重合区, 将会返回 false,
	 * 让盒子的持有者自行决定如何处理.
	 * </p>
	 * @return
	 *   当出现无法修复的情况, 返回 false
	 */
	public boolean glitchFix(Box box) {
		// 判断重合
		BoxOccupation occ = box.getOccupation();
		int bxstart = occ.xleft;
		int bxend = occ.xright;
		int bystart = occ.ybottom;
		int byend = occ.ytop;
		Array<TerrainParam> overlaps = new Array<>();
		
		for (int x = bxstart; x <= bxend; x++) {
			for (int y = bystart; y <= byend; y++) {
				byte terrain = getTerrain(x, y);
				
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
		// 如果采用左右上下移动, 将最少使用的偏移量. 下面四个参数是记录偏移量的. 单位: p
		// 四个偏移量超过 1 就不允许
		int plDelta, prDelta, puDelta, pdDelta;
		int rightMax = Integer.MIN_VALUE, leftMin = Integer.MAX_VALUE,
				topMax = Integer.MIN_VALUE, bottomMin = Integer.MAX_VALUE;
		for (int i = 0; i < overlaps.size; i++) {
			TerrainParam t = overlaps.get(i);
			if (t.terrain == TERRAIN_SOLID) { // TODO 其它实体块
				int ptLeft = block2P(t.bx);
				int ptRight = block2P(t.bx + 1);
				int ptTop = block2P(t.by + 1);
				int ptBottom = block2P(t.by);
				
				rightMax = (ptRight > rightMax) ? ptRight : rightMax;
				leftMin = (ptLeft < leftMin) ? ptLeft : leftMin;
				topMax = (ptTop > topMax) ? ptTop : topMax;
				bottomMin = (ptBottom < bottomMin) ? ptBottom : bottomMin;
			}
		}
		
		plDelta = Math.abs(box.posX + box.posWidth - leftMin); // 向左推
		prDelta = Math.abs(box.posX - rightMax); // 向右推
		puDelta = Math.abs(box.posY - topMax); // 向上推
		pdDelta = Math.abs(box.posY + box.posHeight - bottomMin); // 向下推
		// 按左右上下取最小值
		boolean l = plDelta < P_PER_BLOCK, r = prDelta < P_PER_BLOCK,
				u = puDelta < P_PER_BLOCK, d = pdDelta < P_PER_BLOCK;
		int loopCount = l ? 1 : 0;
		loopCount = r ? loopCount + 1 : loopCount;
		loopCount = u ? loopCount + 1 : loopCount;
		loopCount = d ? loopCount + 1 : loopCount;
		
		int delta ; // 记录移动的数量
		// 尝试移动之后
		int pxStart, pyStart;
		for (int i = 0; i < loopCount; i++) {
			int choose = 0; // 1:左, 2:右, 3:上, 4:下
			delta = P_PER_BLOCK;
			
			if (l && plDelta < delta) {
				choose = 1;
				delta = plDelta;
			}
			if (r && prDelta < delta) {
				choose = 2;
				delta = prDelta;
			}
			if (u && puDelta < delta) {
				choose = 3;
				delta = puDelta;
			}
			if (d && pdDelta < delta) {
				choose = 4;
				delta = pdDelta;
			}
			
			// 执行
			pxStart = box.posX;
			pyStart = box.posY;
			switch (choose) {
			case 1:
				pxStart = box.posX - delta;
				l = false;
				break;
			case 2:
				pxStart = box.posX + delta;
				r = false;
				break;
			case 3:
				pyStart = box.posY + delta;
				u = false;
				break;
			case 4:
				pyStart = box.posY - delta;
				d = false;
				break;
			default:
				return false;
			}
			
			if (isBoxOverlap(pxStart, pyStart, box.boxWidth, box.boxHeight)) {
				continue;
			}
			
			// 修正成功
			switch (choose) {
			case 1:
				box.addAnchorX(-delta);
				return true;
			case 2:
				box.addAnchorX(delta);
				return true;
			case 3:
				box.addAnchorY(delta);
				return true;
			case 4:
				box.addAnchorY(-delta);
				return true;
			}
		}
		
		return true;
	}
	
	class TerrainParam {
		int bx, by;
		byte terrain;
		public TerrainParam(int bx, int by, byte terrain) {
			this.bx = bx;
			this.by = by;
			this.terrain = terrain;
		}
	}

}
