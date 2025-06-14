package net.cvs0.context;

import java.util.HashMap;
import java.util.Map;

public class TransformerContext
{
    private final Map<String, Object> transformerData = new HashMap<>();
    
    public void setData(String key, Object value)
    {
        transformerData.put(key, value);
    }
    
    public Object getData(String key)
    {
        return transformerData.get(key);
    }
    
    public <T> T getData(String key, Class<T> type)
    {
        Object value = transformerData.get(key);
        if (value != null && type.isInstance(value)) {
            return type.cast(value);
        }
        return null;
    }
    
    public boolean hasData(String key)
    {
        return transformerData.containsKey(key);
    }
    
    public void clearData()
    {
        transformerData.clear();
    }
}
