package com.baqend.workload.generators;

import com.baqend.config.Config;
import com.baqend.core.subscription.query.Query;
import com.baqend.core.subscription.query.QuerySet;
import com.baqend.workload.LoadData;
import com.baqend.workload.SingleDataSet;
import com.baqend.workload.Workload;
import com.baqend.workload.WorkloadEvent;
import com.baqend.workload.WorkloadEventType;
import com.baqend.utils.JsonExporter;
import com.baqend.utils.RandomDataGenerator;
import com.google.gson.Gson;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

/**
 * Workload
 * Generates a workload that does not contain events that update already relevant data to be removed
 * Applicable for Baqend and Flink benchmarking
 * Clause: WHERE fieldThree LIKE "%RTDBSP%"
 */
public class WorkloadBGenerator {

    private static final Gson gson = new Gson();
    private static final RandomDataGenerator randomDataGenerator = new RandomDataGenerator();
    private static final JsonExporter jsonExporter = new JsonExporter();

    public static void main(String[] args) throws FileNotFoundException {
        Config config = gson.fromJson(new FileReader("src\\main\\java\\com\\baqend\\config\\config.json"), Config.class);
        String workloadName = "workload_b";
        QuerySet querySet = generateQuerySet();
        Workload workload = generateWorkload(config.duration, config.throughput, config.insertProportion, config.updateProportion);
        jsonExporter.exportQuerySetToJsonFile(querySet, workloadName);
        jsonExporter.exportWorkloadToJsonFile(workload, workloadName, config.throughput);
    }

    public static QuerySet generateQuerySet() {
        QuerySet querySet = new QuerySet();
        Query fieldThreeQuery = new Query("{ \\\"fieldThree\\\": { $regex: /.*RTDBSP.*/ } }", "");
        querySet.addQuery(fieldThreeQuery);
        return querySet;
    }

    public static Workload generateWorkload(int duration, int throughput, int insertProportion, int updateProportion) throws FileNotFoundException {
        Workload initialWorkloadData = gson.fromJson(new FileReader("src\\main\\java\\com\\baqend\\generated\\workloads\\initialLoad.json"), Workload.class);
        LoadData relevantTupels = new LoadData();
        LoadData irrelevantTupels = new LoadData();
        Workload workload = new Workload();

        for (WorkloadEvent workloadEvent : initialWorkloadData.getWorkload()) {
            if (workloadEvent.getSingleDataSet().getData().get("fieldThree").contains("RTDBSP")) {
                relevantTupels.addSingleDataSet(workloadEvent.getSingleDataSet());
            } else {
                irrelevantTupels.addSingleDataSet(workloadEvent.getSingleDataSet());
            }
        }

        ArrayList<UUID> forbiddenIDs = new ArrayList<UUID>();

        for (int i = 1; i <= (duration * throughput); i++) {
            int randomNumber = randomDataGenerator.generateRandomInteger(1, 100);
            UUID transactionID = UUID.randomUUID();

            if (forbiddenIDs.size() == throughput) {
                forbiddenIDs.remove(0);
            }
            // measurement relevant events
            if (i % (throughput / 100) == 0) {
                // INSERT
                if (randomNumber <= insertProportion) {
                    HashMap<String, String> data = new HashMap<String, String>();
                    data.put("fieldOne", Integer.toString(randomDataGenerator.generateRandomInteger(1, 1000)));
                    data.put("fieldTwo", Double.toString(randomDataGenerator.generateRandomDouble(1, 1000)));
                    data.put("fieldThree", "RTDBSP");
                    data.put("fieldFour", Integer.toString(randomDataGenerator.generateRandomInteger(1001, 10000)));
                    data.put("fieldFive", Double.toString(randomDataGenerator.generateRandomDouble(1001, 10000)));
                    data.put("fieldSix", randomDataGenerator.generateRandomString(12, true, false));
                    data.put("fieldSeven", Integer.toString(randomDataGenerator.generateRandomInteger(10001, 100000)));
                    data.put("fieldEight", Double.toString(randomDataGenerator.generateRandomDouble(10001, 100000)));
                    data.put("fieldNine", randomDataGenerator.generateRandomString(18, true, false));
                    data.put("number", Integer.toString(initialWorkloadData.getWorkload().size() + 1));
                    SingleDataSet newSingleDataSet = new SingleDataSet(UUID.randomUUID(), data);

                    relevantTupels.addSingleDataSet(newSingleDataSet);
                    WorkloadEvent newWorkloadEvent = new WorkloadEvent(transactionID, WorkloadEventType.INSERT, true, newSingleDataSet);
                    initialWorkloadData.addWorkloadEvent(newWorkloadEvent);
                    workload.addWorkloadEvent(newWorkloadEvent);

                    forbiddenIDs.add(newSingleDataSet.getUuid());
                    // UPDATE
                } else if (randomNumber <= insertProportion + updateProportion) {
                    boolean isDone = false;
                    while (!isDone) {
                        int randomIndex = randomDataGenerator.generateRandomInteger(0, initialWorkloadData.getWorkload().size() - 1);
                        WorkloadEvent workloadEvent = initialWorkloadData.getWorkload().get(randomIndex);
                        SingleDataSet singleDataSet = workloadEvent.getSingleDataSet();
                        if (!relevantTupels.getLoad().contains(singleDataSet)) {
                            // update notification
                            HashMap<String, String> data = new HashMap<String, String>();
                            data.put("fieldOne", Integer.toString(randomDataGenerator.generateRandomInteger(1, 1000)));
                            data.put("fieldTwo", Double.toString(randomDataGenerator.generateRandomDouble(1, 1000)));
                            data.put("fieldThree", "RTDBSP");
                            data.put("fieldFour", Integer.toString(randomDataGenerator.generateRandomInteger(1001, 10000)));
                            data.put("fieldFive", Double.toString(randomDataGenerator.generateRandomDouble(1001, 10000)));
                            data.put("fieldSix", randomDataGenerator.generateRandomString(12, true, false));
                            data.put("fieldSeven", Integer.toString(randomDataGenerator.generateRandomInteger(10001, 100000)));
                            data.put("fieldEight", Double.toString(randomDataGenerator.generateRandomDouble(10001, 100000)));
                            data.put("fieldNine", randomDataGenerator.generateRandomString(18, true, false));
                            data.put("number", singleDataSet.getData().get("number"));
                            SingleDataSet newSingleDataSet = new SingleDataSet(singleDataSet.getUuid(), data);

                            relevantTupels.addSingleDataSet(newSingleDataSet);
                            irrelevantTupels.getLoad().remove(newSingleDataSet);
                            WorkloadEvent newWorkloadEvent = new WorkloadEvent(transactionID, WorkloadEventType.UPDATE, true, newSingleDataSet);
                            workload.addWorkloadEvent(newWorkloadEvent);
                            isDone = true;

                            forbiddenIDs.add(newSingleDataSet.getUuid());
                        }
                    }
                }
            } else {
                HashMap<String, String> data = new HashMap<String, String>();
                data.put("fieldOne", Integer.toString(randomDataGenerator.generateRandomInteger(1, 1000)));
                data.put("fieldTwo", Double.toString(randomDataGenerator.generateRandomDouble(1, 1000)));
                data.put("fieldThree", randomDataGenerator.generateRandomString(6, true, false));
                data.put("fieldFour", Integer.toString(randomDataGenerator.generateRandomInteger(1001, 10000)));
                data.put("fieldFive", Double.toString(randomDataGenerator.generateRandomDouble(1001, 10000)));
                data.put("fieldSix", randomDataGenerator.generateRandomString(12, true, false));
                data.put("fieldSeven", Integer.toString(randomDataGenerator.generateRandomInteger(10001, 100000)));
                data.put("fieldEight", Double.toString(randomDataGenerator.generateRandomDouble(10001, 100000)));
                data.put("fieldNine", randomDataGenerator.generateRandomString(18, true, false));

                if (randomNumber <= insertProportion) {
                    data.put("number", Integer.toString(initialWorkloadData.getWorkload().size() + 1));
                    SingleDataSet newSingleDataSet = new SingleDataSet(UUID.randomUUID(), data);

                    irrelevantTupels.addSingleDataSet(newSingleDataSet);
                    WorkloadEvent newWorkloadEvent = new WorkloadEvent(transactionID, WorkloadEventType.INSERT, false, newSingleDataSet);
                    initialWorkloadData.addWorkloadEvent(newWorkloadEvent);
                    workload.addWorkloadEvent(newWorkloadEvent);

                    forbiddenIDs.add(newSingleDataSet.getUuid());
                } else if (randomNumber <= insertProportion + updateProportion) {
                    int randomIndex = randomDataGenerator.generateRandomInteger(0, irrelevantTupels.getLoad().size() - 1);
                    SingleDataSet singleDataSet = irrelevantTupels.getLoad().get(randomIndex);
                    data.put("number", singleDataSet.getData().get("number"));
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
