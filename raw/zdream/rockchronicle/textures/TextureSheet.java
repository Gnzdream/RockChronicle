package zdream.rockchronicle.textures;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectMap;

import zdream.rockchronicle.platform.world.LevelWorld;
import zdream.rockchronicle.utils.JsonUtils;

public class TextureSheet {
	
	public ObjectMap<String, TextureSheetEntry> entrys;
	public ObjectMap<String, TextureSequence> sequences;
	
	public Texture texture;
	public String defaultState = "normal";
	
	/*
	 * 这里是 Json 文件里面的数据
	 */
	/**
	 * 所说明的 texture 的图片文件相对于该 json 文件的位置
	 */
	String imagePath;
	
	public static TextureSheet createSheet(FileHandle file) {
		JsonReader r = JsonUtils.jreader;
		JsonValue j = r.parse(file);
		
		TextureSheet sheet = new TextureSheet(file, j);
		
		return sheet;
	}
	
	public TextureSheet() {
		entrys = new ObjectMap<>();
		sequences = new ObjectMap<>();
	}
	
	private TextureSheet(FileHandle jsonFile, JsonValue json) {
		this();
		
		imagePath = json.getString("image");
		FileHandle f = jsonFile.parent().child(imagePath);
		this.texture = new Texture(f);
		
		for (JsonValue entry = json.child; entry != null; entry = entry.next) {
			switch (entry.name) {
			case "textures":
				createEntries(entry);
				break;
				
			case "select":
				createSequences(entry);
				break;
				
			case "default":
				this.defaultState = entry.asString();
				break;

			default:
				break;
			}
		}
	}
	
	private void createEntries(JsonValue texArray) {
		final int len = texArray.size;
		entrys.shrink(len * 4 / 3);
		for (JsonValue texJson = texArray.child; texJson != null; texJson = texJson.next) {
			TextureSheetEntry entry = new TextureSheetEntry();
			
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
	
	private void createSequences(JsonValue texArray) {
		final int len = texArray.size;
		sequences.shrink(len * 4 / 3);
		for (JsonValue texJson = texArray.child; texJson != null; texJson = texJson.next) {
			TextureSequence seq = new TextureSequence();
			
			seq.state = texJson.getString("state");
			if (texJson.has("step")) {
				seq.step = (int) (texJson.getFloat("step") * LevelWorld.STEPS_PER_SECOND + 0.5f);
			}
			JsonValue array = texJson.get("sequence");
			if (array.size == 0) {
				continue;
			}
			seq.loopIdx = texJson.getInt("loopIndex", -1);
			seq.seqs = new String[array.size];
			
			int i = 0;
			for (JsonValue seqItem = array.child; seqItem != null; seqItem = seqItem.next) {
				seq.seqs[i++] = seqItem.asString();
			}
			
			this.sequences.put(seq.state, seq);
		}
	}
	
}
