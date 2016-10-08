package com.hazelcast.sqlclient.jdbc.entity;

public interface WriterWatcher {
	void writerClosed(WatchableWriter out);
}
