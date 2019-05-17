package zdream.rockchronicle.platform.world;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import zdream.rockchronicle.core.character.CharacterEntry;

/**
 * <p>房间切换数据
 * </p>
 * 
 * @author Zdream
 * @since v0.0.1
 * @date
 *   2019-05-17 (created)
 *   2019-05-17 (last modified)
 */
public class RoomShiftParam {
	
	/**
	 * 起点房间, 终点房间
	 */
	public int srcRoom, destRoom;
	
	/**
	 * 暂存, 哪些角色将不需要删除. 一般是控制角色
	 */
	public Array<CharacterEntry> entries = new Array<>();
	
	/**
	 * 当前位置, 参照点为左下角顶点, 不是中心点
	 */
	public Vector2 currentPos = new Vector2();
	
	/**
	 * 阶段一结束后镜头的位置, 参照点为左下角顶点, 不是中心点
	 */
	public Vector2 phase1Pos = new Vector2();
	
	/**
	 * 阶段二结束后镜头的位置, 参照点为左下角顶点, 不是中心点
	 */
	public Vector2 phase2Pos = new Vector2();
	
	/* **********
	 * 时间状态 *
	 ********** */
	
	/**
	 * 现在是第几个阶段. 开始是 0, 表示还未启动移屏
	 */
	public int phase;
	
	
}
