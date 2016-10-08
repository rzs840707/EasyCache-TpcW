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

package com.hazelcast.queue.tx;

import com.hazelcast.core.TransactionalQueue;
import com.hazelcast.nio.serialization.Data;
import com.hazelcast.queue.QueueService;
import com.hazelcast.spi.NodeEngine;
import com.hazelcast.transaction.Transaction;

import java.util.concurrent.TimeUnit;

/**
 * @ali 3/25/13
 */
public class TransactionalQueueProxy<E> extends TransactionalQueueProxySupport implements TransactionalQueue<E> {


    public TransactionalQueueProxy(NodeEngine nodeEngine, QueueService service, String name, Transaction tx) {
        super(nodeEngine, service, name, tx);
    }

    public boolean offer(E e) {
        try {
            return offer(e, 0, TimeUnit.MILLISECONDS);
        } catch (InterruptedException ignored) {
        }
        return false;
    }

    public boolean offer(E e, long timeout, TimeUnit unit) throws InterruptedException {
        Data data = getNodeEngine().toData(e);
        return offerInternal(data, unit.toMillis(timeout));
    }

    public E poll() {
        try {
            return poll(0, TimeUnit.MILLISECONDS);
        } catch (InterruptedException ignored) {
        }
        return null;
    }

    public E poll(long timeout, TimeUnit unit) throws InterruptedException {
        Data data = pollInternal(unit.toMillis(timeout));
        return getNodeEngine().toObject(data);
    }

}
