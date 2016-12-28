package io.github.ivetech.auxiliaries.proxool;

import com.alibaba.druid.pool.DruidDataSource;
import org.junit.Before;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.NumberFormat;
import java.util.concurrent.CountDownLatch;

/**
 * io.github.ivetech.auxiliaries.proxool Tester
 *
 * @author Elve.xu [xuhw@yyft.com]
 * @version v1.0 - 28/12/2016.
 */
public class ProxoolDataSourceTester4Java {


    public final int LOOP_COUNT = 5;
    private final int THREAD_COUNT = 50;
    public final int COUNT = 1000 * 10;

    private String jdbcUrl;
    private String user;
    private String password;
    private String driverClass;

    private int maxActive = 100;
    private int minActive = 5;

    private String validationQuery = "SELECT 1";


    @Before
    public void before () {
        jdbcUrl = "jdbc:mysql://127.0.0.1:3306/cancal?zeroDateTimeBehavior=convertToNull&useUnicode=true&characterEncoding=utf8&generateSimpleParameterMetadata=true&useSSL=false";
        user = "root";
        password = "online";
        driverClass = "com.mysql.jdbc.Driver";
    }


    @Test
    public void testDruid () throws Exception {
        final DruidDataSource dataSource = new DruidDataSource();

        dataSource.setInitialSize(0);
        dataSource.setMaxActive(maxActive);
        dataSource.setMinIdle(3);
        dataSource.setMaxIdle(5);
        dataSource.setPoolPreparedStatements(true);
        dataSource.setDriverClassName(driverClass);
        dataSource.setUrl(jdbcUrl);
        dataSource.setPoolPreparedStatements(true);
        dataSource.setMaxWait(6000);
        dataSource.setUsername(user);
        dataSource.setPassword(password);
        dataSource.setValidationQuery(validationQuery);
        dataSource.setTestOnBorrow(false);
        dataSource.setMinEvictableIdleTimeMillis(3000);

//        for (int i = 0; i < LOOP_COUNT; ++i) {
        p0(dataSource, "druid");
//        }

        System.out.println();
    }


    @Test
    public void testConnection () throws Exception {

        ProxoolDataSource proxoolDataSource = new ProxoolDataSource();
        proxoolDataSource.setAlias("T-ALIAS");
        proxoolDataSource.setDriverUrl(jdbcUrl);
        proxoolDataSource.setDriver(driverClass);
        proxoolDataSource.setUser(user);
        proxoolDataSource.setPassword(password);


        proxoolDataSource.setSimultaneousBuildThrottle(100);

//        proxoolDataSource.setMaximumActiveTime();
        proxoolDataSource.setMaximumConnectionCount(maxActive);
//        proxoolDataSource.setMinimumConnectionCount(minActive);
//        proxoolDataSource.setHouseKeepingTestSql(validationQuery);
        proxoolDataSource.setVerbose(true);
        proxoolDataSource.setStatisticsLogLevel("ERROR");
        proxoolDataSource.setStatistics("10s,1m,1d");
        proxoolDataSource.setPrototypeCount(3);


        p0(proxoolDataSource, "proxool");
    }


    private void p0 (final DataSource dataSource, String name) throws Exception {
        long startMillis = System.currentTimeMillis();
        long startYGC = TestUtil.getYoungGC();
        long startFullGC = TestUtil.getFullGC();

        final CountDownLatch endLatch = new CountDownLatch(THREAD_COUNT);
        for (int i = 0; i < THREAD_COUNT; ++i) {
            Thread thread = new Thread() {

                public void run () {
                    try {

                        for (int i = 0; i < COUNT; ++i) {
                            Connection conn = dataSource.getConnection();
                            Statement stmt = conn.createStatement();
                            ResultSet rs = stmt.executeQuery("SELECT 1");
                            Thread.sleep(0, 1000 * 100);
                            rs.close();
                            stmt.close();
                            conn.close();
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        endLatch.countDown();
                    }
                }
            };
            thread.start();
        }
        endLatch.await();

        long millis = System.currentTimeMillis() - startMillis;
        long ygc = TestUtil.getYoungGC() - startYGC;
        long fullGC = TestUtil.getFullGC() - startFullGC;

        System.out.println(name + " millis : " + NumberFormat.getInstance().format(millis) + ", YGC " + ygc + " FGC " + fullGC);
    }

}
