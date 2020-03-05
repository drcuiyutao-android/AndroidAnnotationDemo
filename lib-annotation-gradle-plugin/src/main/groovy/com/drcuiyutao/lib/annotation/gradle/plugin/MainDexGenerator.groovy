package com.drcuiyutao.lib.annotation.gradle.plugin

import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.Task

import java.nio.file.Files
import java.nio.file.StandardOpenOption
import java.util.stream.Collectors

@CompileStatic
class MainDexGenerator implements Action<Task> {
    private static final String MAIN_DEX_FILE = 'mainDexList.txt'

    // list we want to exclude frome MainDex
    private List<String> excludedList

    MainDexGenerator(List<String> excludedPackages) {
        excludedList = excludedPackages
    }

    @Override
    void execute(Task task) {
        for (File inputFile : task.inputs.files.files) {
            if (inputFile.absolutePath.endsWith(MAIN_DEX_FILE)) {
                List<String> result = Files.lines(inputFile.toPath())
                        .filter { isNotMatch(it) }
                        .collect(Collectors.toList())
                Files.write(inputFile.toPath(), result, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)
                break
            }
        }
    }

    private boolean isNotMatch(String line) {
        for (String item : excludedList) {
            if (line.contains(item)) {
                return false
            }
        }
        return true
    }
}