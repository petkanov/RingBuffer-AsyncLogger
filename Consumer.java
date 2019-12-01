package asynclogger;

public abstract class Consumer implements Runnable {
	public static final String CONSOLE_CONSUMER = "console.consumer";
	
	private static final long MIN_PROD_CONS_DISTANCE = 325;
	private static final int ENTRY_RETRY_NUMBER = 5;
	private long consumerIncrementIndex = 0;
	private final RingBuffer ringBuffer;
	private Object lock;
	private int retry;
	
	public Consumer(RingBuffer ringBuffer) {
		this.ringBuffer = ringBuffer;
		this.retry = ENTRY_RETRY_NUMBER;
	}
	
	public abstract void consumeData(Entry entry);
	
	@Override
	public void run() {
		while (true) {
			if (isConsumptionSlowerThenProduction()) {
				ringBuffer.setCloseToOverflow(true);
			}
			synchronized (lock) {
				if (!ringBuffer.isIndexCommited(consumerIncrementIndex) && retry-- > 0) {
					try {
						              System.out.println(System.nanoTime() + " waiting for index "+ Thread.currentThread().getName() + " votes: " + ringBuffer.commitedIndexToVotesMap.get(ringBuffer.toRingIndex(consumerIncrementIndex)).get());
						lock.wait();
					} catch (InterruptedException e) {
						break;
					}
					continue;
				}
			}
			final Entry entry = ringBuffer.getEntry(consumerIncrementIndex);
			
			consumeData(entry);
			
			ringBuffer.clearCommitedIndex(consumerIncrementIndex);
			nextIteration();
		}
	}
	
	private void nextIteration() {
		consumerIncrementIndex++;
		retry = ENTRY_RETRY_NUMBER;
	}

	private boolean isConsumptionSlowerThenProduction() {
		final int consumerProducerDistance = getConsumerProducerDistance();

		final boolean isProducerBehindConsumer = consumerProducerDistance < 0;
		final boolean isDistanceNotAllowed = Math.abs(consumerProducerDistance) <= MIN_PROD_CONS_DISTANCE;

		return isDistanceNotAllowed && isProducerBehindConsumer;
	}

	private int getConsumerProducerDistance() {
		final int ringBufferSize = ringBuffer.getBufferSize();
		final int bufferMiddleIndex = ringBufferSize / 2;

		final int consumerRingIndex = ringBuffer.toRingIndex(consumerIncrementIndex);
		final int producersRingIndex = ringBuffer.getCurrentRingIndex();

		final int consumerDistanceFromZeroIndex = consumerRingIndex <= bufferMiddleIndex ? consumerRingIndex
				: consumerRingIndex - ringBufferSize;

		final int producerDistanceFromZeroIndex = producersRingIndex <= bufferMiddleIndex ? producersRingIndex
				: producersRingIndex - ringBufferSize;

		final int consumerProducerDistance = Math.abs(consumerDistanceFromZeroIndex - producerDistanceFromZeroIndex);

		final int predictionIndex = ringBuffer.toRingIndex(producersRingIndex + consumerProducerDistance);
		final int distanceSide = predictionIndex == consumerRingIndex ? -1 : 1;

		return consumerProducerDistance * distanceSide;
	}

	public void setLock(Object lock) {
		this.lock = lock;
	}
}
