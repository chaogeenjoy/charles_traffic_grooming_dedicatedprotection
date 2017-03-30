package general;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileOutPut {

	public void filewrite(String filename, String result) {
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(filename, true);
			byte[] buffer1 = result.getBytes();
			fos.write(buffer1);
			String str = "\r\n";

			byte[] buffer = str.getBytes();
			fos.write(buffer);
			fos.close();

		} catch (FileNotFoundException e1) {
			System.out.println(e1);
		} catch (IOException e1) {
			System.out.println(e1);
		}
	}

}
