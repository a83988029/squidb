/*
 * Copyright 2015, Yahoo Inc.
 * Copyrights licensed under the Apache 2.0 License.
 * See the accompanying LICENSE file for terms.
 */
package com.yahoo.squidb.json;

import com.yahoo.squidb.data.AbstractModel;
import com.yahoo.squidb.sql.Property.StringProperty;
import com.yahoo.squidb.utility.Logger;

import java.lang.reflect.Type;

/**
 * Business logic of managing serialization and deserialization of JSON properties. Clients of this plugin should
 * be sure to call {@link #setJSONMapper(JSONMapper)} to initialize the serialization engine. See the JSONPropertyTest
 * class in the squidb tests project for example implementations of the JSONMapper interface.
 */
public class SquidbJSONSupport {

    private static final String TAG = "squidb-json";

    public static JSONMapper MAPPER;

    public static void setJSONMapper(JSONMapper jsonMapper) {
        MAPPER = jsonMapper;
    }

    @SuppressWarnings("unchecked")
    /**
     * Deserialize a JSON string property into the specified Java type
     */
    public static <T> T getObjectValue(AbstractModel model, StringProperty property, Type javaType) {
        if (!model.hasTransitory(property.getName())) {
            T data = null;
            if (model.containsNonNullValue(property)) {
                String value = model.get(property);
                try {
                    data = MAPPER.fromJson(value, javaType);
                } catch (Exception e) {
                    Logger.w(TAG, "Error deserializing JSON string: " + value, e);
                    model.clearValue(property);
                }
            }
            model.putTransitory(property.getName(), data);
            return data;
        }

        return (T) model.getTransitory(property.getName());
    }

    /**
     * Sets the given JSON-serialized property to the given value
     *
     * @return true if the value object was successfully serialized, false otherwise
     */
    public static boolean setObjectProperty(AbstractModel model, StringProperty property, Object data) {
        try {
            String json = null;
            if (data != null) {
                json = MAPPER.toJSON(data);
                if (model.containsNonNullValue(property) && json.equals(model.get(property))) {
                    return false;
                }
            }
            model.set(property, json);
            model.putTransitory(property.getName(), data);
            return true;
        } catch (Exception e) {
            Logger.w(TAG, "Error serializing object to JSON string: " + data, e);
            return false;
        }
    }

}
