package com.example.demo.service;

import java.util.List;

public record StylistEvaluateResult(long reviewCount, double reviewAverage, List<String> comments) {

}
