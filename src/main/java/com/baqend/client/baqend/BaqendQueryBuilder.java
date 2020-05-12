package com.baqend.client.baqend;

public class BaqendQueryBuilder {

    public BaqendQueryBuilder() {
    }

    public String translateQuery(String userSession, String query) {
        // TODO: do the query mapping from SQL to baqend query string
        String queryString = "{\n" +
                "  \"id\": \"" + userSession + "\",\n" +
                "  \"type\": \"subscribe\",\n" +
                "  \"token\": null,\n" +
                "  \"initial\": true,\n";
        queryString = queryString.concat("  \"bucket\": \"Test\",\n");
        queryString = queryString.concat(
                "  \"query\": \"" + query + "\",\n");
        queryString = queryString.concat("  \"operations\": [\n" +
                "    \"any\"\n" +
                "  ],\n" +
                "  \"matchTypes\": [\n" +
                "    \"all\"\n" +
                "  ]\n}");

        return queryString;
    }
}

/*String queryString1 = "{\n" +
                "  \"id\": \"" + webSocketClient.userSession.getId().toString() + "\",\n" +
                "  \"type\": \"subscribe\",\n" +
                "  \"token\": null,\n" +
                "  \"initial\": true,\n" +
                "  \"bucket\": \"Test\",\n" +
                "  \"query\": \"{\\\"testName\\\": \\\"Patrick\\\"}\",\n" +
                "  \"operations\": [\n" +
                "    \"any\"\n" +
                "  ],\n" +
                "  \"matchTypes\": [\n" +
                "    \"all\"\n" +
                "  ]\n}";*/