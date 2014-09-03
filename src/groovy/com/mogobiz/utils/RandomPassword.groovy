/**
 * 
 */
package com.mogobiz.utils

/**
 * @version $Id $
 *
 */
class RandomPassword {
    /**
     * 
     */
    private static final String charset = "_0123456789AaBbCcDdEeFfGgHhIiJjKkLlMmNnOoPpQqRrSsTtUuVvWwXxYyZz";

    /**
     * @param length - password length
     * @return random password
     */
    def static String getRandomPassword(int length) {
        Random rand = new Random(System.currentTimeMillis())
        StringBuffer sb = new StringBuffer()
        for (int i = 0; i < length; i++) {
            int pos = rand.nextInt(charset.length())
            sb.append(charset.charAt(pos))
        }
        return sb.toString()
    }
}
