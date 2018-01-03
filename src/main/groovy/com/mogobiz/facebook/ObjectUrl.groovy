package com.mogobiz.facebook

import com.restfb.Facebook

/**
 * @author Hedi Abidi
 *
 */
public class ObjectUrl {
	@Facebook(value="url")
	String url;

	@Facebook(value="id")
	String id;

	@Facebook(value="type")
	String type;

	@Facebook(value="site")
	String site;

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getSite() {
		return site;
	}

	public void setSite(String site) {
		this.site = site;
	}
}
