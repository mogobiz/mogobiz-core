package com.mogobiz.exceptions

/**
 * Created by hayssams on 16/09/14.
 */
class InvalidDomainObjectException extends Exception {
    String domainName
    InvalidDomainObjectException(String s) {
        domainName = s
    }
}
