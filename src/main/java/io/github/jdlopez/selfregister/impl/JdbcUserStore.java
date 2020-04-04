package io.github.jdlopez.selfregister.impl;

import io.github.jdlopez.selfregister.ConfigStore;
import io.github.jdlopez.selfregister.SelfRegisterException;
import io.github.jdlopez.selfregister.UserRegistration;
import io.github.jdlopez.selfregister.UserStore;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * UserStore implementation using:
 * JDBC 4.0 Driver
 * @see <a href="https://docs.oracle.com/javase/tutorial/jdbc/basics/connecting.html#db_connection_url">https://docs.oracle.com/javase/tutorial/jdbc/basics/connecting.html#db_connection_url</a>
 */
public class JdbcUserStore extends UserStore {

    private String sqlInsert = "INSERT INTO %s (%s, %s, %s, %s, %s) VALUES (?, ?, ?, ?, ?)";
    private String sqlUpdate = "UPDATE %s set %s =?, %s=?, %s=?, %s=? WHERE %s = ?";
    private String sqlSelectByCode = "SELECT %s, %s, %s, %s, %s FROM %s WHERE %s = ?";
    private String sqlSelectByEmail = "SELECT %s, %s, %s, %s, %s FROM %s WHERE %s = ?";
    private String sqlCreateTable = "CREATE TABLE %s (%s VARCHAR(255) PRIMARY KEY, %s VARCHAR(34), %s BOOLEAN, %s TIMESTAMP, %s TIMESTAMP)";
    private String sqlTableName = "USER_STORE";
    private String fieldEmail = "email";
    private String fieldConfirmcode = "confirmcode";
    private String fieldActive = "active";
    private String fieldCreationdate = "creationdate";
    private String fieldLastmodified = "lastmodified";

    private DataSource dataSource;

    public JdbcUserStore(ConfigStore config) throws SelfRegisterException {
        super.config = config;
        Properties p = config.getExternalProperties();
        // sql statement configuration
        sqlTableName = p.getProperty("JdbcUserStore.tablename", sqlTableName);
        fieldEmail = p.getProperty("JdbcUserStore.field.email", fieldEmail);
        fieldConfirmcode = p.getProperty("JdbcUserStore.field.confirmcode", fieldConfirmcode);
        fieldActive = p.getProperty("JdbcUserStore.field.active", fieldActive);
        fieldCreationdate = p.getProperty("JdbcUserStore.field.creationdate", fieldCreationdate);
        fieldLastmodified = p.getProperty("JdbcUserStore.field.lastmodified", fieldLastmodified);
        // insert
        if (p.containsKey("JdbcUserStore.sql.insert"))
            sqlInsert = p.getProperty("JdbcUserStore.sql.insert");
        else
            sqlInsert = String.format(sqlInsert, sqlTableName, fieldEmail, fieldConfirmcode, fieldActive, fieldCreationdate, fieldLastmodified);
        // update
        if (p.containsKey("JdbcUserStore.sql.update"))
            sqlUpdate = p.getProperty("JdbcUserStore.sql.update");
        else
            sqlUpdate = String.format(sqlUpdate, sqlTableName, fieldConfirmcode, fieldActive, fieldCreationdate, fieldLastmodified, fieldEmail);
        // select by code
        if (p.containsKey("JdbcUserStore.sql.sqlselectbycode"))
            sqlSelectByCode = p.getProperty("JdbcUserStore.sql.sqlselectbycode");
        else
            sqlSelectByCode = String.format(sqlSelectByCode, fieldEmail, fieldConfirmcode, fieldActive, fieldCreationdate, fieldLastmodified, sqlTableName, fieldConfirmcode);
        // select by email
        if (p.containsKey("JdbcUserStore.sql.sqlselectbyemail"))
            sqlSelectByEmail = p.getProperty("JdbcUserStore.sql.sqlselectbyemail");
        else
            sqlSelectByEmail = String.format(sqlSelectByEmail, fieldEmail, fieldConfirmcode, fieldActive, fieldCreationdate, fieldLastmodified, sqlTableName, fieldEmail);
        // CREATE TABLE
        if (p.containsKey("JdbcUserStore.sql.sqlcreatetable"))
            sqlCreateTable = p.getProperty("JdbcUserStore.sql.sqlcreatetable");
        else
            sqlCreateTable = String.format(sqlCreateTable, sqlTableName, fieldEmail, fieldConfirmcode, fieldActive, fieldCreationdate, fieldLastmodified, fieldConfirmcode);

        // datasource creation
        if (p.containsKey("JdbcUserStore.datasource.jndi")) {
            try {
                InitialContext ic = new InitialContext();
                dataSource = (DataSource) ic.lookup(p.getProperty("JdbcUserStore.datasource.jndi"));
            } catch (NamingException e) {
                throw new SelfRegisterException(e.getMessage(), e);
            }
        } else {
            try {
                this.dataSource = new SimpleDataSource(
                        p.getProperty("JdbcUserStore.datasource.user"),
                        p.getProperty("JdbcUserStore.datasource.pass"),
                        p.getProperty("JdbcUserStore.datasource.url"),
                        p.getProperty("JdbcUserStore.datasource.driver"));
            } catch (Exception e) {
                throw new SelfRegisterException(e.getMessage(), e);
            }
        }
        if ("true".equalsIgnoreCase(p.getProperty("JdbcUserStore.datasource.createtable", "false"))) {
            Connection conn = null;
            try {
                try {
                    conn = dataSource.getConnection();
                    Statement st = conn.createStatement();
                    boolean tableExists = false;
                    try {
                        ResultSet rstbl = st.executeQuery("select count(*) from " + sqlTableName);
                        tableExists = rstbl.next(); // no existe!
                    } catch (Exception ex) {
                        // table does not exist
                        // could put here executeUpdate
                    }
                    if (!tableExists)
                        st.executeUpdate(sqlCreateTable);
                } finally {
                    if (conn != null)
                        conn.close();
                }
            } catch (SQLException e) {
                throw new SelfRegisterException(e.getMessage(), e);
            }
        }
    }

    private Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public UserRegistration findUserByCode(String confirmcode) throws SelfRegisterException {
        UserRegistration u = null;
        Connection conn = null;
        try {
            conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(sqlSelectByCode);
            ps.setString(1, confirmcode);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                u = new UserRegistration();
                u.setEmail(rs.getString(fieldEmail));
                u.setConfirmCode(rs.getString(fieldConfirmcode));
                u.setActive(rs.getBoolean(fieldActive));
                u.setCreationDate(rs.getTimestamp(fieldCreationdate));
                u.setLastModified(rs.getTimestamp(fieldLastmodified));
            }
            rs.close();
            ps.close();

        } catch (SQLException e) {
            throw new SelfRegisterException(e.getMessage(), e);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    throw new SelfRegisterException(e.getMessage(), e);
                }
            }
        }
        return u;
    }

    public UserRegistration findUserByEmail(String email) throws SelfRegisterException {
        UserRegistration u = null;
        Connection conn = null;
        try {
            conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(sqlSelectByEmail);
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                u = new UserRegistration();
                u.setEmail(rs.getString(fieldEmail));
                u.setConfirmCode(rs.getString(fieldConfirmcode));
                u.setActive(rs.getBoolean(fieldActive));
                u.setCreationDate(rs.getTimestamp(fieldCreationdate));
                u.setLastModified(rs.getTimestamp(fieldLastmodified));
            }
            rs.close();
            ps.close();

        } catch (SQLException e) {
            throw new SelfRegisterException(e.getMessage(), e);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    throw new SelfRegisterException(e.getMessage(), e);
                }
            }
        }
        return u;
    }

    public UserRegistration createUser(String email) throws SelfRegisterException {
        UserRegistration u = new UserRegistration();
        u.setEmail(email);
        u.setConfirmCode(generateConfirmCode());
        u.setActive(false);
        u.setCreationDate(new Date());
        u.setLastModified(u.getCreationDate());
        Connection conn = null;
        try {
            conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(sqlInsert);
            ps.setString(1, u.getEmail());
            ps.setString(2, u.getConfirmCode());
            ps.setBoolean(3, u.isActive());
            ps.setTimestamp(4, new Timestamp(u.getCreationDate().getTime()));
            ps.setTimestamp(5, new Timestamp(u.getLastModified().getTime()));
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            throw new SelfRegisterException(e.getMessage(), e);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    throw new SelfRegisterException(e.getMessage(), e);
                }
            }
        }
        return u;
    }

    public void saveUser(UserRegistration user) throws SelfRegisterException {
        Connection conn = null;
        try {
            conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(sqlUpdate);
            ps.setString(1, user.getConfirmCode());
            ps.setBoolean(2, user.isActive());
            ps.setTimestamp(3, new Timestamp(user.getCreationDate().getTime()));
            ps.setTimestamp(4, new Timestamp(user.getLastModified().getTime()));

            ps.setString(5, user.getEmail());

            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            throw new SelfRegisterException(e.getMessage(), e);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    throw new SelfRegisterException(e.getMessage(), e);
                }
            }
        }
    }

    private class SimpleDataSource implements DataSource {
        private final String jdbcUser;
        private final String jdbcPass;
        private final String jdbcUrl;
        private final String jdbcDriver;

        public SimpleDataSource(String user, String pass, String url, String driver) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException, SQLException {
            this.jdbcUser = user;
            this.jdbcPass = pass;
            this.jdbcUrl = url;
            this.jdbcDriver = driver;
            DriverManager.registerDriver((Driver) Class.forName(this.jdbcDriver).getConstructor().newInstance());
        }

        public Connection getConnection() throws SQLException {
            return DriverManager.getConnection(this.jdbcUrl, this.jdbcUser, this.jdbcPass);
        }

        public Connection getConnection(String username, String password) throws SQLException {
            return DriverManager.getConnection(this.jdbcUrl, username, password);
        }

        public PrintWriter getLogWriter() throws SQLException {
            return null;
        }

        public void setLogWriter(PrintWriter out) throws SQLException {

        }

        public void setLoginTimeout(int seconds) throws SQLException {

        }

        public int getLoginTimeout() throws SQLException {
            return 0;
        }

        public Logger getParentLogger() throws SQLFeatureNotSupportedException {
            return null;
        }

        public <T> T unwrap(Class<T> iface) throws SQLException {
            return null;
        }

        public boolean isWrapperFor(Class<?> iface) throws SQLException {
            return false;
        }
    }
}
