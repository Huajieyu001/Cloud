package com.heima.item.test;

import org.checkerframework.checker.units.qual.A;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@SpringBootTest
public class test {

    @Test
    void test1(){
        List<Object> list = Arrays.asList();
        list.add("hello");
    }

    @Test
    void test2(){
        String [] strs = {"AAA", "BBB", "CCC"};
        List<String> list1 = Arrays.asList(strs);

        ArrayList<String> list2 = new ArrayList(list1);
        list2.add("XXX");

        list2.forEach(System.out::println);
    }

    @Test
    void test3(){
        String [] strs = {"AAA", "BBB", "CCC"};
        List<String> list1 = Arrays.asList(strs);

        ArrayList<String> list2 = new ArrayList<>();
        Collections.addAll(list2, strs);

        list2.add("YYY");
        list2.forEach(System.out::println);
    }

    @Test
    void test4(){
        String [] strs = {"AAA", "BBB", "CCC"};
        List<String> list1 = Arrays.asList(strs);

        ArrayList<String> list2 = new ArrayList<>();

        list1.forEach(e -> {
            list2.add(e);
        });

        list2.add("ZZZ");
        list2.forEach(System.out::println);
    }

    @Test
    void test5(){
        String [] strs = {"AAA", "BBB", "CCC"};
        List<String> list1 = Arrays.asList(strs);

        ArrayList<String> list2 = new ArrayList<>();
        list2.addAll(list1);

        list2.add("OOO");
        list2.forEach(System.out::println);
    }

}
