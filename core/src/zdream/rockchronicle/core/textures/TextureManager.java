package zdream.rockchronicle.core.textures;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.resolvers.LocalFileHandleResolver;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;

import zdream.rockchronicle.core.textures.TextureSheetLoader.TextureSheetTask;

public class TextureManager {
	
	private final AssetManager asset;
	
	/**
	 * 全是 json 文件
	 */
	Array<String> resourcePaths = new Array<>();
	
	/**
	 * 没有加载完的放这里
	 */
	private Array<TextureSheetTask> tasks = new Array<>();
	ObjectMap<String, TextureSheet> sheets = new ObjectMap<>();
	
	TextureSheetLoader loader = new TextureSheetLoader();
	
	public TextureManager() {
		FileHandleResolver resolver = new LocalFileHandleResolver();
		asset = new AssetManager(resolver);
		
		// misc
		resourcePaths.add("res/misc/base_misc_sheet.json");
		
		// texture
		resourcePaths.add("res/characters/megaman/sprites/megaman7sheet.json");
		resourcePaths.add("res/characters/megaman/sprites/mm7buster-sheet.json");
		
		resourcePaths.add("res/characters/mm2birds/sprites/mm2birds_sheet.json");
		resourcePaths.add("res/characters/mm2birds/sprites/mm2pipi_egg_sheet.json");
		resourcePaths.add("res/characters/mm2birds/sprites/mm2pipi_chicks_sheet.json");
		resourcePaths.add("res/characters/mm2shotman/sprites/mm2shotman_sheet_a.json");
		resourcePaths.add("res/characters/mm2shotman/sprites/mm2shotman_sheet_b.json");
		resourcePaths.add("res/characters/mm5bbitter/mm5bbitter_sheet_a.json");
//		resourcePaths.add("res/characters/mm5bbitter/mm5bbitter_sheet_b.json");
	}
	
	public void startLoading() {
		for (int i = 0; i < resourcePaths.size; i++) {
			loadTextureSheet(resourcePaths.get(i));
		}
		
		asset.update();
	}
	
	public void loadTextureSheet(String path) {
		TextureSheetTask task = loader.loadSync(path);
		tasks.add(task);
		asset.load(task.imagePath, Texture.class);
	}
	
	public int hasLoaded() {
		int notyet = 0;

		asset.update();
		
		Array<TextureSheetTask> finishedTasks = new Array<>(tasks.size);
		for (int i = 0; i < tasks.size; i++) {
			TextureSheetTask task = tasks.get(i);
			
			if (asset.isLoaded(task.imagePath)) {
				TextureSheet sheet = task.sheet;
				loader.setTexture(task, asset.get(task.imagePath));
				sheets.put(task.jsonPath, sheet);
				
				finishedTasks.add(task);
			} else {
				notyet++;
			}
		}
		
		this.tasks.removeAll(finishedTasks, true);
		return notyet;
	}
	
//	public boolean hasLoaded(String path) {
//		
//		
//		
//		
//	}
	
	public TextureSheet getSheet(String path) {
//		if (!asset.isLoaded(path)) {
//			Gdx.app.log("TextureManager", String.format("%s 没有加载", path));
//			return loader.loadSync(path);
//		}
		
		try {
			return sheets.get(path);
		} catch (RuntimeException e) {
			Gdx.app.log("TextureManager", String.format("%s 没有加载", path));
			throw e;
		}
	}

}
