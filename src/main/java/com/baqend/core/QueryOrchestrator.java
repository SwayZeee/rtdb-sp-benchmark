package com.baqend.core;

import com.baqend.client.Client;
import com.baqend.query.Query;

public class QueryOrchestrator {

    private final Query query;
    private final Client client;

    public QueryOrchestrator(Query query, Client client) {
        this.query = query;
        this.client = client;
    }

    public void registerQuery() {
        //UUID uuid = UUID.randomUUID();
        //LatencyMeasurement.getInstance().tick(uuid);
        //LatencyMeasurement.getInstance().setInitialTick(uuid, System.nanoTime());
        client.doQuery(query.getQuery());
    }
}
