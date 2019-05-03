package zdream.rockchronicle.platform.world;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

import zdream.rockchronicle.platform.body.Box;
import zdream.rockchronicle.platform.body.TerrainParam;
import zdream.rockchronicle.platform.region.ITerrainStatic;
import zdream.rockchronicle.platform.region.Room;

/**
 * 关卡, 含物理世界
 * @author Zdream
 */
public class LevelWorld implements ITerrainStatic {

	public LevelWorld() {
		terrainCollisionFilter = new Filter();
		terrainCollisionFilter.groupIndex = -1;
	}
	
	public World world;
	public Box2DDebugRenderer debugRenderer = new Box2DDebugRenderer();
	public Room currentRoom;
	
	public void doCreate() {
		// 世界重力向下
		world = new World(new Vector2(0, -1), true);
		this.pause = true;
	}
	
	/**
	 * 暂停
	 */
	public void doPause() {
		this.pause = true;
	}
	
	/**
	 * 从暂停中恢复
	 */
	public void doResume() {
		this.pause = false;
		this.frameTime = 0;
		this.accumulator = 0;
	}
	
	/**
	 * 是否在暂停中
	 */
	public boolean pause;
	
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
	 * 世界的更新频率为每秒 120 步
	 */
	public static final float TIME_STEP = 1/120f;
	
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
		if (this.pause) {
			this.frameTime = 0;
			if (stepCallBack != null) {
				stepCallBack.onStepFinished(this, true);
			}
			return;
		}
		
		this.frameTime = deltaTime = Math.min(deltaTime, 0.1f);
		
	    accumulator += deltaTime;
	    int index = 0;
	    while (accumulator >= TIME_STEP) {
	        accumulator -= TIME_STEP;
	        if (stepCallBack != null) {
	        	stepCallBack.step(this, index++, accumulator >= TIME_STEP);
	        }
	        world.step(TIME_STEP, 2, 2); // 世界在状态修改之后进行更新
	    }
		stepCallBack.onStepFinished(this, false);
	}
	
	public void setCurrentRoom(Room currentRoom) {
		this.currentRoom = currentRoom;
		buildTerrainBlocks();
	}
	
	/**
	 * 按照房间数据创建地形的方块
	 * @param room
	 *   现在所在的房间
	 */
	private void buildTerrainBlocks() {
		for (int x = 0; x < currentRoom.terrains.length; x++) {
			int[] ts = currentRoom.terrains[x];
			
			for (int y = 0; y < ts.length; y++) {
				int terrain = ts[y];
				
				switch (terrain) {
				case TERRAIN_STAB_BALL:
					buildTerrainBlock(x, y, terrain);
					break;

				default:
					// 跳过
					break;
				}
			}
		}
	}
	
	private Filter terrainCollisionFilter;
	
	private void buildTerrainBlock(int x, int y, int terrain) {
		BodyDef bodyDef = new BodyDef();
		bodyDef.type = BodyType.StaticBody;
		bodyDef.position.set(x, y); // 锚点位置: 左下角
		bodyDef.fixedRotation = true; // 不旋转
		
		Body body = world.createBody(bodyDef);
		body.setUserData(new TerrainParam(x, y, terrain));
		PolygonShape shape = new PolygonShape();
		shape.setAsBox(0.4f, 0.4f, new Vector2(0.5f, 0.5f), 0);
		
		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.shape = shape;
		fixtureDef.friction = 0f; // 摩擦因子
		Fixture fixture = body.createFixture(fixtureDef);
		
		shape.dispose();
		fixture.setFilterData(terrainCollisionFilter);
	}
	
	/**
	 * <p>判断一个物体是否在地上.
	 * 如果在地上, 在 box.grounds 设置踩在哪些地块上
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
			return false;
		}
		
		box.grounds.clear();
		Rectangle rect = box.getPosition();
		// 底边左端所在的格子 (x)
		int xleft = Math.max((int) Math.floor(rect.x), 0);
		int xright = Math.min((int) Math.floor(rect.x + rect.width), currentRoom.width - 1);
		float fybottom = rect.y;
		int ybottom = (int) Math.floor(fybottom);
		
		// 抬升部分
//		for (int x = xleft; x <= xright; x++) {
//			int terrain = getTerrain(x, ybottom);
//			
//			if (terrain == TERRAIN_SOLID) { // TODO 其它实体块
//				// 位置将抬升
//				int upTerrain = getTerrain(x, ybottom + 1);
//				if (upTerrain == TERRAIN_SOLID) {
//					// TODO 抬升的幅度超过 1 格, 可以判断死亡
//				} else {
//					// 位置抬升至上一格
//					position.setY(ybottom + 1);
//					serachGround(position, array);
//					return;
//				}
//			}
//		}
		
		// TODO 下面暂时不判断斜坡
		
		if (fybottom != ybottom) {
			// 肯定不在地面上
			return false;
		}
		
		// 落地检测部分
		for (int x = xleft; x <= xright; x++) {
			int terrain = getTerrain(x, ybottom - 1);
			
			if (terrain == TERRAIN_SOLID) { // TODO 其它实体块
				TerrainParam param = new TerrainParam(x, ybottom, terrain);
				box.grounds.add(param);
			}
		}
		return box.onTheGround();
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
		
		Rectangle rect = box.getPosition();
		// 底边左端所在的格子 (x)
		int xleft = Math.max((int) Math.floor(rect.x), 0);
		int xright = Math.min((int) Math.floor(rect.x + rect.width), currentRoom.width - 1);
		float fytop = rect.y + rect.height;
		int ytop = (int) Math.ceil(fytop);
		
		if (ytop != fytop) {
			// 肯定不在地面上
			return box.topStop = false;
		}
		
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
		
		Rectangle rect = box.getPosition();
		float fxleft = rect.x;
		int xleft = (int) Math.floor(fxleft);
		if (xleft != fxleft) {
			return box.leftStop = false;
		}
		
		float fytop = rect.y + rect.height;
		int ytop = (int) Math.floor(fytop);
		if (ytop == fytop) {
			ytop -= 1;
		}
		int ybottom = (int) Math.floor(rect.y);
		
		for (int y = ybottom; y <= ytop; y++) {
			int terrain = getTerrain(xleft - 1, y);
			
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
		
		Rectangle rect = box.getPosition();
		float fxright = rect.x + rect.width;
		int xright = (int) Math.ceil(fxright);
		if (xright != fxright) {
			return box.rightStop = false;
		}
		
		float fytop = rect.y + rect.height;
		int ytop = (int) Math.floor(fytop);
		if (ytop == fytop) {
			ytop -= 1;
		}
		int ybottom = (int) Math.floor(rect.y);
		
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
		
		float vx = box.velocity.x;
		if (vx == 0) {
			return;
		}
		
		Rectangle rect = box.getPosition();
		if (vx < 0) {
			float fxsleft = rect.x; // src
			int xsleft = (int) Math.floor(fxsleft);
			float fxdleft = fxsleft + vx; // dest
			int xdleft = (int) Math.floor(fxdleft);
			
			if (xsleft == xdleft) { // 向左移的过程中, 没有跨格子
				box.addAnchorX(vx);
				return;
			}
			
			// 出现了跨格子 (只判断跨一个格子的情况)
			float fytop = rect.y + rect.height;
			int ytop = (int) Math.floor(fytop);
			if (ytop == fytop) {
				ytop -= 1;
			}
			int ybottom = (int) Math.floor(rect.y);
			
			for (int y = ybottom; y <= ytop; y++) {
				int terrain = getTerrain(xdleft, y);
				
				if (terrain == TERRAIN_SOLID) { // TODO 其它实体块
					// 最后向左移动的结果就是撞到该格子
					box.addAnchorX(xsleft - fxsleft);
					return;
				}
			}
			box.addAnchorX(vx);
		} else { // vx > 0
			float fxsright = rect.x + rect.width; // src
			int xsright = (int) Math.ceil(fxsright);
			float fxdright = fxsright + vx; // dest
			int xdright = (int) Math.ceil(fxdright);
			
			if (xsright == xdright) { // 向左移的过程中, 没有跨格子
				box.addAnchorX(vx);
				return;
			}
			
			// 出现了跨格子 (只判断跨一个格子的情况)
			float fytop = rect.y + rect.height;
			int ytop = (int) Math.floor(fytop);
			if (ytop == fytop) {
				ytop -= 1;
			}
			int ybottom = (int) Math.floor(rect.y);
			
			for (int y = ybottom; y <= ytop; y++) {
				int terrain = getTerrain(xsright, y);
				
				if (terrain == TERRAIN_SOLID) { // TODO 其它实体块
					// 最后向左移动的结果就是撞到该格子
					box.addAnchorX(xsright - fxsright);
					return;
				}
			}
			box.addAnchorX(vx);
		}
	}
	
	/**
	 * <p>执行上下移动.
	 * <p>根据 box 给出的位置和速度, 执行水平移动, 并将新的位置赋值到 box 中
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
		
		float vy = box.velocity.y;
		if (vy == 0) {
			return;
		}
		
		Rectangle rect = box.getPosition();
		if (vy < 0) { // 向下
			float fysbottom = rect.y; // src
			int ysbottom = (int) Math.floor(fysbottom);
			float fydbottom = fysbottom + vy; // dest
			int ydbottom = (int) Math.floor(fydbottom);
			
			if (ysbottom == ydbottom) { // 向左移的过程中, 没有跨格子
				box.addAnchorY(vy);
				return;
			}
			
			// 出现了跨格子 (只判断跨一个格子的情况)
			int xleft = Math.max((int) Math.floor(rect.x), 0);
			int xright = Math.min((int) Math.floor(rect.x + rect.width), currentRoom.width - 1);
			
			for (int x = xleft; x <= xright; x++) {
				int terrain = getTerrain(x, ydbottom);
				
				if (terrain == TERRAIN_SOLID) { // TODO 其它实体块
					// 最后向左移动的结果就是撞到该格子
					box.addAnchorY(ydbottom + 1 - fysbottom);
					return;
				}
			}
			box.addAnchorY(vy);
		} else { // 向上
			float fystop = rect.y + rect.height; // src
			int ystop = (int) Math.ceil(fystop);
			float fydtop = fystop + vy; // dest
			int ydtop = (int) Math.ceil(fydtop);
			
			if (ystop == ydtop) { // 向左移的过程中, 没有跨格子
				box.addAnchorY(vy);
				return;
			}
			
			// 出现了跨格子 (只判断跨一个格子的情况)
			int xleft = Math.max((int) Math.floor(rect.x), 0);
			int xright = Math.min((int) Math.floor(rect.x + rect.width), currentRoom.width - 1);
			
			for (int x = xleft; x <= xright; x++) {
				int terrain = getTerrain(x, ystop); // 注意因为前面是 Math.ceil 所以用 ystop 而不是 fydtop
				
				if (terrain == TERRAIN_SOLID) { // TODO 其它实体块
					// 最后向左移动的结果就是撞到该格子
					box.addAnchorY(ystop - fystop);
					return;
				}
			}
			box.addAnchorY(vy);
		}
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
	public int getTerrain(int x, int y) {
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
	

}
