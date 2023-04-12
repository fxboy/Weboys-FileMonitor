package icu.weboys.io.monitor;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileMonitor {
    public final static String CONFIG = "config";
    private static Scheduler scheduler ;
    private JobDetail job;
    private Trigger trigger;
    private int time;

    static {{
        try {
            scheduler =  StdSchedulerFactory.getDefaultScheduler();
            scheduler.start();
        } catch (SchedulerException e) {
            throw new RuntimeException(e);
        }
    }}

    public static FileMonitorBuild builder(){
        return new FileMonitorBuild();
    };

    public FileMonitor(JobDetail job, int time) {
        this.job = job;
        this.time = time;
        this.trigger =  TriggerBuilder.newTrigger()
                .startNow()
                .withSchedule(SimpleScheduleBuilder.repeatSecondlyForever(time))
                .build();
    }

    // 加入任务列表
    public boolean start(){
        Boolean b = false;
       try{
           scheduler.scheduleJob(this.job,this.trigger);
           b = true;
        } catch (SchedulerException e) {
           e.printStackTrace();
       }finally {
           return b;
       }
    }

    // 移除任务列表
    public Boolean stop(){
        Boolean b = false;
        try{
            scheduler.deleteJob(this.job.getKey());
            b = true;
        } catch (SchedulerException e) {
            e.printStackTrace();
        }finally {
            return b;
        }
    }


    // 定时器关闭
    public static void shutdown() throws SchedulerException {
        scheduler.shutdown(true);
    }

    static class FileMonitorBuild{
        private String name;
        private String group;
        private int seconds;
        private String[] filter;
        private Path path;

        private FileListenerAdaptor listenerAdaptor;

        public FileMonitorBuild withIdentity(String name,String group){
            this.name = name;
            this.group = group;
            return this;
        }

        public FileMonitorBuild time(int seconds){
            this.seconds = seconds;
            return this;
        }

        public  FileMonitorBuild filter(String... filters){
            this.filter = filters;
            return this;
        }

        public  FileMonitorBuild bindListenAdaptor(FileListenerAdaptor listenerAdaptor){
            this.listenerAdaptor = listenerAdaptor;
            return this;
        }
        public  FileMonitorBuild setPath(String path){
            this.path = Paths.get(path);
            return this;
        }
        public FileMonitor build(){
            JobDataMap c = new JobDataMap();
            c.put(FileMonitor.CONFIG,new FileExecutorAdaptor(String.format("%s.%s",this.group,this.name),listenerAdaptor,path,filter));
            return new FileMonitor(JobBuilder.newJob(FileMonitorJob.class).withIdentity(this.name,this.group).setJobData(c).build(),seconds);
        }
    }
}




