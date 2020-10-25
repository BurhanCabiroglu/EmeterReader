package com.example.myapplication;


import android.text.format.Formatter;

import androidx.annotation.Nullable;

public class StringFormat {

    String element_one ;
    String element_two;
    String element_tree;
    byte[] mFormat=new byte[]{27,33,0};
    /*public StringFormat(String element_one){
        this.element_one=element_one;
    }
    public StringFormat(String element_one String element_two){
        this.element_one=element_one;
        this.element_two=element_two;
    }*/


    public StringFormat(String element_one,String element_two,String element_tree){
        this.element_one=element_one;
        this.element_two=element_two;
        this.element_tree=element_tree;
    }

    public StringFormat(String element_one, String element_two){
        this(element_one,element_two,null);
    }

    public StringFormat(String element_one){
        this(element_one,null,null);
    }

    public String format1(){
        String printted;

        printted=String.format("%60s",element_one)+"\n";
        return printted;
    }

    public String format2(){
        String printted;
        printted=String.format("%25s %30s",element_one,element_two)+"\n";
        return printted;
    }
    public String format3(){
        String printted;
        printted=String.format("%30s %12s %15s",element_one,element_two,element_tree)+"\n";
        return printted;
    }


}
