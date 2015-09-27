package com.mogobiz.service

import com.mogobiz.store.domain.Company
import com.mogobiz.store.domain.LocalTaxRate
import com.mogobiz.store.domain.Product
import com.mogobiz.store.domain.TaxRate
import org.apache.commons.lang.StringUtils

class TaxRateService {

    TaxRate findTaxRateById(long taxRateId) {
        return TaxRate.createCriteria().get { eq("id", taxRateId) }
    }

    TaxRate findTaxRateOfProduct(Product product) {
        return product.taxRate
    }

    Float findTaxRateByProduct(Product product, String country, String state = null) {
        TaxRate taxRate = product.taxRate
        return findTaxRate(taxRate, country, state)
    }

    Float findTaxRate(TaxRate taxRate, String country, String state) {
        if (taxRate != null) {
            StringBuffer query = new StringBuffer("SELECT DISTINCT localTaxRate FROM TaxRate taxRate RIGHT JOIN taxRate.localTaxRates AS localTaxRate WHERE taxRate.id = :taxRateId AND localTaxRate.active = true AND localTaxRate.countryCode = :country")
            def params = [taxRateId: taxRate.id, country: country]
            if (!StringUtils.isEmpty(state)) {
                query.append(" AND (localTaxRate.stateCode = :state)")
                params << [state: state]
            }
            else {
                query.append(" AND localTaxRate.stateCode is null")
            }
            List<LocalTaxRate> listLocalTaxRate = LocalTaxRate.executeQuery(query.toString(), params)

            if (listLocalTaxRate?.size() > 0) {
                return listLocalTaxRate.get(0).rate
            }
            else if (!StringUtils.isEmpty(state)) {
                return findTaxRate(taxRate, country, null);
            }
        }
        return null;
    }

    Long calculateEndPrix(long price, Float taxRate) {
        if (taxRate != null) {
            return price + (long) (price * taxRate / 100f)
        }
        return null;
    }

    List<Map> getAllActiveLocalTaxRateByProduct(Product product) {
        List<Map> result = [];

        TaxRate taxRate = product.taxRate
        if (taxRate != null) {
            String query = "SELECT DISTINCT localTaxRate FROM TaxRate taxRate RIGHT JOIN taxRate.localTaxRates AS localTaxRate WHERE taxRate.id = :taxRateId AND localTaxRate.active = true";
            List<LocalTaxRate> listLocalTaxRate = LocalTaxRate.executeQuery(query, [taxRateId: taxRate.id])
            listLocalTaxRate.each { LocalTaxRate localTaxRate ->
                result << [rate: localTaxRate.rate, countryCode: localTaxRate.countryCode]
            }
        }

        return result;
    }

    List<Map> getAllLocalTaxRateByProduct(long productId) {
        List<Map> result = [];

        TaxRate taxRate = Product.load(productId)?.taxRate
        if (taxRate != null) {
            String query = "SELECT DISTINCT localTaxRate FROM TaxRate taxRate RIGHT JOIN taxRate.localTaxRates AS localTaxRate WHERE taxRate.id = :taxRateId";
            List<LocalTaxRate> listLocalTaxRate = LocalTaxRate.executeQuery(query, [taxRateId: taxRate.id])
            listLocalTaxRate.each { LocalTaxRate localTaxRate ->
                result << [rate: localTaxRate.rate, countryCode: localTaxRate.countryCode, active: localTaxRate.active]
            }
        }

        return result;
    }

    /**
     * List all TaxRate for the given company
     * @param company
     * @return
     */
    List<TaxRate> listTaxRate(Company c) {
        return TaxRate.createCriteria().list {
            company { eq("id", c.id) }
        }
    }

    /**
     * Create a TaxRate for the given company
     * @param company
     * @param name
     * @return
     */
    TaxRate createTaxRate(Company company, String name) {
        TaxRate taxRate = new TaxRate();
        taxRate.company = company;
        taxRate.name = name;
        if (taxRate.validate()) {
            taxRate.save();
        }
        return taxRate;
    }

    /**
     * Update the given TaxRate for the given company
     * @param company
     * @param taxRateId
     * @param name
     * @return the modified TaxRate or null if the TaxRate doesn't exist
     */
    TaxRate updateTaxRate(Company company, long taxRateId, String name) {
        TaxRate taxRate = TaxRate.get(taxRateId);
        if (taxRate != null && taxRate.company.id == company.id) {
            taxRate.name = name;
            if (taxRate.validate()) {
                taxRate.save();
            }
        }
        return taxRate;
    }

    /**
     * Delete the given TaxRate for the given company
     * @param company
     * @param taxRateId
     * @return the modified TaxRate or null if the TaxRate doesn't exist
     */
    TaxRate deleteTaxRate(Company company, long taxRateId) {
        TaxRate taxRate = TaxRate.get(taxRateId);
        if (taxRate != null && taxRate.company.id == company.id) {
            Product p = Product.findByTaxRate(taxRate)
            if (p != null) {
                taxRate.errors.rejectValue(null, "not.empty");
            } else if (taxRate.localTaxRates != null && taxRate.localTaxRates.size() > 0) {
                taxRate.errors.rejectValue("localTaxRates", "not.empty");
            } else {
                taxRate.delete();
            }
        }
        return taxRate;
    }

    /**
     * List all LocalTaxRate of the given TaxRate for the given company
     * @param company
     * @param taxRateId
     * @return
     */
    List<LocalTaxRate> listLocalTaxRate(Company company, long taxRateId) {
        String query = "SELECT DISTINCT localTaxRate FROM TaxRate taxRate JOIN taxRate.company AS company RIGHT JOIN taxRate.localTaxRates AS localTaxRate WHERE company.id = :companyId AND taxRate.id = :taxRateId";
        return LocalTaxRate.executeQuery(query, [companyId: company.id, taxRateId: taxRateId])
    }

    /**
     * Create a LocalTaxRate for the given TaxRate for the given company
     * @param company
     * @param taxRateId
     * @param country
     * @param rate
     * @param active
     * @return the created LocalTaxRate or null if the given TaxRate doesn't not exist
     */
    LocalTaxRate createLocalTaxRate(Company company, long taxRateId, String country, String state, float rate, boolean active) {
        TaxRate taxRate = TaxRate.get(taxRateId);
        if (taxRate != null && taxRate.company.id == company.id) {
            LocalTaxRate localTaxRate = new LocalTaxRate();
            localTaxRate.countryCode = country;
            localTaxRate.stateCode = state;
            localTaxRate.rate = rate;
            localTaxRate.active = active;
            if (localTaxRate.validate()) {
                localTaxRate.save();
            }

            taxRate.addToLocalTaxRates(localTaxRate);
            taxRate.save();

            return localTaxRate;
        }
        return null;
    }

    /**
     * Update the given LocalTaxRate
     * @param localTaxRateId
     * @param rate
     * @param active
     * @return the modified LocalTaxRate or null if the given LocalTaxRate doesn't exist
     */
    LocalTaxRate updateLocalTaxRate(Company company, long localTaxRateId, String state, float rate, boolean active) {
        TaxRate taxRate = findTaxRateFromLocalTaxRate(localTaxRateId)
        LocalTaxRate localTaxRate = LocalTaxRate.get(localTaxRateId);
        if (localTaxRate != null && taxRate != null && taxRate.company.id == company.id) {
            localTaxRate.stateCode = state;
            localTaxRate.rate = rate;
            localTaxRate.active = active;
            if (localTaxRate.validate()) {
                localTaxRate.save();
            }
        }
        return localTaxRate;
    }

    /**
     * Delete the given LocalTaxRate
     * @param localTaxRateId
     * @return the deleted LocalTaxRate of null if the given LocalTaxRate doesn't exist
     */
    LocalTaxRate deleteLocalTaxRate(Company company, long localTaxRateId) {
        TaxRate taxRate = findTaxRateFromLocalTaxRate(localTaxRateId)
        LocalTaxRate localTaxRate = LocalTaxRate.get(localTaxRateId);
        if (localTaxRate != null && taxRate != null && taxRate.company.id == company.id) {
            taxRate.removeFromLocalTaxRates(localTaxRate);
            taxRate.save();

            localTaxRate.delete();
        }
        return localTaxRate;
    }

    /**
     * Find the TaxRate of the given LocalTaxRate
     * @param localTaxRateId
     * @return
     */
    private TaxRate findTaxRateFromLocalTaxRate(long localTaxRateId) {
        return TaxRate.createCriteria().get() {
            localTaxRates { eq("id", localTaxRateId) }
        }
    }
} 
