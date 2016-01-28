package com.beijunyi.parallelgit.web.connection;

import java.util.Map;

public class TitledMessage {

  private String title;
  private Map<String, String> data;

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public Map<String, String> getData() {
    return data;
  }

  public void setData(Map<String, String> data) {
    this.data = data;
  }
}
