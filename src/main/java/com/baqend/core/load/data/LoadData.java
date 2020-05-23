package com.baqend.core.load.data;

import java.util.ArrayList;

public class LoadData {
    private ArrayList<SingleDataSet> load;

    public LoadData() {
        this.load = new ArrayList<SingleDataSet>();
    }

    public LoadData(ArrayList<SingleDataSet> load) {
        this.load = load;
    }

    public ArrayList<SingleDataSet> getLoad() {
        return load;
    }

    public void setLoad(ArrayList<SingleDataSet> load) {
        this.load = load;
    }

    public void addSingleDataSet(SingleDataSet singleDataSet) {
        this.load.add(singleDataSet);
    }

}