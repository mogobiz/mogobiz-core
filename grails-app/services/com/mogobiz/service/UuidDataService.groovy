package com.mogobiz.service


import com.mogobiz.store.domain.UuidData
import static com.mogobiz.constant.IperConstant.*
import com.mogobiz.store.vo.CartVO
import com.fasterxml.jackson.databind.ObjectMapper

class UuidDataService {
	static transactional = true
	
	TrackingService trackingService
	public recycle() {
		Calendar now = Calendar.getInstance();
		UuidData.where { expireDate < now }.deleteAll();
	}
	
	public String getUuidData() {
		return trackingService.getTrackingUuid();
	}
	
	public CartVO getCart() {
		UuidData data = findByXType(QUEUE_XTYPE_CART);
		if (data != null) {
			ObjectMapper mapper = new ObjectMapper()
			return mapper.readValue(data.payload, CartVO.class)
		}
		return null;
	}
	
	public void setCart(CartVO cart) {
		ObjectMapper mapper = new ObjectMapper()
		String payload = mapper.writeValueAsString(cart)
		notifyEvent(QUEUE_ACTION_SET, QUEUE_XTYPE_CART, payload)		
	}
	
	public void removeCart() {
		notifyEvent(QUEUE_ACTION_DELETE, QUEUE_XTYPE_CART, null)
	}
	
	public String[] getProducts() {
		UuidData data = findByXType(QUEUE_XTYPE_PRODUCT);
		if (data != null) {
			return data.payload.split(',');
		}
		return new String[0];
	}

	public void addProduct(long productId) {
		notifyEvent(QUEUE_ACTION_ADD, QUEUE_XTYPE_PRODUCT, String.valueOf(productId))		
	}
	
	public String getCountry() {
		UuidData data = findByXType(QUEUE_XTYPE_COUNTRY);
		if (data != null) {
			return data.payload
		}
		return null;
	}
	
	public void setCountry(String country) {
		notifyEvent(QUEUE_ACTION_SET, QUEUE_XTYPE_COUNTRY, String.valueOf(country))		
	}
	
	private UuidData findByXType(String xtype) {
		String uuid = getUuidData();
		if (uuid != null) {
			return UuidData.findByUuidAndXtype(uuid, xtype);
		}	
		else {
			return null;
		}	
	}
	private void notifyEvent(String action, String xtype, String payload) {
		String uuid = getUuidData();
		if (uuid != null) {
			Map<String,String> map = [:]
			map.put('action', action)
			map.put('uuid', uuid)
			map.put("payload", payload)
			map.put('xtype', xtype)
            event(namespace:QUEUE_NS, topic:QUEUE_UUID, data:map)
		}
	}

}
