package com.legeyda.mts.store;

import com.github.jedis.lock.JedisLock;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.legeyda.mts.model.Task;
import com.legeyda.mts.model.TaskImpl;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@Component
@Profile("default")
public class RedisStore<K> implements Store<K, Task> {

	private JedisPool jedisPool = new JedisPool(new JedisPoolConfig(), "localhost");

	@Override
	public Optional<Task> read(K id) {
		try(final Jedis jedis = jedisPool.getResource()) {
			return parseTask(jedis.get(this.getKey(id)));
		}
	}

	private String getKey(K id) {
		return "mts-task-app_" + id.toString();
	}

	@Override
	public void write(K id, Function<Optional<Task>, Optional<? extends Task>> func) {
		try (final Jedis jedis = jedisPool.getResource()) {
			final JedisLock lock = new JedisLock(jedis, this.getKey(id) + "_lock");
			try {
				lock.acquire();
				final Optional<Task> oldValue = this.read(id);
				final Optional<? extends Task> newValue = func.apply(oldValue);
				if (newValue.isPresent()) {
					if (!oldValue.isPresent() || !oldValue.get().equals(newValue.get())) {
						jedis.set(this.getKey(id), dumpTask(newValue.get()));
					}
				} else {
					jedis.del(getKey(id));
				}
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			} finally {
				lock.release();
			}
		}
	}

	private String dumpTask(final Task value) {
		return String.format(
				"%s,%d",
				value.getStatus(),
				value.getTimestamp().getEpochSecond()*1000 + value.getTimestamp().getNano()/1000000);
	}

	private Optional<Task> parseTask(final String dump) {
		if(dump!=null && !dump.isEmpty()) {
			final List<String> parts = Lists.newArrayList(Splitter.on(",").limit(2).split(dump));
			if (parts.size() == 2) {
				return Optional.of(new TaskImpl(
						Task.Status.valueOf(parts.get(0)),
						Instant.ofEpochMilli(Long.parseLong(parts.get(1)))));
			}
		}
		return Optional.empty();
	}

}
