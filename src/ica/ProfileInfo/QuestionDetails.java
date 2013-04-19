package ica.ProfileInfo;

public class QuestionDetails {

	private String ID;
	private String Text;
	private String SessionNo;
	private String ChapterID;
	private String BatchId;
	private boolean IsChecked;
	private int RightPercentage;
	private int WrongPercentage;
	
	public String getID() {
		return ID;
	}

	public void setID(String iD) {
		ID = iD;
	}

	public String getText() {
		return Text;
	}

	public void setText(String text) {
		Text = text;
	}

	public String getSessionNo() {
		return SessionNo;
	}

	public void setSessionNo(String sessionNo) {
		SessionNo = sessionNo;
	}

	public String getChapterID() {
		return ChapterID;
	}

	public void setChapterID(String chapterID) {
		ChapterID = chapterID;
	}

	public String getBatchId() {
		return BatchId;
	}

	public void setBatchId(String batchId) {
		BatchId = batchId;
	}

	public boolean isChecked() {
		return IsChecked;
	}

	public void setIsChecked(boolean isChecked) {
		IsChecked = isChecked;
	}

	public int getRightPercentage() {
		return RightPercentage;
	}

	public void setRightPercentage(int rightPercentage) {
		RightPercentage = rightPercentage;
	}

	public int getWrongPercentage() {
		return WrongPercentage;
	}

	public void setWrongPercentage(int wrongPercentage) {
		WrongPercentage = wrongPercentage;
	}



}
