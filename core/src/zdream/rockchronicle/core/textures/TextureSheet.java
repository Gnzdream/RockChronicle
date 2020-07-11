package zdream.rockchronicle.core.textures;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.ObjectMap;

import zdream.rockchronicle.RockChronicle;

public class TextureSheet {
	
	public ObjectMap<String, TextureSheetEntry> entrys;
	public ObjectMap<String, TextureSequence> sequences;
	
	public Texture texture;
	public String defaultState = "normal";
	
	public int blockWidth = 24, blockHeight = 24;
	
	/**
	 * @param path
	 *   到 .json 文件的路径
	 * @return
	 */
	public static TextureSheet getSheet(String path) {
		// 在缓存中寻找
		return RockChronicle.INSTANCE.assets.getSheet(path);
	}
	
	public TextureSheet() {
		entrys = new ObjectMap<>();
		sequences = new ObjectMap<>();
	}
	
}
