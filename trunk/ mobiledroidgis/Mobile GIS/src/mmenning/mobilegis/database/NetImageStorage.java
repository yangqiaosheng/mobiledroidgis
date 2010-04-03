/*
 * Copyright (C) 2010 by Mathias Menninghaus (mmenning (at) uos (dot) de)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package mmenning.mobilegis.database;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

/**
 * Manages the download and Storage of images from the internet on SDCard in the
 * folder 'application-packagename'/img.
 * 
 * @author Mathias Menninghaus
 * @version 23.10.2009
 */
public class NetImageStorage {

	private static final String DT = "NetImageStorage";

	private static final String SDCARD = "/sdcard/";
	private static final String IMG = "/img";

	private static final int IO_BUFFER_SIZE = 1024;

	private String path;

	/**
	 * Instantiate new NetImageStorage. If not done yet, it will create a new
	 * folder named by the application package name referring to the given
	 * Context and '/img'.
	 * 
	 * @param context
	 *            Context in which the NetImageStorage will work.
	 */
	public NetImageStorage(Context context) {
		path = SDCARD + context.getPackageName() + IMG;
		File dir = new File(path);
		if (!dir.exists()) {
			dir.mkdirs();
		}
		path += File.separator;
	}

	/**
	 * Delete imagefile if it exists
	 * 
	 * @param url
	 *            Identifier for the image file to be deleted
	 */
	public void delete(String url) {
		if (url == null) {
			return;
		}
		int img = url.hashCode();

		File file = new File(path + img);

		if (file.exists()) {
			file.delete();
		}
	}

	/**
	 * Load a Bitmap from the given url if it is not already stored in the
	 * filesystem. If it is stored, it will be decoded from this file.
	 * 
	 * @param url
	 *            Identfier for file or URL to download from if no such file
	 *            exists
	 * @return the loaded Bitmap, or null if null was given
	 */
	public Bitmap load(String url) {
		if (url == null) {
			return null;
		}
		int img = url.hashCode();
		Bitmap bmp = null;

		File file = new File(path + img);

		if (!file.exists()) {
			store(url);
		}

		bmp = BitmapFactory.decodeFile(file.getAbsolutePath());

		return bmp;
	}

	/**
	 * Store an image to the filesystem.
	 * 
	 * @param url
	 *            URL to load the image from and Identifier for the file where
	 *            it is stored. Any equal files will be overridden!
	 */
	public void store(String url) {
		if (url == null) {
			return;
		}
		int img = url.hashCode();
		File file = null;
		try {
			file = new File(path + img);
			if (!file.exists()) {
				file.createNewFile();
				URL u = new URL(url);
				InputStream input = null;
				input = u.openStream();
				BufferedOutputStream out = new BufferedOutputStream(
						new FileOutputStream(file), IO_BUFFER_SIZE);
				byte[] b = new byte[IO_BUFFER_SIZE];
				int read;
				while ((read = input.read(b)) != -1) {
					out.write(b, 0, read);
				}
				out.flush();
				out.close();
				input.close();
			}
		} catch (FileNotFoundException e) {
			if (file != null) {
				file.delete();
			}
			Log.w(DT, e);
		} catch (IOException e) {
			if (file != null) {
				file.delete();
			}
			Log.w(DT, e);
		} finally {

		}
	}
}