package com.example.entrance;

import com.example.deal.Enimal;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;


@Component
public class MyRunner implements CommandLineRunner {
    @Override
    public void run(String... args) throws Exception {
        Enimal e  = new Enimal();
        for (Field f : e.getClass().getDeclaredFields()) {
            f.set
        }
    }
}
