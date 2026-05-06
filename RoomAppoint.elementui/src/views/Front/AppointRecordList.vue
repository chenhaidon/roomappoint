<template>
    <div class="record-page">
        <!-- 顶部 -->
        <div class="page-header">
            <div class="header-back" @click="$router.go(-1)">
                <i class="el-icon-arrow-left"></i>
            </div>
            <div class="header-title">我的学习</div>
            <div class="header-filter" @click="showFilter = !showFilter">
                <i class="el-icon-s-operation"></i>
            </div>
        </div>

        <!-- 状态卡片：学习中 -->
        <div class="status-card card-active" v-if="currentRecord && currentRecord.AppointStatus === 2">
            <div class="status-glow"></div>
            <div class="status-badge-row">
                <span class="status-badge badge-active">学习中</span>
                <span class="status-room">{{ currentRecord.RoomDto?.Name || '' }}</span>
            </div>
            <div class="status-seat">{{ currentRecord.SeatDto?.No || '' }}</div>
            <div class="status-timer">
                <i class="el-icon-time"></i>
                已学习 <span class="timer-value">{{ elapsedTime }}</span>
            </div>
            <div class="status-actions">
                <el-button type="success" size="small" class="action-btn success" @click="EarlyEnd(currentRecord.Id)">
                    结束学习
                </el-button>
            </div>
        </div>

        <!-- 状态卡片：已预约未开始 -->
        <div class="status-card card-booked" v-else-if="currentRecord && currentRecord.AppointStatus === 1">
            <div class="status-badge-row">
                <span class="status-badge badge-booked">已预约</span>
                <span class="status-room">{{ currentRecord.RoomDto?.Name || '' }}</span>
            </div>
            <div class="status-seat">{{ currentRecord.SeatDto?.No || '' }}</div>
            <div class="status-time">
                <i class="el-icon-time"></i>
                {{ displayDate }} {{ periodTime }}
            </div>
            <div class="status-actions">
                <el-button type="primary" size="small" class="action-btn primary" @click="ArrivalClock(currentRecord.Id)">
                    立即入座
                </el-button>
                <el-button type="text" class="action-cancel" @click="CancelAppoint(currentRecord.Id)">
                    取消预约
                </el-button>
            </div>
        </div>

        <!-- 状态卡片：待评价 -->
        <div class="status-card card-review" v-else-if="pendingReview">
            <div class="status-badge-row">
                <span class="status-badge badge-review">待评价</span>
                <span class="status-room">{{ pendingReview.RoomDto?.Name || '' }}</span>
            </div>
            <div class="status-seat">{{ pendingReview.SeatDto?.No || '' }}</div>
            <div class="status-time">
                <i class="el-icon-time"></i>
                {{ formatDate(pendingReview.AppointDate) }} {{ periodLabel(pendingReview.AppointDateType) }}
            </div>
            <div class="status-actions">
                <el-button type="warning" size="small" class="action-btn warning" @click="ShowEditModal(pendingReview.Id)">
                    去评价
                </el-button>
            </div>
        </div>

        <!-- 状态卡片：无预约 -->
        <div class="status-card card-empty" v-else>
            <div class="empty-icon"><i class="el-icon-coffee-cup"></i></div>
            <div class="empty-text">今天还没有学习计划</div>
            <el-button type="primary" size="small" class="empty-btn" @click="$router.push('/Front/Home')">
                去选座
            </el-button>
        </div>

        <!-- 状态分组标签 -->
        <div class="tab-bar">
            <div class="tab-item" :class="{ active: activeTab === 'all' }" @click="activeTab = 'all'">
                全部
            </div>
            <div class="tab-item" :class="{ active: activeTab === 'active' }" @click="activeTab = 'active'">
                进行中
                <span class="tab-count" v-if="countActive > 0">{{ countActive }}</span>
            </div>
            <div class="tab-item" :class="{ active: activeTab === 'pending' }" @click="activeTab = 'pending'">
                未开始
                <span class="tab-count" v-if="countPending > 0">{{ countPending }}</span>
            </div>
            <div class="tab-item" :class="{ active: activeTab === 'done' }" @click="activeTab = 'done'">
                已结束
            </div>
        </div>

        <!-- 可折叠筛选 -->
        <div class="filter-panel" v-show="showFilter">
            <div class="filter-row">
                <el-input v-model="searchForm.NameLike" placeholder="姓名" size="small" clearable></el-input>
                <el-input v-model="searchForm.PhoneLike" placeholder="手机号" size="small" clearable></el-input>
            </div>
            <div class="filter-row">
                <SigleSelect url="/Room/List" columnName="Name" :clearable="true" columnValue="Id"
                    v-model="searchForm.RoomId" placeholder="自习室" size="small">
                </SigleSelect>
            </div>
            <div class="filter-actions">
                <el-button type="primary" size="mini" @click="SearchClick">搜索</el-button>
                <el-button size="mini" @click="ResetClick">重置</el-button>
            </div>
        </div>

        <!-- 待评价提醒 -->
        <div class="review-hint" v-if="pendingReviewCount > 0 && activeTab !== 'review'">
            <i class="el-icon-chat-dot-round"></i>
            <span>你有 {{ pendingReviewCount }} 条待评价</span>
            <span class="review-link" @click="activeTab = 'review'">去评价</span>
        </div>

        <!-- 记录卡片列表 -->
        <div class="record-list">
            <div class="record-card" v-for="item in filteredRecords" :key="item.Id">
                <div class="record-main">
                    <div class="record-room">{{ item.RoomDto?.Name || '' }}</div>
                    <div class="record-seat-row">
                        <span class="record-seat-no">{{ item.SeatDto?.No || '' }}</span>
                        <span class="record-status" :class="'status-' + item.AppointStatus">{{ item.AppointStatusFormat }}</span>
                    </div>
                    <div class="record-meta">
                        {{ formatDate(item.AppointDate) }} {{ periodLabel(item.AppointDateType) }}
                    </div>
                </div>
                <div class="record-action">
                    <el-button v-if="item.AppointStatus === 1" type="primary" size="mini" class="btn-action" @click="ArrivalClock(item.Id)">
                        去入座
                    </el-button>
                    <el-button v-if="item.AppointStatus === 2" type="success" size="mini" class="btn-action" @click="EarlyEnd(item.Id)">
                        学习中
                    </el-button>
                    <el-button v-if="item.AppointStatus === 6" type="warning" size="mini" class="btn-action btn-review" @click="ShowEditModal(item.Id)">
                        去评价
                    </el-button>
                    <span v-if="item.AppointStatus === 3" class="record-done">已完成</span>
                    <span v-if="item.AppointStatus === 4" class="record-cancelled">已取消</span>
                    <span v-if="item.AppointStatus === 5" class="record-expired">已逾期</span>
                </div>
                <div class="record-cancel" v-if="item.AppointStatus === 1" @click="CancelAppoint(item.Id)">
                    取消预约
                </div>
            </div>

            <div class="record-empty" v-if="filteredRecords.length === 0">
                <i class="el-icon-document"></i>
                <span>暂无记录</span>
            </div>
        </div>

        <!-- 加载更多 -->
        <div class="load-more" v-if="hasMore" @click="loadMore">
            加载更多
        </div>

        <!-- 评论弹窗 -->
        <el-dialog title="评价" :visible.sync="editorShow" width="90%" :lock-scroll="true">
            <div class="comment-form">
                <div class="comment-score">
                    <span>评分：</span>
                    <el-rate v-model="formData.CommentScore"></el-rate>
                </div>
                <el-input type="textarea" :rows="4" v-model="formData.Comment" placeholder="分享你的学习体验"></el-input>
            </div>
            <div slot="footer">
                <el-button @click="editorShow = false">取消</el-button>
                <el-button type="primary" @click="CreateOrEditForm()">确定</el-button>
            </div>
        </el-dialog>
    </div>
</template>

<script>
import { mapGetters } from 'vuex';

const PERIOD_MAP = {
    1: { label: '上午', time: '08:00–12:00' },
    2: { label: '下午', time: '14:00–18:00' },
    3: { label: '夜晚', time: '19:00–23:00' },
};

export default {
    name: "AppointRecordList",
    computed: {
        ...mapGetters(['Token', 'UserInfo', 'UserId']),
        currentRecord() {
            return this.records.find(r => r.AppointStatus === 1 || r.AppointStatus === 2) || null;
        },
        pendingReview() {
            return this.records.find(r => r.AppointStatus === 6) || null;
        },
        pendingReviewCount() {
            return this.records.filter(r => r.AppointStatus === 6).length;
        },
        countActive() {
            return this.records.filter(r => r.AppointStatus === 2).length;
        },
        countPending() {
            return this.records.filter(r => r.AppointStatus === 1).length;
        },
        filteredRecords() {
            if (this.activeTab === 'all') return this.records;
            if (this.activeTab === 'active') return this.records.filter(r => r.AppointStatus === 2);
            if (this.activeTab === 'pending') return this.records.filter(r => r.AppointStatus === 1);
            if (this.activeTab === 'done') return this.records.filter(r => [3, 4, 5].includes(r.AppointStatus));
            if (this.activeTab === 'review') return this.records.filter(r => r.AppointStatus === 6);
            return this.records;
        },
        displayDate() {
            if (!this.currentRecord?.AppointDate) return '';
            return this.currentRecord.AppointDate.split(' ')[0];
        },
        periodTime() {
            if (!this.currentRecord) return '';
            return PERIOD_MAP[this.currentRecord.AppointDateType]?.time || '';
        },
        elapsedTime() {
            if (!this.currentRecord?.BeginTime) return '0分钟';
            const begin = new Date(this.currentRecord.BeginTime).getTime();
            const now = this.currentTime;
            const diff = Math.max(0, Math.floor((now - begin) / 1000));
            const h = Math.floor(diff / 3600);
            const m = Math.floor((diff % 3600) / 60);
            if (h > 0) return `${h}小时${m}分钟`;
            return `${m}分钟`;
        }
    },
    data() {
        return {
            records: [],
            page: 1,
            limit: 20,
            hasMore: true,
            searchForm: {},
            showFilter: false,
            activeTab: 'all',
            editorShow: false,
            formData: {},
            timer: null,
            currentTime: Date.now(),
        };
    },
    created() {
        this.loadRecords();
        this.timer = setInterval(() => {
            if (this.currentRecord && this.currentRecord.AppointStatus === 2) {
                this.currentTime = Date.now();
            }
        }, 1000);
    },
    beforeDestroy() {
        if (this.timer) clearInterval(this.timer);
    },
    methods: {
        async loadRecords(append = false) {
            const params = {
                UserId: this.UserId,
                Page: this.page,
                Limit: this.limit,
                ...this.searchForm
            };
            try {
                const res = await this.$Post('/AppointRecord/List', params);
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
        SearchClick() {
            this.page = 1;
            this.loadRecords();
        },
        ResetClick() {
            this.searchForm = {};
            this.page = 1;
            this.loadRecords();
        },
        formatDate(d) {
            if (!d) return '';
            return d.split(' ')[0];
        },
        periodLabel(type) {
            return PERIOD_MAP[type]?.label || '';
        },
        async ArrivalClock(id) {
            await this.$PostSigleUpdate('/AppointRecord/Get', '/AppointRecord/ArrivalClock', id, '确认入座？', {});
            this.page = 1;
            this.loadRecords();
        },
        async CancelAppoint(id) {
            await this.$PostSigleUpdate('/AppointRecord/Get', '/AppointRecord/CancelAppoint', id, '确认取消预约？', {});
            this.page = 1;
            this.loadRecords();
        },
        async EarlyEnd(id) {
            await this.$PostSigleUpdate('/AppointRecord/Get', '/AppointRecord/EarlyEnd', id, '确认结束学习？', {});
            this.page = 1;
            this.loadRecords();
        },
        async ShowEditModal(id) {
            const { Data } = await this.$Post('/AppointRecord/Get', { Id: id });
            this.formData = Data;
            this.editorShow = true;
        },
        async CreateOrEditForm() {
            const { Success } = await this.$Post('/AppointRecord/Comment', this.formData);
            if (Success) {
                this.editorShow = false;
                this.page = 1;
                this.loadRecords();
            }
        }
    }
};
</script>

<style scoped>
.record-page {
    max-width: 600px;
    margin: 0 auto;
    min-height: 100vh;
    background: #f5f6f8;
    padding-bottom: 32px;
}

/* 顶部 */
.page-header {
    display: flex;
    align-items: center;
    padding: 16px 16px 16px 8px;
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
.header-filter {
    font-size: 18px;
    color: #999;
    padding: 8px;
    cursor: pointer;
}

/* 状态卡片 */
.status-card {
    margin: 20px 16px;
    padding: 24px;
    border-radius: 20px;
    background: #fff;
    box-shadow: 0 4px 16px rgba(0,0,0,0.06);
    position: relative;
    overflow: hidden;
}
.card-active {
    background: linear-gradient(135deg, #e8f5e9, #f1f8f1);
    border-left: 4px solid #4caf50;
}
.card-booked {
    background: linear-gradient(135deg, #e3f2fd, #f0f7ff);
    border-left: 4px solid #3A5F73;
}
.card-review {
    background: linear-gradient(135deg, #fff8e1, #fffaf0);
    border-left: 4px solid #ff9800;
}
.card-empty {
    text-align: center;
    padding: 40px 24px;
}
.status-glow {
    position: absolute;
    top: -20px;
    right: -20px;
    width: 80px;
    height: 80px;
    background: rgba(76, 175, 80, 0.1);
    border-radius: 50%;
}
.status-badge-row {
    display: flex;
    align-items: center;
    gap: 10px;
    margin-bottom: 12px;
}
.status-badge {
    font-size: 12px;
    padding: 3px 12px;
    border-radius: 12px;
    font-weight: 600;
    letter-spacing: 0.5px;
}
.badge-active { background: #4caf50; color: #fff; }
.badge-booked { background: #3A5F73; color: #fff; }
.badge-review { background: #ff9800; color: #fff; }
.status-room {
    font-size: 14px;
    color: #666;
}
.status-seat {
    font-size: 40px;
    font-weight: 800;
    color: #1a1a1a;
    margin: 8px 0 12px;
    letter-spacing: 2px;
}
.status-timer {
    font-size: 14px;
    color: #4caf50;
    margin-bottom: 20px;
    font-weight: 500;
}
.status-timer i { margin-right: 4px; }
.timer-value {
    font-size: 18px;
    font-weight: 700;
}
.status-time {
    font-size: 13px;
    color: #888;
    margin-bottom: 20px;
}
.status-time i { margin-right: 4px; }
.status-actions {
    display: flex;
    gap: 12px;
    align-items: center;
}
.action-btn {
    border-radius: 24px;
    padding: 10px 28px;
    font-size: 14px;
    font-weight: 500;
}
.action-btn.primary { background: #3A5F73; border-color: #3A5F73; }
.action-btn.success { background: #4caf50; border-color: #4caf50; }
.action-btn.warning { background: #ff9800; border-color: #ff9800; }
.action-cancel { color: #999; font-size: 13px; }

.empty-icon { font-size: 40px; color: #ddd; margin-bottom: 12px; }
.empty-text { font-size: 15px; color: #aaa; margin-bottom: 20px; }
.empty-btn { border-radius: 24px; padding: 10px 32px; }

/* 标签栏 */
.tab-bar {
    display: flex;
    gap: 0;
    padding: 0 16px;
    margin-bottom: 4px;
    background: transparent;
}
.tab-item {
    flex: 1;
    text-align: center;
    padding: 12px 0;
    font-size: 13px;
    color: #999;
    cursor: pointer;
    border-bottom: 2px solid transparent;
    transition: all 0.2s;
    position: relative;
}
.tab-item.active {
    color: #3A5F73;
    font-weight: 600;
    border-bottom-color: #3A5F73;
}
.tab-count {
    position: absolute;
    top: 6px;
    right: 10%;
    font-size: 10px;
    background: #ff9800;
    color: #fff;
    min-width: 16px;
    height: 16px;
    line-height: 16px;
    border-radius: 8px;
    text-align: center;
    padding: 0 4px;
}

/* 待评价提醒 */
.review-hint {
    display: flex;
    align-items: center;
    gap: 8px;
    margin: 12px 16px 0;
    padding: 12px 16px;
    background: #fff8e1;
    border-radius: 12px;
    font-size: 13px;
    color: #e65100;
}
.review-hint i { font-size: 16px; }
.review-link {
    margin-left: auto;
    color: #ff9800;
    font-weight: 600;
    cursor: pointer;
}

/* 筛选面板 */
.filter-panel {
    margin: 12px 16px;
    padding: 16px;
    background: #fff;
    border-radius: 12px;
    box-shadow: 0 2px 8px rgba(0,0,0,0.04);
}
.filter-row {
    display: flex;
    gap: 8px;
    margin-bottom: 12px;
}
.filter-row .el-input,
.filter-row .el-select { flex: 1; }
.filter-actions { text-align: right; }

/* 记录卡片 */
.record-list {
    padding: 8px 16px;
}
.record-card {
    background: #fff;
    border-radius: 16px;
    padding: 18px;
    margin-bottom: 12px;
    box-shadow: 0 2px 8px rgba(0,0,0,0.04);
}
.record-main {
    margin-bottom: 12px;
}
.record-room {
    font-size: 16px;
    font-weight: 700;
    color: #1a1a1a;
    margin-bottom: 6px;
}
.record-seat-row {
    display: flex;
    align-items: center;
    gap: 10px;
    margin-bottom: 6px;
}
.record-seat-no {
    font-size: 20px;
    font-weight: 700;
    color: #3A5F73;
}
.record-status {
    font-size: 11px;
    padding: 2px 10px;
    border-radius: 10px;
    font-weight: 500;
}
.status-1 { background: #e3f2fd; color: #1976d2; }
.status-2 { background: #e8f5e9; color: #388e3c; }
.status-3 { background: #f5f5f5; color: #999; }
.status-4 { background: #fff3e0; color: #f57c00; }
.status-5 { background: #fce4ec; color: #d32f2f; }
.status-6 { background: #fff8e1; color: #f9a825; }
.record-meta {
    font-size: 12px;
    color: #bbb;
}
.record-action {
    display: flex;
    align-items: center;
    gap: 8px;
}
.btn-action {
    border-radius: 20px;
    padding: 6px 16px;
    font-size: 12px;
}
.btn-review {
    background: #ff9800;
    border-color: #ff9800;
    color: #fff;
}
.record-done,
.record-cancelled,
.record-expired {
    font-size: 12px;
    color: #ccc;
}
.record-cancel {
    margin-top: 10px;
    font-size: 12px;
    color: #999;
    cursor: pointer;
    text-align: right;
}
.record-empty {
    text-align: center;
    padding: 48px 0;
    color: #ddd;
    font-size: 14px;
}
.record-empty i {
    display: block;
    font-size: 36px;
    margin-bottom: 8px;
}

/* 加载更多 */
.load-more {
    text-align: center;
    padding: 20px;
    font-size: 13px;
    color: #3A5F73;
    cursor: pointer;
}

/* 评论弹窗 */
.comment-form { padding: 8px 0; }
.comment-score {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 12px;
    font-size: 14px;
    color: #666;
}

/* 动效 */
@keyframes fadeUp {
    from { opacity: 0; transform: translateY(12px); }
    to { opacity: 1; transform: translateY(0); }
}
.record-page { animation: fadeUp 0.3s ease; }
</style>
