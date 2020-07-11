package zdream.rockchronicle.core;

public class Config {
	
	public static final Config INSTANCE = new Config();
	private Config() {}
	
//	public void init(JsonValue v) {
//		blockWidth = v.getInt("blockWidth");
//		blockHeight = v.getInt("blockHeight");
//	}
	
	/**
	 * 在屏幕上 (没有屏幕调整时), 一个块的宽度为几个像素.
	 * 默认: 48 (等于洛克人 7 游戏中一个块的大小 x2, 等于 FC 洛克人游戏中一个块的大小 x3)
	 */
	public int blockWidth = 48;
	/**
	 * 在屏幕上 (没有屏幕调整时), 一个块的高度为几个像素.
	 * 默认: 48 (等于洛克人 7 游戏中一个块的大小 x2, 等于 FC 洛克人游戏中一个块的大小 x3)
	 */
	public int blockHeight = 48;

}
