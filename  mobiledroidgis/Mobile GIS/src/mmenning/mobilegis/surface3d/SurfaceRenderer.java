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
package mmenning.mobilegis.surface3d;

import java.util.LinkedList;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.graphics.Color;
import android.opengl.GLU;
import android.opengl.GLSurfaceView.Renderer;

/**
 * Renderer, which draws from a FloatBuffer of vertices and ShortBuffer of
 * indices triangles and manipulates the visualization of them by using a
 * Listener3D Object
 * 
 * @see GLView
 * @see Listener3D
 * 
 * @author Mathias Menninghaus
 * @version 06.08.2009
 * 
 */
public class SurfaceRenderer implements Renderer {

	/**
	 * Tag for Android-Logfile
	 */
	private static final String DT = "BodenRenderer";
	/**
	 * Direction of the light-source. Always assumed to be illuminated from
	 * above.
	 */
	private static final float[] lighting_direction = { +0.745133f,
			-0.0586743f, -0.0499261f, 0 };
	/*
	 * different direction for light-source direction: north-west (315°)
	 * altitude: 45° TODO: should be able to switch between different
	 * light-directions
	 */
	// {+0.745133f,-0.0586743f,-0.0499261f,0};
	/**
	 * Color of diffuse and ambient light. Always full (white, no transparency)
	 * The color of an object should only be influenced by its own color and its
	 * position towards the light-source.
	 */
	private static final float[] diff_amb_color = { 1, 1, 1, 1 };
	/**
	 * Listener to observe several informations about the point-source and the
	 * rendering-options
	 */
	private Listener3D l;

	/**
	 * list of oglLayers which should be drawn
	 */
	private LinkedList<OGLLayer> oglLayers;

	/**
	 * 
	 * @param context
	 *            Activity to which the renderer is linked
	 * @param listen
	 *            Listener to control display-options, walk-,rotate-, and
	 *            zoom-functions
	 * @param vertices
	 *            list of oglLayers which should be displayed
	 */
	public SurfaceRenderer(Context context, Listener3D listen,
			LinkedList<OGLLayer> oglLayers) {
		l = listen;
		setOGLLayers(oglLayers);
	}

	/**
	 * Setup configuration for the related GLSurfaceView
	 */
	public int[] getConfigSpec() {
		int[] configSpec = { EGL10.EGL_DEPTH_SIZE, 0, EGL10.EGL_NONE };
		return configSpec;
	}

	public Listener3D getListener() {
		return l;
	}

	public void setListener(Listener3D l) {
		this.l = l;
	}

	/**
	 * Is called once when the GLSurfaceView is displayed. In this method
	 * general settings are made.
	 */
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {

		/*
		 * as fast as you can
		 */
		gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_FASTEST);

		/*
		 * activate depth test to avoid problems if objects avoid each other
		 */
		gl.glEnable(GL10.GL_DEPTH_TEST);
		gl.glDepthFunc(GL10.GL_LEQUAL);

		/*
		 * set up clear color (black)
		 */
		gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

		/*
		 * enable transparency
		 */
		if (l.isBlending()) {
			gl.glEnable(GL10.GL_BLEND);
			gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		} else {
			gl.glDisable(GL10.GL_BLEND);
		}
		/*
		 * Dithering on/off - spends a lot of time
		 */
		if (l.isDithering()) {
			gl.glEnable(GL10.GL_DITHER);
		} else {
			gl.glDisable(GL10.GL_DITHER);
		}

		/*
		 * Flat Shading (cheap) - Gourad Shading (needs-time)
		 */
		if (l.isSmooth_shading()) {
			gl.glShadeModel(GL10.GL_SMOOTH);
		} else {
			gl.glShadeModel(GL10.GL_FLAT);
		}
		/*
		 * enable Lighting with one lighting source
		 */
		if (l.isLighting()) {
			gl.glDisable(GL10.GL_COLOR_MATERIAL);

			/*
			 * light from both sides of the objects
			 */
			gl.glLightModelf(GL10.GL_LIGHT_MODEL_TWO_SIDE, 1);

			if (l.isAmbient_lighting()) {
				gl.glLightModelfv(GL10.GL_LIGHT_MODEL_AMBIENT, diff_amb_color,
						0);
			}

			/*
			 * set up lighting
			 */
			if (l.isDiffuse_lighting()) {
				gl
						.glLightfv(GL10.GL_LIGHT1, GL10.GL_DIFFUSE,
								diff_amb_color, 0);
				gl.glEnable(GL10.GL_LIGHT1);
			}
			gl.glEnable(GL10.GL_LIGHTING);
		} else {
			/*
			 * if no lighting is enabled, set up color
			 */
			gl.glEnable(GL10.GL_COLOR_MATERIAL);
			gl.glDisable(GL10.GL_LIGHTING);
		}

		
	}

	/**
	 * Constantly called by the GLSurfaceView to display the scene.
	 */
	public void onDrawFrame(GL10 gl) {

		/*
		 * clear the depth-buffer from the last time
		 */
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

	
		/*
		 * draw objects in clockwise direction
		 */
		gl.glFrontFace(GL10.GL_CCW);

		/*
		 * initialize the modelview-matrix
		 */
		gl.glMatrixMode(GL10.GL_MODELVIEW);
		gl.glLoadIdentity();

		/*
		 * define the point from which the user looks on the scene and the
		 * reference point to which he looks.
		 */
		GLU.gluLookAt(gl, l.getXwalk(), l.getYwalk(), l.getZwalk()
				+ l.getZDistance(), l.getXwalk(), l.getYwalk(), l.getZwalk(),
				0f, 1.0f, 0.0f);

		/*
		 * rotate the scene in addition to the listeners attributes. therefore
		 * first translate into the origin, then rotate and at last translate
		 * back.
		 */
		gl.glTranslatef(l.getXwalk(), l.getYwalk(), l.getZwalk());
		gl.glRotatef(l.getYrot(), 0.0f, 1.0f, 0);
		gl.glRotatef(l.getXrot(), 1.0f, 0.0f, 0);
		gl.glTranslatef(-l.getXwalk(), -l.getYwalk(), -l.getZwalk());

		/*
		 * light-source moves with the scene
		 */
		if (l.isLighting() && l.isDiffuse_lighting()) {
			gl.glLightfv(GL10.GL_LIGHT1, GL10.GL_POSITION, lighting_direction,
					0);
		}
		/*
		 * display every tsObject
		 */
		for (OGLLayer layer : oglLayers) {

			/*
			 * only if it is set on visible
			 */
			if (layer.isVisible()) {

				int color = layer.getColor();
				/*
				 * if an object is not selected it should be transparent
				 */
				float alpha = layer.isSelected() ? 1.0f : 0.5f;

				/*
				 * transform color from android to opengl|es
				 */
				float[] colorArray = { ((float) Color.red(color)) / 255.0f,
						((float) Color.green(color)) / 255.0f,
						((float) Color.blue(color)) / 255.0f, alpha };

				/*
				 * lighting setting for every object
				 */
				if (l.isLighting()) {
					if (l.isDiffuse_lighting()) {
						/*
						 * the material belongs to the color of the object
						 */
						gl.glMaterialfv(GL10.GL_FRONT_AND_BACK,
								GL10.GL_DIFFUSE, colorArray, 0);

					}
					/*
					 * diffuse light should be reflected stronger
					 */
					for (int i = 0; i < 3; i++) {
						colorArray[i] /= 2.0f;
					}

					if (l.isAmbient_lighting()) {
						gl.glMaterialfv(GL10.GL_FRONT_AND_BACK,
								GL10.GL_AMBIENT, colorArray, 0);
					}
					/*
					 * activate normals. normalize the vertex-vectors and use
					 * them as normals
					 */
					gl.glEnable(GL10.GL_NORMALIZE);
					gl.glEnableClientState(GL10.GL_NORMAL_ARRAY);
					gl.glNormalPointer(GL10.GL_FLOAT, 0, layer.getVertexBuffer());

				} else {
					/*
					 * with no lighting an object only has its color
					 */
					gl.glDisable(GL10.GL_NORMALIZE);
					gl.glDisableClientState(GL10.GL_NORMAL_ARRAY);
					gl.glColor4f(colorArray[0], colorArray[1], colorArray[2],
							colorArray[3]);
				}
				/*
				 * at last draw all vertices and connect them into triangles
				 */
				gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);

				gl.glVertexPointer(3, GL10.GL_FLOAT, 0, layer.getVertexBuffer());

				layer.getVertexBuffer().position(0);

				/*
				 * do not draw triangles if the object should not be filled.
				 * then only draw the vertices.
				 */
				gl.glDrawElements(layer.isFill() ? GL10.GL_TRIANGLES
						: GL10.GL_POINTS, layer.getIndexBuffer().capacity(),
						GL10.GL_UNSIGNED_SHORT, layer.getIndexBuffer());

			}
		}
	}

	/**
	 * Called if the Size of the View is changed
	 */
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		gl.glViewport(0, 0, width, height);

		/*
		 * set up projection matrix (frustum-definition)
		 */
		float ratio = (float) width / height;
		gl.glMatrixMode(GL10.GL_PROJECTION);
		gl.glLoadIdentity();
		gl.glFrustumf(-ratio, ratio, -1, 1, l.getZNear(), l.getZFar());

	}

	public LinkedList<OGLLayer> getOGLLayers() {
		return oglLayers;
	}

	public void setOGLLayers(LinkedList<OGLLayer> oglLayers) {
		this.oglLayers = oglLayers;
	}

}
