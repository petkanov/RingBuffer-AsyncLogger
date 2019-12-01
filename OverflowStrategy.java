package asynclogger;

public interface OverflowStrategy {
	
	void apply(RingBuffer queue);
	
}
