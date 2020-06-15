package zdream.rockchronicle.core.world;

import static zdream.rockchronicle.core.foe.Box.P_PER_BLOCK;
import static zdream.rockchronicle.core.foe.Box.block2P;
import static zdream.rockchronicle.core.foe.Box.blockLeft;
import static zdream.rockchronicle.core.foe.Box.blockRight;
import static zdream.rockchronicle.core.region.Gate.DIRECTION_BOTTOM;
import static zdream.rockchronicle.core.region.Gate.DIRECTION_LEFT;
import static zdream.rockchronicle.core.region.Gate.DIRECTION_RIGHT;
import static zdream.rockchronicle.core.region.Gate.DIRECTION_TOP;
import static java.lang.Math.*;

import java.util.Iterator;
import java.util.function.Predicate;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.IntMap.Entry;
import com.badlogic.gdx.utils.IntSet;

import zdream.rockchronicle.core.GameRuntime;
import zdream.rockchronicle.core.foe.Box;
import zdream.rockchronicle.core.foe.BoxOccupation;
import zdream.rockchronicle.core.foe.Foe;
import zdream.rockchronicle.core.region.ConnectionProperties;
import zdream.rockchronicle.core.region.Gate;
import zdream.rockchronicle.core.region.ITerrainStatic;
import zdream.rockchronicle.core.region.Region;
import zdream.rockchronicle.core.region.RegionBuilder;
import zdream.rockchronicle.core.region.RegionPoint;
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
	 *    Foe   *
	 ********** */
	
	public Array<Foe> foes = new Array<>();
	public Array<Box> boxes = new Array<>();
	private final Array<Foe> foesWaitingForAdd = new Array<>();
	private final Array<Foe> foesWaitingForRemove = new Array<>();
	public Foe player1;
	
	public void tick(byte pause) {
		// 移动部分
		for (int i = 0; i < foes.size; i++) {
			try {
				foes.get(i).step(pause);
			} catch (RuntimeException e) {
				e.printStackTrace();
			}
			
			boxesUpdate();
		}
		
		// 进行删增工作
		handleFoeAddAndRemove();
		
		// 确定状态部分
		for (int i = 0; i < foes.size; i++) {
			try {
				foes.get(i).submit(pause);
			} catch (RuntimeException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * 用 id 来寻找角色.
	 * @param id
	 * @return
	 *   可能为 null
	 */
	public Foe findEntry(int id) {
		for (int i = 0; i < foes.size; i++) {
			Foe entry = foes.get(i);
			if (entry.id == id) {
				return entry;
			}
		}
		for (int i = 0; i < foesWaitingForAdd.size; i++) {
			Foe entry = foesWaitingForAdd.get(i);
			if (entry.id == id) {
				return entry;
			}
		}
		return null;
	}
	
	public void setPlayer1(Foe entry) {
		this.player1 = entry;
		addFoe(entry);
	}
	
	public void addFoe(Foe entry) {
		if (entry != null)
			foesWaitingForAdd.add(entry);
	}
	
	public void removeFoe(Foe entry) {
		if (entry != null)
			foesWaitingForRemove.add(entry);
	}
	
	public void destroyFoeNow(Foe entry) {
		entry.onDispose();
		foes.removeValue(entry, true);
	}
	
	private void handleFoeAddAndRemove() {
		foesWaitingForRemove.forEach((foe) -> foe.onDispose());
		foes.removeAll(foesWaitingForRemove, true);
		foesWaitingForRemove.clear();
		
		if (foesWaitingForAdd.size > 0) {
			Foe[] foeadds = foesWaitingForAdd.toArray(Foe.class);
			
			for (int i = 0; i < foeadds.length; i++) {
				Foe foeadd = foeadds[i];
				foeadd.init(runtime);
				foes.add(foeadd);
				foesWaitingForAdd.removeValue(foeadd, true);
			}
		}
	}
	
	/**
	 * 这个由 addBox 和在房间切换时, 原有的 box 添加时调用. 一般不直接调用该方法.
	 * @param box
	 */
	public void putBoxToZone(Box box) {
		// 将 Box 放入分区
		int[] array = boxZone(box);
		for (int x = array[0]; x <= array[1]; x++) {
			for (int y = array[2]; y <= array[3]; y++) {
				int zoneIndex = x + y * zoneWidth;
				zone[zoneIndex].add(box);
			}
		}
		boxBelong.put(box.id, array);
	}

	public void addBox(Box box) {
		boxes.add(box);
		putBoxToZone(box);
	}

	public void removeBox(Box box) {
		boxes.removeValue(box, true);
		updatedBox.remove(box.id);
		
		// Box 移出分区
		int[] array = boxBelong.remove(box.id);
		for (int x = array[0]; x <= array[1]; x++) {
			for (int y = array[2]; y <= array[3]; y++) {
				int zoneIndex = x + y * zoneWidth;
				zone[zoneIndex].removeValue(box, true);
			}
		}
	}
	
	public void roomShiftingStarted() {
		foesWaitingForAdd.clear();
	}
	
	public void roomShiftingFinished() {
		// do-nothing
	}
	
	/*
	 * Box 分区部分
	 * 
	 * 一般每 5x5 的区域设置一个分区, 房间外也算.
	 * 例如, 标准房间大小 25x14, 横坐标分区:
	 * <0, [0-5), [5-10), [10-15), [15-20), [20-25), >=25, 共 7 个段;
	 * 纵坐标分区:
	 * <0, [0-5), [5-10), >=10, 共 4 个段;
	 * 
	 * 因此, 整个房间分成 7x4=28 个分区.
	 * 第一个分区: zone[0] { x < 0, y < 0 };
	 * 第二个分区: zone[1] { x 属于 [0, 5) , y < 0 };
	 * ...
	 * 第七个分区: zone[6] { x >= 25 , y < 0 };
	 * 第八个分区: zone[7] { x < 0 , y 属于 [0, 5) };
	 * ...
	 */
	private IntMap<Box> updatedBox = new IntMap<>();
	private int zoneWidth, zoneHeight;
	private Array<Box>[] zone;
	/**
	 * int[4], 分别为 zoneXStart, zoneXEnd, zoneYStart, zoneYEnd
	 */
	private IntMap<int[]> boxBelong = new IntMap<>();

	/**
	 * 这是个 debug 方法
	 * @return
	 */
	public int zoneSum() {
		if (zone == null) {
			return -1;
		}
		int sum = 0;
		for (int i = 0; i < zone.length; i++) {
			sum += zone[i].size;
		}
		return sum;
	}
	
	/**
	 * 计算一个 box 所属的分区
	 * @return
	 */
	private int[] boxZone(Box box) {
		return hasBoxZoneUpdated(box, null);
	}
	
	/**
	 * 如果计算出来, box 的分区没有变化, 返回 null.
	 * @param box
	 */
	private int[] hasBoxZoneUpdated(Box box, int[] oldArray) {
		BoxOccupation occ = box.getOccupation();
//		IntArray array = new IntArray();
		int maxZoneX = zoneWidth - 1;
		int maxZoneY = zoneHeight - 1;
		int zxStart = (occ.xleft < 0) ? 0 : min(occ.xleft / 5 + 1, maxZoneX);
		int zxEnd = (occ.xright < 0) ? 0 : min(occ.xright / 5 + 1, maxZoneX);
		int zyStart = (occ.ybottom < 0) ? 0 : min(occ.ybottom / 5 + 1, maxZoneY);
		int zyEnd = (occ.ytop < 0) ? 0 : min(occ.ytop / 5 + 1, maxZoneY);
		
		if (oldArray == null || oldArray[0] != zxStart || oldArray[1] != zxEnd
				|| oldArray[2] != zyStart || oldArray[3] != zyEnd) {
			return new int[] { zxStart, zxEnd, zyStart, zyEnd };
		} else {
			return null;
		}
	}
	
	private int[] zoneArray(int px, int py, int pWidth, int pHeight) {
		int maxZoneX = zoneWidth - 1;
		int maxZoneY = zoneHeight - 1;
		
		int bxStart = blockRight(px);
		int bxEnd = blockLeft(px + pWidth);
		int byStart = blockRight(py);
		int byEnd = blockLeft(py + pHeight);
		
		int zxStart = (bxStart < 0) ? 0 : min(bxStart / 5 + 1, maxZoneX);
		int zxEnd = (bxEnd < 0) ? 0 : min(bxEnd / 5 + 1, maxZoneX);
		int zyStart = (byStart < 0) ? 0 : min(byStart / 5 + 1, maxZoneY);
		int zyEnd = (byEnd < 0) ? 0 : min(byEnd / 5 + 1, maxZoneY);
		return new int[] { zxStart, zxEnd, zyStart, zyEnd };
	}
	
	public void notifyBoxUpdated(Box box) {
		updatedBox.put(box.id, box);
	}
	
	private void boxesUpdate() {
		if (updatedBox.size == 0) {
			return;
		}
		
		for (Iterator<Entry<Box>> it = updatedBox.iterator(); it.hasNext();) {
			Entry<Box> entry = it.next();
			entry.value.flush();
			
			// TODO 重新放置 box 的分区
			Box box = entry.value;
			int[] array = boxBelong.get(box.id);
			int[] newArray = hasBoxZoneUpdated(box, array);
			if (newArray != null) {
				// 分区发生变化
				// 去掉原来的分区
				for (int x = array[0]; x <= array[1]; x++) {
					for (int y = array[2]; y <= array[3]; y++) {
						int zoneIndex = x + y * zoneWidth;
						zone[zoneIndex].removeValue(box, true);
					}
				}
				
				// 补充新分区
				for (int x = newArray[0]; x <= newArray[1]; x++) {
					for (int y = newArray[2]; y <= newArray[3]; y++) {
						int zoneIndex = x + y * zoneWidth;
						zone[zoneIndex].add(box);
					}
				}
				array[0] = newArray[0];
				array[1] = newArray[1];
				array[2] = newArray[2];
				array[3] = newArray[3];
			}
		}
		updatedBox.clear();
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
		onRoomChanged();
	}
	
	/**
	 * <p>获得地形数据.
	 * <li>当 x 出界时, 返回 TERRAIN_SOLID
	 * <li>当 y 向上出界时, 返回 room.terrains[room.height - 1]
	 * <li>当 y 向下出界时, 返回 TERRAIN_EMPTY
	 * </li></p>
	 * @param x
	 *   相对于房间的横坐标, 单位: 块
	 * @param y
	 *   相对于房间的纵坐标, 单位: 块
	 * @return
	 *   地形码
	 */
	public byte getTerrain(int x, int y) {
		return getTerrain(x, y, TERRAIN_SOLID);
	}
	
	/**
	 * <p>获得地形数据.
	 * <li>当 x 出界时, 返回 xOutsideTerrain
	 * <li>当 y 向上出界时, 返回 room.terrains[room.height - 1]
	 * <li>当 y 向下出界时, 返回 TERRAIN_EMPTY
	 * </li></p>
	 * @param x
	 *   相对于房间的横坐标, 单位: 块
	 * @param y
	 *   相对于房间的纵坐标, 单位: 块
	 * @param xOutsideTerrain
	 *   横坐标出界后返回的地形
	 * @return
	 *   地形码
	 */
	public byte getTerrain(int x, int y, byte xOutsideTerrain) {
		Room currentRoom = curRegion.rooms[room];
		if (x < 0 || x >= currentRoom.width) {
			return xOutsideTerrain;
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
	 * @see #freshBox(Box, boolean, byte)
	 */
	public Box freshBox(Box box, boolean climbable) {
		return freshBox(box, climbable, TERRAIN_SOLID);
	}
	
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
	 * @param xOutsideTerrain
	 *   横坐标出界后返回的地形
	 */
	public Box freshBox(Box box, boolean climbable, byte xOutsideTerrain) {
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
				int terrain = getTerrain(ibxLeft - 1, y, xOutsideTerrain);
				if (terrain == TERRAIN_SOLID) { // TODO 其它实体块
					box.leftTouched = true; break;
				}
			}
		}
		if (occ.xrightTightly) {
			for (int y = ibyBottom; y <= ibyTop; y++) {
				int terrain = getTerrain(ibxRight + 1, y, xOutsideTerrain);
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
				int terrain = getTerrain(x, ibyBottom - 1, xOutsideTerrain);
				if (terrain == TERRAIN_SOLID) { // TODO 其它实体块
					box.bottomTouched = true; break;
				} else if (terrain == TERRAIN_STAB_LADDER) {
					// 梯子只有最上面那一块能站
					if (getTerrain(x, ibyBottom, xOutsideTerrain) != TERRAIN_STAB_LADDER
							&& box.gravityDown && climbable) {
						box.bottomTouched = true; break;
					}
				}
			}
		}
		if (occ.ytopTightly) {
			for (int x = ibxLeft; x <= ibxRight; x++) {
				int terrain = getTerrain(x, ibyTop + 1, xOutsideTerrain);
				if (terrain == TERRAIN_SOLID) { // TODO 其它实体块
					box.topTouched = true; break;
				} else if (terrain == TERRAIN_STAB_LADDER) {
					// 梯子只有最上面那一块能站
					if (getTerrain(x, ibyTop, xOutsideTerrain) != TERRAIN_STAB_LADDER && !box.gravityDown && climbable) {
						box.topTouched = true; break;
					}
				}
			}
		}
		
		// 是否在空中
		box.inAir = !(box.bottomTouched && box.gravityDown || box.topTouched && !box.gravityDown);
		
		return box;
	}
	
	public void submitFloatBoxMotion(Box box) {
		box.flush();
		box.addAnchorX(box.velocityX);
		box.addAnchorY(box.velocityY);
		box.flush();
	}
	
	/**
	 * @see #submitMotion(Box, boolean, byte)
	 */
	public void submitMotion(Box box, boolean climbable) {
		submitMotion(box, climbable, TERRAIN_SOLID);
	}
	
	/**
	 * 处理 Foe 的移动, 按照盒子中的速度来更新盒子中的 anchor 位置.
	 * @param box
	 * @param climbable
	 *   角色会不会爬梯子
	 * @param xOutsideTerrain
	 *   横坐标出界后返回的地形. 默认是固体
	 */
	public void submitMotion(Box box, boolean climbable, byte xOutsideTerrain) {
		box.flush();
		
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
					int terrain = getTerrain(bxdLeft, y, xOutsideTerrain);
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
					int terrain = getTerrain(bxdRight, y, xOutsideTerrain);
					
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
					int terrain = getTerrain(x, bydBottom, xOutsideTerrain);
					
					boolean barrier = terrain == TERRAIN_SOLID; // 是否被格子拦住
					if (!barrier && climbable && terrain == TERRAIN_STAB_LADDER && box.gravityDown) {
						// 只有顶端的梯子有效
						barrier = getTerrain(x, bydBottom + 1, xOutsideTerrain) != TERRAIN_STAB_LADDER;
					}
					
					if (barrier) {
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
					int terrain = getTerrain(x, bydTop, xOutsideTerrain);
					
					boolean barrier = terrain == TERRAIN_SOLID; // 是否被格子拦住
					if (!barrier && climbable && terrain == TERRAIN_STAB_LADDER && !box.gravityDown) {
						// 只有顶端的梯子有效
						barrier = getTerrain(x, bydTop - 1, xOutsideTerrain) != TERRAIN_STAB_LADDER;
					}
					
					if (barrier) {
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
			
	}
	
	public boolean isBoxOverlap(Box box) {
		return isBoxOverlap(box.posX, box.posY, box.posWidth, box.posHeight);
	}
	
	/**
	 * 判断是否和地形或地形方块 Foe 重合
	 * @return
	 *   重合了返回 false
	 */
	public boolean isBoxOverlap(int pxStart, int pyStart, int pWidth, int pHeight) {
		int pxEnd = pxStart + pWidth;
		int pyEnd = pyStart + pHeight;
		
		int bxStart = blockRight(pxStart);
		int bxEnd = blockLeft(pxEnd);
		int byStart = blockRight(pyStart);
		int byEnd = blockLeft(pyEnd);
		
		for (int x = bxStart; x <= bxEnd; x++) {
			for (int y = byStart; y <= byEnd; y++) {
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
	
	@SuppressWarnings("unchecked")
	public void onRoomChanged() {
		Room room = getCurrentRoom();
		
		// 如果该房间有连接到其它区域, 需要初始化相邻的区域
		// 扫描点
		Array<RegionPoint> ps = curRegion.points;
		for (int i = 0; i < ps.size; i++) {
			RegionPoint p = ps.get(i);
			if (!room.contain(p.x, p.y)) {
				continue;
			}
			if (p.conn != null) {
				ConnectionProperties conn = p.conn;
				Region destRegion = regionBuilder.build(conn.destRegionName);
				RegionPoint point = destRegion.findPoint(conn.destPoint);
				if (point == null) {
					continue; // 找不到这个点
				}
				Room destRoom = destRegion.of(point.x, point.y);
				if (destRoom == null) {
					continue; // 这个点不属于任何房间
				}
				regionBuilder.createGate(room, destRoom, p, point);
			}
		}
		
		// 新部分, 扫描房间大小, 建立 Box 分区
		// 房间大小, 单位: 块
		// 房间切换后, onRoomUpdated 最先调用, 随后是 addFoe.
		zoneWidth = room.width / 5 + 2;
		zoneHeight = room.height / 5 + 2;
		final int zoneLen = zoneWidth * zoneHeight;
		Array<Box>[] oldZone = zone;
		if (oldZone == null) {
			zone = new Array[zoneLen];
			for (int i = 0; i < zone.length; i++) {
				zone[i] = new Array<>();
			}
		} else {
			final int copyedLen = Math.min(oldZone.length, zoneLen);
			zone = new Array[zoneLen];
			for (int i = 0; i < copyedLen; i++) {
				zone[i] = oldZone[i];
				zone[i].clear();
			}
			for (int i = copyedLen; i < zoneLen; i++) {
				zone[i] = new Array<>();
			}
		}
		boxBelong.clear();
	}
	
	/**
	 * 查看与指定的盒子发生碰撞的盒子, 并逐个进行判断.
	 * @param test
	 *   对碰撞的盒子进行判断. 返回 true 则继续判断后面的盒子, 否则停止
	 * @param touched
	 *   如果为 true, 贴边的也算
	 */
	public void overlaps(int px, int py, int pWidth, int pHeight,
			Predicate<Box> test, boolean touched, IntSet ignoreIds) {
		int pxLeft = px;
		int pxRight = px + pWidth;
		int pyBottom = py;
		int pyTop = py + pHeight;
		
		if (boxes.size < 20) {
			// 当判断的盒子只有不到 20 个, 原来的判断方法就适用.
			for (int i = 0; i < boxes.size; i++) {
				Box other = boxes.get(i);
				if (ignoreIds == null || !ignoreIds.contains(other.id)) {
					int oxleft = other.posX;
					int oxright = oxleft + other.posWidth;
					int oybottom = other.posY;
					int oytop = oybottom + other.posHeight;
					
					boolean b = (touched) ?
							(oxleft <= pxRight &&
							oxright >= pxLeft &&
							oybottom <= pyTop &&
							oytop >= pyBottom) :
							(oxleft < pxRight &&
							oxright > pxLeft &&
							oybottom < pyTop &&
							oytop > pyBottom); // rectagle.overlaps(otherRect)
					if (!b) {
						continue;
					}
					if (!test.test(other)) {
						return;
					}
				}
			}
		} else {
			// 分区方法
			IntMap<Box> boxes = new IntMap<>();
			
			int[] array = zoneArray(pxLeft, pyBottom, pWidth, pHeight);
			for (int x = array[0]; x <= array[1]; x++) {
				for (int y = array[2]; y <= array[3]; y++) {
					int zoneIndex = x + y * zoneWidth;
					Array<Box> bs = zone[zoneIndex];
					for (int i = 0; i < bs.size; i++) {
						Box box = bs.get(i);
						boxes.put(box.id, box);
					}
				}
			}
			
			for (Iterator<Entry<Box>> it = boxes.iterator(); it.hasNext();) {
				Entry<Box> entry = it.next();
				Box other = entry.value;
				if (ignoreIds == null || !ignoreIds.contains(other.id)) {
					int oxleft = other.posX;
					int oxright = oxleft + other.posWidth;
					int oybottom = other.posY;
					int oytop = oybottom + other.posHeight;
					
					boolean b = (touched) ?
							(oxleft <= pxRight &&
							oxright >= pxLeft &&
							oybottom <= pyTop &&
							oytop >= pyBottom) :
							(oxleft < pxRight &&
							oxright > pxLeft &&
							oybottom < pyTop &&
							oytop > pyBottom); // rectagle.overlaps(otherRect)
					if (!b) {
						continue;
					}
					if (!test.test(other)) {
						return;
					}
				}
			}
		}
		
	}
	
	/**
	 * 查看与指定的盒子发生碰撞的盒子, 并逐个进行判断.
	 * @param box
	 *   指定的碰撞盒子
	 * @param test
	 *   对碰撞的盒子进行判断. 返回 true 则继续判断后面的盒子, 否则停止
	 * @param touched
	 *   如果为 true, 贴边的也算
	 */
	public void overlaps(Box box, Predicate<Box> test, boolean touched) {
		this.overlaps(box.posX, box.posY, box.posWidth, box.posHeight,
				test, touched, IntSet.with(box.id));
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
	public Gate checkGateTouched(Box box) {
		if (!roomShiftEnable) {
			return null;
		}
		
		BoxOccupation p = box.getOccupation();
		Room currentRoom = getCurrentRoom();
		
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
	
	/**
	 * <p>切换房间的额外判定
	 * <p>已经满足有大门的条件了, 还需要判定:
	 * <p>如果顺着重力及其它环境力的合力方向，碰到边缘就可以切房间；
	 * 如果逆着合力方向，就需要额外判定，比如需要在攀爬状态、踩在指定的角色上等
	 * </p>
	 * @param entry
	 * @param g
	 * @return
	 */
	public boolean checkShift(Foe entry, Gate g) {
		if (g.direction == DIRECTION_LEFT || g.direction == DIRECTION_RIGHT) {
			return true;
		}
		
		Box box = entry.getBoxes()[0];
		boolean gravityDown = box.gravityDown && box.gravityScale > 0
				|| !box.gravityDown && box.gravityScale < 0;
		
		if (gravityDown && g.direction == DIRECTION_BOTTOM
				|| !gravityDown && g.direction == DIRECTION_TOP) {
			// 顺着重力（合力）往下掉的
			return true;
		}
		
		// TODO 这里判断比如需要在攀爬状态、踩在指定的角色上等
		if (entry.getBoolean("climbing", false) || !box.inAir) {
			return true;
		}
		
		return false;
	}

}
