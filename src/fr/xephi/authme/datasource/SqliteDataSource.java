package fr.xephi.authme.datasource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import org.sqlite.*;

import fr.xephi.authme.ConsoleLogger;
import fr.xephi.authme.api.API;
import fr.xephi.authme.cache.auth.PlayerAuth;
import fr.xephi.authme.datasource.MiniConnectionPoolManager.TimeoutException;
import fr.xephi.authme.settings.Settings;

/**
 *
 * @author stefano
 */
@SuppressWarnings("unused")
public class SqliteDataSource implements DataSource {

    private String database;
    private String tableName;
    private String columnName;
    private String columnPassword;
    private String columnIp;
    private String columnLastLogin;
    private String columnSalt;
    private String columnGroup;
    private String lastlocX;
    private String lastlocY;
    private String lastlocZ;
    private String lastlocWorld;
    private String columnEmail;
    private String columnID;
    private Connection con;

    public SqliteDataSource() throws ClassNotFoundException, SQLException {
    	this.database = Settings.getMySQLDatabase;
        this.tableName = Settings.getMySQLTablename;
        this.columnName = Settings.getMySQLColumnName;
        this.columnPassword = Settings.getMySQLColumnPassword;
        this.columnIp = Settings.getMySQLColumnIp;
        this.columnLastLogin = Settings.getMySQLColumnLastLogin;
        this.columnSalt = Settings.getMySQLColumnSalt;
        this.columnGroup = Settings.getMySQLColumnGroup;
        this.lastlocX = Settings.getMySQLlastlocX;
        this.lastlocY = Settings.getMySQLlastlocY;
        this.lastlocZ = Settings.getMySQLlastlocZ;
        this.lastlocWorld = Settings.getMySQLlastlocWorld;
        this.columnEmail = Settings.getMySQLColumnEmail;
        this.columnID = Settings.getMySQLColumnId;

        connect();
        setup();
    }

    private synchronized void connect() throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");
        ConsoleLogger.info("SQLite driver loaded");
        this.con = DriverManager.getConnection("jdbc:sqlite:plugins/AuthMe/"+database+".db");
    }

    private synchronized void setup() throws SQLException {
        Statement st = null;
        ResultSet rs = null;
        try {
            st = con.createStatement();
            st.executeUpdate("CREATE TABLE IF NOT EXISTS " + tableName + " ("
                    + columnID + " INTEGER AUTO_INCREMENT,"
                    + columnName + " VARCHAR(255) NOT NULL UNIQUE,"
                    + columnPassword + " VARCHAR(255) NOT NULL,"
                    + columnIp + " VARCHAR(40) NOT NULL,"
                    + columnLastLogin + " BIGINT,"
                    + lastlocX + " smallint(6) DEFAULT '0',"
                    + lastlocY + " smallint(6) DEFAULT '0',"
                    + lastlocZ + " smallint(6) DEFAULT '0',"
                    + lastlocWorld + " VARCHAR(255) DEFAULT 'world',"
                    + columnEmail + " VARCHAR(255) DEFAULT 'your@email.com',"
                    + "CONSTRAINT table_const_prim PRIMARY KEY (" + columnID + "));");
            rs = con.getMetaData().getColumns(null, null, tableName, columnPassword);
            if (!rs.next()) {
                st.executeUpdate("ALTER TABLE " + tableName + " ADD COLUMN "
                        + columnPassword + " VARCHAR(255) NOT NULL;");
            }
            rs.close();
            rs = con.getMetaData().getColumns(null, null, tableName, columnIp);
            if (!rs.next()) {
                st.executeUpdate("ALTER TABLE " + tableName + " ADD COLUMN "
                        + columnIp + " VARCHAR(40) NOT NULL;");
            }
            rs.close();
            rs = con.getMetaData().getColumns(null, null, tableName, columnLastLogin);
            if (!rs.next()) {
                st.executeUpdate("ALTER TABLE " + tableName + " ADD COLUMN "
                        + columnLastLogin + " BIGINT;");
            }
            rs.close();
            rs = con.getMetaData().getColumns(null, null, tableName, lastlocX);
            if (!rs.next()) {
                st.executeUpdate("ALTER TABLE " + tableName + " ADD COLUMN " + lastlocX + " smallint(6) NOT NULL DEFAULT '0'; " 
                        + "ALTER TABLE " + tableName + " ADD COLUMN " + lastlocY + " smallint(6) NOT NULL DEFAULT '0'; "
                        + "ALTER TABLE " + tableName + " ADD COLUMN " + lastlocZ + " smallint(6) NOT NULL DEFAULT '0';");
            }
            rs.close();
            rs = con.getMetaData().getColumns(null, null, tableName, lastlocWorld);
            if (!rs.next()) {
            	st.executeUpdate("ALTER TABLE " + tableName + " ADD COLUMN " + lastlocWorld + " VARCHAR(255) NOT NULL DEFAULT 'world';");
            }
            rs.close();
            rs = con.getMetaData().getColumns(null, null, tableName, columnEmail);
            if (!rs.next()) {
            	st.executeUpdate("ALTER TABLE " + tableName + " ADD COLUMN " + columnEmail + "  VARCHAR(255) DEFAULT 'your@email.com';");
            }
        } finally {
            close(rs);
            close(st);
        }
        ConsoleLogger.info("SQLite Setup finished");
    }

    @Override
    public synchronized boolean isAuthAvailable(String user) {
        PreparedStatement pst = null;
        ResultSet rs = null;
        try {
            pst = con.prepareStatement("SELECT * FROM " + tableName + " WHERE " + columnName + "=?");
            pst.setString(1, user);
            rs = pst.executeQuery();
            return rs.next();
        } catch (SQLException ex) {
            ConsoleLogger.showError(ex.getMessage());
            return false;
        } finally {
            close(rs);
            close(pst);
        }
    }

    @Override
    public synchronized PlayerAuth getAuth(String user) {
        PreparedStatement pst = null;
        ResultSet rs = null;
        try {
            pst = con.prepareStatement("SELECT * FROM " + tableName + " WHERE "
                    + columnName + "=?;");
            pst.setString(1, user);
            rs = pst.executeQuery();
            if (rs.next()) {
                if (rs.getString(columnIp).isEmpty() ) {
                    return new PlayerAuth(rs.getString(columnName), rs.getString(columnPassword), "198.18.0.1", rs.getLong(columnLastLogin), rs.getInt(lastlocX), rs.getInt(lastlocY), rs.getInt(lastlocZ), rs.getString(lastlocWorld) , rs.getString(columnEmail), API.getPlayerRealName(rs.getString(columnName)));
                } else {
                        if(!columnSalt.isEmpty()){
                            return new PlayerAuth(rs.getString(columnName), rs.getString(columnPassword),rs.getString(columnSalt), rs.getInt(columnGroup), rs.getString(columnIp), rs.getLong(columnLastLogin), rs.getInt(lastlocX), rs.getInt(lastlocY), rs.getInt(lastlocZ), rs.getString(lastlocWorld) , rs.getString(columnEmail), API.getPlayerRealName(rs.getString(columnName)));
                        } else {
                            return new PlayerAuth(rs.getString(columnName), rs.getString(columnPassword), rs.getString(columnIp), rs.getLong(columnLastLogin), rs.getInt(lastlocX), rs.getInt(lastlocY), rs.getInt(lastlocZ), rs.getString(lastlocWorld) , rs.getString(columnEmail), API.getPlayerRealName(rs.getString(columnName)));
                        }
                 }
            } else {
                return null;
            }
        } catch (SQLException ex) {
            ConsoleLogger.showError(ex.getMessage());
            return null;
        } finally {
            close(rs);
            close(pst);
        }
    }

    @Override
    public synchronized boolean saveAuth(PlayerAuth auth) {
        PreparedStatement pst = null;
        try {
            if (columnSalt.isEmpty() && auth.getSalt().isEmpty()) {
                pst = con.prepareStatement("INSERT INTO " + tableName + "(" + columnName + "," + columnPassword + "," + columnIp + "," + columnLastLogin + ") VALUES (?,?,?,?);");
                pst.setString(1, auth.getNickname());
                pst.setString(2, auth.getHash());
                pst.setString(3, auth.getIp());
                pst.setLong(4, auth.getLastLogin());
                pst.executeUpdate();
            } else {
                pst = con.prepareStatement("INSERT INTO " + tableName + "(" + columnName + "," + columnPassword + "," + columnIp + "," + columnLastLogin + "," + columnSalt + ") VALUES (?,?,?,?,?);");
                pst.setString(1, auth.getNickname());
                pst.setString(2, auth.getHash());
                pst.setString(3, auth.getIp());
                pst.setLong(4, auth.getLastLogin());
                pst.setString(5, auth.getSalt());
                pst.executeUpdate();
            }
        } catch (SQLException ex) {
            ConsoleLogger.showError(ex.getMessage());
            return false;
        } finally {
            close(pst);
        }
        return true;
    }

    @Override
    public synchronized boolean updatePassword(PlayerAuth auth) {
        PreparedStatement pst = null;
        try {
            pst = con.prepareStatement("UPDATE " + tableName + " SET " + columnPassword + "=? WHERE " + columnName + "=?;");
            pst.setString(1, auth.getHash());
            pst.setString(2, auth.getNickname());
            pst.executeUpdate();
        } catch (SQLException ex) {
            ConsoleLogger.showError(ex.getMessage());
            return false;
        } finally {
            close(pst);
        }
        return true;
    }

    @Override
    public boolean updateSession(PlayerAuth auth) {
        PreparedStatement pst = null;
        try {
            pst = con.prepareStatement("UPDATE " + tableName + " SET " + columnIp + "=?, " + columnLastLogin + "=? WHERE " + columnName + "=?;");
            pst.setString(1, auth.getIp());
            pst.setLong(2, auth.getLastLogin());
            pst.setString(3, auth.getNickname());
            pst.executeUpdate();
        } catch (SQLException ex) {
            ConsoleLogger.showError(ex.getMessage());
            return false;
        } finally {
            close(pst);
        }
        return true;
    }

    @Override
    public int purgeDatabase(long until) {
        PreparedStatement pst = null;
        try {
           
            pst = con.prepareStatement("DELETE FROM " + tableName + " WHERE " + columnLastLogin + "<?;");
            pst.setLong(1, until);
            return pst.executeUpdate();
        } catch (SQLException ex) {
            ConsoleLogger.showError(ex.getMessage());
            return 0;
        } finally {
            close(pst);
        }
    }

    @Override
    public List<String> autoPurgeDatabase(long until) {
        PreparedStatement pst = null;
        ResultSet rs = null;
        List<String> list = new ArrayList<String>();
        try {
            pst = con.prepareStatement("SELECT * FROM " + tableName + " WHERE " + columnLastLogin + "<?;");
            pst.setLong(1, until);
            rs = pst.executeQuery();
            while (rs.next()) {
            	list.add(rs.getString(columnName));
            }
            return list;
        } catch (SQLException ex) {
            ConsoleLogger.showError(ex.getMessage());
            return new ArrayList<String>();
        } finally {
        	close(rs);
            close(pst);
        }
    }

    @Override
    public synchronized boolean removeAuth(String user) {
        PreparedStatement pst = null;
        try {
            pst = con.prepareStatement("DELETE FROM " + tableName + " WHERE " + columnName + "=?;");
            pst.setString(1, user);
            pst.executeUpdate();
        } catch (SQLException ex) {
            ConsoleLogger.showError(ex.getMessage());
            return false;
        } finally {
            close(pst);
        }
        return true;
    }

    @Override
    public boolean updateQuitLoc(PlayerAuth auth) {
        PreparedStatement pst = null;
        try {
            pst = con.prepareStatement("UPDATE " + tableName + " SET " + lastlocX + "=?, "+ lastlocY +"=?, "+ lastlocZ +"=?, " + lastlocWorld + "=? WHERE " + columnName + "=?;");
            pst.setLong(1, auth.getQuitLocX());
            pst.setLong(2, auth.getQuitLocY());
            pst.setLong(3, auth.getQuitLocZ());
            pst.setString(4, auth.getWorld());
            pst.setString(5, auth.getNickname());
            pst.executeUpdate();
        } catch (SQLException ex) {
            ConsoleLogger.showError(ex.getMessage());
            return false;
        } finally {
            close(pst);
        }
        return true;
    }

    @Override
    public int getIps(String ip) {
        PreparedStatement pst = null;
        ResultSet rs = null;
        int countIp=0;
        try {
            pst = con.prepareStatement("SELECT * FROM " + tableName + " WHERE "
                    + columnIp + "=?;");
            pst.setString(1, ip);
            rs = pst.executeQuery();
            while(rs.next()) {
                countIp++;    
            } 
             return countIp;
        } catch (SQLException ex) {
            ConsoleLogger.showError(ex.getMessage());
            return 0;
        }  finally {
            close(rs);
            close(pst);
        }         
    }

	@Override
	public boolean updateEmail(PlayerAuth auth) {
        PreparedStatement pst = null;
        try {
            pst = con.prepareStatement("UPDATE " + tableName + " SET " + columnEmail + "=? WHERE " + columnName + "=?;");
            pst.setString(1, auth.getEmail());
            pst.setString(2, auth.getNickname());
            pst.executeUpdate();
        } catch (SQLException ex) {
            ConsoleLogger.showError(ex.getMessage());
            return false;
        } finally {
            close(pst);
        }
        return true;
    }

	@Override
	public boolean updateSalt(PlayerAuth auth) {
		if(columnSalt.isEmpty()) {
			return false;
		}
        PreparedStatement pst = null;
        try {
            pst = con.prepareStatement("UPDATE " + tableName + " SET " + columnSalt + "=? WHERE " + columnName + "=?;");
            pst.setString(1, auth.getSalt());
            pst.setString(2, auth.getNickname());
            pst.executeUpdate();
        } catch (SQLException ex) {
            ConsoleLogger.showError(ex.getMessage());
            return false;
        } finally {
            close(pst);
        }
        return true;
    }

    @Override
    public synchronized void close() {
        try {
            con.close();
        } catch (SQLException ex) {
            ConsoleLogger.showError(ex.getMessage());
        }
    }

    @Override
    public void reload() {
    }

    private void close(Statement st) {
        if (st != null) {
            try {
                st.close();
            } catch (SQLException ex) {
                ConsoleLogger.showError(ex.getMessage());
            }
        }
    }

    private void close(ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException ex) {
                ConsoleLogger.showError(ex.getMessage());
            }
        }
    }

    private void close(Connection con) {
        if (con != null) {
            try {
                con.close();
            } catch (SQLException ex) {
                ConsoleLogger.showError(ex.getMessage());
            }
        }
    }

	@Override
	public List<String> getAllAuthsByName(PlayerAuth auth) {
        PreparedStatement pst = null;
        ResultSet rs = null;
        List<String> countIp = new ArrayList<String>();
        try {
            pst = con.prepareStatement("SELECT * FROM " + tableName + " WHERE "
                    + columnIp + "=?;");
            pst.setString(1, auth.getIp());
            rs = pst.executeQuery();
            while(rs.next()) {
                countIp.add(rs.getString(columnName));    
            } 
             return countIp;
        } catch (SQLException ex) {
            ConsoleLogger.showError(ex.getMessage());
            return new ArrayList<String>();
        } catch (TimeoutException ex) {
            ConsoleLogger.showError(ex.getMessage());
            return new ArrayList<String>();
        } catch (NullPointerException npe) {
        	return new ArrayList<String>();
        } finally {
            close(rs);
            close(pst);
        } 
	}

	@Override
	public List<String> getAllAuthsByIp(String ip) {
        PreparedStatement pst = null;
        ResultSet rs = null;
        List<String> countIp = new ArrayList<String>();
        try {
            pst = con.prepareStatement("SELECT * FROM " + tableName + " WHERE "
                    + columnIp + "=?;");
            pst.setString(1, ip);
            rs = pst.executeQuery();
            while(rs.next()) {
                countIp.add(rs.getString(columnName));    
            } 
             return countIp;
        } catch (SQLException ex) {
            ConsoleLogger.showError(ex.getMessage());
            return new ArrayList<String>();
        } catch (TimeoutException ex) {
            ConsoleLogger.showError(ex.getMessage());
            return new ArrayList<String>();
        } catch (NullPointerException npe) {
        	return new ArrayList<String>();
        } finally {
            close(rs);
            close(pst);
        } 
	}

	@Override
	public List<String> getAllAuthsByEmail(String email) {
        PreparedStatement pst = null;
        ResultSet rs = null;
        List<String> countEmail = new ArrayList<String>();
        try {
            pst = con.prepareStatement("SELECT * FROM " + tableName + " WHERE "
                    + columnEmail + "=?;");
            pst.setString(1, email);
            rs = pst.executeQuery();
            while(rs.next()) {
                countEmail.add(rs.getString(columnName));    
            } 
             return countEmail;
        } catch (SQLException ex) {
            ConsoleLogger.showError(ex.getMessage());
            return new ArrayList<String>();
        } catch (TimeoutException ex) {
            ConsoleLogger.showError(ex.getMessage());
            return new ArrayList<String>();
        } catch (NullPointerException npe) {
        	return new ArrayList<String>();
        } finally {
            close(rs);
            close(pst);
        } 
	}

	@Override
	public void purgeBanned(List<String> banned) {
        PreparedStatement pst = null;
        try {
           for (String name : banned) {
        	   pst = con.prepareStatement("DELETE FROM " + tableName + " WHERE " + columnName + "=?;");
        	   pst.setString(1, name);
        	   pst.executeUpdate();
           }
        } catch (SQLException ex) {
            ConsoleLogger.showError(ex.getMessage());
        } finally {
            close(pst);
        }
	}

}
