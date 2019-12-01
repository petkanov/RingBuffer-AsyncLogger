package asynclogger;

import java.util.Random;

public class AsyncLogger implements Logger{

	private final RingBuffer queue;
	
	private AsyncLogger() {
		this.queue = new RingBuffer(2048);
	}
	
    private static class Instance {
        private static final AsyncLogger INSTANCE = new AsyncLogger();
    }
 
    public static AsyncLogger getInstance() {
        return Instance.INSTANCE;
    }
    
	@Override
	public void logData(String msg) {
		queue.logData(new Random().nextInt(100), msg);
	}
	
	public AsyncLogger createConsumer(String type) {
		switch (type) {
		case Consumer.CONSOLE_CONSUMER:
			final Consumer consumer = new ConsoleConsumer(queue);
			queue.addConsumer(consumer);
			break;

		default:
			break;
		}
		return this;
	}
	
	public AsyncLogger activate() {
		this.queue.start();
		return this;
	}
}
