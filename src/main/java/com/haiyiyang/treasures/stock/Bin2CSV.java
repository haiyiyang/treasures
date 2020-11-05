package data;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class FileIndex {
	String code;
	long Offsize;
	long Size;

	public FileIndex(String code, long offsize, long size) {
		super();
		this.code = code;
		Offsize = offsize;
		Size = size;
	}

}

class DayPriceSummary {
	int date; // 日期
	int time; // 时间
	double prevClosePx; // 前收价
	double openPx; // 开盘价
	double highPx; // 最高价
	double lowPx; // 最低价
	double lastPx; // 最新价
	long totalVolumeTrade; // 成交总量
	double totalValueTrade; // 成交总金额

	void setValues(byte[] src, int index) {
		if (index == 0) {
			this.date = Bin2CSV.bytesToInt(src, 0);
		} else if (index == 1) {
			this.time = Bin2CSV.bytesToInt(src, 0);
		} else if (index == 2) {
			this.prevClosePx = ((double) Bin2CSV.bytesToInt(src, 0)) / 1000;
		} else if (index == 3) {
			this.openPx = ((double) Bin2CSV.bytesToInt(src, 0)) / 1000;
		} else if (index == 4) {
			this.highPx = ((double) Bin2CSV.bytesToInt(src, 0)) / 1000;
		} else if (index == 5) {
			this.lowPx = ((double) Bin2CSV.bytesToInt(src, 0)) / 1000;
		} else if (index == 6) {
			this.lastPx = ((double) Bin2CSV.bytesToInt(src, 0)) / 1000;
		} else if (index == 7) {
			this.totalVolumeTrade = Bin2CSV.bytesToLong(src, 0);
		} else if (index == 8) {
			this.totalValueTrade = ((double) Bin2CSV.bytesToInt(src, 0)) / 1000;
		}
	}

	@Override
	public String toString() {
		return "DayPriceSummary [date=" + date + ", time=" + time + ", prevClosePx=" + prevClosePx + ", openPx="
				+ openPx + ", highPx=" + highPx + ", lowPx=" + lowPx + ", lastPx=" + lastPx + ", totalVolumeTrade="
				+ totalVolumeTrade + ", totalValueTrade=" + totalValueTrade + "]";
	}

}

class Bin2CSV {
	private static List<FileIndex> fileIndexList;
	private static List<DayPriceSummary> dayPriceSummaryList;

	public static void readFile(String fileName) {

		File file = new File(fileName);
		if (file.exists()) {
			try {
				FileInputStream in = new FileInputStream(file);
				DataInputStream dis = new DataInputStream(in);
				byte[] bs = new byte[12];
				dis.read(bs, 0, 4);
				int dataLength = bytesToInt(bs, 0);
				System.out.println("data length:" + dataLength);
				fileIndexList = new ArrayList<>(dataLength);
				dayPriceSummaryList = new ArrayList<>(dataLength * 22);
				for (int i = 0; i < dataLength; i++) {
					dis.read(bs, 0, 12);
					String stockCode = new String(bs, 0, 12);
					System.out.println("stockCode:" + stockCode);
					dis.read(bs, 0, 8);
					long offsize = bytesToLong(bs, 0);
					System.out.println("offsize:" + offsize);
					dis.read(bs, 0, 8);
					long size = bytesToLong(bs, 0);
					System.out.println("size:" + size);
					fileIndexList.add(new FileIndex(stockCode, offsize, size));
				}
				for (FileIndex fi : fileIndexList) {
					long size = fi.Size;
					for (int j = 0; j < size; j++) {
						DayPriceSummary dayPriceSummary = new DayPriceSummary();
						dayPriceSummaryList.add(dayPriceSummary);
						for (int k = 0; k < 9; k++) {
							dis.read(bs, 0, k < 7 ? 4 : 8);
							dayPriceSummary.setValues(bs, k);
						}
					}
				}
				dis.close();
				in.close();
				for (DayPriceSummary dayPriceSummary : dayPriceSummaryList) {
					System.out.println("dayPriceSummary:" + dayPriceSummary);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				// close
			}
		}
	}

	public static void main(String[] args) {
		readFile("/home/dade/201401.DAT");
	}

	/**
	 * byte数组中取int数值，本方法适用于(低位在前，高位在后)的顺序
	 * 
	 * @param src    byte数组
	 * @param offset 从数组的第offset位开始
	 * @return int 数值
	 */
	public static int bytesToInt(byte[] src, int offset) {
		int value;
		value = (int) ((src[offset] & 0xFF) | ((src[offset + 1] & 0xFF) << 8) | ((src[offset + 2] & 0xFF) << 16)
				| ((src[offset + 3] & 0xFF) << 24));
		return value;
	}

	/**
	 * byte数组中取long数值，本方法适用于(低位在前，高位在后)的顺序
	 * 
	 * @param src    byte数组
	 * @param offset 从数组的第offset位开始
	 * @return long 数值
	 */
	public static int bytesToLong(byte[] src, int offset) {
		int value;
		value = (int) ((src[offset] & 0xFF) | ((src[offset + 1] & 0xFF) << 8) | ((src[offset + 2] & 0xFF) << 16)
				| ((src[offset + 3] & 0xFF) << 24) | ((src[offset + 4] & 0xFF) << 32) | ((src[offset + 5] & 0xFF) << 40)
				| ((src[offset + 6] & 0xFF) << 48) | ((src[offset + 7] & 0xFF) << 56));
		return value;
	}

}
