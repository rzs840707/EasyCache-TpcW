/*
 * Copyright (c) 2008-2013, Hazelcast, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hazelcast.concurrent.idgen;

import com.hazelcast.core.IAtomicLong;
import com.hazelcast.core.IdGenerator;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @ali 1/23/13
 */
public class IdGeneratorProxy implements IdGenerator {

    private static final int BLOCK_SIZE = 10000;

    final String name;

    final IAtomicLong atomicLong;

    AtomicInteger residue;

    AtomicLong local;

    public IdGeneratorProxy(IAtomicLong atomicLong, String name) {
        this.name = name;
        this.atomicLong = atomicLong;
        residue = new AtomicInteger(BLOCK_SIZE);
        local = new AtomicLong(-1);
    }

    public boolean init(long id) {
        if (id <= 0) {
            return false;
        }
        long step = (id / BLOCK_SIZE);

        synchronized (this) {
            boolean init = atomicLong.compareAndSet(0, step+1);
            if (init){
                local.set(step);
                residue.set((int)(id % BLOCK_SIZE)+1);
            }
            return init;
        }

    }

    public long newId() {
        int value = residue.getAndIncrement();
        if (value >= BLOCK_SIZE) {
            synchronized (this) {
                value = residue.get();
                if (value >= BLOCK_SIZE) {
                    local.set(atomicLong.getAndIncrement());
                    residue.set(0);
                }
                return newId();
            }
        }
        return local.get() * BLOCK_SIZE + value;
    }

    public Object getId() {
        return name;
    }

    public String getName() {
        return name;
    }

    public void destroy() {
        atomicLong.destroy();
        residue = null;
        local = null;
    }
}
