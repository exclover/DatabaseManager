package org.exclover;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Tüm özellikleri gösteren genişletilmiş örnek kullanım sınıfı
 */
public class DBManagerExample {
    public static void main(String[] args) {
        // SQLite veritabanı oluşturma ve test etme
        DBManager sqliteDb = new DBManager("users.db");
        sqliteDb.connect();
        System.out.println("SQLite Database test:");
        System.out.println("=====================");
        testFullFeatures(sqliteDb);

        // MySQL veritabanı oluşturma ve test etme (parametreleri kendi ayarlarınıza göre değiştirin)
        System.out.println("\n\n");
        DBManager mysqlDb = new DBManager("example", "localhost", "root", "");
        mysqlDb.connect();
        System.out.println("\nMySQL Database test:");
        System.out.println("====================");

        // MySQL testinden önce veritabanını tamamen temizleyelim
        if (mysqlDb.getDatabaseType() == DBManager.DatabaseType.MYSQL) {
            // Foreign key kısıtlamalarını geçici olarak devre dışı bırakalım
            mysqlDb.executeUpdate("SET FOREIGN_KEY_CHECKS = 0");
            
            // Mevcut tabloları sorgula ve sil
            List<String> tables = mysqlDb.getTables();
            for (String table : tables) {
                mysqlDb.executeUpdate("DROP TABLE IF EXISTS " + table);
            }
            
            // Foreign key kısıtlamalarını tekrar aktifleştirelim
            mysqlDb.executeUpdate("SET FOREIGN_KEY_CHECKS = 1");
        }

        // Veritabanında temiz bir başlangıç yaptıktan sonra testleri çalıştır
        testFullFeatures(mysqlDb);
    }

    private static void testFullFeatures(DBManager database) {
        // Veritabanı türünü göster
        System.out.println("Database type: " + database.getDatabaseType());

        // Bağlantı kontrolü
        if (database.isConnected()) {
            System.out.println("Database connection is active.\n");
        } else {
            System.out.println("Database connection failed.\n");
            return;
        }

        // 1. Mevcut tabloları listele
        System.out.println("Existing tables:");
        List<String> tables = database.getTables();
        if (tables.isEmpty()) {
            System.out.println("- No tables found");
        } else {
            for (String table : tables) {
                System.out.println("- " + table);
            }
        }

        // 2. Tablo oluşturma
        System.out.println("\nCreating users table...");
        database.createTable("users")
                .addString("username", 50)
                .addString("email", 100)
                .addString("firstName")
                .addString("lastName")
                .addIntegerDefault("loginCount", 0)
                .addDoubleDefault("money", 0.0)
                .addBooleanDefault("active", false)
                .addStringDefault("level", "beginner")
                .addTimestamp("lastLogin")
                .addTextDefault("notes", "New user")
                .createOrReplace();

        // Tablo yapısını kontrol et
        System.out.println("\nTable structure of 'users':");
        List<Map<String, Object>> columns = database.getTableStructure("users");
        for (Map<String, Object> column : columns) {
            System.out.println("- " + column.get("name") + " (" + column.get("type") + ")");
        }

        // 3. Veri ekleme
        System.out.println("\nInserting test users...");
        // Veri 1: Tüm değerleri belirtilmiş
        long user1Id = database.insert("users")
                .setString("username", "johndoe")
                .setString("email", "john@example.com")
                .setString("firstName", "John")
                .setString("lastName", "Doe")
                .setInteger("loginCount", 5)
                .setDouble("money", 100.0)
                .setBoolean("active", true)
                .setString("level", "advanced")
                .setDate("lastLogin", new java.util.Date())
                .setString("notes", "Regular user")
                .execute();
        System.out.println("- User 1 added with ID: " + user1Id);

        // Veri 2: Bazı değerler için varsayılanlar kullanılacak
        long user2Id = database.insert("users")
                .setString("username", "janedoe")
                .setString("email", "jane@example.com")
                .setString("firstName", "Jane")
                .setString("lastName", "Doe")
                .execute();
        System.out.println("- User 2 added with ID: " + user2Id);

        // Veri 3: Asenkron ekleme
        database.insert("users")
                .setString("username", "bobsmith")
                .setString("email", "bob@example.com")
                .setString("firstName", "Bob")
                .setString("lastName", "Smith")
                .setBoolean("active", false)
                .executeAsync(id -> {
                    System.out.println("- Async user added with ID: " + id);
                });

        // Kısa bir bekleme (asenkron işlemin tamamlanması için)
        try { Thread.sleep(1000); } catch (InterruptedException e) {}

        // 4. Toplu veri ekleme (Batch Insert)
        System.out.println("\nBatch inserting users...");
        List<Map<String, Object>> userBatch = new ArrayList<>();
        
        for (int i = 1; i <= 5; i++) {
            Map<String, Object> user = new HashMap<>();
            user.put("username", "batch_user" + i);
            user.put("email", "batch" + i + "@example.com");
            user.put("firstName", "Batch");
            user.put("lastName", "User" + i);
            user.put("money", 50.0 * i);
            user.put("active", i % 2 == 0);  // Çift sayılı kullanıcılar aktif
            userBatch.add(user);
        }
        
        int batchCount = database.insert("users").executeBatch(userBatch);
        System.out.println("- Added " + batchCount + " users via batch insert");

        // 5. Tekli veri okuma
        System.out.println("\nReading user 1 data...");
        if (database.select("users", user1Id)) {
            printUserInfo(database);
        }

        // 6. QueryBuilder ile veriye erişim
        System.out.println("\nReading user 2 with QueryBuilder...");
        if (database.query("users").where("username", "janedoe").first()) {
            printUserInfo(database);
        }

        // 7. Koşul tabanlı sorgulama
        System.out.println("\nActive users with login count > 0:");
        List<QueryResult> activeUsers = database.query("users")
                .where("active", true)
                .whereGreaterThan("loginCount", 0)
                .orderBy("money", false) // Para miktarına göre azalan sıralama
                .getResults();

        for (QueryResult user : activeUsers) {
            System.out.println("- " + user.getString("username") + ": $" + 
                               user.getDouble("money") +
                               ", Login count: " + user.getInt("loginCount"));
        }

        // 8. LIKE operatörü kullanımı
        System.out.println("\nUsers with 'doe' in username:");
        List<QueryResult> doeUsers = database.query("users")
                .whereLike("username", "%doe%")
                .getResults();

        for (QueryResult user : doeUsers) {
            System.out.println("- " + user.getString("username") + " (" +
                    user.getString("firstName") + " " + user.getString("lastName") + ")");
        }

        // 9. Between ve In operatörleri
        System.out.println("\nUsers with money between 50 and 150:");
        List<QueryResult> richUsers = database.query("users")
                .whereBetween("money", 50.0, 150.0)
                .orderBy("money", true)
                .getResults();

        for (QueryResult user : richUsers) {
            System.out.println("- " + user.getString("username") + ": $" + user.getDouble("money"));
        }

        System.out.println("\nUsers with specific usernames:");
        List<QueryResult> specificUsers = database.query("users")
                .whereIn("username", "johndoe", "janedoe", "unknownuser")
                .getResults();

        for (QueryResult user : specificUsers) {
            System.out.println("- " + user.getString("username"));
        }

        // 10. OR koşulu ile sorgulama
        System.out.println("\nUsers who are either not active or beginners:");
        List<QueryResult> specialUsers = database.query("users")
                .where("active", false)
                .orWhere("level", "beginner")
                .getResults();

        for (QueryResult user : specialUsers) {
            System.out.println("- " + user.getString("username") +
                    " (Active: " + user.getBoolean("active") +
                    ", Level: " + user.getString("level") + ")");
        }

        // 11. NULL ve NOT NULL sorgulama
        System.out.println("\nUsers with notes not NULL:");
        List<QueryResult> usersWithNotes = database.query("users")
                .whereNotNull("notes")
                .getResults();

        for (QueryResult user : usersWithNotes) {
            System.out.println("- " + user.getString("username") + ": " + user.getString("notes"));
        }

        // 12. Sayfalama (Pagination)
        System.out.println("\nPaginated users (Page 1, 3 users per page):");
        List<QueryResult> page1 = database.paginateResults("users", 0, 3);
        
        for (QueryResult user : page1) {
            System.out.println("- " + user.getString("username"));
        }
        
        System.out.println("\nPaginated users (Page 2, 3 users per page):");
        List<QueryResult> page2 = database.paginateResults("users", 3, 3);
        
        for (QueryResult user : page2) {
            System.out.println("- " + user.getString("username"));
        }

        // 13. Veri sayısı
        int totalUsers = database.query("users").count();
        int activeUserCount = database.query("users").where("active", true).count();
        int beginnerCount = database.query("users").where("level", "beginner").count();

        System.out.println("\nUser statistics:");
        System.out.println("- Total users: " + totalUsers);
        System.out.println("- Active users: " + activeUserCount);
        System.out.println("- Beginner users: " + beginnerCount);

        // 14. Ham SQL sorgusu çalıştırma
        System.out.println("\nRunning raw SQL query:");
        List<QueryResult> rawResults = database.executeQueryResults(
                "SELECT username, email, active FROM users WHERE money > ? ORDER BY money DESC LIMIT 3", 
                40.0);
        
        for (QueryResult result : rawResults) {
            System.out.println("- " + result.getString("username") + " (" + result.getString("email") + ")");
        }

        // 15. InsertBuilder ile güncelleme
        System.out.println("\nUpdating user with InsertBuilder:");
        int updateCount = database.insert("users")
                .setString("level", "expert")
                .setInteger("loginCount", 10)
                .setBoolean("active", true)
                .executeUpdate("id = ?", user2Id);
        
        System.out.println("- Updated " + updateCount + " rows");

        // Güncellenmiş veriyi kontrol et
        if (database.select("users", user2Id)) {
            System.out.println("User after update:");
            System.out.println("- Username: " + database.getString("username", ""));
            System.out.println("- Login count: " + database.getInteger("loginCount", 0));
            System.out.println("- Level: " + database.getString("level", ""));
            System.out.println("- Active: " + database.getBoolean("active", false));
        }

        // 16. Geleneksel SQL güncelleme
        System.out.println("\nUpdating users with executeUpdate:");
        String updateSql = "UPDATE users SET notes = ? WHERE active = ?";
        int updated = database.executeUpdate(updateSql, "Active user", true);
        System.out.println("- Updated " + updated + " rows");

        // 17. Group By ve Having
        System.out.println("\nUser count by activity status:");
        List<QueryResult> groupResults = database.executeQueryResults(
                "SELECT active, COUNT(*) as user_count FROM users GROUP BY active");

        for (QueryResult result : groupResults) {
            boolean isActive = result.getBoolean("active");
            long count = result.getLong("user_count");
            System.out.println("- " + (isActive ? "Active" : "Inactive") + " users: " + count);
        }


                


        // Asenkron sayfalama örneği
        database.paginateResultsAsync("users", 0, 10, results -> {
            System.out.println("İlk sayfa kullanıcıları:");
            for (QueryResult user : results) {
                System.out.println("- " + user.getString("username"));
            }
        });

        // Asenkron SQL sorgusu örneği
        database.executeQueryResultsAsync(
            "SELECT username, email FROM users WHERE active = ?", 
            results -> {
                System.out.println("Aktif kullanıcılar:");
                for (QueryResult user : results) {
                    System.out.println("- " + user.getString("username") + " (" + user.getString("email") + ")");
                }
            },
            true
        );


        // 18. Index oluşturma
        System.out.println("\nCreating indexes:");
        boolean indexCreated = database.createIndex("users", "idx_users_email", 
                                                    new String[]{"email"}, true);
        System.out.println("- Unique index on email: " + (indexCreated ? "Created" : "Failed"));
        
        boolean indexCreated2 = database.createIndex("users", "idx_users_level_active", 
                                                    new String[]{"level", "active"}, false);
        System.out.println("- Composite index on level+active: " + (indexCreated2 ? "Created" : "Failed"));

        // 19. Foreign Key oluşturma (sadece MySQL'de desteklenir)
        if (database.getDatabaseType() == DBManager.DatabaseType.MYSQL) {
            System.out.println("\nCreating posts table with foreign key...");
            
            database.createTable("posts")
                    .addIntegerDefault("userId", 0)
                    .addString("title", 100)
                    .addTextDefault("content", "")
                    .addColumn("createdAt", "TIMESTAMP", "DEFAULT CURRENT_TIMESTAMP")
                    .addBooleanDefault("published", false)
                    .createOrReplace();
            
            boolean fkCreated = database.addForeignKey("posts", "userId", "users", "id", "CASCADE");
            System.out.println("- Foreign key on posts.userId: " + (fkCreated ? "Created" : "Failed"));
            
            // Bazı post verileri ekle
            database.insert("posts")
                    .setInteger("userId", (int)user1Id)
                    .setString("title", "First Post")
                    .setString("content", "Hello world!")
                    .setBoolean("published", true)
                    .setDate("createdAt", new java.util.Date())
                    .execute();
                    
            // Join sorgusu
            System.out.println("\nPosts with author information (JOIN):");
            List<QueryResult> joinResults = database.executeQueryResults(
                    "SELECT p.title, p.content, u.username, u.email FROM posts p " +
                    "INNER JOIN users u ON p.userId = u.id");
                    
            for (QueryResult result : joinResults) {
                System.out.println("- " + result.getString("title") + " by " + result.getString("username"));
                System.out.println("  " + result.getString("content"));
            }
        }

        // 20. Tablo var mı kontrol et
        boolean usersTableExists = database.tableExists("users");
        System.out.println("\nUsers table exists: " + usersTableExists);

        // 21. Tabloyu temizle (tüm verileri sil)
        System.out.println("\nTruncating users table...");
        try {
            if (database.getDatabaseType() == DBManager.DatabaseType.MYSQL) {
                // MySQL için foreign key kontrollerini geçici olarak kapat
                database.executeUpdate("SET FOREIGN_KEY_CHECKS=0");
                database.truncateTable("users");
                database.executeUpdate("SET FOREIGN_KEY_CHECKS=1");
            } else {
                // SQLite için normal truncate işlemi yeterli
                database.truncateTable("users");
            }
            
            // Kontrol et
            int remainingUsers = database.query("users").count();
            System.out.println("Remaining users after truncate: " + remainingUsers);
        } catch (Exception e) {
            System.err.println("Truncate error: " + e.getMessage());
        }

        // Kontrol et
        int remainingUsers = database.query("users").count();
        System.out.println("Remaining users after truncate: " + remainingUsers);

        // 22. Veritabanı bağlantısını kapat
        System.out.println("\nClosing database connection...");
        database.close();
        System.out.println("Database operations completed.");
    }

    private static void printUserInfo(DBManager database) {
        System.out.println("User details:");
        System.out.println("- Username: " + database.getString("username", "unknown"));
        System.out.println("- Email: " + database.getString("email", "unknown"));
        System.out.println("- Name: " + database.getString("firstName", "") + " " +
                database.getString("lastName", ""));
        System.out.println("- Login count: " + database.getInteger("loginCount", 0));
        System.out.println("- Money: $" + database.getDouble("money", 0.0));
        System.out.println("- Active: " + database.getBoolean("active", false));
        System.out.println("- Level: " + database.getString("level", "unknown"));
        System.out.println("- Notes: " + database.getString("notes", ""));
    }
}