package zdream.rockchronicle.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;

import zdream.rockchronicle.RockChronicle;

public class MainMenuScreen implements Screen {
	
	final RockChronicle app;
	
	OrthographicCamera camera;
	
	public MainMenuScreen() {
		app = RockChronicle.INSTANCE;
		
		camera = new OrthographicCamera();
		camera.setToOrtho(false, app.widthInPixel, app.heightInPixel);
	}

	@Override
	public void show() {
		app.assets.startLoading();

	}

	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(0, 0, 0.2f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		camera.update();
		app.batch.setProjectionMatrix(camera.combined);
		

		// 检查资源加载情况
		int remain = app.assets.hasLoaded();
		String text = (remain > 0) ? "还剩 " + remain : "还剩 0 : 点击屏幕开始";
		

		app.batch.begin();
		app.font.draw(app.batch, "这是 Demo!!! ", 100, 150);
		app.font.draw(app.batch, text, 100, 100);
		app.batch.end();

		if (Gdx.input.isTouched()) {
			app.game.setScreen(new LevelScreen());
			dispose();
		}
		
	}

	@Override
	public void resize(int width, int height) {
		// TODO Auto-generated method stub

	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub

	}

	@Override
	public void resume() {
		// TODO Auto-generated method stub

	}

	@Override
	public void hide() {
		// TODO Auto-generated method stub

	}

	@Override
	public void dispose() {
		// nothing

	}

}
