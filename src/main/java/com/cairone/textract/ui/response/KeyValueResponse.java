package com.cairone.textract.ui.response;

import java.util.Map;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class KeyValueResponse {

    private final Map<String, String> data;
}
