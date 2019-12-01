package asynclogger;

public class Demo {

    public static void main(String[] args) {
    	
    	final Logger log = AsyncLogger.getInstance()
    			                      .createConsumer(Consumer.CONSOLE_CONSUMER)
    			                      .createConsumer(Consumer.CONSOLE_CONSUMER)
    			                      .createConsumer(Consumer.CONSOLE_CONSUMER)
    			                      .createConsumer(Consumer.CONSOLE_CONSUMER)
    			                      .createConsumer(Consumer.CONSOLE_CONSUMER)
    			                      .createConsumer(Consumer.CONSOLE_CONSUMER)
    			                      .createConsumer(Consumer.CONSOLE_CONSUMER)
    			                      .createConsumer(Consumer.CONSOLE_CONSUMER)
    			                      .createConsumer(Consumer.CONSOLE_CONSUMER)
    			                      .activate();

    	
        Thread prod = new Thread(new Runnable() {
            @Override
            public void run() {
                for (int q = 0; q < 1700; q++) {
                	log.logData(Thread.currentThread().getName());
                }
            }
        });
        Thread prod2 = new Thread(new Runnable() {
        	@Override
        	public void run() {
        		for (int q = 0; q < 1700; q++) {
        			log.logData(Thread.currentThread().getName());
        		}
        	}
        });
        Thread prod3 = new Thread(new Runnable() {
        	@Override
        	public void run() {
        		for (int q = 0; q < 1700; q++) {
        			log.logData(Thread.currentThread().getName());
        		}
        	}
        });
        Thread prod4 = new Thread(new Runnable() {
        	@Override
        	public void run() {
        		for (int q = 0; q < 1701; q++) {
        			log.logData(Thread.currentThread().getName());
        		}
        	}
        });
        Thread prod5 = new Thread(new Runnable() {
        	@Override
        	public void run() {
        		for (int q = 0; q < 1701; q++) {
        			log.logData(Thread.currentThread().getName());
        		}
        	}
        });
        Thread prod6 = new Thread(new Runnable() {
        	@Override
        	public void run() {
        		for (int q = 0; q < 1701; q++) {
        			log.logData(Thread.currentThread().getName());
        		}
        	}
        });
        Thread prod7 = new Thread(new Runnable() {
        	@Override
        	public void run() {
        		for (int q = 0; q < 1701; q++) {
        			log.logData(Thread.currentThread().getName());
        		}
        	}
        });
        Thread prod8 = new Thread(new Runnable() {
        	@Override
        	public void run() {
        		for (int q = 0; q < 1701; q++) {
        			log.logData(Thread.currentThread().getName());
        		}
        	}
        });

        prod.start();
        prod2.start();
        prod3.start();
        prod4.start();
        prod5.start();
        prod6.start();
        prod7.start();
        prod8.start();

        try {
            prod.join();
            prod2.join();
            prod3.join();
            prod4.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        try {
        	Thread.sleep(1500);
        } catch (InterruptedException e) {
        	e.printStackTrace();
        }

    }
}
