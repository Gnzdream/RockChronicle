package zdream.rockchronicle.platform.region;

public interface ITerrainStatic {
	
	public static final byte
		TERRAIN_EMPTY = 0,
		TERRAIN_SOLID = 1,
		TERRAIN_STAB_BALL = 17,
		TERRAIN_STAB_UP = 18,
		TERRAIN_STAB_LADDER = 33;
	
	public static final String
		TERRAIN_STRING_SOLID = "solid",
		TERRAIN_STRING_STAB_BALL = "stabBall",
		TERRAIN_STRING_STAB_UP = "stabUp",
		TERRAIN_STRING_LADDER = "ladder";

}
