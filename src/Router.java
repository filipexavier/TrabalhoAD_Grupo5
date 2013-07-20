
public class Router {

	private Integer rate;
	private BottleNeck bottleNeckPolicy;
	private Integer bufferSize;
	//private List<> queue;

	public Router() {
	}

	public Router(int rate) {
		this.rate = rate;
	}

	
	public Integer getRate() {
		return rate;
	}
	
	public void setRate(Integer rate) {
		this.rate = rate;
	}

	public BottleNeck getBottleNeckPolicy() {
		return bottleNeckPolicy;
	}

	public void setBottleNeckPolicy(BottleNeck bottleNeckPolicy) {
		this.bottleNeckPolicy = bottleNeckPolicy;
	}

	public Integer getBufferSize() {
		return bufferSize;
	}

	public void setBufferSize(Integer bufferSize) {
		this.bufferSize = bufferSize;
	}
}
