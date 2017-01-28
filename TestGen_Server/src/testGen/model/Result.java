package testGen.model;

import java.io.Serializable;
import java.util.ArrayList;

public class Result implements Serializable {

	private static final long serialVersionUID = 8986508673311030342L;
	
	private String testName;
	private int testId;
	
	private int numOfPoints, outOf;
	
	private ArrayList<VerifiedQuestionDescription> partialResultDescriptions = new ArrayList<VerifiedQuestionDescription>();

	
	public Result(String testName, int testId, int outOf) {
		this.testName = testName;
		this.testId = testId;
		this.numOfPoints = 0;
		this.outOf = outOf;
	}
	
	public int getNumOfPoints() {
		return numOfPoints;
	}

	public int getOutOf() {
		return outOf;
	}
	
	public void addOnePoint() {
		numOfPoints++;
	}
	
	public String getTestName() {
		return testName;
	}

	public void setTestName(String testName) {
		this.testName = testName;
	}

	public int getTestId() {
		return testId;
	}

	public void setTestId(int testId) {
		this.testId = testId;
	}

	public ArrayList<VerifiedQuestionDescription> getPartialResultDescriptions() {
		return partialResultDescriptions;
	}
	
	public void addPartialResultDescription(VerifiedQuestionDescription desc) {
		partialResultDescriptions.add(desc);
	}

}
