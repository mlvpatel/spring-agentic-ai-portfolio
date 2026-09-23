package com.portfolio.designrag.service;

import java.util.List;

public interface DesignCorpus {
    int ingestCsv(String csv);

    List<TokenDoc> retrieve(String query, int limit);

    int size();
}
