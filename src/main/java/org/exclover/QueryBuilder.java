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
    private String joinClause = "";
    private String groupByClause = "";
    private String havingClause = "";
    private List<Object> havingParameters = new ArrayList<>();

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
     * Between koşulu ekler
     * @param columnName Sütun adı
     * @param value1 Alt sınır
     * @param value2 Üst sınır
     * @return QueryBuilder nesnesi
     */
    public QueryBuilder whereBetween(String columnName, Object value1, Object value2) {
        if (whereClause.length() > 0) {
            whereClause.append(" AND ");
        }
        whereClause.append(columnName).append(" BETWEEN ? AND ?");
        parameters.add(value1);
        parameters.add(value2);
        return this;
    }

    /**
     * NOT Between koşulu ekler
     * @param columnName Sütun adı
     * @param value1 Alt sınır
     * @param value2 Üst sınır
     * @return QueryBuilder nesnesi
     */
    public QueryBuilder whereNotBetween(String columnName, Object value1, Object value2) {
        if (whereClause.length() > 0) {
            whereClause.append(" AND ");
        }
        whereClause.append(columnName).append(" NOT BETWEEN ? AND ?");
        parameters.add(value1);
        parameters.add(value2);
        return this;
    }

    /**
     * IN koşulu ekler
     * @param columnName Sütun adı
     * @param values Değerler listesi
     * @return QueryBuilder nesnesi
     */
    public QueryBuilder whereIn(String columnName, Object... values) {
        if (values.length == 0) return this;
        
        if (whereClause.length() > 0) {
            whereClause.append(" AND ");
        }
        
        whereClause.append(columnName).append(" IN (");
        
        for (int i = 0; i < values.length; i++) {
            if (i > 0) {
                whereClause.append(", ");
            }
            whereClause.append("?");
            parameters.add(values[i]);
        }
        
        whereClause.append(")");
        return this;
    }

    /**
     * NOT IN koşulu ekler
     * @param columnName Sütun adı
     * @param values Değerler listesi
     * @return QueryBuilder nesnesi
     */
    public QueryBuilder whereNotIn(String columnName, Object... values) {
        if (values.length == 0) return this;
        
        if (whereClause.length() > 0) {
            whereClause.append(" AND ");
        }
        
        whereClause.append(columnName).append(" NOT IN (");
        
        for (int i = 0; i < values.length; i++) {
            if (i > 0) {
                whereClause.append(", ");
            }
            whereClause.append("?");
            parameters.add(values[i]);
        }
        
        whereClause.append(")");
        return this;
    }

    /**
     * NULL koşulu ekler
     * @param columnName Sütun adı
     * @return QueryBuilder nesnesi
     */
    public QueryBuilder whereNull(String columnName) {
        if (whereClause.length() > 0) {
            whereClause.append(" AND ");
        }
        whereClause.append(columnName).append(" IS NULL");
        return this;
    }

    /**
     * NOT NULL koşulu ekler
     * @param columnName Sütun adı
     * @return QueryBuilder nesnesi
     */
    public QueryBuilder whereNotNull(String columnName) {
        if (whereClause.length() > 0) {
            whereClause.append(" AND ");
        }
        whereClause.append(columnName).append(" IS NOT NULL");
        return this;
    }

    /**
     * GROUP BY ekler
     */
    public QueryBuilder groupBy(String... columns) {
        if (columns.length == 0) return this;
        
        groupByClause = " GROUP BY ";
        
        for (int i = 0; i < columns.length; i++) {
            if (i > 0) {
                groupByClause += ", ";
            }
            groupByClause += columns[i];
        }
        
        return this;
    }

    /**
     * HAVING koşulu ekler
     */
    public QueryBuilder having(String condition, Object... params) {
        havingClause = " HAVING " + condition;
        
        for (Object param : params) {
            havingParameters.add(param);
        }
        
        return this;
    }

    /**
     * INNER JOIN ekler
     * @param table Join yapılacak tablo
     * @param column Ana tablo sütunu
     * @param operator Karşılaştırma operatörü (genellikle =)
     * @param foreignColumn Join tablosu sütunu
     * @return QueryBuilder nesnesi
     */
    public QueryBuilder join(String table, String column, String operator, String foreignColumn) {
        joinClause += " INNER JOIN " + table + " ON " + column + " " + operator + " " + foreignColumn;
        return this;
    }

    /**
     * LEFT JOIN ekler
     * @param table Join yapılacak tablo
     * @param column Ana tablo sütunu
     * @param operator Karşılaştırma operatörü (genellikle =)
     * @param foreignColumn Join tablosu sütunu
     * @return QueryBuilder nesnesi
     */
    public QueryBuilder leftJoin(String table, String column, String operator, String foreignColumn) {
        joinClause += " LEFT JOIN " + table + " ON " + column + " " + operator + " " + foreignColumn;
        return this;
    }

    /**
     * RIGHT JOIN ekler (SQLite'da desteklenmez)
     * @param table Join yapılacak tablo
     * @param column Ana tablo sütunu
     * @param operator Karşılaştırma operatörü (genellikle =)
     * @param foreignColumn Join tablosu sütunu
     * @return QueryBuilder nesnesi
     */
    public QueryBuilder rightJoin(String table, String column, String operator, String foreignColumn) {
        // SQLite RIGHT JOIN desteklemez, duruma göre uyarı verebiliriz
        if (database.getDatabaseType() == DBManager.DatabaseType.SQLITE) {
            System.err.println("SQLite RIGHT JOIN desteklemez. LEFT JOIN kullanılıyor.");
            return leftJoin(table, foreignColumn, operator, column);
        }
        
        joinClause += " RIGHT JOIN " + table + " ON " + column + " " + operator + " " + foreignColumn;
        return this;
    }

    /**
     * SQL sorgusunu oluşturur (JOIN, GROUP BY ve HAVING destekli)
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

        // Join ifadesini ekle
        if (!joinClause.isEmpty()) {
            query.append(joinClause);
        }
        
        // Where ifadesini ekle
        if (whereClause.length() > 0) {
            query.append(" WHERE ").append(whereClause);
        }

        // Group By ifadesini ekle (sadece sayım olmayan sorgular için)
        if (!selectCount && !groupByClause.isEmpty()) {
            query.append(groupByClause);
            
            // Having ifadesini ekle (sadece Group By varsa)
            if (!havingClause.isEmpty()) {
                query.append(havingClause);
                
                // Having parametrelerini ekle
                parameters.addAll(havingParameters);
            }
        }
        
        // Order By ifadesini ekle (sadece sayım olmayan sorgular için)
        if (!selectCount && !orderByClause.isEmpty()) {
            query.append(orderByClause);
        }

        // Limit ifadesini ekle (sadece sayım olmayan sorgular için)
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

    /**
     * Sorguyu çalıştırır ve sonuçları QueryResult listesi olarak döndürür
     * @return QueryResult listesi
     */
    public List<QueryResult> getResults() {
        List<Map<String, Object>> mapResults = database.selectMultipleByQuery(tableName, this);
        return QueryResult.fromList(mapResults);
    }

    /**
     * Sorguyu asenkron çalıştırır ve sonuçları QueryResult listesi olarak döndürür
     * @param callback Sonuç callback'i
     */
    public void getResultsAsync(Consumer<List<QueryResult>> callback) {
        database.getExecutorService().submit(() -> {
            List<QueryResult> results = getResults();
            if (callback != null) {
                callback.accept(results);
            }
        });
    }
}