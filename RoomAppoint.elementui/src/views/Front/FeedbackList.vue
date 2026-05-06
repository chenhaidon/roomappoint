<template>
    <div class="feedback-page">
        <!-- 顶部 -->
        <div class="page-header">
            <div class="header-back" @click="$router.go(-1)">
                <i class="el-icon-arrow-left"></i>
            </div>
            <div class="header-title">意见反馈</div>
            <div class="header-add" @click="ShowCreateModal()">
                <i class="el-icon-edit"></i> 提交反馈
            </div>
        </div>

        <!-- 筛选栏 -->
        <div class="filter-bar">
            <div class="filter-tags">
                <span class="filter-tag" :class="{ active: filterStatus === '' }" @click="filterStatus = ''">全部</span>
                <span class="filter-tag" :class="{ active: filterStatus === '1' }" @click="filterStatus = '1'">待处理</span>
                <span class="filter-tag" :class="{ active: filterStatus === '2' }" @click="filterStatus = '2'">已处理</span>
                <span class="filter-tag" :class="{ active: filterStatus === '3' }" @click="filterStatus = '3'">已关闭</span>
            </div>
        </div>

        <!-- 反馈列表 -->
        <div class="feedback-list" v-if="filteredRecords.length > 0">
            <div class="feedback-card" v-for="item in filteredRecords" :key="item.Id" @click="ShowDetailModal(item)">
                <div class="feedback-top">
                    <div class="feedback-title">{{ item.Title }}</div>
                    <span class="feedback-status" :class="statusClass(item.Status)">{{ statusText(item.Status) }}</span>
                </div>
                <div class="feedback-content">{{ item.Content }}</div>
                <div class="feedback-reply-hint" v-if="item.Reply">
                    <i class="el-icon-chat-dot-round"></i> 管理员已回复
                </div>
                <div class="feedback-time">{{ formatTime(item.CreationTime) }}</div>
            </div>
        </div>

        <!-- 空状态 -->
        <div class="empty-state" v-else>
            <div class="empty-icon"><i class="el-icon-chat-line-round"></i></div>
            <div class="empty-text">暂无反馈记录</div>
            <div class="empty-hint">有问题随时告诉我们</div>
        </div>

        <!-- 加载更多 -->
        <div class="load-more" v-if="hasMore" @click="loadMore">
            加载更多
        </div>

        <!-- 提交反馈弹窗 -->
        <el-dialog :visible.sync="createShow" width="90%" :show-close="true" :lock-scroll="true" :fullscreen="isMobile">
            <div class="dialog-title" slot="title">提交反馈</div>
            <el-form ref="createFormRef" :rules="createFormRules" :model="createForm" label-position="top" size="small">
                <el-form-item label="标题" prop="Title">
                    <el-input v-model.trim="createForm.Title" placeholder="请输入反馈标题" clearable maxlength="200" show-word-limit></el-input>
                </el-form-item>
                <el-form-item label="内容" prop="Content">
                    <el-input type="textarea" :rows="6" v-model.trim="createForm.Content" placeholder="请详细描述您遇到的问题"></el-input>
                </el-form-item>
            </el-form>
            <div slot="footer">
                <el-button @click="createShow = false" size="small">取消</el-button>
                <el-button type="warning" @click="SubmitCreate()" size="small" class="submit-btn">提交</el-button>
            </div>
        </el-dialog>

        <!-- 详情弹窗 -->
        <el-dialog :visible.sync="detailShow" width="90%" :show-close="true" :lock-scroll="true" :fullscreen="isMobile">
            <div class="dialog-title" slot="title">反馈详情</div>
            <div class="detail-content" v-if="detailData">
                <div class="detail-title">{{ detailData.Title }}</div>
                <div class="detail-text">{{ detailData.Content }}</div>
                <div class="detail-time">提交时间：{{ formatTime(detailData.CreationTime) }}</div>

                <div class="detail-reply-section" v-if="detailData.Reply">
                    <div class="reply-label">
                        <i class="el-icon-chat-dot-round"></i> 管理员回复
                    </div>
                    <div class="reply-content">{{ detailData.Reply }}</div>
                    <div class="reply-time">回复时间：{{ formatTime(detailData.ReplyTime) }}</div>
                </div>
                <div class="detail-no-reply" v-else>
                    <i class="el-icon-time"></i> 暂未回复，请耐心等待
                </div>
            </div>
        </el-dialog>
    </div>
</template>

<script>
import { mapGetters } from 'vuex';

export default {
    name: "FeedbackList",
    computed: {
        ...mapGetters(['Token', 'UserId']),
        filteredRecords() {
            if (!this.filterStatus) return this.records;
            return this.records.filter(x => String(x.Status) === this.filterStatus);
        }
    },
    data() {
        return {
            records: [],
            page: 1,
            limit: 20,
            hasMore: true,
            filterStatus: '',
            isMobile: false,
            createFormRules: {
                Title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
                Content: [{ required: true, message: '请输入内容', trigger: 'blur' }],
            },
            createForm: {},
            createShow: false,
            detailData: null,
            detailShow: false,
        };
    },
    created() {
        if (!this.Token) {
            this.$router.push('/Login');
            return;
        }
        this.UpdateIsMobile();
        window.addEventListener('resize', this.UpdateIsMobile);
        this.loadRecords();
    },
    beforeDestroy() {
        window.removeEventListener('resize', this.UpdateIsMobile);
    },
    methods: {
        UpdateIsMobile() {
            this.isMobile = window.matchMedia && window.matchMedia('(max-width: 768px)').matches;
        },
        async loadRecords(append = false) {
            try {
                const res = await this.$Post('/Feedback/List', {
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
        statusClass(s) {
            if (s === 1) return 'status-pending';
            if (s === 2) return 'status-done';
            return 'status-closed';
        },
        statusText(s) {
            if (s === 1) return '待处理';
            if (s === 2) return '已处理';
            return '已关闭';
        },
        ShowCreateModal() {
            this.createForm = {};
            this.createShow = true;
        },
        async SubmitCreate() {
            this.$refs.createFormRef.validate(async valid => {
                if (valid) {
                    this.createForm.UserId = this.UserId;
                    const { Success } = await this.$Post('/Feedback/CreateOrEdit', this.createForm);
                    if (Success) {
                        this.createShow = false;
                        this.$message.success('反馈提交成功');
                        this.page = 1;
                        this.loadRecords();
                    }
                }
            });
        },
        ShowDetailModal(row) {
            this.detailData = row;
            this.detailShow = true;
        }
    }
};
</script>

<style scoped>
.feedback-page {
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
.header-add {
    font-size: 13px;
    color: #ff9800;
    cursor: pointer;
    font-weight: 500;
}
.header-add i {
    margin-right: 2px;
}

/* 筛选栏 */
.filter-bar {
    padding: 12px 16px 8px;
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

/* 反馈列表 */
.feedback-list {
    padding: 8px 16px;
}
.feedback-card {
    background: #fff;
    border-radius: 14px;
    padding: 16px;
    margin-bottom: 12px;
    box-shadow: 0 2px 8px rgba(0,0,0,0.04);
    cursor: pointer;
    transition: all 0.2s;
}
.feedback-card:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 16px rgba(0,0,0,0.08);
}
.feedback-top {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 8px;
}
.feedback-title {
    font-size: 15px;
    font-weight: 700;
    color: #1a1a1a;
    flex: 1;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
    margin-right: 8px;
}
.feedback-status {
    font-size: 11px;
    padding: 2px 10px;
    border-radius: 10px;
    font-weight: 500;
    flex-shrink: 0;
}
.status-pending { background: #fff3e0; color: #e65100; }
.status-done { background: #e8f5e9; color: #2e7d32; }
.status-closed { background: #f5f5f5; color: #999; }
.feedback-content {
    font-size: 13px;
    color: #666;
    line-height: 1.6;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
    margin-bottom: 8px;
}
.feedback-reply-hint {
    font-size: 12px;
    color: #4caf50;
    margin-bottom: 4px;
}
.feedback-reply-hint i {
    margin-right: 2px;
}
.feedback-time {
    font-size: 12px;
    color: #ccc;
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

/* 弹窗 */
.dialog-title {
    font-size: 16px;
    font-weight: 700;
    color: #1a1a1a;
}
.submit-btn {
    background: #ff9800;
    border-color: #ff9800;
}

/* 详情 */
.detail-title {
    font-size: 18px;
    font-weight: 700;
    color: #1a1a1a;
    margin-bottom: 12px;
}
.detail-text {
    font-size: 14px;
    color: #666;
    line-height: 1.8;
    white-space: pre-wrap;
    margin-bottom: 12px;
}
.detail-time {
    font-size: 12px;
    color: #ccc;
    margin-bottom: 20px;
}
.detail-reply-section {
    background: #f9f5f0;
    border-radius: 12px;
    padding: 16px;
}
.reply-label {
    font-size: 14px;
    font-weight: 600;
    color: #e65100;
    margin-bottom: 8px;
}
.reply-label i {
    margin-right: 4px;
}
.reply-content {
    font-size: 14px;
    color: #333;
    line-height: 1.8;
    white-space: pre-wrap;
    margin-bottom: 8px;
}
.reply-time {
    font-size: 12px;
    color: #ccc;
}
.detail-no-reply {
    text-align: center;
    padding: 20px;
    color: #ccc;
    font-size: 14px;
}
.detail-no-reply i {
    margin-right: 4px;
}

/* 动效 */
@keyframes fadeUp {
    from { opacity: 0; transform: translateY(12px); }
    to { opacity: 1; transform: translateY(0); }
}
.feedback-page { animation: fadeUp 0.3s ease; }
</style>
