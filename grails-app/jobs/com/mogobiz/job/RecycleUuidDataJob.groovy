package com.mogobiz.job

import com.mogobiz.service.UuidDataService;

import grails.util.Holders;

class RecycleUuidDataJob {

	UuidDataService uuidDataService
	
	static triggers = {
		cron name:'RecycleUuidDataJobTrigger', startDelay:0, cronExpression: Holders.config.uuidData.recycle.cron
	}

	def execute() {
		log.info("Recycle UuidData is started...")
		uuidDataService.recycle()
		log.info("Recycle UuidData is finished.")
	}

}
