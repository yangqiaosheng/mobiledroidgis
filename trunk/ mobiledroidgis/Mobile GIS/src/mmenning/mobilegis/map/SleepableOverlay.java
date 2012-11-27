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
