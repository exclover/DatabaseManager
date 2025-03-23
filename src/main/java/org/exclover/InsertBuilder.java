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
}