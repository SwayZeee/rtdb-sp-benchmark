package com.baqend.workload.generators;

import com.baqend.config.Config;
import com.baqend.core.subscription.query.Query;
import com.baqend.core.subscription.query.QuerySet;
import com.baqend.utils.JsonExporter;
import com.baqend.utils.RandomDataGenerator;
import com.baqend.workload.*;
import com.google.gson.Gson;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class WorkloadCGeneratorLimited {

    private static final Gson gson = new Gson();
    private static final RandomDataGenerator randomDataGenerator = new RandomDataGenerator();
    private static final JsonExporter jsonExporter = new JsonExporter();

    public static void main(String[] args) throws FileNotFoundException {
        Config config = gson.fromJson(new FileReader("src\\main\\java\\com\\baqend\\config\\config.json"), Config.class);
        String workloadName = "workload_c";
        QuerySet querySet = generateQuerySet();
        Workload workload = generateWorkload(config.duration, config.throughput, config.insertProportion, config.updateProportion);
        jsonExporter.exportQuerySetToJsonFile(querySet, workloadName);
        jsonExporter.exportWorkloadToJsonFile(workload, workloadName, config.throughput);
    }

    public static QuerySet generateQuerySet() {
        QuerySet querySet = new QuerySet();
        int queryAmount = 10;
        for (int i = 0; i < queryAmount; i++) {
            Query numberQuery = new Query("{ $and: [ { \\\"number\\\": { $gt: " + 500 * i + " } }, { \\\"number\\\": { $lt: " + (500 * (i + 1) + 1) + " } } ] }", "");
            querySet.addQuery(numberQuery);
        }
        return querySet;
    }

    public static Workload generateWorkload(int duration, int throughput, int insertProportion, int updateProportion) throws FileNotFoundException {
        Workload initialWorkloadData = gson.fromJson(new FileReader("src\\main\\java\\com\\baqend\\generated\\workloads\\initialLoad.json"), Workload.class);
        LoadData relevantTupels = new LoadData();
        LoadData irrelevantTupels = new LoadData();
        Workload workload = new Workload();
        int queryAmount = 10;

        for (WorkloadEvent workloadEvent : initialWorkloadData.getWorkload()) {
            // TODO: perform check for relevant data tupels in initial load
            if (Integer.parseInt(workloadEvent.getSingleDataSet().getData().get("number")) <= 500 * queryAmount) {
                relevantTupels.addSingleDataSet(workloadEvent.getSingleDataSet());
            } else {
                irrelevantTupels.addSingleDataSet(workloadEvent.getSingleDataSet());
            }
        }

        ArrayList<UUID> forbiddenIDs = new ArrayList<UUID>();

        for (int i = 1; i <= (duration * throughput); i++) {
            int randomNumber = randomDataGenerator.generateRandomInteger(1, 100);
            UUID transactionID = UUID.randomUUID();

            if (forbiddenIDs.size() == throughput / 2) {
                forbiddenIDs.remove(0);
            }

            // measurement relevant events
            if (i % (throughput / 100) == 0) {
                // updates only, no relevant inserts or deletes
                SingleDataSet singleDataSet;
                do {
                    int randomIndex = randomDataGenerator.generateRandomInteger(0, relevantTupels.getLoad().size() - 1);
                    singleDataSet = relevantTupels.getLoad().get(randomIndex);
                }
                while (forbiddenIDs.contains(singleDataSet.getUuid()));
                HashMap<String, String> data = randomDataGenerator.generateRandomDataset(Integer.parseInt(singleDataSet.getData().get("number")));
                SingleDataSet newSingleDataSet = new SingleDataSet(singleDataSet.getUuid(), data);

                WorkloadEvent newWorkloadEvent = new WorkloadEvent(transactionID, WorkloadEventType.UPDATE, true, newSingleDataSet);
                workload.addWorkloadEvent(newWorkloadEvent);

                forbiddenIDs.add(newSingleDataSet.getUuid());
            } else {
                if (randomNumber <= insertProportion) {
                    HashMap<String, String> data = randomDataGenerator.generateRandomDataset(initialWorkloadData.getWorkload().size() + 1);
                    SingleDataSet newSingleDataSet = new SingleDataSet(UUID.randomUUID(), data);

                    irrelevantTupels.addSingleDataSet(newSingleDataSet);
                    WorkloadEvent newWorkloadEvent = new WorkloadEvent(transactionID, WorkloadEventType.INSERT, false, newSingleDataSet);
                    initialWorkloadData.addWorkloadEvent(newWorkloadEvent);
                    workload.addWorkloadEvent(newWorkloadEvent);

                    forbiddenIDs.add(newSingleDataSet.getUuid());
                } else if (randomNumber <= insertProportion + updateProportion) {
                    SingleDataSet singleDataSet;
                    do {
                        int randomIndex = randomDataGenerator.generateRandomInteger(0, irrelevantTupels.getLoad().size() - 1);
                        singleDataSet = irrelevantTupels.getLoad().get(randomIndex);
                    } while (forbiddenIDs.contains(singleDataSet.getUuid()));
                    HashMap<String, String> data = randomDataGenerator.generateRandomDataset(Integer.parseInt(singleDataSet.getData().get("number")));
                    SingleDataSet newSingleDataSet = new SingleDataSet(singleDataSet.getUuid(), data);

                    WorkloadEvent newWorkloadEvent = new WorkloadEvent(transactionID, WorkloadEventType.UPDATE, false, newSingleDataSet);
                    workload.addWorkloadEvent(newWorkloadEvent);

                    forbiddenIDs.add(newSingleDataSet.getUuid());
                }
            }
        }
        return workload;
    }
}
