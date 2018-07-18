package cn.adonis.rpc.serialize;

import cn.adonis.rpc.serialize.impl.HessianSerializer;
import cn.adonis.rpc.serialize.impl.JavaDefaultSerializer;
import cn.adonis.rpc.serialize.impl.ProtoStuffSerializer;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

public class SerializerFactory {
    private static final Map<String, Serializer> SERIALIZER_MAP = new HashMap<String, Serializer>() {{
        put("default", new JavaDefaultSerializer());
        put("hessian", new HessianSerializer());
        put("protostuff", new ProtoStuffSerializer());
    }};

    public static Serializer create(String serializeType) {
        if (StringUtils.isEmpty(serializeType)) {
            return SERIALIZER_MAP.get("hessian");
        }
        return SERIALIZER_MAP.getOrDefault(serializeType.toLowerCase().trim(), SERIALIZER_MAP.get("hessian"));
    }

}
