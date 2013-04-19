package ica.ProfileInfo;

import ica.ICAConstants.UploadTask;


public class TaskStatusMsg 
{
	UploadTask taskDone;
	String Message;
	String title;
	
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	int Status;
	
	public UploadTask getTaskDone() {
		return taskDone;
	}
	public void setTaskDone(UploadTask taskDone) {
		this.taskDone = taskDone;
	}
	public String getMessage() {
		return Message;
	}
	public void setMessage(String message) {
		Message = message;
	}
	public int getStatus() {
		return Status;
	}
	public void setStatus(int status) {
		Status = status;
	}
}
