package zdream.rockchronicle.core.region;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.JsonValue;

/**
 * <p>在 TMX 文件中对场的描述
 * </p>
 * 
 * @author Zdream
 * @since v0.0.1
 * @date
 *   2019-05-12 (create)
 *   2019-05-12 (last modified)
 */
public class FieldDef {

	/**
	 * <p>所属房间.
	 * <p>如果多个房间拥有这个场, 每个房间保留一个场的实例, 而且大多数情况它们不同.
	 * <p>注意, 如果尚不清楚该场属于哪个房间, 这里的 room = -1
	 * </p>
	 */
	public int room = -1;
	
	public String name;
	
	/**
	 * 在该房间中, 场的范围.
	 * 如果尚不清楚该场属于哪个房间, 这里为在所属区域 {@link Region} 中, 该场的范围.
	 */
	public final Rectangle rect = new Rectangle();
	
	/**
	 * 房间其它必要参数, 用于在游戏内生成场的实例的
	 */
	public JsonValue param;

}
