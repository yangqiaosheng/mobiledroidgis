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

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * Interface to manage the Layers which are displayed in SurfaceVisualizer.
 * IMPORTANT: The Buffers must use allocateDirect(..) and nativeOrder(..)
 * 
 * @version 01.09.2009
 * @author Mathias Menninghaus
 * 
 */
public interface OGLLayer {
	/**
	 * returns a FloatBuffer of indices in the order of: x1,y1,z1,x2,y2...
	 * 
	 * @return
	 */
	public FloatBuffer getVertexBuffer() ;

	/**
	 * returns indices as an ShortBuffer in order of Index_1_of_Triangle_1,
	 * Index_2_of_Triangle_1, Index_3_of_Triangle_1, Index_1_of_Triangle_2,
	 * ... , Index_3_of_Triangle_n
	 * 
	 * The indices shall start by 0!
	 * 
	 * @return
	 */
	public ShortBuffer getIndexBuffer() ;

	/**
	 * returns the name of the OGLLayer. If no name has been read a default
	 * name according to a number.
	 * 
	 * @return read name or default-name
	 */
	public String getName() ;
	

	/**
	 * returns color in android-format. if no color has been read a random
	 * color will be created.
	 * 
	 * @return read or random color (@see android.graphics.color)
	 */
	public int getColor() ;

	public boolean isFill() ;

	public void setFill(boolean fill) ;

	public boolean isVisible() ;
	
	public void setVisible(boolean visible) ;

	public boolean isSelected() ;

	public void setSelected(boolean selected) ;

}
