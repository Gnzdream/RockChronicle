package zdream.rockchronicle.core;

import com.badlogic.gdx.utils.JsonValue;

public class Config {
	
	public static final Config INSTANCE = new Config();
	private Config() {}
	
	public void init(JsonValue v) {
		blockWidth = v.getInt("blockWidth");
		blockHeight = v.getInt("blockHeight");
	}
	
	/**
	 * 在屏幕上 (没有屏幕调整时), 一个块的宽度为几个像素.
	 * 默认: 24 (等于洛克人 7 游戏中一个块的大小)
	 */
	public int blockWidth;
	/**
	 * 在屏幕上 (没有屏幕调整时), 一个块的高度为几个像素.
	 * 默认: 24 (等于洛克人 7 游戏中一个块的大小)
	 */
	public int blockHeight;

}
