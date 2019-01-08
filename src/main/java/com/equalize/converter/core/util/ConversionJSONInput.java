package com.equalize.converter.core.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public class ConversionJSONInput {
	private final JSONObject jsonObj;

	public ConversionJSONInput(String input) {
		this.jsonObj = new JSONObject(input.trim());
	}

	public ConversionJSONInput(String input, String topArrayName) {
		String content = input.trim();
		Object top;
		if (content.startsWith("[")) {
			top = new JSONArray(content);
		} else {
			top = new JSONObject(content);
		}
		this.jsonObj = new JSONObject();
		this.jsonObj.put(topArrayName, top);
	}

	public List<Field> extractJSONContent() {
		return parseJSON(this.jsonObj);
	}

	private List<Field> parseJSON(JSONObject jo) {
		List<Field> arr = new ArrayList<>();
		Iterator<String> keyIter = jo.keys();
		while (keyIter.hasNext()) {
			String keyName = keyIter.next();
			Object parsedObj = parseJSON(jo.get(keyName));
			arr.add(new Field(keyName, parsedObj));
		}
		return arr;
	}

	private Object[] parseJSON(JSONArray ja) {
		Object[] objects = new Object[ja.length()];
		for (int i = 0; i < ja.length(); i++) {
			Object parsedObj = parseJSON(ja.get(i));
			objects[i] = parsedObj;
		}
		return objects;
	}

	private Object parseJSON(Object obj) {
		if (obj instanceof JSONObject) {
			return parseJSON((JSONObject) obj);
		} else if (obj instanceof JSONArray) {
			return parseJSON((JSONArray) obj);
		} else if (obj == JSONObject.NULL) {
			return null;
		} else {
			return obj.toString();
		}
	}
}
