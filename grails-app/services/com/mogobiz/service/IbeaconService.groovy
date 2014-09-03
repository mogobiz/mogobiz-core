package com.mogobiz.service
import com.mogobiz.store.cmd.IBeaconCommand
import com.mogobiz.store.cmd.PagedListCommand
import com.mogobiz.store.domain.*
import grails.orm.PagedResultList

class IbeaconService {
    static transactional = true

    PagedResultList list(Seller seller, PagedListCommand cmd) {
        if (seller?.company == null || cmd == null) {
            throw new IllegalArgumentException()
        }

        return Ibeacon.createCriteria().list(cmd.getPagination()) {
            company {
                eq "id", seller.company.id
            }
            order("name", "asc")
        }
    }

    Ibeacon save(Seller seller, IBeaconCommand cmd) {
        if (seller?.company == null || cmd == null || !cmd.validate()) {
            throw new IllegalArgumentException()
        }

        Ibeacon beaconInBase = Ibeacon.findByUuid(cmd.uuid)
        if (beaconInBase != null && (cmd.id == null || cmd.id != beaconInBase.id)) {
            beaconInBase.errors.rejectValue("uuid", "already.exist")
            return beaconInBase
        }

        if (cmd.id == null) {
            beaconInBase = new Ibeacon()
        }
        else {
            beaconInBase = Ibeacon.get(cmd.id)

            if (beaconInBase == null) {
                beaconInBase = new Ibeacon()
                beaconInBase.errors.rejectValue("id", "unknown")
                return beaconInBase
            }

            if (beaconInBase.company.id != seller.company.id) {
                beaconInBase.errors.rejectValue("id", "unknown")
                return beaconInBase
            }
        }

        beaconInBase.properties = cmd.properties
        beaconInBase.company = seller.company

        if (beaconInBase.validate()) {
            beaconInBase.save()
        }
        return beaconInBase
    }

    Ibeacon delete(Seller seller, Long id) {
        if (seller?.company == null || id == null) {
            throw new IllegalArgumentException()
        }

        Ibeacon beaconInBase = Ibeacon.get(id)
        if (beaconInBase == null) {
            beaconInBase = new Ibeacon()
            beaconInBase.errors.rejectValue("id", "unknown")
            return beaconInBase
        }

        if (beaconInBase.company.id != seller.company.id) {
            beaconInBase.errors.rejectValue("id", "unknown")
            return beaconInBase
        }

        if (Tag.findAllByIbeacon(beaconInBase)?.size() > 0
                || Brand.findAllByIbeacon(beaconInBase)?.size() > 0
                || Category.findAllByIbeacon(beaconInBase)?.size() > 0
                || Product.findAllByIbeacon(beaconInBase)?.size() > 0) {
            beaconInBase = new Ibeacon()
            beaconInBase.errors.rejectValue("id", "is.used")
            return beaconInBase
        }
        
        beaconInBase.delete()

        return beaconInBase
    }
}
