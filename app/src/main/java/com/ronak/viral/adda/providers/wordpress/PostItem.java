package com.ronak.viral.adda.providers.wordpress;

import com.ronak.viral.adda.util.SerializableJSONArray;

import org.json.JSONArray;

import java.io.Serializable;
import java.util.Date;

public class PostItem implements Serializable {

	private static final long serialVersionUID = 1L;
	public enum PostType{JETPACK, JSON}

    private boolean isCompleted;
	
	private String title;
	private Date date;
	private String attachmentUrl;
	private String thumbnailUrl;
	private Long id;
	private String content;
	private String url;
	private String author;
	private String tag;
    private Long commentCount;
	private PostType type;
    private SerializableJSONArray commentsArray;

    public PostItem(PostType type){
        this.isCompleted = false;
		this.type = type;
    }

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
	
	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}
	
	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public String getAttachmentUrl() {
		return attachmentUrl;
	}

	public void setAttachmentUrl(String attachmentUrl) {
		this.attachmentUrl = attachmentUrl;
	}
	
	public String getThumbnailUrl() {
		return thumbnailUrl;
	}

	public void setThumbnailUrl(String thumbnailUrl) {
		this.thumbnailUrl = thumbnailUrl;
	}

    public JSONArray getCommentsArray() {
		if (commentsArray != null)
        	return commentsArray.getJSONArray();
		else
			return null;
    }

    public void setCommentsArray(JSONArray commentsArray) {
        this.commentsArray = new SerializableJSONArray(commentsArray);
    }

    public Long getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(Long commentCount) {
        this.commentCount = commentCount;
    }

    public void setPostCompleted(){
        isCompleted = true;
    }

    public boolean isCompleted(){
        return isCompleted;
    }

    public PostType getPostType(){
        return type;
    }
}

