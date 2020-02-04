package com.abc;

import com.def.MyInterface;
import com.zy.welfare.entity.DefaultIdEntity;
import org.apache.commons.lang3.StringUtils;

public class Demo1 implements Runnable, MyInterface {
    public void run() {
        System.out.println("this is the demo running");
        StringUtils.isBlank("abc");

        DefaultIdEntity<Integer> a = new DefaultIdEntity<>();
        a.setId(1234);
        System.out.println("a.id2 = " + a.getId());

        MyInterface mm = this;
        mm.execute();

        this.execute();
    }

    public void execute() {
        System.out.println("this is the demo executing");
    }
}
