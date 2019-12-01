package asynclogger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class RingBuffer {
	private static final int NOT_COMMITED = -1;
	private static final int COMMITED = 0;
	
	private final Entry[] buffer;
	private final AtomicLong interationIndex;
	public final Map<Integer, AtomicInteger> commitedIndexToVotesMap;

	private final ExecutorService executor;
	private final List<Consumer> consumers;
	
	private final Object lock = new Object();
	
	private volatile boolean isCloseToOverflow;
	private final OverflowStrategy overflowStrategy;

	public RingBuffer(int bufferSize) {
		if(!isNumberPowerOfTwo(bufferSize)) {
			throw new RuntimeException("The Size for the RingBuffer MUST be a number Power Of Two. Provided number is not");
		}
		this.overflowStrategy = new OverflowStrategy() {
			@Override
			public void apply(RingBuffer queue) {
				System.out.println(System.nanoTime()+"~~~~~~~~~~~~~~~~~~~~~~~~~~~~~   applyOverflowStrategy(): SLEEPING " + Thread.currentThread().getName());
				try { Thread.sleep(500); } catch (InterruptedException e) { e.printStackTrace(); }
			}
		};
		
		this.buffer = new Entry[bufferSize];
		this.executor = Executors.newCachedThreadPool();
		this.interationIndex = new AtomicLong(0);
		this.commitedIndexToVotesMap = new HashMap<>();
		this.consumers = new ArrayList<>();

		for (int i = 0; i < bufferSize; i++) {
			buffer[i] = new Entry();
			commitedIndexToVotesMap.put(i, new AtomicInteger(NOT_COMMITED));
		}
	}
	
	private final boolean isNumberPowerOfTwo(int number) {
		return number != 0 && ((number & (number - 1)) == 0);
	}

	public int toRingIndex(long linearIndex) {
		return (int) linearIndex & (this.buffer.length - 1);
	}
	
	public void addConsumer(Consumer consumer) {
		consumer.setLock(lock);
		this.consumers.add(consumer);
	}

	public void logData(int id, String name) {
		if( isCloseToOverflow ) {
			applyOverflowStrategy();
		}
		final int index = getNextEmptySlotIndex();
		
		final Entry entry = getEntry(index);
		entry.setId(id);
		entry.setName(name + " index commit: " + index);
		
		commit(index);
		
		System.out.println(System.nanoTime()+"<<<<< PRODUCER "+entry);
	}

	private int getNextEmptySlotIndex() {
		final int nextSlotIndex = toRingIndex( this.interationIndex.getAndIncrement() );
		commitedIndexToVotesMap.get(nextSlotIndex).set(NOT_COMMITED);
		return nextSlotIndex;
	}

	private void commit(int index) {
		commitedIndexToVotesMap.get(index).set(COMMITED);
		synchronized(lock) {
			lock.notifyAll();
		}
	}

	public Entry getEntry(final long index) {
		return this.buffer[toRingIndex(index)];
	}

	public void start() {
		for (Consumer consumer : this.consumers) {
			this.executor.submit(consumer);
		}
	}
	
	private void applyOverflowStrategy() {
		overflowStrategy.apply(this);
		setCloseToOverflow(false);
	}

	public void setCloseToOverflow(boolean isCloseToOverflow) {
		this.isCloseToOverflow = isCloseToOverflow;
	}

	public long getIterationIndex() {
		return this.interationIndex.get();
	}

	public int getCurrentRingIndex() {
		return toRingIndex(this.interationIndex.get());
	}

	public int getBufferSize() {
		return this.buffer.length;
	}

	public void stop() {
		this.executor.shutdownNow();
	}

	public boolean isIndexCommited(long linearIndex) {
		return commitedIndexToVotesMap.get(toRingIndex(linearIndex)).get() != NOT_COMMITED;
	}

	public void clearCommitedIndex(final long consumerIncrementIndex) {
		synchronized (lock) {
			final int votesToClear = this.commitedIndexToVotesMap.get(toRingIndex(consumerIncrementIndex)).incrementAndGet();
			if (votesToClear >= consumers.size()) {
				this.commitedIndexToVotesMap.get(toRingIndex(consumerIncrementIndex)).set(NOT_COMMITED);
				
				System.out.println(System.nanoTime() + "  Index " + toRingIndex(consumerIncrementIndex) + 
						"["+consumerIncrementIndex+"] CLEARED by " + Thread.currentThread().getName());
			}
		}
	}
}
