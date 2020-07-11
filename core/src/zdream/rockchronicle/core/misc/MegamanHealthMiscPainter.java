package zdream.rockchronicle.core.misc;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import zdream.rockchronicle.RockChronicle;
import zdream.rockchronicle.core.foe.Foe;
import zdream.rockchronicle.core.foe.IPainter;
import zdream.rockchronicle.core.textures.TextureSelect;
import zdream.rockchronicle.core.textures.TextureSheet;
import zdream.rockchronicle.core.textures.TextureSheetEntry;
import zdream.rockchronicle.foes.megaman.Megaman;

public class MegamanHealthMiscPainter implements IPainter {
	
	public MegamanHealthMiscPainter() {
		app = RockChronicle.INSTANCE;
		initTextures();
	}

	Sprite bg;
	Sprite black;
	Sprite healthRed;
	TextureRegion health;
	
	TextureSheet textures;
	RockChronicle app;
	
	protected TextureSelect select = new TextureSelect();
	
	/**
	 * @param paths
	 *  比如 ["res/characters/megaman/sprites/megaman7sheet.json"]
	 */
	private void initTextures() {
		textures = TextureSheet.getSheet("res/misc/base_misc_sheet.json");
		
		TextureSheetEntry entry = textures.entrys.get("health_misc");
		bg = new Sprite();
		bg.setRegion(entry.region);
		
		entry = textures.entrys.get("black");
		black = new Sprite();
		black.setRegion(entry.region);
		
		entry = textures.entrys.get("health");
		health = new TextureRegion();
		
		entry = textures.entrys.get("health_red");
		healthRed = new Sprite();
		healthRed.setRegion(entry.region);
	}
	
	@Override
	public void draw(SpriteBatch batch, OrthographicCamera camera) {
		Megaman mm = findMegaman();
		if (mm == null) {
			return;
		}
		TextureSheetEntry entry;
		
		// 所有绘画部分大小 x2
		
		entry = textures.entrys.get("health_misc");
		int xstart = 32;
		int ystart = app.heightInPixel - entry.height * 2 - 48;
		
		int realHp = Math.max(mm.hp, 0);
		int hp = Math.min(realHp / 256, 28);
		boolean hpPlus = realHp % 256 != 0;
		if (hp == 28) {
			hpPlus = false;
		}
		
		batch.begin();
		// 首先画底
		batch.draw(bg, xstart, ystart,
				entry.width * 2, entry.height * 2);
		
		// 黑底
		entry = textures.entrys.get("black");
		batch.draw(black, xstart + 14, ystart + 46,
				entry.width * 2, entry.height * 28 * 2);
		
		// 血条
		entry = textures.entrys.get("health");
		health.setRegion(entry.region, 0, entry.height - hp * 3, entry.width, hp * 3);
		batch.draw(health, xstart + 14, ystart + 46, entry.width * 2, hp * 6);
//		batch.d
		if (hpPlus) {
			entry = textures.entrys.get("health_red");
			batch.draw(healthRed, xstart + 14, ystart + 46 + hp * 6,
					entry.width * 2, entry.height * 2);
		}
		
		batch.end();
		
//		TextureSheetEntry entry = getCurrentTexture();
//		if (entry == null) {
//			return;
//		}
//		sprite.setRegion(entry.region);
//		
//		float x, // 单位: 像素 -> 格子
//				y = getBy() + entry.offsety / (float) widthInPixel; // 单位: 像素 -> 格子
//		
//		float fw = (float) entry.width / widthInPixel,
//				fh = (float) entry.height / heightInPixel;
//		
//		if (orientation) {
//			sprite.setFlip(false, false);
//			x = getBx() + entry.offsetx / (float) widthInPixel;
//		} else {
//			sprite.setFlip(true, false);
//			x = getBx() - entry.offsetx / (float) widthInPixel - fw;
//		}
//		
//		// 绘画
//		batch.begin();
//		batch.draw(sprite, x, y, fw, fh);
//		batch.end();
	}
	
	private Megaman findMegaman() {
		Foe foe = app.runtime.world.player1;
		
		if (foe instanceof Megaman) {
			return (Megaman) foe;
		}
		return null;
	}

	@Override
	public int zIndex() {
		return 0;
	}

}
