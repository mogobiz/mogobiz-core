package com.mogobiz.store.partner

import grails.converters.JSON
import grails.util.Holders
import groovyx.net.http.HTTPBuilder

import com.mogobiz.service.RateService


class RateController {
	RateService rateService

    def rates() {
		Map<String, Double> res = rateService.rates() 
		withFormat {
			json {render res as JSON }
		}
	}
    def currencies() {
		Map<String, Double> res = rateService.currencies() 
		withFormat {
			json {render res as JSON }
		}
	}
    def format(long amount, String currency, String country) { 
		def http = new HTTPBuilder(Holders.config.mogopay.url)
		def data = http.get( path : 'rate/format', query : [amount:amount, currency:currency, country:country])
		withFormat {
			json {render data.toString() }
		}
	}
}
