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
                        <el-select v-model="searchForm.Status" placeholder="请选择状态" clearable>
                            <el-option label="草稿" value="草稿"></el-option>
                            <el-option label="已发布" value="已发布"></el-option>
                        </el-select>
                    </el-form-item>
                </el-form>
            </div>
        </el-card>

        <el-dialog :title="formData.Id ? '修改公告' : '新增公告'" :visible.sync="editorShow" width="60%" :lock-scroll="true">
            <el-form v-if="editorShow == true" ref="editModalForm" :rules="editModalFormRules" :model="formData" label-width="120px" size="mini">
                <el-row :gutter="10" class="EditFromBody">
                    <el-col :span="24">
                        <el-form-item label="标题" prop="Title">
                            <el-input type="text" v-model.trim="formData.Title" placeholder="请输入标题" :clearable="true"></el-input>
                        </el-form-item>
                    </el-col>
                    <el-col :span="24">
                        <el-form-item label="封面" prop="Cover">
                            <UploadImages :limit="1" v-model="formData.Cover"></UploadImages>
                        </el-form-item>
                    </el-col>
                    <el-col :span="24">
                        <el-form-item label="摘要" prop="Summary">
                            <el-input type="textarea" :rows="3" v-model.trim="formData.Summary" placeholder="请输入摘要"></el-input>
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="状态" prop="Status">
                            <el-select v-model="formData.Status" placeholder="请选择状态">
                                <el-option label="草稿" value="草稿"></el-option>
                                <el-option label="已发布" value="已发布"></el-option>
                            </el-select>
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="是否置顶" prop="IsTop">
                            <el-switch v-model="formData.IsTop" :active-value="1" :inactive-value="0"></el-switch>
                        </el-form-item>
                    </el-col>
                    <el-col :span="24" v-if="formData.Status == '已发布'">
                        <el-form-item label="发布时间" prop="PublishTime">
                            <el-date-picker v-model="formData.PublishTime" type="datetime" value-format="yyyy-MM-dd HH:mm:ss" placeholder="请选择发布时间"></el-date-picker>
                        </el-form-item>
                    </el-col>
                    <el-col :span="24">
                        <el-form-item label="正文" prop="Content">
                            <QillRichText v-model="formData.Content"></QillRichText>
                        </el-form-item>
                    </el-col>
                </el-row>

                <el-row type="flex" justify="end" align="bottom">
                    <el-form-item>
                        <el-button type="primary" plain @click="CreateOrEditForm()">确 定</el-button>
                        <el-button @click="editorShow = false">取 消</el-button>
                    </el-form-item>
                </el-row>
            </el-form>
        </el-dialog>

        <PaginationTable ref="PaginationTableId" url="/Announcement/List" :column="dataColum" :where="where">
            <template v-slot:header>
                <el-button type="primary" size="mini" plain icon="el-icon-edit" @click="ShowEditModal()">新 增</el-button>
                <el-button type="danger" size="mini" icon="el-icon-delete" @click="ShowBatchDeleteModal()">批 量 删 除</el-button>
            </template>
            <template v-slot:IsTop="scope">
                <el-tag v-if="scope.row.IsTop == 1" type="danger">置顶</el-tag>
                <el-tag v-else type="info">普通</el-tag>
            </template>
            <template v-slot:Status="scope">
                <el-tag v-if="scope.row.Status == '已发布'" type="success">已发布</el-tag>
                <el-tag v-else type="warning">草稿</el-tag>
            </template>
            <template v-slot:Operate="scope">
                <el-button type="primary" size="mini" @click="ShowEditModal(scope.row.Id)">修 改</el-button>
                <el-button type="danger" size="mini" @click="ShowDeleteModal(scope.row.Id)">删 除</el-button>
            </template>
        </PaginationTable>
    </div>
</template>

<script>
import store from '@/store';
import { mapGetters } from 'vuex'

export default {
    name: "AnnouncementList",
    computed: {
        ...mapGetters(['Token', 'UserInfo', 'RoleType', 'HasUserInfo', 'ColumnType', 'UserId'])
    },
    data() {
        return {
            where: {},
            searchForm: {},
            dataColum: [
                { key: 'Id', hidden: true },
                { key: 'Title', title: '标题', type: store.getters.ColumnType.SHORTTEXT },
                { key: 'Cover', title: '封面', type: store.getters.ColumnType.IMAGES },
                { key: 'Summary', title: '摘要', width: '220px', type: store.getters.ColumnType.SHORTTEXT },
                { key: 'IsTop', title: '置顶', type: store.getters.ColumnType.USERDEFINED },
                { key: 'Status', title: '状态', type: store.getters.ColumnType.USERDEFINED },
                { key: 'PublishTime', title: '发布时间', type: store.getters.ColumnType.DATETIME },
                { title: '操作', width: '300px', key: 'Operate', type: store.getters.ColumnType.USERDEFINED },
            ],
            editModalFormRules: {
                Title: [{ required: true, message: '该项为必填项', trigger: 'blur' }],
                Summary: [{ required: true, message: '该项为必填项', trigger: 'blur' }],
                Content: [{ required: true, message: '该项为必填项', trigger: 'blur' }],
                Status: [{ required: true, message: '该项为必填项', trigger: 'change' }],
            },
            formData: {},
            editorShow: false,
        }
    },
    methods: {
        async ShowEditModal(Id) {
            let { Data } = await this.$Post('/Announcement/Get', { Id: Id });
            this.formData = Data;
            if (!this.formData.Status) {
                this.formData.Status = '草稿';
            }
            if (this.formData.IsTop === undefined || this.formData.IsTop === null) {
                this.formData.IsTop = 0;
            }
            this.editorShow = true;
        },
        async CreateOrEditForm() {
            this.$refs.editModalForm.validate(async valid => {
                if (valid) {
                    if (this.formData.Status !== '已发布') {
                        this.formData.PublishTime = null;
                    }
                    const { Success } = await this.$Post('/Announcement/CreateOrEdit', this.formData);
                    if (Success) {
                        this.editorShow = false;
                        this.$refs.PaginationTableId.Reload(this.searchForm);
                    }
                }
            })
        },
        async ShowDeleteModal(Id) {
            await this.$PostDelete('/Announcement/Delete', { Id: Id });
            this.$refs.PaginationTableId.Reload(this.searchForm);
        },
        async ShowBatchDeleteModal() {
            let ids = this.$refs.PaginationTableId.GetSelectionRow().map(x => x.Id);
            if (ids.length == 0) {
                this.$message.error('你选择需要删除的记录');
                return;
            }
            await this.$PostDelete('/Announcement/BatchDelete', { Ids: ids });
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
