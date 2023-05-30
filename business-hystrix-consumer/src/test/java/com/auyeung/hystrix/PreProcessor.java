package com.auyeung.hystrix;


public interface PreProcessor {


    void process();

    default void log() {
        System.out.println(PreProcessor.class.getCanonicalName() + " process log");
    }
}

