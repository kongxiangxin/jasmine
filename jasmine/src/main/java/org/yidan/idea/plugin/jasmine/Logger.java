package org.yidan.idea.plugin.jasmine;

/**
 * Created by kongxiangxin on 2017/8/1.
 */
public interface Logger {

    void error(String message);

    void error(Exception e);

    void info(String message);

    void setProgress(int percent);

}
