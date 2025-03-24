package org.exclover;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Sorgu sonuçlarını saran ve tip güvenli erişim sağlayan sınıf
 */
public class QueryResult {
    private final Map<String, Object> data;

    /**
     * QueryResult constructor
     * @param data Sorgu sonuç verisi
     */
    public QueryResult(Map<String, Object> data) {
        this.data = data;
    }

    /**
     * Map verisi üzerinden QueryResult listesi oluşturur
     * @param mapList Sorgu sonuç listesi
     * @return QueryResult listesi
     */
    public static List<QueryResult> fromList(List<Map<String, Object>> mapList) {
        List<QueryResult> resultList = new ArrayList<>();
        for (Map<String, Object> map : mapList) {
            resultList.add(new QueryResult(map));
        }
        return resultList;
    }

    /**
     * String değeri döndürür
     * @param key Sütun adı
     * @return Değer
     */
    public String getString(String key) {
        return getString(key, "");
    }

    /**
     * String değeri döndürür
     * @param key Sütun adı
     * @param defaultValue Varsayılan değer
     * @return Değer
     */
    public String getString(String key, String defaultValue) {
        Object value = data.get(key);
        return value != null ? value.toString() : defaultValue;
    }

    /**
     * Integer değeri döndürür
     * @param key Sütun adı
     * @return Değer
     */
    public int getInt(String key) {
        return getInt(key, 0);
    }

    /**
     * Integer değeri döndürür
     * @param key Sütun adı
     * @param defaultValue Varsayılan değer
     * @return Değer
     */
    public int getInt(String key, int defaultValue) {
        Object value = data.get(key);
        if (value == null) return defaultValue;
        
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Long değeri döndürür
     * @param key Sütun adı
     * @return Değer
     */
    public long getLong(String key) {
        return getLong(key, 0L);
    }

    /**
     * Long değeri döndürür
     * @param key Sütun adı
     * @param defaultValue Varsayılan değer
     * @return Değer
     */
    public long getLong(String key, long defaultValue) {
        Object value = data.get(key);
        if (value == null) return defaultValue;
        
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Double değeri döndürür
     * @param key Sütun adı
     * @return Değer
     */
    public double getDouble(String key) {
        return getDouble(key, 0.0);
    }

    /**
     * Double değeri döndürür
     * @param key Sütun adı
     * @param defaultValue Varsayılan değer
     * @return Değer
     */
    public double getDouble(String key, double defaultValue) {
        Object value = data.get(key);
        if (value == null) return defaultValue;
        
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        
        try {
            return Double.parseDouble(value.toString());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Boolean değeri döndürür
     * @param key Sütun adı
     * @return Değer
     */
    public boolean getBoolean(String key) {
        return getBoolean(key, false);
    }

    /**
     * Boolean değeri döndürür
     * @param key Sütun adı
     * @param defaultValue Varsayılan değer
     * @return Değer
     */
    public boolean getBoolean(String key, boolean defaultValue) {
        Object value = data.get(key);
        if (value == null) return defaultValue;
        
        if (value instanceof Boolean) {
            return (Boolean) value;
        } else if (value instanceof Number) {
            return ((Number) value).intValue() != 0;
        }
        
        String strValue = value.toString().toLowerCase();
        return "true".equals(strValue) || "1".equals(strValue);
    }

    /**
     * Date değeri döndürür
     * @param key Sütun adı
     * @return Değer
     */
    public Date getDate(String key) {
        return getDate(key, null);
    }

    /**
     * Date değeri döndürür
     * @param key Sütun adı
     * @param defaultValue Varsayılan değer
     * @return Değer
     */
    public Date getDate(String key, Date defaultValue) {
        Object value = data.get(key);
        if (value == null) return defaultValue;
        
        if (value instanceof Date) {
            return (Date) value;
        } else if (value instanceof java.sql.Date) {
            return new Date(((java.sql.Date) value).getTime());
        } else if (value instanceof java.sql.Timestamp) {
            return new Date(((java.sql.Timestamp) value).getTime());
        }
        
        return defaultValue;
    }

    /**
     * Değerin var olup olmadığını kontrol eder
     * @param key Sütun adı
     * @return Değer varsa true
     */
    public boolean has(String key) {
        return data.containsKey(key) && data.get(key) != null;
    }

    /**
     * Tüm sütun adlarını döndürür
     * @return Sütun adları kümesi
     */
    public java.util.Set<String> getColumns() {
        return data.keySet();
    }
    
    /**
     * Tüm veriyi döndürür
     * @return Veri haritası
     */
    public Map<String, Object> getData() {
        return data;
    }

    /**
     * String temsilini döndürür
     * @return String temsili
     */
    @Override
    public String toString() {
        return data.toString();
    }
} 