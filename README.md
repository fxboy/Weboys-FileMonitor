# 使用方法
```java
 FileMonitor fileMonitor = FileMonitor.builder().setPath("监控路径")
                .withIdentity("test","test")
                .bindListenAdaptor(new FileListenerAdaptor() {
                    @Override
                    public void onCreate(File file) {
                        System.out.println("创建：" + file.getName());
                    }
                    @Override
                    public void onDelete(File file) {
                        System.out.println("删除：" + file.getName());
                    }
                })
                //.filter(".txt",".pdf")
                .time(1).build();
        fileMonitor.start();
```