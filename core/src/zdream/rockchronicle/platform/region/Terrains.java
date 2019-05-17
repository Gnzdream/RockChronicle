package zdream.rockchronicle.platform.region;

/**
 * 地形的通用方法类
 * 
 * @author Zdream
 * @date 2019-04-29
 */
public class Terrains implements ITerrainStatic {
	
	/**
	 * 地形的文字转成码
	 */
	public static byte terrainCode(String s) {
		switch (s) {
		case TERRAIN_STRING_SOLID:
			return TERRAIN_SOLID;

		default:
			return TERRAIN_EMPTY;
		}
	}
	
	public static boolean isEmpty(byte terrain) {
		return terrain == TERRAIN_EMPTY;
	}

}
