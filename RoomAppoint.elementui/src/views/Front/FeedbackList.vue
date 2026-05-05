<template>
    <div class="app-container margin-top-lg">
        <el-card class="box-card">
            <div slot="header" class="clearfix">
                <el-button type="primary" size="mini" icon="el-icon-edit" @click="ShowCreateModal()">提交反馈</el-button>
                <el-button type="primary" size="mini" icon="el-icon-search" @click="SearchClick">搜 索</el-button>
                <el-button type="warning" size="mini" @click="ResetClick" icon="el-icon-s-promotion">重 置</el-button>
            </div>
            <div class="tb-body">
                <el-form ref="searchFormRef" :model="searchForm" :inline="true" label-width="120px" :style="`flex:1;`">
                    <el-form-item label="状态" prop="Status">
                        <SigleSelect url="/Select/FeedbackStatusEnum" columnName="Name" columnValue="Value"
                            v-model="searchForm.Status" :clearable="true" placeholder="请选择状态">
                        </SigleSelect>
                    </el-form-item>
                </el-form>
            </div>
        </el-card>

        <el-dialog title="提交反馈" :visible.sync="createShow" width="60%" :fullscreen="isMobile" :lock-scroll="true">
            <el-form v-if="createShow == true" ref="createFormRef" :rules="createFormRules" :model="createForm"
                label-width="100px" size="small">
                <el-form-item label="标题" prop="Title">
                    <el-input v-model.trim="createForm.Title" placeholder="请输入反馈标题" :clearable="true"
                        maxlength="200" show-word-limit></el-input>
                </el-form-item>
                <el-form-item label="内容" prop="Content">
                    <el-input type="textarea" :rows="8" v-model.trim="createForm.Content"
                        placeholder="请详细描述您遇到的问题"></el-input>
                </el-form-item>
                <el-form-item>
                    <el-button type="primary" @click="SubmitCreate()">提 交</el-button>
                    <el-button @click="createShow = false">取 消</el-button>
                </el-form-item>
            </el-form>
        </el-dialog>

        <el-dialog title="反馈详情" :visible.sync="detailShow" width="60%" :fullscreen="isMobile" :lock-scroll="true">
            <div v-if="detailShow" class="feedback-detail">
                <h3>{{ detailData.Title }}</h3>
                <p class="detail-content">{{ detailData.Content }}</p>
                <el-divider></el-divider>
                <div v-if="detailData.Reply">
                    <h4>管理员回复：</h4>
                    <p class="detail-reply">{{ detailData.Reply }}</p>
                    <p class="detail-time">回复时间：{{ detailData.ReplyTime }}</p>
                </div>
                <div v-else class="no-reply">暂无回复</div>
            </div>
        </el-dialog>

        <PaginationTable ref="PaginationTableId" url="/Feedback/List" :column="dataColum" :where="where">
            <template v-slot:Status="scope">
                <el-tag v-if="scope.row.Status == 1" type="warning">待处理</el-tag>
                <el-tag v-else-if="scope.row.Status == 2" type="success">已处理</el-tag>
                <el-tag v-else-if="scope.row.Status == 3" type="info">已关闭</el-tag>
            </template>
            <template v-slot:Operate="scope">
                <el-button type="primary" size="mini" @click="ShowDetailModal(scope.row)">查 看</el-button>
            </template>
        </PaginationTable>
    </div>
</template>

<script>
import store from '@/store';
import { mapGetters } from 'vuex'

export default {
    name: "FeedbackList",
    computed: {
        ...mapGetters(['Token', 'UserInfo', 'RoleType', 'HasUserInfo', 'ColumnType', 'UserId'])
    },
    data() {
        return {
            isMobile: false,
            where: { UserId: store.getters.UserId },
            searchForm: {},
            dataColum: [
                { key: 'Id', hidden: true },
                { key: 'Title', title: '标题', type: store.getters.ColumnType.SHORTTEXT },
                { key: 'Status', title: '状态', type: store.getters.ColumnType.USERDEFINED },
                { key: 'Reply', title: '管理员回复', width: '220px', type: store.getters.ColumnType.SHORTTEXT },
                { key: 'CreationTime', title: '提交时间', type: store.getters.ColumnType.DATETIME },
                { key: 'ReplyTime', title: '回复时间', type: store.getters.ColumnType.DATETIME },
                { title: '操作', width: '150px', key: 'Operate', type: store.getters.ColumnType.USERDEFINED },
            ],
            createFormRules: {
                Title: [{ required: true, message: '该项为必填项', trigger: 'blur' }],
                Content: [{ required: true, message: '该项为必填项', trigger: 'blur' }],
            },
            createForm: {},
            createShow: false,
            detailData: {},
            detailShow: false,
        }
    },
    created() {
        this.UpdateIsMobile();
        window.addEventListener('resize', this.UpdateIsMobile);
    },
    beforeDestroy() {
        window.removeEventListener('resize', this.UpdateIsMobile);
    },
    methods: {
        UpdateIsMobile() {
            this.isMobile = window.matchMedia && window.matchMedia('(max-width: 768px)').matches;
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
                        this.$refs.PaginationTableId.Reload(this.searchForm);
                    }
                }
            })
        },
        ShowDetailModal(row) {
            this.detailData = row;
            this.detailShow = true;
        },
        async SearchClick() {
            this.$refs.PaginationTableId.Reload(this.searchForm);
        },
        async ResetClick() {
            this.searchForm = {};
            this.$refs.PaginationTableId.Reload(this.searchForm);
        },
    }
}
</script>

<style scoped>
.feedback-detail h3 {
    margin-bottom: 12px;
    color: #303133;
}

.detail-content {
    color: #606266;
    line-height: 1.8;
    white-space: pre-wrap;
}

.detail-reply {
    color: #409EFF;
    line-height: 1.8;
    background: #f0f9ff;
    padding: 12px;
    border-radius: 4px;
    white-space: pre-wrap;
}

.detail-time {
    color: #909399;
    font-size: 13px;
    margin-top: 8px;
}

.no-reply {
    color: #909399;
    font-style: italic;
}
</style>
