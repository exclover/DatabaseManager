package org.exclover;

import java.util.function.Consumer;

/**
 * InsertBuilder sınıfı - Veri eklemek için Fluent API sağlar
 */
public class InsertBuilder {
    private final DBManager database;
    private final String tableName;
    private long insertId = -1;

    /**
     * InsertBuilder constructor
     * @param database Database nesnesi
     * @param tableName Tablo adı
     */
    public InsertBuilder(DBManager database, String tableName) {
        this.database = database;
        this.tableName = tableName;
    }

    /**
     * String değeri ekler
     * @param columnName Sütun adı
     * @param value Değer
     * @return InsertBuilder nesnesi
     */
    public InsertBuilder setString(String columnName, String value) {
        database.setValue(columnName, value != null ? value : "");
        return this;
    }

    /**
     * Integer değeri ekler
     * @param columnName Sütun adı
     * @param value Değer
     * @return InsertBuilder nesnesi
     */
    public InsertBuilder setInteger(String columnName, int value) {
        database.setValue(columnName, value);
        return this;
    }

    /**
     * Double değeri ekler
     * @param columnName Sütun adı
     * @param value Değer
     * @return InsertBuilder nesnesi
     */
    public InsertBuilder setDouble(String columnName, double value) {
        database.setValue(columnName, value);
        return this;
    }

    /**
     * Boolean değeri ekler
     * @param columnName Sütun adı
     * @param value Değer
     * @return InsertBuilder nesnesi
     */
    public InsertBuilder setBoolean(String columnName, boolean value) {
        database.setValue(columnName, value);
        return this;
    }

    /**
     * Tarih değeri ekler
     * @param columnName Sütun adı
     * @param value Değer (java.util.Date)
     * @return InsertBuilder nesnesi
     */
    public InsertBuilder setDate(String columnName, java.util.Date value) {
        database.setValue(columnName, value);
        return this;
    }

    /**
     * Veriyi tabloya ekler
     * @return Eklenen satır ID'si
     */
    public long execute() {
        insertId = database.insertData(tableName);
        return insertId;
    }

    /**
     * Veriyi tabloya asenkron olarak ekler
     * @param callback Sonuç callback'i
     */
    public void executeAsync(Consumer<Long> callback) {
        database.insertDataAsync(tableName, id -> {
            insertId = id;
            if (callback != null) {
                callback.accept(id);
            }
        });
    }

    /**
     * Eklenen son kaydın ID'sini döndürür
     * @return ID değeri
     */
    public long getLastInsertId() {
        return insertId;
    }

    /**
     * Veriyi tabloda günceller
     * @param whereClause Güncelleme koşulu (örn. "id = ?")
     * @param params WHERE koşulu için parametreler
     * @return Güncellenen satır sayısı
     */
    public int executeUpdate(String whereClause, Object... params) {
        if (whereClause == null || whereClause.isEmpty()) {
            throw new IllegalArgumentException("Güncelleme için WHERE koşulu gereklidir");
        }
        
        StringBuilder sql = new StringBuilder();
        sql.append("UPDATE ").append(tableName).append(" SET ");
        
        boolean firstColumn = true;
        // values haritasını doğrudan DBManager'dan alamayız, bu yüzden kendi değerlerimizi kullanacağız
        java.util.Map<String, Object> values = new java.util.HashMap<>();
        
        try {
            // DBManager sınıfının values field'ına reflection ile erişim
            java.lang.reflect.Field valuesField = database.getClass().getDeclaredField("values");
            valuesField.setAccessible(true);
            values = (java.util.Map<String, Object>) valuesField.get(database);
        } catch (Exception e) {
            System.err.println("Values haritasına erişim hatası: " + e.getMessage());
            return -1;
        }
        
        java.util.List<Object> valuesList = new java.util.ArrayList<>();
        
        for (java.util.Map.Entry<String, Object> entry : values.entrySet()) {
            if (!firstColumn) {
                sql.append(", ");
            }
            sql.append(entry.getKey()).append(" = ?");
            valuesList.add(entry.getValue());
            firstColumn = false;
        }
        
        // WHERE koşulunu ekle
        sql.append(" WHERE ").append(whereClause);
        
        // Tüm parametreleri birleştir (önce SET değerleri, sonra WHERE parametreleri)
        Object[] allParams = new Object[valuesList.size() + params.length];
        int i = 0;
        
        for (Object value : valuesList) {
            allParams[i++] = value;
        }
        
        for (Object param : params) {
            allParams[i++] = param;
        }
        
        return database.executeUpdate(sql.toString(), allParams);
    }

    /**
     * Veriyi tabloda asenkron olarak günceller
     * @param whereClause Güncelleme koşulu (örn. "id = ?")
     * @param callback Sonuç callback'i
     * @param params WHERE koşulu için parametreler
     */
    public void executeUpdateAsync(String whereClause, Consumer<Integer> callback, Object... params) {
        database.getExecutorService().submit(() -> {
            int result = executeUpdate(whereClause, params);
            if (callback != null) {
                callback.accept(result);
            }
        });
    }

    /**
     * Birden fazla veriyi toplu olarak ekler (Batch Insert)
     * @param valuesList Eklenecek değerlerin listesi (her liste öğesi bir kayıt)
     * @return Eklenen kayıt sayısı
     */
    public int executeBatch(java.util.List<java.util.Map<String, Object>> valuesList) {
        if (valuesList == null || valuesList.isEmpty()) {
            return 0;
        }
        
        // Veritabanı bağlantı kontrolü
        if (!database.isConnected()) {
            database.reconnect();
        }
        
        try {
            // İlk kaydın sütunlarını kullanarak SQL hazırla
            java.util.Map<String, Object> firstRecord = valuesList.get(0);
            
            StringBuilder columnNames = new StringBuilder();
            StringBuilder placeholders = new StringBuilder();
            
            boolean first = true;
            for (String column : firstRecord.keySet()) {
                if (!first) {
                    columnNames.append(", ");
                    placeholders.append(", ");
                }
                
                columnNames.append(column);
                placeholders.append("?");
                first = false;
            }
            
            String sql = "INSERT INTO " + tableName + " (" + columnNames + ") VALUES (" + placeholders + ")";
            
            java.sql.Connection conn = null;
            try {
                // Reflection ile connection'a eriş
                java.lang.reflect.Field connectionField = database.getClass().getDeclaredField("connection");
                connectionField.setAccessible(true);
                conn = (java.sql.Connection) connectionField.get(database);
            } catch (Exception e) {
                System.err.println("Connection'a erişim hatası: " + e.getMessage());
                return -1;
            }
            
            // Otomatik commit'i kapat (performans için)
            boolean originalAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            
            java.sql.PreparedStatement pstmt = conn.prepareStatement(sql);
            
            int batchCount = 0;
            for (java.util.Map<String, Object> record : valuesList) {
                int paramIndex = 1;
                for (Object value : record.values()) {
                    pstmt.setObject(paramIndex++, value);
                }
                pstmt.addBatch();
                batchCount++;
                
                // Her 100 kayıtta bir batch'i işle
                if (batchCount % 100 == 0) {
                    pstmt.executeBatch();
                }
            }
            
            // Kalan batch'leri işle
            if (batchCount % 100 != 0) {
                pstmt.executeBatch();
            }
            
            // Değişiklikleri kaydet ve orijinal autoCommit değerine geri dön
            conn.commit();
            conn.setAutoCommit(originalAutoCommit);
            
            pstmt.close();
            return batchCount;
        } catch (Exception e) {
            System.err.println("Batch insert hatası: " + e.getMessage());
            return -1;
        }
    }

    /**
     * Birden fazla veriyi toplu olarak asenkron ekler
     * @param valuesList Eklenecek değerlerin listesi
     * @param callback Sonuç callback'i
     */
    public void executeBatchAsync(java.util.List<java.util.Map<String, Object>> valuesList, Consumer<Integer> callback) {
        database.getExecutorService().submit(() -> {
            int result = executeBatch(valuesList);
            if (callback != null) {
                callback.accept(result);
            }
        });
    }
}