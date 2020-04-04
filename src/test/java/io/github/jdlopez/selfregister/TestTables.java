package io.github.jdlopez.selfregister;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.ResultSet;

public class TestTables {

    public static void main(String[] args) throws Exception {
        DriverManager.registerDriver((Driver) Class.forName(args[0]).getConstructor().newInstance());
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(args[1], null, null);
            ResultSet rs = conn.getMetaData().getTables("", "", "%", new String[]{"TABLE"});
            while (rs.next()) {
                System.out.println(rs.getString("TABLE_SCHEM") + "." + rs.getString("TABLE_NAME"));
            }
        } finally {
            conn.close();
        }
    }
}
