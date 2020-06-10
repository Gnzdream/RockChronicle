package zdream.rockchronicle.core.world;

import static zdream.rockchronicle.core.foe.Box.*;

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
		
		
		// 看看是否 glitch - debug
		
		
		
		
	}

}
