<template>
    <div class="app-container">
        <el-card class="box-card">
            <div slot="header" class="clearfix">
                <el-button type="primary" size="mini" icon="el-icon-search" @click="SearchClick">搜 索</el-button>
                <el-button type="warning" size="mini" @click="ResetClick" icon="el-icon-s-promotion">重 置</el-button>
            </div>
            <div class="tb-body">
                <el-form ref="searchFormRef" :model="searchForm" :inline="true" label-width="120px" :style="`flex:1;`">
                    <el-form-item label="标题" prop="TitleLike">
                        <el-input v-model.trim="searchForm.TitleLike" placeholder="请输入标题" :clearable="true"></el-input>
                    </el-form-item>
                    <el-form-item label="状态" prop="Status">
                        <SigleSelect url="/Select/FeedbackStatusEnum" columnName="Name" columnValue="Value"
                            v-model="searchForm.Status" :clearable="true" placeholder="请选择状态">
                        </SigleSelect>
                    </el-form-item>
                </el-form>
            </div>
        </el-card>

        <el-dialog title="回复反馈" :visible.sync="replyShow" width="60%" :lock-scroll="true">
            <el-form v-if="replyShow == true" ref="replyFormRef" :rules="replyFormRules" :model="replyForm"
                label-width="100px" size="small">
                <el-form-item label="反馈标题">
                    <el-input :value="replyForm.Title" disabled></el-input>
                </el-form-item>
                <el-form-item label="反馈内容">
                    <el-input type="textarea" :rows="4" :value="replyForm.Content" disabled></el-input>
                </el-form-item>
                <el-form-item label="回复内容" prop="Reply">
                    <el-input type="textarea" :rows="6" v-model.trim="replyForm.Reply" placeholder="请输入回复内容"></el-input>
                </el-form-item>
                <el-form-item>
                    <el-button type="primary" @click="SubmitReply()">提 交</el-button>
                    <el-button @click="replyShow = false">取 消</el-button>
                </el-form-item>
            </el-form>
        </el-dialog>

        <PaginationTable ref="PaginationTableId" url="/Feedback/List" :column="dataColum" :where="where">
            <template v-slot:header>
                <el-button type="danger" size="mini" icon="el-icon-delete" @click="ShowBatchDeleteModal()">批 量 删 除</el-button>
            </template>
            <template v-slot:Status="scope">
                <el-tag v-if="scope.row.Status == 1" type="warning">待处理</el-tag>
                <el-tag v-else-if="scope.row.Status == 2" type="success">已处理</el-tag>
                <el-tag v-else-if="scope.row.Status == 3" type="info">已关闭</el-tag>
            </template>
            <template v-slot:Operate="scope">
                <el-button type="success" size="mini" @click="ShowReplyModal(scope.row)">回 复</el-button>
                <el-button type="danger" size="mini" @click="ShowDeleteModal(scope.row.Id)">删 除</el-button>
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
            where: {},
            searchForm: {},
            dataColum: [
                { key: 'Id', hidden: true },
                { key: 'UserDto.Name', title: '反馈用户', type: store.getters.ColumnType.SHORTTEXT },
                { key: 'Title', title: '标题', type: store.getters.ColumnType.SHORTTEXT },
                { key: 'Content', title: '内容', width: '220px', type: store.getters.ColumnType.SHORTTEXT },
                { key: 'Status', title: '状态', type: store.getters.ColumnType.USERDEFINED },
                { key: 'Reply', title: '回复', width: '200px', type: store.getters.ColumnType.SHORTTEXT },
                { key: 'CreationTime', title: '提交时间', type: store.getters.ColumnType.DATETIME },
                { key: 'ReplyTime', title: '回复时间', type: store.getters.ColumnType.DATETIME },
                { title: '操作', width: '300px', key: 'Operate', type: store.getters.ColumnType.USERDEFINED },
            ],
            replyFormRules: {
                Reply: [{ required: true, message: '该项为必填项', trigger: 'blur' }],
            },
            replyForm: {},
            replyShow: false,
        }
    },
    methods: {
        ShowReplyModal(row) {
            this.replyForm = {
                Id: row.Id,
                Title: row.Title,
                Content: row.Content,
                Reply: row.Reply || '',
            };
            this.replyShow = true;
        },
        async SubmitReply() {
            this.$refs.replyFormRef.validate(async valid => {
                if (valid) {
                    const { Success } = await this.$Post('/Feedback/Reply', {
                        Id: this.replyForm.Id,
                        Reply: this.replyForm.Reply,
                    });
                    if (Success) {
                        this.replyShow = false;
                        this.$refs.PaginationTableId.Reload(this.searchForm);
                    }
                }
            })
        },
        async ShowDeleteModal(Id) {
            await this.$PostDelete('/Feedback/Delete', { Id: Id });
            this.$refs.PaginationTableId.Reload(this.searchForm);
        },
        async ShowBatchDeleteModal() {
            let ids = this.$refs.PaginationTableId.GetSelectionRow().map(x => x.Id);
            if (ids.length == 0) {
                this.$message.error('你选择需要删除的记录');
                return;
            }
            await this.$PostDelete('/Feedback/BatchDelete', { Ids: ids });
            this.$refs.PaginationTableId.Reload(this.searchForm);
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
