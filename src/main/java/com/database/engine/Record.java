package com.database.engine;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Record implements Serializable {
    private static final long serialVersionUID = 1L;
    private Map<String, Object> data;
    private int id;
    
    public Record(int id) {
        this.id = id;
        this.data = new HashMap<>();
    }
    
    public void setField(String fieldName, Object value) {
        data.put(fieldName, value);
    }
    
    public Object getField(String fieldName) {
        return data.get(fieldName);
    }
    
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public Map<String, Object> getAllFields() {
        return new HashMap<>(data);
    }
    
    public void setAllFields(Map<String, Object> fields) {
        this.data = new HashMap<>(fields);
    }
    
    public boolean hasField(String fieldName) {
        return data.containsKey(fieldName);
    }
    
    public void removeField(String fieldName) {
        data.remove(fieldName);
    }
    
    public int getFieldCount() {
        return data.size();
    }
    
    @Override
    public String toString() {
        return "Record{id=" + id + ", data=" + data + "}";
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        Record record = (Record) obj;
        return id == record.id && data.equals(record.data);
    }
    
    @Override
    public int hashCode() {
        return id * 31 + data.hashCode();
    }
}