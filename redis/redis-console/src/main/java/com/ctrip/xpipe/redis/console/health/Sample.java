package com.ctrip.xpipe.redis.console.health;

import java.util.concurrent.atomic.AtomicInteger;

import com.ctrip.xpipe.metric.HostPort;

/**
 * @author marsqing
 *
 *         Dec 6, 2016 1:59:22 PM
 */
@SuppressWarnings("rawtypes")
public class Sample<T extends BaseInstanceResult> {

	private long startTime;
	private long expireTime;
	private BaseSamplePlan<T> samplePlan;
	private long startNanoTime;
	private AtomicInteger remainingRedisCount;

	public Sample(long startTime, long startNanoTime, BaseSamplePlan<T> samplePlan, int expireDelayMillis) {
		this.startTime = startTime;
		this.samplePlan = samplePlan;
		expireTime = startTime + expireDelayMillis;
		this.startNanoTime = startNanoTime;
		remainingRedisCount = new AtomicInteger(samplePlan.getHostPort2SampleResult().size());
	}

	@SuppressWarnings("unchecked")
	public <C> void addInstanceResult(String host, int port, C context) {
		BaseInstanceResult<C> instanceResult = samplePlan.findInstanceResult(new HostPort(host, port));

		if (instanceResult != null && !instanceResult.isDone()) {
			instanceResult.done(System.nanoTime(), context);
			remainingRedisCount.decrementAndGet();
		}
	}

	public long getStartNanoTime() {
		return startNanoTime;
	}

	public long getStartTime() {
		return startTime;
	}

	public BaseSamplePlan<T> getSamplePlan() {
		return samplePlan;
	}

	public boolean isExpired() {
		return System.currentTimeMillis() > expireTime;
	}

	public boolean isDone() {
		return remainingRedisCount.get() == 0;
	}

}
