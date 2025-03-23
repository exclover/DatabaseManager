package org.exclover;

import java.util.function.Consumer;

/**
 * TableBuilder sınıfı - Tablo oluşturmak için Fluent API sağlar
 */
public class TableBuilder {
    private final DBManager database;
    private final String tableName;

    /**
     * TableBuilder constructor
     * @param database Database nesnesi
     * @param tableName Tablo adı
     */
    public TableBuilder(DBManager database, String tableName) {
        this.database = database;
        this.tableName = tableName;
    }

    /**
     * String tipinde sütun tanımlar
     * @param columnName Sütun adı
     * @return TableBuilder nesnesi
     */
    public TableBuilder addString(String columnName) {
        database.addColumn(columnName, "VARCHAR(255)", null);
        return this;
    }

    /**
     * Belirtilen uzunlukta String tipinde sütun tanımlar
     * @param columnName Sütun adı
     * @param length Maximum uzunluk
     * @return TableBuilder nesnesi
     */
    public TableBuilder addString(String columnName, int length) {
        database.addColumn(columnName, "VARCHAR(" + length + ")", null);
        return this;
    }

    /**
     * Integer tipinde sütun tanımlar
     * @param columnName Sütun adı
     * @return TableBuilder nesnesi
     */
    public TableBuilder addInteger(String columnName) {
        database.addColumn(columnName, "INTEGER", null);
        return this;
    }

    /**
     * Double tipinde sütun tanımlar
     * @param columnName Sütun adı
     * @return TableBuilder nesnesi
     */
    public TableBuilder addDouble(String columnName) {
        database.addColumn(columnName, "REAL", null);
        return this;
    }

    /**
     * Boolean tipinde sütun tanımlar
     * @param columnName Sütun adı
     * @return TableBuilder nesnesi
     */
    public TableBuilder addBoolean(String columnName) {
        database.addColumn(columnName, "BOOLEAN", null);
        return this;
    }

    /**
     * TEXT tipinde sütun tanımlar
     * @param columnName Sütun adı
     * @return TableBuilder nesnesi
     */
    public TableBuilder addText(String columnName) {
        database.addColumn(columnName, "TEXT", null);
        return this;
    }

    /**
     * DATE tipinde sütun tanımlar
     * @param columnName Sütun adı
     * @return TableBuilder nesnesi
     */
    public TableBuilder addDate(String columnName) {
        database.addColumn(columnName, "DATE", null);
        return this;
    }

    /**
     * TIMESTAMP tipinde sütun tanımlar
     * @param columnName Sütun adı
     * @return TableBuilder nesnesi
     */
    public TableBuilder addTimestamp(String columnName) {
        database.addColumn(columnName, "TIMESTAMP", null);
        return this;
    }

    /**
     * Tabloyu oluşturur
     * @return Database nesnesi
     */
    public DBManager create() {
        database.createTable(tableName, false);
        return database;
    }

    /**
     * Tabloyu oluşturur (varsa siler)
     * @return Database nesnesi
     */
    public DBManager createOrReplace() {
        database.createTable(tableName, true);
        return database;
    }

    /**
     * Tabloyu asenkron olarak oluşturur
     * @param callback Sonuç callback'i
     */
    public void createAsync(Consumer<DBManager> callback) {
        database.createTableAsync(tableName, false, result -> {
            if (callback != null) {
                callback.accept(database);
            }
        });
    }

    /**
     * Tabloyu asenkron olarak oluşturur (varsa siler)
     * @param callback Sonuç callback'i
     */
    public void createOrReplaceAsync(Consumer<DBManager> callback) {
        database.createTableAsync(tableName, true, result -> {
            if (callback != null) {
                callback.accept(database);
            }
        });
    }

    /**
     * String tipinde varsayılan değerli sütun tanımlar
     * @param columnName Sütun adı
     * @param defaultValue Varsayılan değer
     * @return TableBuilder nesnesi
     */
    public TableBuilder addStringDefault(String columnName, String defaultValue) {
        database.addColumn(columnName, "VARCHAR(255)", null, defaultValue);
        return this;
    }

    /**
     * Belirtilen uzunlukta String tipinde varsayılan değerli sütun tanımlar
     * @param columnName Sütun adı
     * @param length Maximum uzunluk
     * @param defaultValue Varsayılan değer
     * @return TableBuilder nesnesi
     */
    public TableBuilder addStringDefault(String columnName, int length, String defaultValue) {
        database.addColumn(columnName, "VARCHAR(" + length + ")", null, defaultValue);
        return this;
    }

    /**
     * Integer tipinde varsayılan değerli sütun tanımlar
     * @param columnName Sütun adı
     * @param defaultValue Varsayılan değer
     * @return TableBuilder nesnesi
     */
    public TableBuilder addIntegerDefault(String columnName, int defaultValue) {
        database.addColumn(columnName, "INTEGER", null, defaultValue);
        return this;
    }

    /**
     * Double tipinde varsayılan değerli sütun tanımlar
     * @param columnName Sütun adı
     * @param defaultValue Varsayılan değer
     * @return TableBuilder nesnesi
     */
    public TableBuilder addDoubleDefault(String columnName, double defaultValue) {
        database.addColumn(columnName, "REAL", null, defaultValue);
        return this;
    }

    /**
     * Boolean tipinde varsayılan değerli sütun tanımlar
     * @param columnName Sütun adı
     * @param defaultValue Varsayılan değer
     * @return TableBuilder nesnesi
     */
    public TableBuilder addBooleanDefault(String columnName, boolean defaultValue) {
        database.addColumn(columnName, "BOOLEAN", null, defaultValue);
        return this;
    }

    /**
     * TEXT tipinde varsayılan değerli sütun tanımlar
     * @param columnName Sütun adı
     * @param defaultValue Varsayılan değer
     * @return TableBuilder nesnesi
     */
    public TableBuilder addTextDefault(String columnName, String defaultValue) {
        database.addColumn(columnName, "TEXT", null, defaultValue);
        return this;
    }
}