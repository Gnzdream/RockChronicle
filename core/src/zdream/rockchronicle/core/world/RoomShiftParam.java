package zdream.rockchronicle.core.world;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import zdream.rockchronicle.core.foe.Foe;
import zdream.rockchronicle.core.region.Gate;

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
	
	public RoomShiftParam(Gate gate) {
		this.gate = gate;
	}

	/**
	 * 起点房间, 终点房间连在一起的大门
	 */
	public final Gate gate;
	
	/**
	 * 暂存, 哪些角色将不需要删除. 一般是控制角色
	 */
	public Array<Foe> entries = new Array<>();
	
	/**
	 * 暂存, 角色传送的目标位置. 单位: p, 以原房间为参考系
	 */
	public int[][] entriesPos;
	
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
	
	/**
	 * 阶段二镜头平移总距离. 正数
	 */
	public float phase2CameraWidth;
	
	/**
	 * 阶段二角色平移总距离, 单位: p
	 */
	public int[] phase2EntryWidth;
	
	/* **********
	 * 时间状态 *
	 ********** */
	
	/**
	 * 现在是第几个阶段. 开始是 0, 表示还未启动移屏
	 */
	public int phase;
	
	
}
