package com.diegeilstegruppe.sasha.witAi;


import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Entities {

    @SerializedName("search_query")
    @Expose
    private List<SearchQuery> searchQuery = null;
    @SerializedName("intent")
    @Expose
    private List<Intent> intent = null;

    public List<SearchQuery> getSearchQuery() {
        return searchQuery;
    }

    public void setSearchQuery(List<SearchQuery> searchQuery) {
        this.searchQuery = searchQuery;
    }

    public List<Intent> getIntent() {
        return intent;
    }

    public void setIntent(List<Intent> intent) {
        this.intent = intent;
    }

}
