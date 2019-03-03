package com.aaurensanz.hashcode.utils;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Photo {
    String id;
    Character orientation;
    Integer numberOfTags;
    List<String> tags = new ArrayList<>();
    Integer interestScore;
}
