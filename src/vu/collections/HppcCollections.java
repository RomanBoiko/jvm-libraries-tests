package vu.collections;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import com.carrotsearch.hppc.IntCharOpenHashMap;
import com.carrotsearch.hppc.IntOpenHashSet;
import com.carrotsearch.hppc.procedures.IntCharProcedure;

public class HppcCollections {

	@Test public void mapImplementations() {
		IntCharOpenHashMap intCharMap = new IntCharOpenHashMap(10);
		intCharMap.put(1, 'a');

		assertThat(intCharMap.get(1), is('a'));

		checkOutForeachOperator(intCharMap);
	}

	@Test public void setImplementations() {
		IntOpenHashSet intSet = new IntOpenHashSet();
		intSet.add(1);
		intSet.add(2);

		assertThat(intSet.size(), is(2));
		assertThat(intSet.contains(1), is(true));
		assertThat(intSet.contains(3), is(false));
	}

	private void checkOutForeachOperator(IntCharOpenHashMap intCharMap) {
		final AtomicInteger entriesCount = new AtomicInteger(0);
		intCharMap.forEach(new IntCharProcedure() {
			@Override public void apply(int key, char value) {
				assertThat(key, is(1));
				assertThat(value, is('a'));
				entriesCount.incrementAndGet();
			}
		});
		assertThat(entriesCount.get(), is(1));
	}

}
