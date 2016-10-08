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

package com.hazelcast.map;

import com.hazelcast.logging.ILogger;
import com.hazelcast.nio.serialization.Data;
import com.hazelcast.util.scheduler.EntryTaskScheduler;
import com.hazelcast.util.scheduler.ScheduledEntry;
import com.hazelcast.util.scheduler.ScheduledEntryProcessor;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.logging.Level;

public class MapStoreWriteProcessor implements ScheduledEntryProcessor<Data, Object> {

    private final MapContainer mapContainer;
    private final MapService mapService;
    private final ILogger logger;

    public MapStoreWriteProcessor(MapContainer mapContainer, MapService mapService) {
        this.mapContainer = mapContainer;
        this.mapService = mapService;
        this.logger = mapService.getNodeEngine().getLogger(getClass());
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
	public void process(EntryTaskScheduler<Data, Object> scheduler, Collection<ScheduledEntry<Data, Object>> entries) {
        if (entries.isEmpty()) return;
        if (entries.size() == 1) {
            ScheduledEntry<Data, Object> entry = entries.iterator().next();
            Exception e = tryStore(scheduler, entry);
            if (e != null) {
            	logger.log(Level.WARNING, mapContainer.getStore().getMapStore().getClass() + " --> storeAll was failed, now Hazelcast is trying to store one by one: ", e);
            }
        } else {   // if entries size > 0, we will call storeAll
//        	final Queue<ScheduledEntry> duplicateKeys = new LinkedList<ScheduledEntry>();
            final Map map = new HashMap(entries.size());
            for (ScheduledEntry<Data, Object> entry : entries) {
                final Object key = mapService.toObject(entry.getKey());
//                if (map.get(key) != null) {
//                    duplicateKeys.offer(entry);
//                    continue;
//                }
                map.put(key, mapService.toObject(entry.getValue()));
            }
            Exception exception = null;
            try {
                mapContainer.getStore().storeAll(map, mapContainer.getTableName());
            } catch (Exception e) {
                logger.log(Level.WARNING, mapContainer.getStore().getMapStore().getClass() + " --> storeAll was failed, now Hazelcast is trying to store one by one: ", e);
                // if store all throws exception we will try to put insert them one by one.
                for (ScheduledEntry<Data, Object> entry : entries) {
                    Exception temp = tryStore(scheduler, entry);
                    if (temp != null) {
                        exception = temp;
                    }
                }
            }
//            ScheduledEntry entry;
//            while ((entry = duplicateKeys.poll()) != null) {
//                final Exception temp = tryStore(scheduler, entry);
//                if (temp != null) {
//                    exception = temp;
//                }
//            }
            if (exception != null) {
                logger.log(Level.SEVERE, exception.toString());
            }
        }

    }
    
    private Exception tryStore(EntryTaskScheduler<Data, Object> scheduler, ScheduledEntry<Data, Object> entry) {
    	// yanshi.wys
    	// System.out.println("tryStore()#\tthreadId: " + Thread.currentThread().getId() + "\tthreadName: " + Thread.currentThread().getName());
    	// System.out.println("\tkey: " + mapService.toObject(entry.getKey()) + "\tvalue: " + ((People)mapService.toObject(entry.getValue())).getName());
    	// return null;
    	
        Exception exception = null;
        try {
        	mapContainer.getStore().store(mapService.toObject(entry.getKey()), mapService.toObject(entry.getValue()), mapContainer.getTableName());
        } catch (Exception e) {
            logger.log(Level.WARNING, mapContainer.getStore().getMapStore().getClass() + " --> store failed, now Hazelcast reschedules this operation ", e);
            exception = e;
            scheduler.schedule(mapContainer.getWriteDelayMillis(), entry.getKey(), entry.getValue());
        }
        return exception;
    }

}
