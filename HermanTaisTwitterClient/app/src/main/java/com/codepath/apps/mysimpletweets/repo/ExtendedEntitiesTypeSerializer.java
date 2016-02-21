package com.codepath.apps.mysimpletweets.repo;

import com.activeandroid.serializer.TypeSerializer;
import com.codepath.apps.mysimpletweets.Common;
import com.codepath.apps.mysimpletweets.models.ExtendedEntities;
import com.google.gson.Gson;

public class ExtendedEntitiesTypeSerializer extends TypeSerializer {
    private Gson mGson = Common.getGson();

    @Override
    public Class<?> getDeserializedType() {
        return ExtendedEntities.class;
    }

    @Override
    public Class<?> getSerializedType() {
        return String.class;
    }

    @Override
    public Object serialize(Object data) {
        return mGson.toJson(data);
    }

    @Override
    public Object deserialize(Object data) {
        return mGson.fromJson(data.toString(), ExtendedEntities.class);
    }
}
