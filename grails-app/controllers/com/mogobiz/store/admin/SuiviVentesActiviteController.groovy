package com.mogobiz.store.admin

import com.mogobiz.store.domain.BOCartItem;
import com.mogobiz.store.domain.Seller;
import com.mogobiz.utils.DateUtilitaire
import grails.transaction.Transactional;

class SuiviVentesActiviteController {

	def authenticationService

	@Transactional(readOnly = true)
    def index() {
		response.contentType = "text/csv";
				
		def Seller seller = authenticationService.retrieveAuthenticatedSeller()
		def liste = [];
		if (seller != null && seller.company != null)
		{
			String requete = "SELECT s.code, s.price, s.startDate, s.endDate, ";
			requete += "p.code, p.name, "
			requete += "t.date, t.status "
			requete += "FROM BOCartItem as s INNER JOIN s.bOCart as t INNER JOIN s.bOProducts as bop INNER JOIN bop.product as p INNER JOIN p.company as c ";
			requete += "WHERE bop.principal = true and c.id = :idCompany";
			liste = BOCartItem.executeQuery(requete, [idCompany: seller.company.id])
		}
		
		String r = "Code;Price;Start Date;End Date;Pack Code;Pack Name;Date;Payment Status\n";
		if (liste != null)
		{
			liste.each {row ->
				row.eachWithIndex {v, index -> 
					if (index > 0)
					{
						r += ";";
					}
					if (v != null)
					{
						if (index == 2 || index == 3 || index == 6)
						{
							r += DateUtilitaire.format(v, "dd/MM/yyyy HH:mm");
						}
						else
						{
							r += v.toString().replaceAll(";", " ");							
						}
					}
				}
				r += "\n";
			}
		}
		render r as String;
	}
}
