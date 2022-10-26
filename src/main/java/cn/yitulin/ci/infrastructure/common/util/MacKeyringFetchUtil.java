package cn.yitulin.ci.infrastructure.common.util;

import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

/**
 * author : ⚡️
 * description :
 * date : Created in 2022/3/10 01:15
 * modified : 💧💨🔥
 */
@Slf4j
public class MacKeyringFetchUtil {

    public static Map<String, String> applicationKeyringMap = Maps.newHashMap();

    public static String getMacKeyringPassword(String application) throws IOException {
        log.info("start getMacKeyringPassword,application:[{}]", application);
        if (applicationKeyringMap.containsKey(application)) {
            String keyring = applicationKeyringMap.get(application);
            log.info("finish getMacKeyringPassword, response:[{}]", keyring);
            return keyring;
        }
        Runtime rt = Runtime.getRuntime();
        String[] commands = {"security", "find-generic-password", "-w", "-s", application};
        Process proc = rt.exec(commands);
        BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));
        StringBuilder result = new StringBuilder();
        String s;
        while ((s = stdInput.readLine()) != null) {
            result.append(s);
        }
        String keyring = result.toString();
        applicationKeyringMap.put(application, keyring);
        log.info("finish getMacKeyringPassword, response:[{}]", keyring);
        return keyring;
    }

}
