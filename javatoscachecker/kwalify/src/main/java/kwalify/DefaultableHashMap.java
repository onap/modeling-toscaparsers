/*
 * The original version of this file was provided under the MIT license,
 * as follows:

 * @(#)DefaultableHashMap.java	$Rev: 4 $ $Release: 0.5.1 $
 *
 * Copyright (c) <2005> <kuwata lab>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 * Modifications to this file are provided under the Apache 2.0 license,
 * as follows:

 * Copyright (c) 2017 <AT&T>.  All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not 
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT 
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations 
 * under the License.
 */

package kwalify;

import java.util.Map;
import java.util.HashMap;

/**
 * hash map which can have default value
 * 
 * @revision    $Rev: 4 $
 * @release     $Release: 0.5.1 $
 */
public class DefaultableHashMap extends HashMap implements Defaultable {

    private static final long serialVersionUID = -5224819562023897380L;

    private transient Object _default = null;

    public DefaultableHashMap() {
        super();
    }

    public DefaultableHashMap(int initialCapacity) {
        super(initialCapacity);
    }

    public DefaultableHashMap(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    public DefaultableHashMap(Map m) {
        super(m);
    }

    public Object getDefault() { return _default; }

    public void setDefault(Object value) { _default = value; }

    public Object get(Object key) {
        return containsKey(key) ? super.get(key) : _default;
    }

}
