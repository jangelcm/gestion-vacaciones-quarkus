
package org.acme.services;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtils {
    public static String hashPassword(String password) {
        return BCrypt.hashpw(password, org.mindrot.jbcrypt.BCrypt.gensalt());
    }

    public static boolean verifyPassword(String password, String hash) {
        return org.mindrot.jbcrypt.BCrypt.checkpw(password, hash);
    }
}
