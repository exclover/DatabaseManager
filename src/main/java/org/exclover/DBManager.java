package org.exclover;

import java.sql.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;

/**
 * Geliştirilmiş Database sınıfı
 */
public class DBManager {
    private final String databaseName;
    private Connection connection;
    private final Map<String, ColumnDefinition> columns;
    private final Map<String, Object> values;
    private final ExecutorService executorService;
    private boolean isConnected = false;
    private final DatabaseType databaseType; // Veritabanı tipi için yeni değişken

    /**
     * Veritabanı tipi enum'u
     */
    public enum DatabaseType {
        SQLITE,
        MYSQL
    }

    /**
     * Database constructor (SQLite için)
     * @param databaseName Veritabanı dosya adı
     */
    public DBManager(String databaseName) {
        this(databaseName, DatabaseType.SQLITE, null, null, null);
    }

    /**
     * Database constructor (MySQL için)
     * @param databaseName Veritabanı adı
     * @param host MySQL sunucu adresi
     * @param user Kullanıcı adı
     * @param password Şifre
     */
    public DBManager(String databaseName, String host, String user, String password) {
        this(databaseName, DatabaseType.MYSQL, host, user, password);
    }

    /**
     * Tam constructor
     */
    private DBManager(String databaseName, DatabaseType type, String host, String user, String password) {
        this.databaseName = databaseName;
        this.databaseType = type;
        this.columns = new LinkedHashMap<>();
        this.values = new HashMap<>();
        this.executorService = Executors.newCachedThreadPool();

        // Veritabanı bağlantısı parametrelerini sakla
        this.host = host;
        this.user = user;
        this.password = password;
    }

    private final String host;
    private final String user;
    private final String password;

    /**
     * Veritabanına bağlanır
     * @return Bağlantı başarılı ise true
     */
    public boolean connect() {
        try {
            if (databaseType == DatabaseType.SQLITE) {
                // SQLite JDBC bağlantısı
                Class.forName("org.sqlite.JDBC");
                this.connection = DriverManager.getConnection("jdbc:sqlite:" + databaseName);
            } else {
                // MySQL JDBC bağlantısı
                Class.forName("com.mysql.cj.jdbc.Driver");
                // MySQL veritabanı yok ise oluştur
                try {
                    Connection tempConn = DriverManager.getConnection(
                            "jdbc:mysql://" + host + "?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true",
                            user, password);
                    Statement stmt = tempConn.createStatement();
                    stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + databaseName);
                    stmt.close();
                    tempConn.close();
                } catch (SQLException e) {
                    System.err.println("Database creation error: " + e.getMessage());
                }

                // Asıl veritabanına bağlan
                this.connection = DriverManager.getConnection(
                        "jdbc:mysql://" + host + "/" + databaseName +
                                "?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true&useUnicode=true&characterEncoding=UTF-8",
                        user, password);
            }

            isConnected = true;
            System.out.println("Database connection successful: " + databaseName);
            return true;
        } catch (Exception e) {
            isConnected = false;
            System.err.println("Database connection error: " + e.getMessage());
            return false;
        }
    }

    public void connectAsync(Consumer<Boolean> callback) {
        executorService.submit(() -> {
            boolean result = connect();
            if (callback != null) {
                callback.accept(result);
            }
        });
    }

    /**
     * Veritabanı bağlantısının durumunu kontrol eder
     * @return Bağlantı açık ise true
     */
    public boolean isConnected() {
        try {
            return connection != null && !connection.isClosed() && isConnected;
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * Veritabanı bağlantısını yeniler
     * @return Yenileme başarılı ise true
     */
    public boolean reconnect() {
        close(false);
        return connect();
    }

    /**
     * Asenkron bağlantı yenileme
     * @param callback Sonuç callback'i
     */
    public void reconnectAsync(Consumer<Boolean> callback) {
        executorService.submit(() -> {
            boolean result = reconnect();
            if (callback != null) {
                callback.accept(result);
            }
        });
    }

    /**
     * Veritabanı bağlantısını kapatır
     */
    public void close() {
        close(true);
    }


    public ExecutorService getExecutorService() {
        return executorService;
    }

    /**
     * Veritabanı bağlantısını kapatır
     * @param shutdownExecutor ExecutorService'i kapatma için
     */
    private void close(boolean shutdownExecutor) {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                isConnected = false;
                System.out.println("Database connection closed.");
            }

            if (shutdownExecutor) {
                executorService.shutdown();
            }
        } catch (SQLException e) {
            System.err.println("Database closing error: " + e.getMessage());
        }
    }

    /**
     * TableBuilder nesnesini başlatır
     * @param tableName Tablo adı
     * @return TableBuilder nesnesi
     */
    public TableBuilder createTable(String tableName) {
        return new TableBuilder(this, tableName);
    }

    /**
     * Veritabanından String değer alır
     * @param columnName Sütun adı
     * @param defaultValue Varsayılan değer
     * @return String değeri
     */
    public String getString(String columnName, String defaultValue) {
        Object value = values.get(columnName);
        return value != null ? value.toString() : defaultValue;
    }

    /**
     * Veritabanından Integer değer alır
     * @param columnName Sütun adı
     * @param defaultValue Varsayılan değer
     * @return Integer değeri
     */
    public int getInteger(String columnName, int defaultValue) {
        Object value = values.get(columnName);
        if (value == null) return defaultValue;
        return value instanceof Integer ? (int) value : Integer.parseInt(value.toString());
    }

    /**
     * Veritabanından Double değer alır
     * @param columnName Sütun adı
     * @param defaultValue Varsayılan değer
     * @return Double değeri
     */
    public double getDouble(String columnName, double defaultValue) {
        Object value = values.get(columnName);
        if (value == null) return defaultValue;
        return value instanceof Double ? (double) value : Double.parseDouble(value.toString());
    }

    /**
     * Veritabanından Boolean değer alır
     * @param columnName Sütun adı
     * @param defaultValue Varsayılan değer
     * @return Boolean değeri
     */
    public boolean getBoolean(String columnName, boolean defaultValue) {
        Object value = values.get(columnName);
        if (value == null) return defaultValue;
        if (value instanceof Boolean) return (boolean) value;
        String strValue = value.toString().toLowerCase();
        return "true".equals(strValue) || "1".equals(strValue);
    }

    /**
     * Tabloyu oluşturur
     * @param tableName Tablo adı
     * @return Başarılı ise true
     */
    boolean createTable(String tableName, boolean dropIfExists) {
        if (!ensureConnection()) return false;

        try {
            if (dropIfExists) {
                String dropSql = "DROP TABLE IF EXISTS " + tableName;
                Statement dropStmt = connection.createStatement();
                dropStmt.execute(dropSql);
                dropStmt.close();
            }

            StringBuilder sql = new StringBuilder("CREATE TABLE IF NOT EXISTS " + tableName + " (");

            // Veritabanı tipine göre primary key yapısını ayarla
            if (databaseType == DatabaseType.SQLITE) {
                sql.append("id INTEGER PRIMARY KEY AUTOINCREMENT");
            } else {
                sql.append("id INT AUTO_INCREMENT PRIMARY KEY");
            }

            for (ColumnDefinition column : columns.values()) {
                sql.append(", ");

                // MySQL'de veri tipi dönüşümlerini yap
                String columnType = column.type;
                if (databaseType == DatabaseType.MYSQL) {
                    // SQLite'den MySQL veri tipi dönüşümleri
                    columnType = convertDataTypeToMySQL(column.type);
                }

                sql.append(column.name).append(" ").append(columnType);

                if (column.defaultValue != null) {
                    sql.append(" DEFAULT ");
                    if (column.defaultValue instanceof String) {
                        sql.append("'").append(column.defaultValue).append("'");
                    } else if (column.defaultValue instanceof Boolean) {
                        if (databaseType == DatabaseType.MYSQL) {
                            // MySQL'de boolean 1/0 olarak saklanır
                            sql.append(((Boolean)column.defaultValue) ? "1" : "0");
                        } else {
                            sql.append(((Boolean)column.defaultValue) ? "1" : "0");
                        }
                    } else {
                        sql.append(column.defaultValue);
                    }
                }

                if (column.constraints != null && !column.constraints.isEmpty()) {
                    sql.append(" ").append(column.constraints);
                }
            }

            sql.append(")");

            // MySQL için tablo oluşturma sorgusuna engine ekle
            if (databaseType == DatabaseType.MYSQL) {
                sql.append(" ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci");
            }

            Statement statement = connection.createStatement();
            statement.execute(sql.toString());
            statement.close();

            System.out.println("Table created successfully: " + tableName);
            return true;
        } catch (SQLException e) {
            System.err.println("Table creation error: " + e.getMessage());
            return false;
        }
    }

    /**
     * SQLite veri tiplerini MySQL veri tiplerine dönüştürür
     * @param sqliteType SQLite veri tipi
     * @return MySQL veri tipi
     */
    private String convertDataTypeToMySQL(String sqliteType) {
        if (sqliteType.startsWith("VARCHAR")) {
            return sqliteType; // Aynı kalabilir
        } else if (sqliteType.equals("INTEGER")) {
            return "INT";
        } else if (sqliteType.equals("REAL")) {
            return "DOUBLE";
        } else if (sqliteType.equals("BOOLEAN")) {
            return "TINYINT(1)";
        } else if (sqliteType.equals("TEXT")) {
            return "TEXT";
        } else if (sqliteType.equals("DATE")) {
            return "DATE";
        } else if (sqliteType.equals("TIMESTAMP")) {
            return "TIMESTAMP";
        }

        return sqliteType; // Bilinmeyen tipler için orijinal tipi kullan
    }

    /**
     * Verileri tabloya ekler
     * @param tableName Tablo adı
     * @return Eklenen satır ID'si, hata durumunda -1
     */
    long insertData(String tableName) {
        if (!ensureConnection()) return -1;

        try {
            // Değer girilmemiş varsayılan değerleri olan sütunlar için değerleri ekleyelim
            for (Map.Entry<String, ColumnDefinition> entry : columns.entrySet()) {
                String columnName = entry.getKey();
                ColumnDefinition column = entry.getValue();

                // Eğer kullanıcı bu sütun için değer girmediyse ve varsayılan değer tanımlıysa
                if (!values.containsKey(columnName) && column.defaultValue != null) {
                    values.put(columnName, column.defaultValue);
                }
            }

            StringBuilder columnNames = new StringBuilder();
            StringBuilder placeholders = new StringBuilder();
            List<Object> valuesList = new ArrayList<>();

            boolean first = true;
            for (Map.Entry<String, Object> entry : values.entrySet()) {
                if (!first) {
                    columnNames.append(", ");
                    placeholders.append(", ");
                }
                first = false;

                columnNames.append(entry.getKey());
                placeholders.append("?");
                valuesList.add(entry.getValue());
            }

            String sql = "INSERT INTO " + tableName + " (" + columnNames + ") VALUES (" + placeholders + ")";

            PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            for (int i = 0; i < valuesList.size(); i++) {
                Object value = valuesList.get(i);
                // Boolean değerleri MySQL için uygun şekilde dönüştür
                if (value instanceof Boolean && databaseType == DatabaseType.MYSQL) {
                    pstmt.setInt(i + 1, ((Boolean)value) ? 1 : 0);
                } else {
                    pstmt.setObject(i + 1, value);
                }
            }

            pstmt.executeUpdate();

            ResultSet rs = pstmt.getGeneratedKeys();
            long id = rs.next() ? rs.getLong(1) : -1;

            pstmt.close();
            System.out.println("Data inserted into table: " + tableName + ", ID: " + id);
            return id;
        } catch (SQLException e) {
            System.err.println("Data insertion error: " + e.getMessage());
            return -1;
        }
    }

    /**
     * Tablodan veri çeker
     * @param tableName Tablo adı
     * @param id Satır ID'si
     * @return Başarılı ise true
     */
    public boolean select(String tableName, long id) {
        if (!ensureConnection()) return false;

        try {
            String sql = "SELECT * FROM " + tableName + " WHERE id = ?";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setLong(1, id);

            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();

                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnName(i);
                    if (!columnName.equals("id")) {
                        Object value = rs.getObject(i);
                        values.put(columnName, value);
                    }
                }

                pstmt.close();
                return true;
            }

            pstmt.close();
            return false;
        } catch (SQLException e) {
            System.err.println("Data retrieval error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Tablodan veri çeker (asenkron)
     * @param tableName Tablo adı
     * @param id Satır ID'si
     * @param callback Sonuç callback'i
     */
    public void selectAsync(String tableName, long id, Consumer<Boolean> callback) {
        executorService.submit(() -> {
            boolean result = select(tableName, id);
            if (callback != null) {
                callback.accept(result);
            }
        });
    }

    /**
     * Sütun ekleme veya güncelleme
     * @param name Sütun adı
     * @param type Veri tipi
     * @param constraints Kısıtlamalar
     */
    void addColumn(String name, String type, String constraints) {
        columns.put(name, new ColumnDefinition(name, type, constraints));
    }

    /**
     * Sütun ekleme veya güncelleme
     * @param name Sütun adı
     * @param type Veri tipi
     * @param constraints Kısıtlamalar
     * @param defaultValue Varsayılan değer
     */
    void addColumn(String name, String type, String constraints, Object defaultValue) {
        columns.put(name, new ColumnDefinition(name, type, constraints, defaultValue));
    }

    /**
     * Değer atama
     * @param columnName Sütun adı
     * @param value Değer
     */
    void setValue(String columnName, Object value) {
        values.put(columnName, value);
    }

    /**
     * Sütun tanımı sınıfı
     */
    private static class ColumnDefinition {
        String name;
        String type;
        String constraints;
        Object defaultValue;

        ColumnDefinition(String name, String type, String constraints) {
            this.name = name;
            this.type = type;
            this.constraints = constraints;
            this.defaultValue = null;
        }

        ColumnDefinition(String name, String type, String constraints, Object defaultValue) {
            this.name = name;
            this.type = type;
            this.constraints = constraints;
            this.defaultValue = defaultValue;
        }
    }

    /**
     * Sorgu oluşturmak için QueryBuilder sınıfı
     */
    public QueryBuilder query(String tableName) {
        return new QueryBuilder(this, tableName);
    }

    /**
     * Sorgu ile veri çeker (tek sonuç)
     * @param tableName Tablo adı
     * @param queryBuilder Sorgu oluşturucu
     * @param firstOnly Sadece ilk sonuç için true
     * @return Başarılı ise true
     */
    boolean selectByQuery(String tableName, QueryBuilder queryBuilder, boolean firstOnly) {
        if (!ensureConnection()) return false;

        try {
            String sql = queryBuilder.buildQuery(false);
            PreparedStatement pstmt = connection.prepareStatement(sql);

            List<Object> parameters = queryBuilder.getParameters();
            for (int i = 0; i < parameters.size(); i++) {
                pstmt.setObject(i + 1, parameters.get(i));
            }

            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                // Mevcut values haritasını temizle
                values.clear();

                ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();

                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnName(i);
                    Object value = rs.getObject(i);
                    values.put(columnName, value);
                }

                pstmt.close();
                return true;
            }

            pstmt.close();
            return false;
        } catch (SQLException e) {
            System.err.println("Query retrieval error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Sorgu ile birden fazla veri çeker
     * @param tableName Tablo adı
     * @param queryBuilder Sorgu oluşturucu
     * @return Sonuç listesi
     */
    List<Map<String, Object>> selectMultipleByQuery(String tableName, QueryBuilder queryBuilder) {
        if (!ensureConnection()) return new ArrayList<>();

        List<Map<String, Object>> resultList = new ArrayList<>();

        try {
            String sql = queryBuilder.buildQuery(false);
            PreparedStatement pstmt = connection.prepareStatement(sql);

            List<Object> parameters = queryBuilder.getParameters();
            for (int i = 0; i < parameters.size(); i++) {
                pstmt.setObject(i + 1, parameters.get(i));
            }

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();

                ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();

                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnName(i);
                    Object value = rs.getObject(i);
                    row.put(columnName, value);
                }

                resultList.add(row);
            }

            pstmt.close();
        } catch (SQLException e) {
            System.err.println("Multiple query retrieval error: " + e.getMessage());
        }

        return resultList;
    }

    /**
     * Sorgu ile veri sayısını çeker
     * @param tableName Tablo adı
     * @param queryBuilder Sorgu oluşturucu
     * @return Sonuç sayısı
     */
    int countByQuery(String tableName, QueryBuilder queryBuilder) {
        if (!ensureConnection()) return 0;

        try {
            String sql = queryBuilder.buildQuery(true);
            PreparedStatement pstmt = connection.prepareStatement(sql);

            List<Object> parameters = queryBuilder.getParameters();
            for (int i = 0; i < parameters.size(); i++) {
                pstmt.setObject(i + 1, parameters.get(i));
            }

            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                int count = rs.getInt(1);
                pstmt.close();
                return count;
            }

            pstmt.close();
            return 0;
        } catch (SQLException e) {
            System.err.println("Count query error: " + e.getMessage());
            return 0;
        }
    }

    /**
     * Bağlantıyı kontrol eder ve gerekirse otomatik yenileme yapar
     * @return Bağlantı başarılı ise true
     */
    private boolean ensureConnection() {
        if (!isConnected()) {
            return reconnect();
        }
        return true;
    }

    /**
     * Veritabanı tipini döndürür
     * @return Veritabanı tipi
     */
    public DatabaseType getDatabaseType() {
        return databaseType;
    }

    /**
     * Belirli bir veritabanı tipi için SQL sorgusunu oluşturur
     * @param sqliteQuery SQLite sorgusu
     * @param mysqlQuery MySQL sorgusu
     * @return Veritabanı tipine göre uygun sorgu
     */
    public String getDialectQuery(String sqliteQuery, String mysqlQuery) {
        return databaseType == DatabaseType.SQLITE ? sqliteQuery : mysqlQuery;
    }

    /**
     * Tabloyu temizler (tüm verileri siler)
     * @param tableName Tablo adı
     * @return İşlem başarılı ise true
     */
    public boolean truncateTable(String tableName) {
        if (!ensureConnection()) return false;

        try {
            Statement stmt = connection.createStatement();
            if (databaseType == DatabaseType.SQLITE) {
                // SQLite DELETE kullanır
                stmt.executeUpdate("DELETE FROM " + tableName);
                stmt.executeUpdate("DELETE FROM sqlite_sequence WHERE name='" + tableName + "'");
            } else {
                // MySQL TRUNCATE kullanır
                stmt.executeUpdate("TRUNCATE TABLE " + tableName);
            }
            stmt.close();
            System.out.println("Table truncated: " + tableName);
            return true;
        } catch (SQLException e) {
            System.err.println("Truncate table error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Tablo var mı kontrol eder
     * @param tableName Tablo adı
     * @return Tablo varsa true
     */
    public boolean tableExists(String tableName) {
        if (!ensureConnection()) return false;

        try {
            DatabaseMetaData meta = connection.getMetaData();
            ResultSet rs;
            if (databaseType == DatabaseType.SQLITE) {
                rs = meta.getTables(null, null, tableName, null);
            } else {
                rs = meta.getTables(null, databaseName, tableName, null);
            }
            boolean exists = rs.next();
            rs.close();
            return exists;
        } catch (SQLException e) {
            System.err.println("Check table exists error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Veritabanı sorgu işlemini gerçekleştirir (INSERT, UPDATE, DELETE için)
     * @param sql SQL sorgusu
     * @param params Sorgu parametreleri
     * @return Etkilenen satır sayısı, hata durumunda -1
     */
    public int executeUpdate(String sql, Object... params) {
        if (!ensureConnection()) return -1;

        try {
            PreparedStatement pstmt = connection.prepareStatement(sql);
            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }
            int result = pstmt.executeUpdate();
            pstmt.close();
            return result;
        } catch (SQLException e) {
            System.err.println("Execute update error: " + e.getMessage());
            return -1;
        }
    }

    /**
     * Veri eklemek için InsertBuilder başlatır
     * @param tableName Tablo adı
     * @return InsertBuilder nesnesi
     */
    public InsertBuilder insert(String tableName) {
        return new InsertBuilder(this, tableName);
    }

    /**
     * Verileri tabloya ekler (asenkron)
     * @param tableName Tablo adı
     * @param callback Sonuç callback'i
     */
    void insertDataAsync(String tableName, Consumer<Long> callback) {
        executorService.submit(() -> {
            long id = insertData(tableName);
            if (callback != null) {
                callback.accept(id);
            }
        });
    }

    /**
     * Tabloyu temizler (asenkron)
     * @param tableName Tablo adı
     * @param callback Sonuç callback'i
     */
    public void truncateTableAsync(String tableName, Consumer<Boolean> callback) {
        executorService.submit(() -> {
            boolean result = truncateTable(tableName);
            if (callback != null) {
                callback.accept(result);
            }
        });
    }

    /**
     * Tablo var mı kontrol eder (asenkron)
     * @param tableName Tablo adı
     * @param callback Sonuç callback'i
     */
    public void tableExistsAsync(String tableName, Consumer<Boolean> callback) {
        executorService.submit(() -> {
            boolean result = tableExists(tableName);
            if (callback != null) {
                callback.accept(result);
            }
        });
    }

    /**
     * Veritabanı sorgu işlemini gerçekleştirir (asenkron)
     * @param sql SQL sorgusu
     * @param callback Sonuç callback'i
     * @param params Sorgu parametreleri
     */
    public void executeUpdateAsync(String sql, Consumer<Integer> callback, Object... params) {
        executorService.submit(() -> {
            int result = executeUpdate(sql, params);
            if (callback != null) {
                callback.accept(result);
            }
        });
    }

    /**
     * Tabloyu oluşturur (asenkron)
     * @param tableName Tablo adı
     * @param dropIfExists Varsa silip yeniden oluşturmak için
     * @param callback Sonuç callback'i
     */
    void createTableAsync(String tableName, boolean dropIfExists, Consumer<Boolean> callback) {
        executorService.submit(() -> {
            boolean result = createTable(tableName, dropIfExists);
            if (callback != null) {
                callback.accept(result);
            }
        });
    }

    /**
     * QueryBuilder için asenkron ilk sonuç getirme
     * @param tableName Tablo adı
     * @param queryBuilder QueryBuilder nesnesi
     * @param callback Sonuç callback'i
     */
    void selectByQueryAsync(String tableName, QueryBuilder queryBuilder, Consumer<Boolean> callback) {
        executorService.submit(() -> {
            boolean result = selectByQuery(tableName, queryBuilder, true);
            if (callback != null) {
                callback.accept(result);
            }
        });
    }

    /**
     * QueryBuilder için asenkron çoklu sonuç getirme
     * @param tableName Tablo adı
     * @param queryBuilder QueryBuilder nesnesi
     * @param callback Sonuç callback'i
     */
    void selectMultipleByQueryAsync(String tableName, QueryBuilder queryBuilder, Consumer<List<Map<String, Object>>> callback) {
        executorService.submit(() -> {
            List<Map<String, Object>> result = selectMultipleByQuery(tableName, queryBuilder);
            if (callback != null) {
                callback.accept(result);
            }
        });
    }

    /**
     * QueryBuilder için asenkron sayım
     * @param tableName Tablo adı
     * @param queryBuilder QueryBuilder nesnesi
     * @param callback Sonuç callback'i
     */
    void countByQueryAsync(String tableName, QueryBuilder queryBuilder, Consumer<Integer> callback) {
        executorService.submit(() -> {
            int result = countByQuery(tableName, queryBuilder);
            if (callback != null) {
                callback.accept(result);
            }
        });
    }
}




