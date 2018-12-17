package com.me.external.sort;

import java.util.Arrays;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

/**
 * forkjoin框架实现多线程排序
 * @author 清明
 *
 */
public class MutilThreadSort {

	public static void sort(int[] list) {
		ForkJoinPool forkJoinPool = new ForkJoinPool();
		forkJoinPool.invoke(new SortTask(list));
	}

	static class SortTask extends RecursiveAction {

		private static final long serialVersionUID = 1L;
		int[] list;
		int max = 1000;

		public SortTask(int[] list) {
			this.list = list;
		}

		@Override
		protected void compute() {
			if (list.length <= max) {
				quickSort(list, 0, list.length - 1);
			} else {
				int mid = list.length / 2;
				int[] first = Arrays.copyOfRange(list, 0, mid);
				int[] second = Arrays.copyOfRange(list, mid, list.length);

				invokeAll(new SortTask(first), new SortTask(second));

				merge(list, first, second);
			}
		}

	}

	// 快速排序
	public static void quickSort(int[] list, int start, int end) {
		if (start >= end)
			return;

		int k = list[start], i = start, j = end;

		while (i < j) {
			while (i < j && list[j] > k) {
				j--;
			}
			if(i<j) {
				list[i] = list[j];
				i++;
			}

			while (i < j && list[i] < k) {
				i++;
			}

			if(i<j) {
				list[j] = list[i];
				j--;
			}
		}
		list[i] = k;
		quickSort(list, start, i-1);
		quickSort(list, i + 1, end);
	}

	// 合并
	public static void merge(int[] list, int[] first, int[] second) {
		int i = 0, j = 0, k = 0;
		while (i < first.length && j < second.length) {
			if (compare(first[i], second[j])) {
				list[k] = first[i];
				i++;
			} else {
				list[k] = second[j];
				j++;
			}
			k++;
		}

		while (i < first.length) {
			list[k++] = first[i++];
		}

		while (j < first.length) {
			list[k++] = first[j++];
		}
		
	}

	// 比大小
	public static boolean compare(int a, int b) {
		return a < b;
	}
}
