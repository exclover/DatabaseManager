package org.exclover;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Sorgu oluşturmak için QueryBuilder sınıfı
 */
public class QueryBuilder {
    private final DBManager database;
    private final String tableName;
    private final StringBuilder whereClause;
    private final List<Object> parameters;
    private String orderByClause;
    private String limitClause;

    /**
     * QueryBuilder constructor
     * @param database Database nesnesi
     * @param tableName Tablo adı
     */
    public QueryBuilder(DBManager database, String tableName) {
        this.database = database;
        this.tableName = tableName;
        this.whereClause = new StringBuilder();
        this.parameters = new ArrayList<>();
        this.orderByClause = "";
        this.limitClause = "";
    }



    /**
     * Sorguyu asenkron çalıştırır ve tek bir sonuç döndürür
     * @param callback Sonuç callback'i
     */
    public void firstAsync(Consumer<Boolean> callback) {
        database.selectByQueryAsync(tableName, this, callback);
    }

    /**
     * Sorguyu asenkron çalıştırır ve birden fazla sonuç döndürür
     * @param callback Sonuç callback'i
     */
    public void getAsync(Consumer<List<Map<String, Object>>> callback) {
        database.selectMultipleByQueryAsync(tableName, this, callback);
    }

    /**
     * Sorguyu asenkron çalıştırır ve sonuç sayısını döndürür
     * @param callback Sonuç callback'i
     */
    public void countAsync(Consumer<Integer> callback) {
        database.countByQueryAsync(tableName, this, callback);
    }


    /**
     * Eşitlik koşulu ekler
     * @param columnName Sütun adı
     * @param value Değer
     * @return QueryBuilder nesnesi
     */
    public QueryBuilder where(String columnName, Object value) {
        if (whereClause.length() > 0) {
            whereClause.append(" AND ");
        }
        whereClause.append(columnName).append(" = ?");
        parameters.add(value);
        return this;
    }

    /**
     * Like koşulu ekler
     * @param columnName Sütun adı
     * @param value Değer (% işaretleri dahil edilmelidir)
     * @return QueryBuilder nesnesi
     */
    public QueryBuilder whereLike(String columnName, String value) {
        if (whereClause.length() > 0) {
            whereClause.append(" AND ");
        }
        whereClause.append(columnName).append(" LIKE ?");
        parameters.add(value);
        return this;
    }

    /**
     * Büyüktür koşulu ekler
     * @param columnName Sütun adı
     * @param value Değer
     * @return QueryBuilder nesnesi
     */
    public QueryBuilder whereGreaterThan(String columnName, Object value) {
        if (whereClause.length() > 0) {
            whereClause.append(" AND ");
        }
        whereClause.append(columnName).append(" > ?");
        parameters.add(value);
        return this;
    }

    /**
     * Küçüktür koşulu ekler
     * @param columnName Sütun adı
     * @param value Değer
     * @return QueryBuilder nesnesi
     */
    public QueryBuilder whereLessThan(String columnName, Object value) {
        if (whereClause.length() > 0) {
            whereClause.append(" AND ");
        }
        whereClause.append(columnName).append(" < ?");
        parameters.add(value);
        return this;
    }

    /**
     * OR koşulu ekler
     * @param columnName Sütun adı
     * @param value Değer
     * @return QueryBuilder nesnesi
     */
    public QueryBuilder orWhere(String columnName, Object value) {
        if (whereClause.length() > 0) {
            whereClause.append(" OR ");
        }
        whereClause.append(columnName).append(" = ?");
        parameters.add(value);
        return this;
    }

    /**
     * Sıralama ekler
     * @param columnName Sütun adı
     * @param ascending Artan sıralama için true, azalan için false
     * @return QueryBuilder nesnesi
     */
    public QueryBuilder orderBy(String columnName, boolean ascending) {
        orderByClause = " ORDER BY " + columnName + (ascending ? " ASC" : " DESC");
        return this;
    }

    /**
     * Limit ekler
     * @param limit Maksimum kayıt sayısı
     * @return QueryBuilder nesnesi
     */
    public QueryBuilder limit(int limit) {
        limitClause = " LIMIT " + limit;
        return this;
    }

    /**
     * Limit ve ofset ekler
     * @param limit Maksimum kayıt sayısı
     * @param offset Başlangıç offset değeri
     * @return QueryBuilder nesnesi
     */
    public QueryBuilder limit(int limit, int offset) {
        limitClause = " LIMIT " + limit + " OFFSET " + offset;
        return this;
    }

    /**
     * Sorguyu çalıştırır ve tek bir sonuç döndürür
     * @return Başarılı ise true
     */
    public boolean first() {
        return database.selectByQuery(tableName, this, true);
    }

    /**
     * Sorguyu çalıştırır ve birden fazla sonuç döndürür
     * @return Sonuç listesi
     */
    public List<Map<String, Object>> get() {
        return database.selectMultipleByQuery(tableName, this);
    }

    /**
     * Sorguyu çalıştırır ve sonuç sayısını döndürür
     * @return Sonuç sayısı
     */
    public int count() {
        return database.countByQuery(tableName, this);
    }

    /**
     * SQL sorgusunu oluşturur
     * @param selectCount Count sorgusu için true
     * @return SQL sorgusu
     */
    public String buildQuery(boolean selectCount) {
        StringBuilder query = new StringBuilder();

        if (selectCount) {
            query.append("SELECT COUNT(*) FROM ");
        } else {
            query.append("SELECT * FROM ");
        }

        query.append(tableName);

        if (whereClause.length() > 0) {
            query.append(" WHERE ").append(whereClause);
        }

        if (!selectCount && !orderByClause.isEmpty()) {
            query.append(orderByClause);
        }

        if (!selectCount && !limitClause.isEmpty()) {
            query.append(limitClause);
        }

        return query.toString();
    }

    /**
     * Sorgu parametrelerini döndürür
     * @return Parametre listesi
     */
    public List<Object> getParameters() {
        return parameters;
    }

}
