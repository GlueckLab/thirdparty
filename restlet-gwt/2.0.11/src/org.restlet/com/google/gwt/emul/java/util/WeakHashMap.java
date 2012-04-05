/**
 * Copyright 2005-2011 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL 1.0 (the
 * "Licenses"). You can select the license that you prefer but you may not use
 * this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1.php
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1.php
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0.php
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package java.util;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Emulate the WeakHashMap class, especially for the GWT module.
 * 
 * @author Thierry Boileau
 * 
 */
public class WeakHashMap<K, V> extends AbstractMap<K, V> implements Map<K, V> {

    private Map<K, V> map;

    public WeakHashMap() {
        super();
        this.map = new HashMap<K, V>();
    }

    @Override
    public Set<java.util.Map.Entry<K, V>> entrySet() {
        return this.map.entrySet();
    }

    @Override
    public void clear() {
        this.map.clear();
    }

    @Override
    public boolean containsKey(Object key) {
        return this.map.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return this.map.containsValue(value);
    }

    @Override
    public boolean equals(Object o) {
        return this.map.equals(o);
    }

    @Override
    public V get(Object key) {
        return this.map.get(key);
    }

    @Override
    public int hashCode() {
        return this.map.hashCode();
    }

    @Override
    public boolean isEmpty() {
        return this.map.isEmpty();
    }

    @Override
    public Set<K> keySet() {
        return this.map.keySet();
    }

    @Override
    public V put(K key, V value) {
        return this.map.put(key, value);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        this.map.putAll(m);
    }

    @Override
    public V remove(Object key) {
        return this.map.remove(key);
    }

    @Override
    public int size() {
        return this.map.size();
    }

    @Override
    public String toString() {
        return this.map.toString();
    }

    @Override
    public Collection<V> values() {
        return this.map.values();
    }

}
