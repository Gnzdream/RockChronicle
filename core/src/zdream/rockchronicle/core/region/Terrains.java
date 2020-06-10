package zdream.rockchronicle.core.region;

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
			
		case TERRAIN_STRING_STAB_BALL:
			return TERRAIN_STAB_BALL;
		case TERRAIN_STRING_STAB_UP:
			return TERRAIN_STAB_UP;
			
		case TERRAIN_STRING_LADDER:
			return TERRAIN_STAB_LADDER;

		default:
			return TERRAIN_EMPTY;
		}
	}
	
	/**
	 * 地形是否为空, 指可以让角色穿过的地形, 但不包括刺;
	 * 允许的有: 空、楼梯
	 */
	public static boolean isEmpty(byte terrain) {
		return terrain == TERRAIN_EMPTY || terrain == TERRAIN_STAB_LADDER;
	}
	
	public static boolean isLadder(byte terrain) {
		return terrain == TERRAIN_STAB_LADDER;
	}

}
