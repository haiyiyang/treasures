package com.haiyiyang.treasures.stock;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

/** 股票 */
class Stock {
	String code; // 股票代码
	String pinyinCode; // 股票拼音代码
	String categroy; // 所属行业
	Double peRatio; // 市盈率
	Double ttm; // Trailing Twelve Months 近十二个月市盈率
	Double marketValue; // 市值
	boolean isHot; // 是否热点
	TransHistory[] thLastest; // 历史交易数据
	Multimap<Integer, StockTrans> stockTransMulitMap = ArrayListMultimap.create(); // 模拟成交数据
}

/** 交易明细 */
class TransDetail {

	double price; // 交易价格
	int quantity; // 交易数量
	int tansDayIndex; // 交易日期Index
	String comment; // 备注（如交易原因）

	TransDetail(double price, int quantity, int tansDayIndex, String comment) {
		this.price = price;
		this.quantity = quantity;
		this.tansDayIndex = tansDayIndex;
		this.comment = comment;
	}
}

/** 股票买卖交易 */
class StockTrans {
	TransDetail tdBuy;// 买入详情
	List<TransDetail> tdSell; // 卖出详情

	StockTrans(TransDetail tdBuy) {
		this.tdBuy = tdBuy;
	}
}

/** 历史交易数据 */
class TransHistory {
	Double openingPrice; // 开盘价
	Double closingPrice; // 收盘价
	Double maxPrice; // 最高价
	Double minPrice; // 最低价
	Double volume; // 当日成交量
	Double volumeOfClosingPriceAndAbove; // 在成交价及以上的价格的成交数量
}

/** 交易规则 */
class Rules {

	/** 排除的特定行业 */
	private static List<String> ExcludedCategroys = Arrays.asList(new String[] { "军工", "券商", "钢铁", "银行", "保险" });

	/** 判断是否在跌停的价格区间范围内 */
	static boolean isWinthinTopLimit(double priceRange) {
		return (priceRange >= 1.0996d && priceRange <= 1.1003d) || (priceRange >= 1.1996d && priceRange <= 1.2003d);
	}

	/** 判断是否在跌停的价格区间范围内 */
	static boolean isWinthinBottomLimit(double priceRange) {
		return priceRange <= 0.9d;
	}

	/** 基本判断 */
	static boolean meetThefundamentals(Stock s) {
		return s.peRatio > 0 // 市盈率大于0
				&& s.ttm > 0 // 近12个月市盈率
				&& s.marketValue < 500 * 10000 * 10000 // 市值小于500亿
				&& !Rules.ExcludedCategroys.contains(s.categroy) // 不在排除的行业内
				&& s.thLastest.length >= 4; // 已经存在4天的交易数据（用于排除才上市不到4天的股票）
	}

	/** 判断是否涨停 */
	static boolean isStockTopLimit(TransHistory th) {
		return isWinthinTopLimit(th.closingPrice / th.openingPrice);
	}

	/** 判断是否跌停 */
	static boolean isStockBottomLimit(TransHistory th) {
		return isWinthinBottomLimit(th.closingPrice / th.openingPrice);
	}

	/** 判断是否连续两天涨停 */
	static boolean isStockLimit2Days(TransHistory th1, TransHistory th2) {
		return isStockTopLimit(th1) // 判断第一天是否涨停
				&& isStockTopLimit(th2); // 判断第二天是否涨停
	}

	/** 判断最高价是否到达过涨停的价格区间范围内 */
	static boolean hasMaxPriceReachedStockLimit(TransHistory th) {
		return isWinthinTopLimit(th.maxPrice / th.openingPrice); // 判断最高价是否到达过涨停的价格区间范围内

	}

	/** 最高价出现过在涨停价格区间范围内，但收盘价不在涨停价格区间范围内 */
	static boolean existsPricesPeakedAndFinallyFell(TransHistory th) {
		return isWinthinTopLimit(th.maxPrice / th.openingPrice) // 最高价出现过在涨停价格区间范围内
				&& !isWinthinTopLimit(th.closingPrice / th.openingPrice); // 收盘价不在涨停价格区间范围内
	}

	/** 判断价格回落幅度是否在范围内 */
	static boolean withinFellRange(TransHistory th) {
		return (th.maxPrice / th.closingPrice > 1.04);
	}

	/** 成交价及以上的价格的成交数量比例是否满足条件 */
	static boolean meetHighPriceTransRatio(TransHistory th) {
		return th.volumeOfClosingPriceAndAbove / th.volume > 0.3d;

	}

	/** 判断是否满足反包规则中的价格要求 */
	static boolean meetFanBaoPriceRules(TransHistory th, TransHistory thTiaozheng) {

		/** ------------------以下3个规则满足其中之一即可-------------------- */

		// 【N+1 日收盘价>N 日最高价*98%】
		if (th.closingPrice > thTiaozheng.maxPrice * 0.98d) {
			return true;
		}
		// 【（N+1 日最高价>N 日最高价）and（N+1日收盘价>N 日收盘价）】
		if (th.maxPrice > thTiaozheng.maxPrice && th.closingPrice > thTiaozheng.closingPrice) {
			return true;
		}
		// 【（N+1 日最高价>N 日最高价*99%）and（N+1 日收盘价>N+1 日开盘价*1.02）】
		if (th.maxPrice > thTiaozheng.maxPrice * 0.99d && th.closingPrice > th.openingPrice * 1.02d) {
			return true;
		}
		return false;
	}

	/** 判断是否满足反包规则 */
	static boolean meetFanBaoRules(TransHistory thTiaozheng, TransHistory thFanbao) {

		// 判断是否连续两天涨停
		if (!isStockLimit2Days(thTiaozheng, thFanbao)) {
			return false;
		}

		// 调整日价格是否到达过涨停价区间
		// 剔除【9.96%≤N 日最高价≤10.03%】
		if (hasMaxPriceReachedStockLimit(thTiaozheng)) {
			return false;
		}

		// 判断最高价出现过在涨停价格区间范围内，但收盘价不在涨停价格区间范围内
		// 剔除【（9.96%≤N+1 日最高价≤10.03%）and（N+1 日收盘价<9.96%）】
		if (existsPricesPeakedAndFinallyFell(thFanbao)) {
			return false;
		}

		// 判断调整幅度是否满足条件
		// 【N 日最高价/N 日收盘价>1.04】
		if (!withinFellRange(thTiaozheng)) {
			return false;
		}

		// 成交价及以上的价格的成交数量比例是否满足条件
		// 【N 日收盘价以 上 的 成 交 量 /N 日成交量>30%】
		if (!meetHighPriceTransRatio(thTiaozheng)) {
			return false;
		}

		// 判断是否满足反包规则中价格要求
		if (meetFanBaoPriceRules(thFanbao, thTiaozheng)) {
			return false;
		}
		return true;
	}

	/** 计算买入价格 */
	static double caculateBuyPrice(TransHistory thTiaozheng, TransHistory thFanbao) {
		return thTiaozheng.minPrice <= thFanbao.minPrice ? thTiaozheng.minPrice * 1.02d : thFanbao.minPrice * 1.02d;
	}

	/** 计算是否能买入 */
	static boolean meetBuyRule(TransHistory thTiaozheng, double buyPrice) {
		return thTiaozheng.minPrice <= buyPrice;
	}

	/** 买入股票及补仓 */
	static Boolean buyAndReplenish(int fanBaoIndex, Stock s, double buyPrice, int transDayIndex) {
		if (!s.stockTransMulitMap.containsKey(fanBaoIndex)) {
			s.stockTransMulitMap.put(fanBaoIndex, new StockTrans(new TransDetail(buyPrice, 100, transDayIndex, "买入")));
			// 补仓逻辑
			return replenish(fanBaoIndex, s, buyPrice, transDayIndex, false);
		}
		return false;
	}

	/** 卖出股票 */
	static void sell(int fanBaoIndex, Stock s, int transDayIndex) {
		if (s.stockTransMulitMap.containsKey(fanBaoIndex)) {
			Collection<StockTrans> transList = s.stockTransMulitMap.get(fanBaoIndex);
			for (StockTrans st : transList) {
				if (st.tdSell == null) {
					// 如果前一天跌停，今早开盘及时卖出
					if (isStockBottomLimit(s.thLastest[transDayIndex - 1])) {
						st.tdSell = Lists.newArrayList(
								new TransDetail(s.thLastest[transDayIndex].openingPrice, 100, transDayIndex, "跌停后卖出"));

					}
					// （成交日+1 日收盘价）/ 成交日收盘价 < 1.0996 收盘时卖出
					else if (s.thLastest[transDayIndex].closingPrice < s.thLastest[transDayIndex - 1].closingPrice
							* 1.0996) {
						st.tdSell = Lists.newArrayList(
								new TransDetail(s.thLastest[transDayIndex].closingPrice, 100, transDayIndex, "卖出"));
					}
				}
			}
		}
	}

	/** 股票补仓 */
	static boolean replenish(int fanBaoIndex, Stock s, double buyPrice, int transDayIndex, boolean hasBucang) {
		double rPrice = hasBucang ? buyPrice * 0.88 : buyPrice * 0.95;
		if (s.thLastest[transDayIndex].minPrice < rPrice) {
			s.stockTransMulitMap.put(fanBaoIndex,
					new StockTrans(new TransDetail(rPrice, 100, transDayIndex, "补仓" + (hasBucang ? "2" : "1"))));
			return true;
		}
		return false;
	}

	public static void main(String[] args) {
		Stock s = new Stock();
		// TODO 初始化股票数据

		// 判断某只股票是否满足基本面要求
		if (!meetThefundamentals(s)) {
			return;
		}

		if (s.thLastest.length >= 6) {
			int fanBaoIndex = -1;
			int l = s.thLastest.length - 2;// 预留最小2个交易日用于买入和卖出
			for (int i = 0; i < l; i++) {
				// 不处理3个可能成交的日
				if (fanBaoIndex > -1 && i <= fanBaoIndex + 3) {
					continue;
				}
				if (meetFanBaoRules(s.thLastest[i], s.thLastest[i + 1])) {
					fanBaoIndex = i + 1;
					double buyPrice = caculateBuyPrice(s.thLastest[i], s.thLastest[i + 1]);
					// 处理3个可能成交的日
					for (int j = 1; j <= 3; j++) {
						int transDayIndex = i + 1 + j;
						if (meetBuyRule(s.thLastest[transDayIndex], buyPrice)) {
							boolean hasBucang = buyAndReplenish(fanBaoIndex, s, buyPrice, transDayIndex);
							if (transDayIndex == fanBaoIndex + 2) {
								// 补仓逻辑
								replenish(fanBaoIndex, s, buyPrice, transDayIndex, hasBucang);
							}
							// 反包日之后的两个交易日起才可以卖出
							if (transDayIndex >= fanBaoIndex + 2) {
								// 不涨停的时候才考虑卖出
								if (!isStockTopLimit(s.thLastest[transDayIndex])) {
									sell(fanBaoIndex, s, transDayIndex);
								}
							}
						}
					}
				}
			}
		}
	}
}