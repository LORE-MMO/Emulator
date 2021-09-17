package nereus.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;
import nereus.config.ConfigData;
import it.gotoandplay.smartfoxserver.SmartFoxServer;
import it.gotoandplay.smartfoxserver.db.DbManager;

import java.lang.management.ManagementFactory;
import jdbchelper.JdbcHelper;

import javax.management.JMX;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

public class Database {
   public JdbcHelper jdbc;
   private HikariDataSource dataSource;
   private static HikariPoolMXBean poolProxy;


   public Database() {
      this(50);
   }

   public Database(int maxPoolSize) {
      super();
      HikariConfig config = new HikariConfig();
      config.setJdbcUrl("jdbc:mysql://"+ConfigData.DB_HOST+":"+ConfigData.DB_PORT+"/"+ConfigData.DB_NAME);
      config.setUsername(ConfigData.DB_USERNAME);
      config.setPassword(ConfigData.DB_PASSWORD);
      config.setIdleTimeout(300000);
      config.setRegisterMbeans(true);
      config.setAllowPoolSuspension(true);
      config.setMaximumPoolSize(maxPoolSize);

      config.addDataSourceProperty("cachePrepStmts", "true");
      config.addDataSourceProperty("prepStmtCacheSize", "250");
      config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
      config.addDataSourceProperty("useSSL", "false");
      this.dataSource = new HikariDataSource(config);
      this.jdbc = new JdbcHelper(dataSource);

      try {
         MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
         ObjectName poolName = new ObjectName("com.zaxxer.hikari:type=Pool (HikariPool-1)");
         poolProxy = JMX.newMXBeanProxy(mBeanServer, poolName, HikariPoolMXBean.class);
      } catch (MalformedObjectNameException e) {
         e.printStackTrace();
      }
      SmartFoxServer.log.info("Database connections initialized.");
   }

   /** @deprecated */
   @Deprecated
   public Database(DbManager db) {
      super();
      this.jdbc = new JdbcHelper(new SFSDataSource(db));
      SmartFoxServer.log.info("Database connections initialized.");
   }

   public int getIdleConnections() {
      return this.poolProxy.getIdleConnections();
   }

   public int getActiveConnections() {
      return this.poolProxy.getActiveConnections();
   }

   public JdbcHelper getJdbc() {
      return this.jdbc ;
   }

   public void destroy() {
      this.dataSource.close();
      SmartFoxServer.log.info("Database connections destroyed.");
   }
}