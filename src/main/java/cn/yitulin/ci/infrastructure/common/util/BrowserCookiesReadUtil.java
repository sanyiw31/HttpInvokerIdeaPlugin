package cn.yitulin.ci.infrastructure.common.util;

import cn.yitulin.ci.infrastructure.common.enums.BrowserEnum;
import cn.yitulin.ci.infrastructure.model.Cookie;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.sql.*;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * author : ⚡️
 * description :
 * date : Created in 2022/3/9 19:52
 * modified : 💧💨🔥
 */
@Slf4j
public class BrowserCookiesReadUtil {

    public static List<Cookie> read(BrowserEnum browserEnum, String domain, List<String> cookieNames) {
        log.info("start read,browserEnum:[{}],domain:[{}],cookieNames:[{}]", browserEnum, domain, cookieNames);
        List<Cookie> cookies = readFromCookiesDb(browserEnum, Lists.newArrayList(domain), cookieNames);
        log.info("finish read, response:[{}]", cookies);
        return cookies;
    }

    public static List<Cookie> read(BrowserEnum browserEnum, List<String> domains, List<String> cookieNames) {
        log.info("start read,browserEnum:[{}],domains:[{}],cookieNames:[{}]", browserEnum, domains, cookieNames);
        List<Cookie> cookies = readFromCookiesDb(browserEnum, domains, cookieNames);
        log.info("finish read, response:[{}]", cookies);
        return cookies;
    }

    private static List<Cookie> readFromCookiesDb(BrowserEnum browserEnum, List<String> domains, List<String> cookieNames) {
        Connection connection = null;
        Statement statement = null;
        List<Cookie> resultList = Lists.newArrayList();
        try {
            Class.forName("org.sqlite.JDBC");
            // create a database connection
            log.info("cookies sqlite数据库文件路径:[{}]", browserEnum.fetchCookiesDbPath());
            connection = DriverManager.getConnection("jdbc:sqlite:" + browserEnum.fetchCookiesDbPath());
            statement = connection.createStatement();
            statement.setQueryTimeout(3); // set timeout to 30 seconds
            ResultSet result;
            String sqlTemplate = "select * from cookies where host_key in (%s) and name in (%s);";
            String domainsInSql = Joiner.on("").join("'", Joiner.on("','").join(domains), "'");
            String namesInSql = Joiner.on("").join("'", Joiner.on("','").join(cookieNames), "'");
            String sql = String.format(sqlTemplate, domainsInSql, namesInSql);
            log.info("sqlite 即将执行的sql:[{}]", sql);
            result = statement.executeQuery(sql);
            while (result.next()) {
                Date creationUtc = result.getDate("creation_utc");
                Date expiresUtc = result.getDate("expires_utc");
                String name = result.getString("name");
                String domain = result.getString("host_key");
                byte[] encryptedBytes = result.getBytes("encrypted_value");
                resultList.add(Cookie.builder()
                        .domain(domain)
                        .name(name)
                        .createTime(calculateDateFromUtc(creationUtc))
                        .expireTime(calculateDateFromUtc(expiresUtc))
                        .value(decryptedValue(browserEnum, encryptedBytes))
                        .build());
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (Objects.nonNull(statement)) {
                try {
                    statement.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            if (Objects.nonNull(connection)) {
                try {
                    connection.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        } finally {
            if (Objects.nonNull(statement)) {
                try {
                    statement.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            if (Objects.nonNull(connection)) {
                try {
                    connection.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        }
        return resultList;
    }

    private static Date calculateDateFromUtc(Date utcDate) {
        return new Date(utcDate.getTime() / 1000 - 11644473600000L);
    }

    private static String decryptedValue(BrowserEnum browserEnum, byte[] encryptedBytes) {
        byte[] decryptedBytes;
        try {
            byte[] salt = "saltysalt".getBytes();
            char[] password = browserEnum.fetchCookiesKeyring().toCharArray();
            char[] iv = new char[16];
            Arrays.fill(iv, ' ');
            int keyLength = 16;

            int iterations = 1003;

            PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, keyLength * 8);
            SecretKeyFactory pbkdf2 = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");

            byte[] aesKey = pbkdf2.generateSecret(spec).getEncoded();

            SecretKeySpec keySpec = new SecretKeySpec(aesKey, "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, keySpec, new IvParameterSpec(new String(iv).getBytes()));
            String encryptedString = new String(encryptedBytes);
            log.info("encryptedString is:[{}]", encryptedString);
            // if cookies are encrypted "v10" is a the prefix (has to be removed before decryption)
            if (encryptedString.startsWith("v10")) {
                encryptedBytes = Arrays.copyOfRange(encryptedBytes, 3, encryptedBytes.length);
            }
            decryptedBytes = cipher.doFinal(encryptedBytes);
        } catch (Exception e) {
            decryptedBytes = null;
        }
        if (decryptedBytes == null) {
            return null;
        } else {
            return new String(decryptedBytes);
        }
    }

}
