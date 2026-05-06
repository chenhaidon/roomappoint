<template>
    <div class="redeem-page">
        <!-- 顶部 -->
        <div class="page-header">
            <div class="header-back" @click="$router.go(-1)">
                <i class="el-icon-arrow-left"></i>
            </div>
            <div class="header-title">兑换记录</div>
            <div class="header-mall" @click="$router.push('/Front/GiftMall')">
                去兑换
            </div>
        </div>

        <!-- 记录列表 -->
        <div class="record-list" v-if="records.length > 0">
            <div class="record-card" v-for="item in records" :key="item.Id">
                <div class="record-top">
                    <div class="record-gift-name">{{ (item.GiftDto && item.GiftDto.Name) || item.Title || '礼品兑换' }}</div>
                    <div class="record-time">{{ formatTime(item.CreationTime) }}</div>
                </div>
                <div class="record-detail">
                    <div class="record-info-row" v-if="item.NeedIntegral">
                        <span class="info-label">标价积分</span>
                        <span class="info-value">{{ item.NeedIntegral }}</span>
                    </div>
                    <div class="record-info-row" v-if="item.DeductIntegral">
                        <span class="info-label">扣减积分</span>
                        <span class="info-value spend">{{ item.DeductIntegral }}</span>
                    </div>
                    <div class="record-info-row" v-if="item.DeductBalance">
                        <span class="info-label">扣减余额</span>
                        <span class="info-value spend">¥{{ item.DeductBalance }}</span>
                    </div>
                </div>
                <div class="record-status">
                    <span class="status-tag">已兑换</span>
                </div>
            </div>
        </div>

        <!-- 空状态 -->
        <div class="empty-state" v-else>
            <div class="empty-icon"><i class="el-icon-present"></i></div>
            <div class="empty-text">暂无兑换记录</div>
            <div class="empty-hint">去积分商城看看吧</div>
        </div>

        <!-- 加载更多 -->
        <div class="load-more" v-if="hasMore" @click="loadMore">
            加载更多
        </div>
    </div>
</template>

<script>
import { mapGetters } from 'vuex';

export default {
    name: 'GiftRedeemMyList',
    computed: {
        ...mapGetters(['Token', 'UserId'])
    },
    data() {
        return {
            records: [],
            page: 1,
            limit: 20,
            hasMore: true,
        };
    },
    created() {
        if (!this.Token) {
            this.$router.push('/Login');
            return;
        }
        this.loadRecords();
    },
    methods: {
        async loadRecords(append = false) {
            try {
                const res = await this.$Post('/GiftRedeem/MyList', {
                    Page: this.page,
                    Limit: this.limit
                });
                if (res && res.Data) {
                    const items = res.Data.Items || [];
                    if (append) {
                        this.records = [...this.records, ...items];
                    } else {
                        this.records = items;
                    }
                    this.hasMore = items.length >= this.limit;
                }
            } catch (e) {}
        },
        loadMore() {
            this.page++;
            this.loadRecords(true);
        },
        formatTime(t) {
            if (!t) return '';
            return t.replace('T', ' ').substring(0, 16);
        }
    }
};
</script>

<style scoped>
.redeem-page {
    max-width: 600px;
    margin: 0 auto;
    min-height: 100vh;
    background: #faf6f2;
    padding-bottom: 32px;
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
.header-mall {
    font-size: 13px;
    color: #ff9800;
    cursor: pointer;
    font-weight: 500;
}

/* 记录列表 */
.record-list {
    padding: 16px;
}
.record-card {
    background: #fff;
    border-radius: 14px;
    padding: 16px;
    margin-bottom: 12px;
    box-shadow: 0 2px 8px rgba(0,0,0,0.04);
}
.record-top {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 12px;
}
.record-gift-name {
    font-size: 15px;
    font-weight: 700;
    color: #1a1a1a;
}
.record-time {
    font-size: 12px;
    color: #ccc;
}
.record-detail {
    display: flex;
    flex-wrap: wrap;
    gap: 8px 20px;
    margin-bottom: 10px;
}
.record-info-row {
    display: flex;
    align-items: center;
    gap: 6px;
}
.info-label {
    font-size: 12px;
    color: #999;
}
.info-value {
    font-size: 14px;
    font-weight: 600;
    color: #1a1a1a;
}
.info-value.spend {
    color: #ff9800;
}
.record-status {
    text-align: right;
}
.status-tag {
    display: inline-block;
    font-size: 11px;
    padding: 2px 10px;
    border-radius: 10px;
    background: #e8f5e9;
    color: #2e7d32;
    font-weight: 500;
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
    margin-bottom: 6px;
}
.empty-hint {
    font-size: 13px;
    color: #ccc;
}

/* 加载更多 */
.load-more {
    text-align: center;
    padding: 20px;
    font-size: 13px;
    color: #ff9800;
    cursor: pointer;
}

/* 动效 */
@keyframes fadeUp {
    from { opacity: 0; transform: translateY(12px); }
    to { opacity: 1; transform: translateY(0); }
}
.redeem-page { animation: fadeUp 0.3s ease; }
</style>
