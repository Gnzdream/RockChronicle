package zdream.rockchronicle.core.textures;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectMap;

import zdream.rockchronicle.core.world.Ticker;
import zdream.rockchronicle.utils.JsonUtils;

/**
 * @author Zdream
 * @date 2020-06-20
 */
public class TextureSheetLoader {
	
	/**
	 * 注意, 这里只是加载的第一阶段.
	 * @param fileName
	 * @return
	 */
	public TextureSheetTask loadSync(String fileName) {
		FileHandle texFile = Gdx.files.local(fileName);
		
		JsonReader r = JsonUtils.jreader;
		JsonValue j = r.parse(texFile);
		return createTextureSheetTask(texFile, j).appendJsonPath(fileName);
	}
	
	private TextureSheetTask createTextureSheetTask(FileHandle jsonFile, JsonValue json) {
		TextureSheet sheet = new TextureSheet();
		
		String imagePath = json.getString("image");
		FileHandle f = jsonFile.parent().child(imagePath);
//		sheet.texture = new Texture(f);
		
		for (JsonValue entry = json.child; entry != null; entry = entry.next) {
			switch (entry.name) {
			case "textures":
				createEntries(sheet, entry);
				break;
				
			case "select":
				createSequences(sheet, entry);
				break;
				
			case "default":
				sheet.defaultState = entry.asString();
				break;

			default:
				break;
			}
		}
		
		return new TextureSheetTask(sheet, f.path());
	}
	
	private void createEntries(TextureSheet sheet, JsonValue texArray) {
		final int len = texArray.size;
		sheet.entrys.shrink(len * 4 / 3);
		for (JsonValue texJson = texArray.child; texJson != null; texJson = texJson.next) {
			TextureSheetEntry entry = new TextureSheetEntry();
			
			entry.name = texJson.getString("name");
			entry.width = texJson.getInt("w");
			entry.height = texJson.getInt("h");
			entry.x = texJson.getInt("x");
			entry.y = texJson.getInt("y");
			entry.offsetx = texJson.getInt("offx");
			entry.offsety = texJson.getInt("offy");
			
			// 这步要延迟操作
			// entry.region = new TextureRegion(sheet.texture, entry.x, entry.y, entry.width, entry.height);
			sheet.entrys.put(entry.name, entry);
		}
	}
	
	private void createSequences(TextureSheet sheet, JsonValue texArray) {
		final int len = texArray.size;
		sheet.sequences.shrink(len * 4 / 3);
		for (JsonValue texJson = texArray.child; texJson != null; texJson = texJson.next) {
			TextureSequence seq = new TextureSequence();
			
			seq.state = texJson.getString("state");
			if (texJson.has("step")) {
				seq.step = (int) (texJson.getFloat("step") * Ticker.STEPS_PER_SECOND + 0.5f);
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
			
			sheet.sequences.put(seq.state, seq);
		}
	}
	
	public void setTexture(TextureSheetTask task, Texture tx) {
		task.sheet.texture = tx;
		ObjectMap<String, TextureSheetEntry> entrys = task.sheet.entrys;
		
		entrys.forEach(entry -> {
			TextureSheetEntry en = entry.value;
			en.region = new TextureRegion(tx, en.x, en.y, en.width, en.height);
		});
	}
	
	static public class TextureSheetTask {
		TextureSheet sheet;
		String imagePath;
		String jsonPath;
		public TextureSheetTask(TextureSheet sheet, String imagePath) {
			super();
			this.sheet = sheet;
			this.imagePath = imagePath;
		}
		TextureSheetTask appendJsonPath(String jsonPath) {
			this.jsonPath = jsonPath;
			return this;
		}
	}

}
