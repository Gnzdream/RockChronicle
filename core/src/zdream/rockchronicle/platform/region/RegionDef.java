package zdream.rockchronicle.platform.region;

import static java.util.Objects.requireNonNull;

/**
 * 一个 RegionDef 是一个 region 的 json 数据的替代物
 * 
 * @author Zdream
 * @since v0.1
 */
public class RegionDef {
	
	public RegionDef(String basePath) {
		requireNonNull(basePath, "basePath == null");
		this.basePath = basePath;
	}
	
	/**
	 * 该 region 所在的路径.
	 */
	public final String basePath;
	
	/**
	 * 属性 tmxPath.
	 * 以 {@link #basePath} 作为基础路径, basePath + tmxPath 获得 tmx 文件的路径.
	 */
	public String tmxPath;
	
}
