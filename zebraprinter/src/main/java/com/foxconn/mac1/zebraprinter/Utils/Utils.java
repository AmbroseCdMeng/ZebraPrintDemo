/**
 * CopyRight© 2019   MAC(I)智網平臺開發部 版權所有
 */
package com.foxconn.mac1.zebraprinter.Utils;

import java.util.UUID;

public class Utils {
    public static String GUID() {
        UUID uuid = UUID.randomUUID();
        String str = uuid.toString();
        String uuidStr = str.replace("-", "");
        return uuidStr;
    }
}
