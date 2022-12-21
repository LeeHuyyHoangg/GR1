package localsearch.applications.idpcndu_network.util;

public class Timer {
    private long initTime;
    private long lastCalledTime;

    public Timer(){
        initTime = System.currentTimeMillis();
        lastCalledTime = initTime;
    }

    public long getTimeInterval(){
        long result = System.currentTimeMillis() - lastCalledTime;
        lastCalledTime = System.currentTimeMillis();
        return result;
    }

    public long getTotalTime(){
        return System.currentTimeMillis() - initTime;
    }
}
