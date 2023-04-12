package icu.weboys.io.monitor;

import org.quartz.*;

public class FileMonitorJob implements StatefulJob {
    @Override
    public void execute(JobExecutionContext jobExecutionContext){
      FileExecutorAdaptor adaptor =   (FileExecutorAdaptor) jobExecutionContext.getMergedJobDataMap().get(FileMonitor.CONFIG);
      adaptor.execute();
    }
}
