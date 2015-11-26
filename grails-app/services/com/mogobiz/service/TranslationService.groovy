/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

package com.mogobiz.service

import com.mogobiz.ajax.AjaxResponse
import com.mogobiz.ajax.AjaxResponseService
import com.mogobiz.store.domain.Translation
import com.mogobiz.store.domain.User
import grails.util.Holders

/**
 * Translation manager service
 */
class TranslationService {
    static transactional = true

    AjaxResponseService ajaxResponseService;

    /**
     * Returns a list of configurable languages ​​by the Partner application
     * @return
     */
    List<String> languages() {
        return Holders.config.application.languages
    }

    /**
     * Returns a list of Translation corresponding to the given target
     * @param target
     * @return
     */
    List<Map> list(long target, String type) {
        List<Translation> list = Translation.findAllByTargetAndType(target, type);
        List<Map> result = [];
        list.each { Translation t ->
            result << t.asMapForJSON();
        }
        return result;
    }

    /**
     * Delete the Translation corresponding to the given target and language
     * @param target
     * @param lang
     * @return
     */
    AjaxResponse delete(long target, String lang, String type) {
        AjaxResponse result = new AjaxResponse()

        Translation t = findByTargetTypeAndLang(target, type, lang);
        if (t != null) {
            t.delete()
            result.success = true;
        }
        return result;
    }

    /**
     * Create or update the Translation corresponding to the given target and language.
     * @param target
     * @param lang
     * @param value
     * @return
     */
    AjaxResponse update(User user, long target, String lang, String value, String type) {
        AjaxResponse result = new AjaxResponse()

        Translation t = findByTargetTypeAndLang(target, type, lang);
        if (t == null) {
            t = new Translation(companyId: user.company.id, target: target, lang: lang, type: type)
        }

        t.value = value;
        if (t.validate()) {
            t.save();
            result.success = true;
        } else {
            result = ajaxResponseService.prepareResponse(t, null);
        }
        return result;
    }

    private com.mogobiz.store.domain.Translation findByTargetTypeAndLang(
            final long target, final java.lang.String type, final java.lang.String lang) {
        def ret = Translation.executeQuery("SELECT translation FROM Translation AS translation WHERE translation.target = :target AND translation.type = :type AND translation.lang = :lang",
                [target: target, type: type, lang: lang]).iterator()
        return ret.hasNext() ? ret.next() : null
    }
}
