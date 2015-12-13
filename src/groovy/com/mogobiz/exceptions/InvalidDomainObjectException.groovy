package com.mogobiz.exceptions

/**
 */
class InvalidDomainObjectException extends Exception {
    String domainName
    InvalidDomainObjectException(String s) {
        domainName = s
    }
}
