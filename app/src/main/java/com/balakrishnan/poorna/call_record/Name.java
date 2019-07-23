package com.balakrishnan.poorna.call_record;

import java.util.ArrayList;

public class Name {
    String name;
    String num;
    Name(String name,String num){
        this.name=name;
        this.num=num;
    }
    Name(){}
    public String getName(){
        return this.name;
    }

    public String getNum() {
        return num;
    }
}
