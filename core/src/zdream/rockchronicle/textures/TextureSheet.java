package zdream.rockchronicle.textures;

import java.io.File;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectMap;

public class TextureSheet {
	
	ObjectMap<String, TextureSheetEntry> entrys;
	
	Texture texture;
	
	/*
	 * 这里是 Json 文件里面的数据
	 */
	/**
	 * 所说明的 texture 的图片文件相对于该 json 文件的位置
	 */
	String imagePath;
	
	public static TextureSheet createSheet(FileHandle file) {
		JsonReader r = new JsonReader();
		JsonValue j = r.parse(file);
		
		TextureSheet sheet = new TextureSheet(file, j);
		
		return sheet;
	}
	
	public TextureSheet() {
		entrys = new ObjectMap<>();
	}
	
	public TextureSheet(FileHandle jsonFile, JsonValue json) {
		this();
		
		imagePath = json.getString("image");
		FileHandle f = new FileHandle(jsonFile.file().getParentFile() + File.separator + imagePath);
		this.texture = new Texture(f);
		
		JsonValue texArray = json.get("textures");
		final int len = texArray.size;
		entrys.shrink(len * 4 / 3);
		for (int i = 0; i < len; i++) {
			TextureSheetEntry entry = new TextureSheetEntry();
			JsonValue texJson = texArray.get(i);
			
			entry.name = texJson.getString("name");
			entry.width = texJson.getInt("w");
			entry.height = texJson.getInt("h");
			entry.x = texJson.getInt("x");
			entry.y = texJson.getInt("y");
			entry.offsetx = texJson.getInt("offx");
			entry.offsety = texJson.getInt("offy");
			
			entry.region = new TextureRegion(texture, entry.x, entry.y, entry.width, entry.height);
			entrys.put(entry.name, entry);
		}
	}
	
	/**
	 * 这返回的是 sprite sheet 的整张图
	 * @return
	 */
	public Texture getTexture() {
		return texture;
	}
	
	public TextureSheetEntry getTextureEntry(String name) {
		return entrys.get(name);
	}

}
