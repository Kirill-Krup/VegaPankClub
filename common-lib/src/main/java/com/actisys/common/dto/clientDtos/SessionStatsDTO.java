package com.actisys.common.dto.clientDtos;

public class SessionStatsDTO {
  private int totalSessions;
  private double totalGameHour;

  public SessionStatsDTO(int i, double v) {
    totalSessions = i;
    totalGameHour = v;
  }
  public SessionStatsDTO() {}

  public int getTotalSessions() {
    return totalSessions;
  }

  public void setTotalSessions(int totalSessions) {
    this.totalSessions = totalSessions;
  }

  public double getTotalGameHour() {
    return totalGameHour;
  }

  public void setTotalGameHour(double totalGameHour) {
    this.totalGameHour = totalGameHour;
  }
}
