/**
 *
 */
package com.mogobiz.store.admin

import com.mogobiz.ajax.AjaxResponseService
import com.mogobiz.store.domain.Seller
import com.mogobiz.store.domain.User
import com.mogobiz.utils.RandomPassword
import com.mogobiz.utils.SymmetricCrypt
import grails.plugin.mail.MailService
import grails.util.Holders
import org.apache.shiro.SecurityUtils
import org.apache.shiro.crypto.hash.Sha256Hash

/**
 * Controller utilis� pour g�rer le mot de passe des vendeurs
 *
 * @author stephane.manciot@ebiznext.com
 *
 */
class SellerPasswordController {
    AjaxResponseService ajaxResponseService
    def grailsApplication

    MailService mailService

    def forgotPassword = {
        return [:]
    }

    /**
     * called when an admin user wants to reset a user password
     * @param id
     * @return
     */
    def resetPassword(long id) {
        Seller seller = Seller.get(id)
        String targetUri = grailsApplication.config.grails.serverURL
        if (seller) {

            // to fix potential security hole
            if (!SecurityUtils.getSubject().isPermitted('company:' + seller.company?.id + ':admin')) {
                redirect(controller: 'auth', action: 'unauthorized')
            } else {

                def clearPassword = RandomPassword.getRandomPassword(10)
                seller.password = new Sha256Hash(clearPassword)
                if (seller.validate()) {
                    seller.save()
                }
                // email
                mailService.sendMail {
                    to seller.email
                    subject 'Password reset'
                    body(view: '/sellerPassword/resetPasswordEmail', model: [targetUri: targetUri, login: seller.email, password: clearPassword])
                }

                render true as boolean;
            }
        } else {
            response.sendError 404
        }
    }

    /**
     * Called when a unauthenticated user forgot his password and wants his email changed.
     * @param email
     * @return
     */
    def resendPassword(String email) {
        User account = User.findByEmail(email)
        if (account) {
            String clearData = email + ";" + System.currentTimeMillis() + ";" + UUID.randomUUID().toString()
            String cryptedData = SymmetricCrypt.encrypt(clearData, Holders.config.application.secret as String, "AES")
            mailService.sendMail {
                to account.email
                subject 'Password reset security check'
                body(view: '/sellerPassword/resendPasswordEmail', model: [resendEmail: account.email, resendKey: cryptedData])
            }
        }
        String targetUri = grailsApplication.config.grails.serverURL
        return ["login": email, "targetUri": targetUri]
    }

    def resendPasswordConfirmation(String resendEmail, String resendKey) {
        String clearText = SymmetricCrypt.decrypt(resendKey, Holders.config.application.secret as String, "AES")
        String[] clearData = clearText.split(';')
        long now = Calendar.instance.time.time
        long duration = now - clearData[1].toLong()
        long twenty4 = 24 * 3600 * 1000
        String targetUri = grailsApplication.config.grails.serverURL;
        if (resendEmail == clearData[0] && duration < twenty4) {
            // ok you asked for it.
            User account = User.findByEmail(resendEmail)
            if (account) {
                def clearPassword = RandomPassword.getRandomPassword(10)
                account.password = new Sha256Hash(clearPassword)
                if (account.validate()) {
                    account.save()
                }

                // email
                mailService.sendMail {
                    to account.email
                    subject 'Password reset'
                    body(view: '/sellerPassword/resendPasswordConfirmationEmail', model: [targetUri: targetUri, login: account.email, password: clearPassword])
                }
                return ["login": account.email, "targetUri": targetUri]
            }
        }
    }
}