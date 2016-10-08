package com.hazelcast.sqlclient.jdbc.entity;

public interface OutputStreamWatcher {
	void streamClosed(WatchableOutputStream out);
}
