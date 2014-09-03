package com.mogobiz.service
import com.mogobiz.constant.IperConstant
import com.mogobiz.store.domain.BOCart
import com.mogobiz.store.domain.BOCartItem
import com.mogobiz.store.domain.Company
import com.mogobiz.store.domain.Product
import com.mogobiz.utils.IperUtil

class SaleService {

    static transactional = true

    /**
     * Returns a paginate list of all BOCart for the given email and date
     * BOCart is searched using the transactionUUID of the given email and name
     * @param sellerCompany
     * @param email
     * @param date
     * @param offset
     * @return
     */
    List<BOCart> searchBOCartByCustomer(Company sellerCompany, String email, Calendar date, int offset) {
        return BOCart.createCriteria().list(max: IperConstant.NUMBER_SALES_PER_PAGE, offset: offset)  {
            company {
                eq ("id", sellerCompany.id)
            }
            if (email) {
                eq ("buyer", email)
            }
            if (date) {
                //the end of the day
                Calendar minDate = IperUtil.resetCalendarTime(date.clone())
                Calendar maxDate = IperUtil.resetCalendarTime(date.clone())
                maxDate.add(Calendar.DAY_OF_YEAR, 1)
                maxDate.add(Calendar.SECOND, -1)
                and {
                    ge("date", minDate)
                    le("date", maxDate)
                }
            }
            order("date", "desc")
        }
    }

    List<BOCart> searchBOCartByProduct(Company sellerCompany, Product searchProduct, int offset) {
        return BOCartItem.createCriteria().list(max: IperConstant.NUMBER_SALES_PER_PAGE, offset: offset) {
            bOCart {
                company {
                    eq ("id", sellerCompany.id)
                }
                order("date", "desc")
            }
            bOProducts {
                product {
                    eq ("id", searchProduct.id)
                }
            }
            projections {
                property("bOCart")
            }
        }.unique();
    }

    /**
     * Returns all BOCartItem for the given parameters.
     * @param cart
     * @param code
     * @return
     */
    List<BOCartItem> searchBOCartItemByCode(BOCart cart, String code) {
        return BOCartItem.createCriteria().list  {
            bOCart {
                eq("id", cart.id)
            }
            if (code != null && code.length() > 0) {
                ilike("code", "%" + code + "%")
            }
            order("startDate", "desc")
        }
    }

    /**
     * Returns all BOCartItem for the given parameters.
     * @param cart
     * @param code
     * @return
     */
    List<BOCartItem> searchBOCartItemByDate(BOCart cart, Calendar startDate, Calendar endDate) {
        Calendar minDate = IperUtil.resetCalendarTime(startDate.clone())
        Calendar maxDate = IperUtil.resetCalendarTime(endDate.clone())
        maxDate.add(Calendar.DAY_OF_YEAR, 1)
        maxDate.add(Calendar.SECOND, -1)

        return BOCartItem.createCriteria().list  {
            bOCart {
                eq("id", cart.id)
            }
            and {
                ge("startDate", minDate)
                le("startDate", maxDate)
            }
            order("startDate", "desc")
        }
    }
}
