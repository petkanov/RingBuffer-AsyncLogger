package asynclogger;

public class ConsoleConsumer extends Consumer {

	public ConsoleConsumer(RingBuffer ringBuffer) {
		super(ringBuffer);
	}
	
	public void consumeData(Entry entry) {
		System.out.println(System.nanoTime() + "##### CONSUMER "+this.ringBuffer.consumers + entry + " " +ringBuffer.getIterationIndex() +" "+Thread.currentThread().getName());
	}
}
