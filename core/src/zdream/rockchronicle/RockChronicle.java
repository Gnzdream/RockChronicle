package zdream.rockchronicle;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;

import zdream.rockchronicle.core.Config;
import zdream.rockchronicle.core.GameRuntime;
import zdream.rockchronicle.core.input.InputCenter;
import zdream.rockchronicle.core.textures.TextureManager;
import zdream.rockchronicle.screen.MainMenuScreen;
//import zdream.rockchronicle.textures.painter.PainterManager;
import zdream.rockchronicle.utils.FilePathUtil;

/**
 * 游戏主体
 * 
 * @author Zdream
 */
public class RockChronicle {
	
	public static RockChronicle INSTANCE;
	
	public static void pushGame(Game game) {
		if (INSTANCE == null) {
			INSTANCE = new RockChronicle(game);
		}
	}
	
	RockChronicle(Game game) {
		this.game = game;
		projectPath = System.getProperty("user.dir");
		input = new InputCenter();
//		painter = new PainterManager();
		runtime = new GameRuntime();
		assets = new TextureManager();
	}
	
	// 其它数据
	public int width = 25;
	public int height = 14;
	public int widthInPixel;
	public int heightInPixel;
	
	public final Game game;
	
	// 类
	public final String projectPath;
	public final InputCenter input;
//	public final PainterManager painter;
	public final GameRuntime runtime;
	public final TextureManager assets;
	
	// 与引擎相关的
	
	public SpriteBatch batch;
	
	public BitmapFont font;

	public void create() {
		// 初始化
		initControl();
		
		widthInPixel = width * Config.INSTANCE.blockWidth;
		heightInPixel = height * Config.INSTANCE.blockHeight;
		
		// 其它
		batch = new SpriteBatch();
		game.setScreen(new MainMenuScreen());
		
		// font
		FreeTypeFontGenerator generator = new FreeTypeFontGenerator(FilePathUtil.localFiles("res", "font", "msyhbd.ttc"));
		FreeTypeFontParameter parameter = new FreeTypeFontParameter();
		
		parameter.size = 16;
		parameter.characters = FreeTypeFontGenerator.DEFAULT_CHARS + chineseCharacter();
		
		font = generator.generateFont(parameter);
		generator.dispose();
		
		// , FreeTypeFontGenerator.DEFAULT_CHARS+ "歌唱我们亲爱的祖国,从今走向繁荣富强", false
		
		// font = new UnicodeFont("res\\font\\msyhbd.ttc", "");
		runtime.init();
	}
	
	private String chineseCharacter() {
		FileHandle f = FilePathUtil.localFiles("res", "conf", "chinese_character.txt");
		String str = f.readString("UTF-8").replaceAll("\n|\r|\t|\\s*", "");
		return str;
	}
	
	private void initControl() {
		input.init();
	}
}
