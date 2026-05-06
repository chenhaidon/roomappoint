<template>
    <div class="balance-page">
        <!-- 顶部 -->
        <div class="page-header">
            <div class="header-back" @click="$router.go(-1)">
                <i class="el-icon-arrow-left"></i>
            </div>
            <div class="header-title">我的余额</div>
        </div>

        <!-- 余额核心卡片 -->
        <div class="balance-card">
            <div class="balance-main">
                <div class="balance-label">当前余额</div>
                <div class="balance-value">
                    <span class="currency">¥</span>{{ balance }}
                </div>
            </div>
            <div class="balance-hint">充值后余额更新可能有延迟</div>
            <el-button type="warning" class="recharge-btn" @click="$router.push('/Front/Recharge')">
                去充值
            </el-button>
        </div>

        <!-- 筛选栏 -->
        <div class="filter-bar">
            <div class="filter-tags">
                <span class="filter-tag" :class="{ active: filterType === '' }" @click="filterType = ''">全部</span>
                <span class="filter-tag" :class="{ active: filterType === 'in' }" @click="filterType = 'in'">充值</span>
                <span class="filter-tag" :class="{ active: filterType === 'out' }" @click="filterType = 'out'">消费</span>
            </div>
        </div>

        <!-- 账单列表 -->
        <div class="bill-list">
            <div class="bill-item" v-for="item in filteredRecords" :key="item.Id">
                <div class="bill-dot" :class="item.Amount > 0 ? 'dot-in' : 'dot-out'"></div>
                <div class="bill-content">
                    <div class="bill-title">{{ item.Title }}</div>
                    <div class="bill-time">{{ formatTime(item.CreationTime) }}</div>
                </div>
                <div class="bill-amount" :class="item.Amount > 0 ? 'amount-in' : 'amount-out'">
                    {{ item.Amount > 0 ? '+' : '' }}{{ item.Amount }}
                </div>
            </div>

            <div class="bill-empty" v-if="filteredRecords.length === 0">
                <i class="el-icon-wallet"></i>
                <span>暂无余额记录</span>
            </div>
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
    name: "BalanceRecordList",
    computed: {
        ...mapGetters(['Token', 'UserId']),
        filteredRecords() {
            let list = this.records;
            if (this.filterType === 'in') list = list.filter(x => x.Amount > 0);
            if (this.filterType === 'out') list = list.filter(x => x.Amount < 0);
            return list;
        }
    },
    data() {
        return {
            balance: '0.00',
            records: [],
            page: 1,
            limit: 20,
            hasMore: true,
            filterType: '',
        };
    },
    created() {
        if (!this.Token) {
            this.$router.push('/Login');
            return;
        }
        this.GetBalance();
        this.loadRecords();
    },
    methods: {
        async GetBalance() {
            const res = await this.$Post('/Balance/GetMyBalance', {});
            if (res && res.Success) {
                this.balance = res.Data.Balance || '0.00';
            }
        },
        async loadRecords(append = false) {
            try {
                const res = await this.$Post('/Balance/RecordList', {
                    UserId: this.UserId,
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
.balance-page {
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
    font-size: 17px;
    font-weight: 600;
    color: #1a1a1a;
    margin-left: 4px;
}

/* 余额卡片 */
.balance-card {
    margin: 20px 16px;
    padding: 28px 24px;
    background: linear-gradient(135deg, #fff8e1, #fff3e0);
    border-radius: 20px;
    text-align: center;
    box-shadow: 0 4px 16px rgba(255,152,0,0.1);
}
.balance-label {
    font-size: 14px;
    color: #e65100;
    margin-bottom: 8px;
}
.balance-value {
    font-size: 48px;
    font-weight: 800;
    color: #ff9800;
    margin-bottom: 8px;
}
.currency {
    font-size: 24px;
    font-weight: 600;
    margin-right: 2px;
}
.balance-hint {
    font-size: 12px;
    color: #cc8a4a;
    margin-bottom: 20px;
}
.recharge-btn {
    width: 100%;
    border-radius: 12px;
    padding: 12px;
    font-size: 15px;
    font-weight: 600;
    background: #ff9800;
    border-color: #ff9800;
}

/* 筛选栏 */
.filter-bar {
    padding: 0 16px 8px;
}
.filter-tags {
    display: flex;
    gap: 8px;
}
.filter-tag {
    padding: 6px 16px;
    border-radius: 16px;
    font-size: 13px;
    color: #888;
    background: #fff;
    cursor: pointer;
    transition: all 0.2s;
}
.filter-tag.active {
    background: #ff9800;
    color: #fff;
}

/* 账单列表 */
.bill-list {
    padding: 8px 16px;
}
.bill-item {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 16px;
    background: #fff;
    border-radius: 14px;
    margin-bottom: 10px;
    box-shadow: 0 2px 6px rgba(0,0,0,0.03);
}
.bill-dot {
    width: 10px;
    height: 10px;
    border-radius: 50%;
    flex-shrink: 0;
}
.dot-in { background: #4caf50; }
.dot-out { background: #ff9800; }
.bill-content {
    flex: 1;
    min-width: 0;
}
.bill-title {
    font-size: 14px;
    font-weight: 600;
    color: #1a1a1a;
    margin-bottom: 2px;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
}
.bill-time {
    font-size: 12px;
    color: #ccc;
}
.bill-amount {
    font-size: 18px;
    font-weight: 800;
    flex-shrink: 0;
}
.amount-in { color: #4caf50; }
.amount-out { color: #ff9800; }

.bill-empty {
    text-align: center;
    padding: 48px 0;
    color: #ddd;
    font-size: 14px;
}
.bill-empty i {
    display: block;
    font-size: 36px;
    margin-bottom: 8px;
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
.balance-page { animation: fadeUp 0.3s ease; }
</style>
