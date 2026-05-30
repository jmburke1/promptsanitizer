/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 Jason Burke
 */
package promptsanitizer.model;

import org.json.JSONObject;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class SanitizerModel {
    public void init(String fileName, String regexFileName) {
        this.fileName = fileName;
        this.regexFileName = regexFileName;
    }
    private Map<String, String> dictionary;
    private Map<String, String> leftToRightDictionary;
    private Map<String, String> rightToLeftDictionary;
    private String fileName;
    private String regexFileName;

    public boolean isValidDictionary() {
        return (dictionary != null) || (leftToRightDictionary != null) || (rightToLeftDictionary != null);
    }
    private boolean notNullAndNotEmpty(Map<String, String> dictionary) {
        return dictionary != null && !dictionary.isEmpty();
    }
    public boolean isStronglyValidDictionary() {
        return notNullAndNotEmpty(dictionary) || notNullAndNotEmpty(leftToRightDictionary) || notNullAndNotEmpty(rightToLeftDictionary);
    }
    public void invalidateDictionary() {
        dictionary = null;
        leftToRightDictionary = null;
        rightToLeftDictionary = null;
    }
    /** Load the personal dictionary from disk. Returns an empty map if the file doesn't exist. */
    public void loadDictionary() {
        loadSimpleDictionary();
        loadRegexDictionary();
    }
    private void loadSimpleDictionary() {
        File f = new File(fileName);
        if (!f.exists()) {
            return;
        }
        try {
            JSONObject json = new JSONObject(Files.readString(Path.of(fileName)));
            dictionary = new HashMap<>();
            for (String k : json.keySet()) {
                dictionary.put(k, json.getString(k));
            }
        } catch (Exception ex) {
            dictionary = Map.of();
        }
    }
    private void loadRegexDictionary() {
        File f = new File(regexFileName);
        if (!f.exists()) {
            return;
        }
        try {
            JSONObject json = new JSONObject(Files.readString(Path.of(regexFileName)));
            rightToLeftDictionary = new HashMap<>();
            leftToRightDictionary = new HashMap<>();
            for (String k : json.keySet()) {
                JSONObject jo = json.getJSONObject(k);
                if(jo.getString("dir").equals("<")) {
                    rightToLeftDictionary.put(k, jo.getString("repl"));
                } else if(jo.getString("dir").equals(">")) {
                    leftToRightDictionary.put(k, jo.getString("repl"));
                } else {
                    System.err.println("direction must be either < or >");
                    throw new IllegalArgumentException();
                }
            }
        } catch (Exception ex) {
            leftToRightDictionary = Map.of();
            rightToLeftDictionary = Map.of();
        }
    }

    /** Apply all replacements from the dictionary, in the appropriate direction, to the given text. */
    public String applyDictionary(String text, boolean isReverseDirection) {
        if(isReverseDirection && rightToLeftDictionary != null) {
            for (Map.Entry<String, String> entry : rightToLeftDictionary.entrySet()) {
                text = text.replaceAll(entry.getKey(), entry.getValue());
            }
        }
        if(dictionary != null) {
            for (Map.Entry<String, String> entry : dictionary.entrySet()) {
                if (isReverseDirection) {
                    text = text.replace(entry.getValue(), entry.getKey());
                } else {
                    text = text.replace(entry.getKey(), entry.getValue());
                }
            }
        }
        if(!isReverseDirection && leftToRightDictionary != null) {
            for (Map.Entry<String, String> entry : leftToRightDictionary.entrySet()) {
                text = text.replaceAll(entry.getKey(), entry.getValue());
            }
        }
        return text;
    }
}
