package com.mogobiz.facebook

import com.restfb.Facebook

/**
 * @author Hayssam Saleh
 *
 */
public class LinkCounter {
	@Facebook(value="url")
	String url;

	@Facebook(value="like_count")
	int likeCount;

	@Facebook(value="share_count")
	int shareCount;

	@Facebook(value="comment_count")
	int comment_count;

	@Facebook(value="total_count")
	int total_count;

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public int getLikeCount() {
		return likeCount;
	}

	public void setLikeCount(int likeCount) {
		this.likeCount = likeCount;
	}

	public int getShareCount() {
		return shareCount;
	}

	public void setShareCount(int shareCount) {
		this.shareCount = shareCount;
	}

	public int getComment_count() {
		return comment_count;
	}

	public void setComment_count(int comment_count) {
		this.comment_count = comment_count;
	}

	public int getTotal_count() {
		return total_count;
	}

	public void setTotal_count(int total_count) {
		this.total_count = total_count;
	}

}
