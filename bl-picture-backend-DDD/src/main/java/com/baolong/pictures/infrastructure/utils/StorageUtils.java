package com.baolong.pictures.infrastructure.utils;

/**
 * 存储大小工具类
 *
 * @author Baolong 2025年03月09 14:35
 * @version 1.0
 * @since 1.8
 */
public class StorageUtils {
	// 定义常量
	private static final long BYTES_IN_KB = 1024;
	private static final long BYTES_IN_MB = BYTES_IN_KB * 1024;
	private static final long BYTES_IN_GB = BYTES_IN_MB * 1024;
	private static final long BYTES_IN_TB = BYTES_IN_GB * 1024;

	/**
	 * 将字节转换为千字节 (KB)
	 *
	 * @param bytes 字节数
	 * @return 千字节数
	 */
	public static double bytesToKB(long bytes) {
		return (double) bytes / BYTES_IN_KB;
	}

	/**
	 * 将字节转换为兆字节 (MB)
	 *
	 * @param bytes 字节数
	 * @return 兆字节数
	 */
	public static double bytesToMB(long bytes) {
		return (double) bytes / BYTES_IN_MB;
	}

	/**
	 * 将字节转换为吉字节 (GB)
	 *
	 * @param bytes 字节数
	 * @return 吉字节数
	 */
	public static double bytesToGB(long bytes) {
		return (double) bytes / BYTES_IN_GB;
	}

	/**
	 * 将字节转换为太字节 (TB)
	 *
	 * @param bytes 字节数
	 * @return 太字节数
	 */
	public static double bytesToTB(long bytes) {
		return (double) bytes / BYTES_IN_TB;
	}

	/**
	 * 将千字节 (KB) 转换为字节
	 *
	 * @param kb 千字节数
	 * @return 字节数
	 */
	public static long kbToBytes(double kb) {
		return (long) (kb * BYTES_IN_KB);
	}

	/**
	 * 将兆字节 (MB) 转换为字节
	 *
	 * @param mb 兆字节数
	 * @return 字节数
	 */
	public static long mbToBytes(double mb) {
		return (long) (mb * BYTES_IN_MB);
	}

	/**
	 * 将吉字节 (GB) 转换为字节
	 *
	 * @param gb 吉字节数
	 * @return 字节数
	 */
	public static long gbToBytes(double gb) {
		return (long) (gb * BYTES_IN_GB);
	}

	/**
	 * 将太字节 (TB) 转换为字节
	 *
	 * @param tb 太字节数
	 * @return 字节数
	 */
	public static long tbToBytes(double tb) {
		return (long) (tb * BYTES_IN_TB);
	}

	public static void main(String[] args) {
		long bytes = 1048576; // 1MB

		System.out.println(bytes + " bytes = " + bytesToKB(bytes) + " KB");
		System.out.println(bytes + " bytes = " + bytesToMB(bytes) + " MB");
		System.out.println(bytes + " bytes = " + bytesToGB(bytes) + " GB");
		System.out.println(bytes + " bytes = " + bytesToTB(bytes) + " TB");

		double mb = 1.0;
		System.out.println(mb + " MB = " + mbToBytes(mb) + " bytes");
	}
}
