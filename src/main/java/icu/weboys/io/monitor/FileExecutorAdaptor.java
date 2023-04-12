package icu.weboys.io.monitor;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileExecutorAdaptor{
    private Path path;
    private Path recordPath;
    // 缓存文件，用于记录是否读入写入
    private File record;
    private FileListenerAdaptor listenerAdaptor;
    private FileObFilter filter;
    // 内存记录
    private Map<Path, Boolean> recordMap =  new ConcurrentHashMap();

    private String jobName;

    public FileExecutorAdaptor(String jobName,FileListenerAdaptor listenerAdaptor, Path path,String[] filter){
        this.listenerAdaptor = listenerAdaptor;
        this.path = path;
        // 设置过滤器
        this.filter = filter !=null ?new FileObFilter(filter):new FileObFilter();
        this.jobName = jobName;
        // 初始化
        initialize();
    }
    // 将内存全部读取出来
    private void initialize(){
        // 创建隐藏文件夹
        new File(System.getProperty("user.home"), ".fmtmp").mkdir();
        this.recordPath = Paths.get(System.getProperty("user.home") + "\\.fmtmp", this.jobName);
        this.recordPath.toFile().mkdir();
        // 此处记录着之前读取过的数据
        Map<Path,Boolean> re = new HashMap<>();
        // 从文件记录中读取
        try (Stream<Path> records = Files.walk(this.recordPath, 1)) {
           re.putAll(records.filter(Files::isRegularFile).distinct().collect(Collectors.toMap(k->k.getFileName(),v->true)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        //this.path.
        // 读取启动后文件夹内列表
        try (Stream<Path> paths = Files.walk(this.path, 1)) {
            // .filter(k->!this.recordMap.containsKey(k))
           recordMap.putAll(paths.filter(Files::isRegularFile).filter(k-> this.filter.isFI(k))
                    // 如果配置中设置了需要启动后重新执行其创建方法，则这里填写为false
                    .distinct().collect(Collectors.toMap(k->k,v->false)));
           // 将之前读取过的数据改为true
           recordMap.keySet().stream().filter(k->re.containsKey(k.getFileName())).forEach(k-> recordMap.put(k,true));
        } catch (IOException e) {
           e.printStackTrace();
        }
    }
    public void execute()  {
        // 获取新增文件
        Map<Path,Boolean> re = new HashMap<>();
        try (Stream<Path> paths = Files.walk(this.path, 1)) {
            // 获取未记录的文件
           Stream<Path> append =  paths.filter(Files::isRegularFile).filter(k-> this.filter.isFI(k)).filter(k->!this.recordMap.containsKey(k));
           append.forEach(k ->{
              try{
                  listenerAdaptor.onCreate(k.toFile());
                  re.put(k,true);
                  // 写入读取记录
                  Files.createFile(Paths.get(new File(this.recordPath.toFile(),k.toFile().getName()).getPath()));
              }catch (Exception exception){
                  re.put(k,false);
                  exception.printStackTrace();
              }
           });
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
           if(re.size()> 0){
               recordMap.putAll(re);
           }
        }

        // 执行删除
       List<Path> removes = new ArrayList<>();
       try{recordMap.keySet().stream().filter(k->!Files.exists(k)).forEach(
                k->{
                   try{
                       listenerAdaptor.onDelete(k.toFile());
                       Files.delete(Paths.get(new File(this.recordPath.toFile(),k.toFile().getName()).getPath()));
                   }catch (Exception e){
                       e.printStackTrace();
                   }finally {
                       removes.add(k);
                   }
                });
       }catch (Exception exception){
           exception.printStackTrace();
       }finally {
          if(removes.size()>0){
              removes.forEach(k->recordMap.remove(k));
          }
       }



        // 获取字典中 v =  false的值重新执行,避免漏掉一些东西
        Map<Path,Boolean> lou = new HashMap<>();
        try{
            recordMap.keySet().stream().filter(k->!recordMap.get(k)).forEach(k->{
                try{
                    listenerAdaptor.onCreate(k.toFile());
                    lou.put(k,true);
                    Files.createFile(Paths.get(new File(this.recordPath.toFile(),k.toFile().getName()).getPath()));
                }catch (Exception exception){
                    exception.printStackTrace();
                }
            });
        }catch (Exception exception){
            exception.printStackTrace();
        }finally {
            if(lou.size()> 0){
                recordMap.putAll(lou);
            }
        }
    }
}
