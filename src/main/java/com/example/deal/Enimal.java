package com.example.deal;

/**
 * 静态方法、静态代码块里不能访问 非静态字段
 * 代码块{}里使用的字段，必须定义在代码块的前面
 * 正常情况下，代码块、字段的初始化（赋值）是从上往下依次处理的，静态代码块特殊会优先先处理，字段和代码块按顺序依次执行（谁排上先处理谁
 */
public class Enimal {
    {
        System.out.println("Enimal普通代码块1"  + " a: " + a);
    }
    int getValue2() {
        System.out.println("Enimal方法getValue2" + " a: " + a+ " b: " + b + " c: " + c + " d: " + d
                + " f: " + f + " e: " + e);
        return 5;
    }

    static int a = 10;
    int f = 1;
    int b = getValue();
    int c = 3;
    int e = getValue2();
    static {
        System.out.println("Enimal静态块1" + " a: " + a);
    }

    public Enimal() {
        System.out.println("Enimal构造方法" + " a: " + a+ " b: " + b + " c: " + c + " d: " + d
                + " f: " + f  + " e: " + e);
    }

    static void testPrint() {
        System.out.println("Enimal静态方法1" + " a: " + a);
    }

    int getValue() {
        System.out.println("Enimal方法getValue" + " a: " + a+ " b: " + b + " c: " + c + " d: " + d
                + " f: " + f  + " e: " + e);
        return 2;
    }
    {
        System.out.println("Enimal普通代码块2" + " a: " + a + " b: " + b + " c: " + c
                + " f: " + f  + " e: " + e);
    }
    static {
        System.out.println("Enimal静态块2  " + a);
    }
    int d = 4;
}
