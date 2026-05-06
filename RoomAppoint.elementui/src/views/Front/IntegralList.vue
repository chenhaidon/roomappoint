<template>
    <div class="integral-page">
        <!-- 顶部 -->
        <div class="page-header">
            <div class="header-back" @click="$router.go(-1)">
                <i class="el-icon-arrow-left"></i>
            </div>
            <div class="header-title">我的积分</div>
            <div class="header-exchange" @click="$router.push('/Front/GiftMall')">
                去兑换
            </div>
        </div>

        <!-- 积分核心卡片 -->
        <div class="points-card">
            <div class="points-main">
                <div class="points-label">当前积分</div>
                <div class="points-value">{{ MyIntegralData.TotalIntegral || 0 }}</div>
            </div>
            <div class="points-divider"></div>
            <div class="points-sub">
                <div class="sub-item">
                    <span class="sub-label">余额</span>
                    <span class="sub-value">¥{{ balance }}</span>
                </div>
                <div class="sub-item" @click="OverdueTimesClear()">
                    <span class="sub-label">逾期次数</span>
                    <span class="sub-value link">用积分清零</span>
                </div>
            </div>
        </div>

        <!-- 今日获得提示 -->
        <div class="today-hint" v-if="todayEarned > 0">
            <i class="el-icon-sunrise-1"></i>
            今日已获得 <span class="today-num">+{{ todayEarned }}</span> 积分
        </div>

        <!-- 筛选栏 -->
        <div class="filter-bar">
            <div class="filter-tags">
                <span class="filter-tag" :class="{ active: filterType === '' }" @click="filterType = ''">全部</span>
                <span class="filter-tag" :class="{ active: filterType === 'earn' }" @click="filterType = 'earn'">获得</span>
                <span class="filter-tag" :class="{ active: filterType === 'spend' }" @click="filterType = 'spend'">消耗</span>
            </div>
        </div>

        <!-- 记录时间轴 -->
        <div class="timeline">
            <div class="timeline-item" v-for="item in filteredRecords" :key="item.Id">
                <div class="timeline-dot" :class="item.IntegralValue > 0 ? 'dot-earn' : 'dot-spend'"></div>
                <div class="timeline-content">
                    <div class="timeline-title">{{ item.Title }}</div>
                    <div class="timeline-time">{{ formatTime(item.CreationTime) }}</div>
                </div>
                <div class="timeline-value" :class="item.IntegralValue > 0 ? 'value-earn' : 'value-spend'">
                    {{ item.IntegralValue > 0 ? '+' : '' }}{{ item.IntegralValue }}
                </div>
            </div>

            <div class="timeline-empty" v-if="filteredRecords.length === 0">
                <i class="el-icon-medal"></i>
                <span>暂无积分记录</span>
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
    name: "IntegralList",
    computed: {
        ...mapGetters(['Token', 'UserInfo', 'UserId']),
        filteredRecords() {
            let list = this.records;
            if (this.filterType === 'earn') list = list.filter(x => x.IntegralValue > 0);
            if (this.filterType === 'spend') list = list.filter(x => x.IntegralValue < 0);
            return list;
        },
        todayEarned() {
            const today = new Date().toISOString().split('T')[0];
            return this.records
                .filter(x => x.IntegralValue > 0 && x.CreationTime && x.CreationTime.startsWith(today))
                .reduce((sum, x) => sum + x.IntegralValue, 0);
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
            MyIntegralData: { TotalIntegral: 0 },
        };
    },
    created() {
        if (!this.Token) {
            this.$router.push('/Login');
            return;
        }
        this.GetMyIntegralData();
        this.GetBalance();
        this.loadRecords();
    },
    methods: {
        async GetMyIntegralData() {
            const { Data } = await this.$Post("/Integral/GetMyIntegralData", {});
            this.MyIntegralData = Data;
        },
        async GetBalance() {
            const res = await this.$Post("/Balance/GetMyBalance", {});
            if (res && res.Success) {
                this.balance = res.Data.Balance || '0.00';
            }
        },
        async loadRecords(append = false) {
            try {
                const res = await this.$Post('/Integral/List', {
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
        },
        async OverdueTimesClear() {
            const confirm = await this.$comm.ConfirmMessageBox({ content: "确定用积分清空逾期次数？" });
            if (confirm) {
                await this.$Post("/Integral/OverdueTimesClear", {});
                this.GetMyIntegralData();
                this.page = 1;
                this.loadRecords();
            }
        }
    }
};
</script>

<style scoped>
.integral-page {
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
.header-exchange {
    font-size: 13px;
    color: #ff9800;
    cursor: pointer;
    font-weight: 500;
}

/* 积分核心卡片 */
.points-card {
    margin: 20px 16px;
    padding: 24px;
    background: linear-gradient(135deg, #fff8e1, #fff3e0);
    border-radius: 20px;
    box-shadow: 0 4px 16px rgba(255,152,0,0.1);
    display: flex;
    align-items: center;
    gap: 20px;
}
.points-main {
    text-align: center;
    min-width: 120px;
}
.points-label {
    font-size: 13px;
    color: #e65100;
    margin-bottom: 4px;
}
.points-value {
    font-size: 42px;
    font-weight: 800;
    color: #ff9800;
}
.points-divider {
    width: 1px;
    height: 48px;
    background: rgba(255,152,0,0.2);
}
.points-sub {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 12px;
}
.sub-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
}
.sub-label {
    font-size: 13px;
    color: #999;
}
.sub-value {
    font-size: 14px;
    font-weight: 600;
    color: #1a1a1a;
}
.sub-value.link {
    color: #ff9800;
    cursor: pointer;
    font-size: 12px;
}

/* 今日提示 */
.today-hint {
    margin: 0 16px 12px;
    padding: 10px 16px;
    background: #fff;
    border-radius: 12px;
    font-size: 13px;
    color: #e65100;
    box-shadow: 0 2px 8px rgba(0,0,0,0.03);
}
.today-hint i {
    margin-right: 4px;
}
.today-num {
    font-weight: 700;
    font-size: 15px;
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

/* 时间轴 */
.timeline {
    padding: 8px 16px;
}
.timeline-item {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 16px;
    background: #fff;
    border-radius: 14px;
    margin-bottom: 10px;
    box-shadow: 0 2px 6px rgba(0,0,0,0.03);
}
.timeline-dot {
    width: 10px;
    height: 10px;
    border-radius: 50%;
    flex-shrink: 0;
}
.dot-earn { background: #4caf50; }
.dot-spend { background: #ff9800; }
.timeline-content {
    flex: 1;
    min-width: 0;
}
.timeline-title {
    font-size: 14px;
    font-weight: 600;
    color: #1a1a1a;
    margin-bottom: 2px;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
}
.timeline-time {
    font-size: 12px;
    color: #ccc;
}
.timeline-value {
    font-size: 18px;
    font-weight: 800;
    flex-shrink: 0;
}
.value-earn { color: #4caf50; }
.value-spend { color: #ff9800; }

.timeline-empty {
    text-align: center;
    padding: 48px 0;
    color: #ddd;
    font-size: 14px;
}
.timeline-empty i {
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
.integral-page { animation: fadeUp 0.3s ease; }
</style>
