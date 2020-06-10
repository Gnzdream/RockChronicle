package zdream.rockchronicle.utils;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.StringTokenizer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

public class FilePathUtil {
	
	/**
	 * 文件位置相加. /a/b + ../c = /a/c
	 * @param file
	 *   指向基础路径的文件
	 * @param path
	 *   文件
	 * @return
	 */
	public static FileHandle relativeFileHandle (FileHandle file, String path) {
		StringTokenizer tokenizer = new StringTokenizer(path, "\\/");
		FileHandle result = file.parent();
		while (tokenizer.hasMoreElements()) {
			String token = tokenizer.nextToken();
			if (token.equals("..")) {
				result = result.parent();
			} else if (!token.equals(".")) {
				result = result.child(token);
			}
		}
		return result;
	}
	
	public static FileHandle relativeFileHandle (String filepath, String path) {
		return relativeFileHandle(Gdx.files.local(filepath), path);
	}
	
	public static FileHandle localFiles(String first, String... more) {
		Path path = Paths.get(first, more);
		return Gdx.files.local(path.toString());
	}

}
