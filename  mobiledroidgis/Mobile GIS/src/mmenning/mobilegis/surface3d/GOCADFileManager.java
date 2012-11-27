/*
 * Copyright 2012 Mathias Menninghaus (mathias.menninghaus (at) googlemail (dot) com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package mmenning.mobilegis.surface3d;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.LinkedList;

import android.content.Context;
import android.graphics.Color;
import android.os.Environment;
import android.util.Log;

/**
 * Helper Class to manage Files in GOCAD Format. 
 * Will store and load from the sdcard folder [applications package name]/gocad.
 * This directory should only be used by a GOCADFileManager!
 * 
 * @author Mathias Menninghaus
 * @version 30.11.2009
 *
 */
public class GOCADFileManager {

	private static final String DT = "GOCADFileManager";

	public static final int TS = 1;

	private static final String SDCARD = Environment.getExternalStorageDirectory().getAbsolutePath();
	private static final String GOCAD = File.separator+"gocad";

	private File dir;

	public GOCADFileManager(Context context) {
		Log.d(DT, SDCARD);
		String path = SDCARD + File.separator + context.getPackageName() + GOCAD;
		dir = new File(path);
		if (!dir.exists()) {
			dir.mkdirs();
		}
	}

	private static void writeAsTs(LinkedList<OGLLayer> layer, BufferedWriter out)
			throws IOException {

		for (OGLLayer l : layer) {
			writeAsTs(l, out);
		}
	}

	private static void writeAsTs(OGLLayer layer, BufferedWriter out)
			throws IOException {

		/*
		 * write Head
		 */
		out.write("GOCAD TSurf 1\n");
		out.write("HEADER {\n");
		out.write("name:" + layer.getName() + "\n");
		out.write("*solid*color:" + ((float) Color.red(layer.getColor()))
				/ 255.0f + " " + ((float) Color.green(layer.getColor()))
				/ 255.0f + " " + ((float) Color.blue(layer.getColor()))
				/ 255.0f + " " + 1 + "\n");
		out.write("}\n");
		out.flush();
		/*
		 * write Body
		 */

		FloatBuffer vrtx = layer.getVertexBuffer().duplicate();
		vrtx.position(0);
		for (int i = 0, k = 1; i < vrtx.capacity(); i += 3, k++) {
			vrtx.position(i);
			out.write("VRTX " + k + " " + vrtx.get(i) + " " + vrtx.get(i + 1)
					+ " " + vrtx.get(i + 2) + "\n");
		}
		vrtx.position(0);
		out.flush();
		ShortBuffer trgl = layer.getIndexBuffer().duplicate();
		trgl.position(0);
		for (int i = 0; i < trgl.capacity(); i += 3) {
			trgl.position(i);
			/*
			 * OGLLayer indices start by 0, GOCAD indices by 1
			 */
			out.write("TRGL " + (trgl.get(i) + 1) + " " + (trgl.get(i + 1) + 1)
					+ " " + (trgl.get(i + 2) + 1) + "\n");
		}

		trgl.position(0);
		out.write("END\n");
		out.flush();
	}

	/**
	 * Get all files in the root folder. 
	 * @return Array of all stored files in the root folder
	 */
	public File[] getStoredFiles() {
		return dir.listFiles();
	}

	/**
	 * Store a OGLLayer List in a given Format to the filesystem.
	 * 
	 * @param layer OGLLayer List
	 * @param filename destination filename - will be created or overridden. 
	 * @param format Format of the destination file
	 * @throws IOException 
	 */
	public void storeToFile(LinkedList<OGLLayer> layer, String filename,
			int format) throws IOException {
		filename = dir.getAbsolutePath() + File.separator + filename;
		File dest = new File(filename);
		if (!dest.exists()) {
			dest.createNewFile();
		}
		BufferedWriter out = new BufferedWriter(new FileWriter(dest));
		switch (format) {
		case TS:
			writeAsTs(layer, out);
			break;
		default:
			throw new IllegalStateException("unknown format");
		}
		out.close();
	}
}
