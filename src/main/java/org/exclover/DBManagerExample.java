package org.exclover;


import java.util.List;
import java.util.Map;

/**
 * Tüm özellikleri gösteren genişletilmiş örnek kullanım sınıfı
 */
public class DBManagerExample {
    public static void main(String[] args) {
        // SQLite veritabanı oluşturma ve test etme
        //DBManager sqliteDb = new DBManager("users.db");
        //sqliteDb.connect();
        //System.out.println("SQLite Database test:");
        //System.out.println("=====================");
        //testFullFeatures(sqliteDb);

        // MySQL veritabanı oluşturma ve test etme (parametreleri kendi ayarlarınıza göre değiştirin)
        DBManager mysqlDb = new DBManager("example", "localhost", "root", "");
        mysqlDb.connect();
        System.out.println("\nMySQL Database test:");
        System.out.println("====================");
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

        // 1. Tablo oluşturma
        System.out.println("Creating users table...");
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

        // 2. Veri ekleme
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

        // Veri 2: Bazı değerler için varsayılanlar kullanılacak
        long user2Id = database.insert("users")
                .setString("username", "janedoe")
                .setString("email", "jane@example.com")
                .setString("firstName", "Jane")
                .setString("lastName", "Doe")
                .execute();

        // Veri 3: Asenkron ekleme
        database.insert("users")
                .setString("username", "bobsmith")
                .setString("email", "bob@example.com")
                .setString("firstName", "Bob")
                .setString("lastName", "Smith")
                .setBoolean("active", false)
                .executeAsync(id -> {
                    System.out.println("Async user added with ID: " + id);
                });

        // Kısa bir bekleme (asenkron işlemin tamamlanması için)
        try { Thread.sleep(1000); } catch (InterruptedException e) {}

        // 3. Tekli veri okuma
        System.out.println("\nReading user 1 data...");
        if (database.select("users", user1Id)) {
            printUserInfo(database);
        }

        // 4. QueryBuilder ile veriye erişim
        System.out.println("\nReading user 2 with QueryBuilder...");
        if (database.query("users").where("username", "janedoe").first()) {
            printUserInfo(database);
        }

        // 5. Koşul tabanlı sorgulama
        System.out.println("\nActive users with login count > 0:");
        List<Map<String, Object>> activeUsers = database.query("users")
                .where("active", true)
                .whereGreaterThan("loginCount", 0)
                .orderBy("money", false) // Para miktarına göre azalan sıralama
                .get();

        for (Map<String, Object> user : activeUsers) {
            System.out.println("- " + user.get("username") + ": $" + user.get("money") +
                    ", Login count: " + user.get("loginCount"));
        }

        // 6. LIKE operatörü kullanımı
        System.out.println("\nUsers with 'doe' in username:");
        List<Map<String, Object>> doeUsers = database.query("users")
                .whereLike("username", "%doe%")
                .get();

        for (Map<String, Object> user : doeUsers) {
            System.out.println("- " + user.get("username") + " (" +
                    user.get("firstName") + " " + user.get("lastName") + ")");
        }

        // 7. OR koşulu ile sorgulama
        System.out.println("\nUsers who are either not active or beginners:");
        List<Map<String, Object>> specialUsers = database.query("users")
                .where("active", false)
                .orWhere("level", "beginner")
                .get();

        for (Map<String, Object> user : specialUsers) {
            System.out.println("- " + user.get("username") +
                    " (Active: " + user.get("active") +
                    ", Level: " + user.get("level") + ")");
        }

        // 8. Veri sayısı
        int totalUsers = database.query("users").count();
        int activeUserCount = database.query("users").where("active", true).count();
        int beginnerCount = database.query("users").where("level", "beginner").count();

        System.out.println("\nUser statistics:");
        System.out.println("- Total users: " + totalUsers);
        System.out.println("- Active users: " + activeUserCount);
        System.out.println("- Beginner users: " + beginnerCount);

        // 9. Veri güncelleme
        System.out.println("\nUpdating user data...");
        String updateSql = "UPDATE users SET loginCount = loginCount + 1, level = ? WHERE id = ?";
        int updated = database.executeUpdate(updateSql, "intermediate", user2Id);
        System.out.println("Updated " + updated + " rows");

        // Güncellenmiş veriyi kontrol et
        if (database.select("users", user2Id)) {
            System.out.println("User after update:");
            System.out.println("- Username: " + database.getString("username", ""));
            System.out.println("- Login count: " + database.getInteger("loginCount", 0));
            System.out.println("- Level: " + database.getString("level", ""));
        }

        // 10. Limit ve sıralama
        System.out.println("\nTop 2 users with highest money:");
        List<Map<String, Object>> richUsers = database.query("users")
                .orderBy("money", false)
                .limit(2)
                .get();

        for (Map<String, Object> user : richUsers) {
            System.out.println("- " + user.get("username") + ": $" + user.get("money"));
        }

        // 11. Tablo varsa kontrol et
        boolean usersTableExists = database.tableExists("users");
        System.out.println("\nUsers table exists: " + usersTableExists);

        // 12. Tabloyu temizle (tüm verileri sil)
        System.out.println("\nTruncating users table...");
        database.truncateTable("users");

        // Kontrol et
        int remainingUsers = database.query("users").count();
        System.out.println("Remaining users after truncate: " + remainingUsers);

                

        // 13. Veritabanı bağlantısını kapat
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