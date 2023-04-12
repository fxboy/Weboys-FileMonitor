package icu.weboys.io.monitor;

import java.nio.file.Path;

public class FileObFilter {
    String[] types;
    public FileObFilter(String[] types){
        this.types = types;
    }
    public FileObFilter(){

    }

    public Boolean isFI(Path a){
        if(types == null) return true;
        Boolean b = false;
        for (String type : types) {
           b = a.toString().endsWith(type);
           if(b) break;
        }
        return b;
    }
}
