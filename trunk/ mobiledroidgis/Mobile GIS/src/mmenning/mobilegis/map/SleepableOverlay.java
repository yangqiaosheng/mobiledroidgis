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
package mmenning.mobilegis.map;

/**
 * Provides whether a Map Overlay is able to sleep or not. This means, that a
 * sleeping Overlay will not be drawn or react on any Events.
 * 
 * @author Mathias Menninghaus
 * @version 26.10.2009
 * 
 */
public interface SleepableOverlay {

	public void makeSleeping();

	public void makeAwake();
}
