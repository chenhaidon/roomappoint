<template>
    <div class="mall-page">
        <!-- 顶部 -->
        <div class="page-header">
            <div class="header-back" @click="$router.go(-1)">
                <i class="el-icon-arrow-left"></i>
            </div>
            <div class="header-title">积分兑换</div>
            <div class="header-record" @click="$router.push('/Front/GiftRedeemMyList')">
                兑换记录
            </div>
        </div>

        <!-- 积分余额 -->
        <div class="balance-card">
            <div class="balance-label">当前积分</div>
            <div class="balance-value">{{ userBalance }}</div>
        </div>

        <!-- 搜索+筛选 -->
        <div class="filter-bar">
            <el-input v-model="keyword" placeholder="搜索礼品" size="small" clearable
                prefix-icon="el-icon-search" class="filter-input"></el-input>
            <div class="filter-tags">
                <span class="filter-tag" :class="{ active: sortType === '' }" @click="sortType = ''">全部</span>
                <span class="filter-tag" :class="{ active: sortType === 'low' }" @click="sortType = 'low'">低积分优先</span>
            </div>
        </div>

        <!-- 礼品网格 -->
        <div class="gift-grid" v-if="filteredGifts.length > 0">
            <div class="gift-card" v-for="item in filteredGifts" :key="item.Id" @click="showDetail(item)">
                <div class="gift-img">
                    <img v-if="item.Cover" :src="NormalizeImage(item.Cover)" :alt="item.Name">
                    <div v-else class="gift-img-placeholder"><i class="el-icon-present"></i></div>
                    <div class="gift-stock-tag" :class="stockClass(item)">
                        {{ stockText(item) }}
                    </div>
                </div>
                <div class="gift-info">
                    <div class="gift-name">{{ item.Name }}</div>
                    <div class="gift-desc" v-if="item.Summary">{{ item.Summary }}</div>
                    <div class="gift-bottom">
                        <span class="gift-points"><span class="points-num">{{ item.NeedIntegral }}</span> 积分</span>
                        <el-button size="mini" class="gift-btn"
                            :class="{ 'btn-disabled': userBalance < item.NeedIntegral || item.Stock <= 0 }"
                            @click.stop="Redeem(item)">
                            {{ userBalance < item.NeedIntegral ? '积分不足' : '兑换' }}
                        </el-button>
                    </div>
                </div>
            </div>
        </div>

        <!-- 空状态 -->
        <div class="empty-state" v-else>
            <div class="empty-icon"><i class="el-icon-present"></i></div>
            <div class="empty-text">暂无礼品</div>
        </div>

        <!-- 加载更多 -->
        <div class="load-more" v-if="hasMore" @click="loadMore">
            加载更多
        </div>

        <!-- 详情弹窗 -->
        <el-dialog :visible.sync="detailVisible" width="90%" :show-close="true" :lock-scroll="true">
            <div class="detail-content" v-if="detailItem">
                <div class="detail-img">
                    <img v-if="detailItem.Cover" :src="NormalizeImage(detailItem.Cover)" :alt="detailItem.Name">
                </div>
                <div class="detail-name">{{ detailItem.Name }}</div>
                <div class="detail-desc" v-if="detailItem.Summary">{{ detailItem.Summary }}</div>
                <div class="detail-points">
                    <span class="points-num">{{ detailItem.NeedIntegral }}</span> 积分
                </div>
                <div class="detail-stock" :class="stockClass(detailItem)">{{ stockText(detailItem) }}</div>
                <el-button type="warning" class="detail-btn" size="medium"
                    :disabled="userBalance < detailItem.NeedIntegral || detailItem.Stock <= 0"
                    @click="Redeem(detailItem); detailVisible = false">
                    {{ userBalance < detailItem.NeedIntegral ? '积分不足' : '确认兑换' }}
                </el-button>
            </div>
        </el-dialog>
    </div>
</template>

<script>
import { mapGetters } from 'vuex';
import { ReplaceImageHttp } from '@/utils/comm';

export default {
    name: 'GiftMall',
    computed: {
        ...mapGetters(['Token', 'UserInfo', 'UserId']),
        userBalance() {
            return this.myBalance;
        },
        filteredGifts() {
            let list = [...this.allData];
            if (this.keyword) {
                const kw = this.keyword.toLowerCase();
                list = list.filter(x => x.Name && x.Name.toLowerCase().includes(kw));
            }
            if (this.sortType === 'low') {
                list.sort((a, b) => a.NeedIntegral - b.NeedIntegral);
            }
            return list;
        }
    },
    data() {
        return {
            allData: [],
            page: 1,
            limit: 12,
            hasMore: true,
            keyword: '',
            sortType: '',
            detailVisible: false,
            detailItem: null,
            myBalance: 0,
        };
    },
    created() {
        if (!this.Token) {
            this.$router.push('/Login');
            return;
        }
        this.GetBalance();
        this.loadData();
    },
    methods: {
        async GetBalance() {
            const res = await this.$Post("/Integral/GetMyIntegralData", {});
            if (res && res.Success) {
                this.myBalance = res.Data.TotalIntegral || 0;
            }
        },
        NormalizeImage(value) {
            if (!value) return '';
            let raw = Array.isArray(value) ? value[0] : String(value);
            raw = raw.trim();
            if (raw.startsWith('[') && raw.endsWith(']')) {
                try { raw = JSON.parse(raw)[0] || raw; } catch (e) {}
            }
            raw = String(raw).split(',')[0].trim().replace(/^['\"]|['\"]$/g, '');
            return ReplaceImageHttp(raw);
        },
        async loadData(append = false) {
            try {
                const res = await this.$Post('/Gift/FrontList', { Page: this.page, Limit: this.limit });
                if (res && res.Data) {
                    const items = res.Data.Items || [];
                    if (append) {
                        this.allData = [...this.allData, ...items];
                    } else {
                        this.allData = items;
                    }
                    this.hasMore = items.length >= this.limit;
                }
            } catch (e) {}
        },
        loadMore() {
            this.page++;
            this.loadData(true);
        },
        stockClass(item) {
            if (item.Stock <= 0) return 'stock-empty';
            if (item.Stock < 10) return 'stock-low';
            return 'stock-ok';
        },
        stockText(item) {
            if (item.Stock <= 0) return '已兑完';
            if (item.Stock < 10) return '即将兑换完';
            return '库存充足';
        },
        showDetail(item) {
            this.detailItem = item;
            this.detailVisible = true;
        },
        async Redeem(item) {
            if (this.userBalance < item.NeedIntegral) {
                this.$message.warning('积分不足');
                return;
            }
            if (item.Stock <= 0) {
                this.$message.warning('已兑完');
                return;
            }
            const confirm = await this.$comm.ConfirmMessageBox({ content: `确定兑换「${item.Name}」？` });
            if (!confirm) return;
            const { Success } = await this.$Post('/GiftRedeem/Redeem', { Id: item.Id });
            if (Success) {
                this.$message.success('兑换成功');
                this.GetBalance();
                this.page = 1;
                this.loadData();
            }
        }
    }
};
</script>

<style scoped>
.mall-page {
    max-width: 600px;
    margin: 0 auto;
    min-height: 100vh;
    background: #faf6f2;
    padding-bottom: 24px;
}

/* 顶部 */
.page-header {
    display: flex;
    align-items: center;
    padding: 16px;
    background: #fff;
}
.header-back {
    font-size: 20px;
    padding: 8px;
    cursor: pointer;
    color: #333;
}
.header-title {
    flex: 1;
    font-size: 17px;
    font-weight: 600;
    color: #1a1a1a;
    margin-left: 4px;
}
.header-record {
    font-size: 13px;
    color: #ff9800;
    cursor: pointer;
}

/* 积分余额 */
.balance-card {
    margin: 16px;
    padding: 20px 24px;
    background: linear-gradient(135deg, #fff8e1, #fff3e0);
    border-radius: 16px;
    text-align: center;
    box-shadow: 0 2px 8px rgba(255,152,0,0.1);
}
.balance-label {
    font-size: 13px;
    color: #e65100;
    margin-bottom: 4px;
}
.balance-value {
    font-size: 36px;
    font-weight: 800;
    color: #ff9800;
}

/* 筛选栏 */
.filter-bar {
    padding: 0 16px 12px;
}
.filter-input {
    margin-bottom: 10px;
}
.filter-tags {
    display: flex;
    gap: 8px;
}
.filter-tag {
    padding: 4px 14px;
    border-radius: 14px;
    font-size: 12px;
    color: #888;
    background: #fff;
    cursor: pointer;
    transition: all 0.2s;
}
.filter-tag.active {
    background: #ff9800;
    color: #fff;
}

/* 礼品网格 */
.gift-grid {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
    padding: 0 16px;
}
@media (min-width: 480px) {
    .gift-grid { grid-template-columns: repeat(3, 1fr); }
}
.gift-card {
    background: #fff;
    border-radius: 16px;
    overflow: hidden;
    box-shadow: 0 2px 8px rgba(0,0,0,0.04);
    transition: all 0.2s;
    cursor: pointer;
}
.gift-card:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 16px rgba(0,0,0,0.08);
}
.gift-img {
    position: relative;
    width: 100%;
    aspect-ratio: 1;
    background: #f9f5f0;
    overflow: hidden;
}
.gift-img img {
    width: 100%;
    height: 100%;
    object-fit: cover;
}
.gift-img-placeholder {
    width: 100%;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 32px;
    color: #ddd;
}
.gift-stock-tag {
    position: absolute;
    top: 8px;
    right: 8px;
    font-size: 10px;
    padding: 2px 8px;
    border-radius: 8px;
    font-weight: 500;
}
.stock-ok { background: rgba(76,175,80,0.9); color: #fff; }
.stock-low { background: rgba(255,152,0,0.9); color: #fff; }
.stock-empty { background: rgba(158,158,158,0.9); color: #fff; }
.gift-info {
    padding: 12px;
}
.gift-name {
    font-size: 14px;
    font-weight: 700;
    color: #1a1a1a;
    margin-bottom: 4px;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
}
.gift-desc {
    font-size: 11px;
    color: #bbb;
    margin-bottom: 8px;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
}
.gift-bottom {
    display: flex;
    align-items: center;
    justify-content: space-between;
}
.gift-points {
    font-size: 12px;
    color: #e65100;
}
.points-num {
    font-size: 18px;
    font-weight: 800;
}
.gift-btn {
    border-radius: 16px;
    padding: 4px 12px;
    font-size: 11px;
    background: #ff9800;
    border-color: #ff9800;
    color: #fff;
}
.gift-btn.btn-disabled {
    background: #eee;
    border-color: #eee;
    color: #ccc;
    cursor: not-allowed;
}

/* 空状态 */
.empty-state {
    text-align: center;
    padding: 60px 24px;
}
.empty-icon {
    font-size: 48px;
    color: #ddd;
    margin-bottom: 12px;
}
.empty-text {
    font-size: 16px;
    color: #999;
}

/* 加载更多 */
.load-more {
    text-align: center;
    padding: 20px;
    font-size: 13px;
    color: #ff9800;
    cursor: pointer;
}

/* 详情弹窗 */
.detail-content {
    text-align: center;
    padding: 8px 0;
}
.detail-img {
    width: 100%;
    max-height: 200px;
    border-radius: 12px;
    overflow: hidden;
    margin-bottom: 16px;
}
.detail-img img {
    width: 100%;
    height: 100%;
    object-fit: cover;
}
.detail-name {
    font-size: 18px;
    font-weight: 700;
    color: #1a1a1a;
    margin-bottom: 8px;
}
.detail-desc {
    font-size: 13px;
    color: #888;
    margin-bottom: 12px;
    line-height: 1.6;
}
.detail-points {
    font-size: 14px;
    color: #e65100;
    margin-bottom: 8px;
}
.detail-points .points-num {
    font-size: 28px;
}
.detail-stock {
    font-size: 12px;
    margin-bottom: 20px;
}
.detail-btn {
    width: 100%;
    border-radius: 12px;
    padding: 12px;
    font-size: 15px;
    background: #ff9800;
    border-color: #ff9800;
}

/* 动效 */
@keyframes fadeUp {
    from { opacity: 0; transform: translateY(12px); }
    to { opacity: 1; transform: translateY(0); }
}
.mall-page { animation: fadeUp 0.3s ease; }
</style>
