    package com.yourteam.ojaitester.config;
    
    import java.io.InputStream;
    import java.sql.Connection;
    import java.sql.DriverManager;
    import java.util.Properties;
    
    public class DatabaseConfig {
    
        private static final Properties properties = new Properties();
    
        static {
            try (InputStream input = DatabaseConfig.class.getClassLoader()
                    .getResourceAsStream("application.properties")) {
                if (input == null) {
                    throw new RuntimeException("application.properties not found");
                }
                properties.load(input);
            } catch (Exception e) {
                throw new RuntimeException("Cannot load application.properties", e);
            }
        }
    
        public static Connection getConnection() {
            try {
                return DriverManager.getConnection(
                        properties.getProperty("db.url"),
                        properties.getProperty("db.username"),
                        properties.getProperty("db.password")
                );
            } catch (Exception e) {
                throw new RuntimeException("Cannot connect to database", e);
            }
        }
    }