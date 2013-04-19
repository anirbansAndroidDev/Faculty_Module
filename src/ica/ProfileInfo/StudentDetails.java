package ica.ProfileInfo;

import ica.ICAConstants.StudentAnsStatus;
import android.graphics.Bitmap;

public class StudentDetails {

	String StudentCode;
	public String getStudentCode() {
		return StudentCode;
	}

	public void setStudentCode(String studentCode) {
		StudentCode = studentCode;
	}

	String Name;
	String ImgPath;
	Bitmap StudentImage;

	StudentAnsStatus AnsStatus;

	String BatchID;
	String SessionID;
	String ChapterID;
	
	public String getChapterID() {
		return ChapterID;
	}

	public void setChapterID(String chapterID) {
		ChapterID = chapterID;
	}

	String QuestionID;



	public String getBatchID() {
		return BatchID;
	}

	public void setBatchID(String batchID) {
		BatchID = batchID;
	}

	public String getSessionID() {
		return SessionID;
	}

	public void setSessionID(String sessionID) {
		SessionID = sessionID;
	}

	public String getQuestionID() {
		return QuestionID;
	}

	public void setQuestionID(String questionID) {
		QuestionID = questionID;
	}

	public StudentAnsStatus getAnsStatus() {
		return AnsStatus;
	}

	public void setAnsStatus(StudentAnsStatus ansStatus) {
		AnsStatus = ansStatus;
	}
	
	public String getName() {
		return Name;
	}

	public void setName(String name) {
		Name = name;
	}

	public String getImgPath() {
		return ImgPath;
	}

	public void setImgPath(String imgPath) {
		ImgPath = imgPath;
	}

	public Bitmap getStudentImage() {
		return StudentImage;
	}

	public void setStudentImage(Bitmap studentImage) {
		StudentImage = studentImage;
	}

}
