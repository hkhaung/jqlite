package hkhaung;

import java.util.Iterator;
import java.util.List;

public class QueryResult<T> implements Iterable<T> {
    private List<T> results;  // Generic list to hold the results (it could be any type of collection)
    private boolean success;  // Flag to indicate if the query was successful
    private String errorMessage;
    private int totalResults;

    // Constructor
    public QueryResult(List<T> results, boolean success, String errorMessage, int totalResults) {
        this.results = results;
        this.success = success;
        this.errorMessage = errorMessage;
        this.totalResults = totalResults;
    }

    // Getters and Setters
    public List<T> getResults() {
        return results;
    }

    public void setResults(List<T> results) {
        this.results = results;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public int getTotalResults() {
        return totalResults;
    }

    public void setTotalResults(int totalResults) {
        this.totalResults = totalResults;
    }

    @Override
    public String toString() {
        return "QueryResult{" +
                "results=" + results +
                ", success=" + success +
                ", errorMessage='" + errorMessage + '\'' +
                ", totalResults=" + totalResults +
                '}';
    }

    @Override
    public Iterator<T> iterator() {
        return results.iterator();
    }
}


