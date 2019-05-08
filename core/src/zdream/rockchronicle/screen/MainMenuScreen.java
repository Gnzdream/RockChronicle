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
		// TODO Auto-generated method stub

	}

	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(0, 0, 0.2f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		camera.update();
		app.batch.setProjectionMatrix(camera.combined);

		app.batch.begin();
		app.font.draw(app.batch, "这是 Demo!!! ", 100, 150);
		app.font.draw(app.batch, "Tap anywhere to begin!", 100, 100);
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
		System.out.println(getClass().getName() + " : dispose");
		// nothing

	}

}
