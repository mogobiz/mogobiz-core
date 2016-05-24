package com.mogobiz.listener

import com.mogobiz.auth.AuthRealm

import javax.servlet.http.HttpSessionEvent
import javax.servlet.http.HttpSessionListener

/**
 * Created by yoannbaudy on 24/05/16.
 */
class SessionListener implements HttpSessionListener {
    @Override
    void sessionCreated(HttpSessionEvent se) {

    }

    @Override
    void sessionDestroyed(HttpSessionEvent se) {
        AuthRealm.unlinkUserAndSessionId(se.session.id)
    }
}
