<template>
  <div class="app-container">
    <el-card class="box-card">
      <div slot="header" class="clearfix">
        <el-button
          type="primary"
          size="mini"
          icon="el-icon-search"
          @click="SearchClick"
          >搜 索</el-button
        >
        <el-button
          type="warning"
          size="mini"
          icon="el-icon-s-promotion"
          @click="ResetClick"
          >重 置</el-button
        >
      </div>

      <div class="tb-body">
        <el-form
          ref="searchFormRef"
          :model="searchForm"
          :inline="true"
          label-width="120px"
          :style="`flex:1;`"
        >
          <el-form-item label="礼品名称" prop="NameLike">
            <el-input
              v-model.trim="searchForm.NameLike"
              placeholder="请输入礼品名称"
              :clearable="true"
            />
          </el-form-item>

          <el-form-item label="状态" prop="Status">
            <el-select
              v-model="searchForm.Status"
              placeholder="请选择状态"
              clearable
            >
              <el-option label="上架" value="上架" />
              <el-option label="下架" value="下架" />
            </el-select>
          </el-form-item>
        </el-form>
      </div>
    </el-card>

    <el-dialog
      :title="formData.Id ? '修改礼品' : '添加礼品'"
      :visible.sync="editorShow"
      width="55%"
      :lock-scroll="true"
    >
      <el-form
        v-if="editorShow === true"
        ref="editModalForm"
        :rules="editModalFormRules"
        :model="formData"
        label-width="140px"
        size="mini"
      >
        <el-row :gutter="10" class="EditFromBody">
          <el-col :span="24">
            <el-form-item label="封面" prop="Cover">
              <UploadImages :limit="1" v-model="formData.Cover" />
            </el-form-item>
          </el-col>

          <el-col :span="24">
            <el-form-item label="礼品名称" prop="Name">
              <el-input
                v-model.trim="formData.Name"
                placeholder="请输入礼品名称"
                :clearable="true"
              />
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="所需积分" prop="NeedIntegral">
              <el-input
                type="number"
                v-model.number="formData.NeedIntegral"
                placeholder="请输入所需积分"
                :clearable="true"
              />
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="库存" prop="Stock">
              <el-input
                type="number"
                v-model.number="formData.Stock"
                placeholder="请输入库存"
                :clearable="true"
              />
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="状态" prop="Status">
              <el-select v-model="formData.Status" placeholder="请选择状态">
                <el-option label="上架" value="上架" />
                <el-option label="下架" value="下架" />
              </el-select>
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="排序" prop="Sort">
              <el-input
                type="number"
                v-model.number="formData.Sort"
                placeholder="默认 0"
                :clearable="true"
              />
            </el-form-item>
          </el-col>

          <el-col :span="24">
            <el-form-item label="简介" prop="Summary">
              <el-input
                type="textarea"
                :rows="3"
                v-model.trim="formData.Summary"
                placeholder="请输入简介（可选）"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row type="flex" justify="end" align="bottom">
          <el-form-item>
            <el-button type="primary" plain @click="CreateOrEditForm()"
              >确 定</el-button
            >
            <el-button @click="editorShow = false">取 消</el-button>
          </el-form-item>
        </el-row>
      </el-form>
    </el-dialog>

    <PaginationTable
      ref="PaginationTableId"
      url="/Gift/List"
      :column="dataColum"
      :where="where"
    >
      <template v-slot:header>
        <el-button
          type="primary"
          size="mini"
          plain
          icon="el-icon-edit"
          @click="ShowEditModal()"
          >新 增</el-button
        >
        <el-button
          type="danger"
          size="mini"
          icon="el-icon-delete"
          @click="ShowBatchDeleteModal()"
          >批 量 删 除</el-button
        >
      </template>

      <template v-slot:Operate="scope">
        <el-button
          type="primary"
          size="mini"
          @click="ShowEditModal(scope.row.Id)"
          >修 改</el-button
        >
        <el-button
          type="danger"
          size="mini"
          @click="ShowDeleteModal(scope.row.Id)"
          >删 除</el-button
        >
      </template>

      <template v-slot:Summary="scope">
        <span class="text-cut">{{ scope.row.Summary || "-" }}</span>
      </template>
    </PaginationTable>
  </div>
</template>

<script>
import store from "@/store";
import { mapGetters } from "vuex";

export default {
  name: "GiftList",
  computed: {
    ...mapGetters([
      "Token",
      "UserInfo",
      "RoleType",
      "HasUserInfo",
      "ColumnType",
      "UserId",
    ]),
  },
  data() {
    return {
      where: {},
      searchForm: {},
      dataColum: [
        { key: "Id", hidden: true },
        {
          key: "Cover",
          title: "封面",
          type: store.getters.ColumnType.IMAGES,
          width: "120px",
        },
        {
          key: "Name",
          title: "礼品名称",
          type: store.getters.ColumnType.SHORTTEXT,
        },
        {
          key: "NeedIntegral",
          title: "所需积分",
          type: store.getters.ColumnType.SHORTTEXT,
          width: "120px",
        },
        {
          key: "Stock",
          title: "库存",
          type: store.getters.ColumnType.SHORTTEXT,
          width: "100px",
        },
        {
          key: "Status",
          title: "状态",
          type: store.getters.ColumnType.SHORTTEXT,
          width: "100px",
        },
        {
          key: "Sort",
          title: "排序",
          type: store.getters.ColumnType.SHORTTEXT,
          width: "80px",
        },
        {
          key: "Summary",
          title: "简介",
          type: store.getters.ColumnType.USERDEFINED,
        },
        {
          title: "操作",
          width: "220px",
          key: "Operate",
          type: store.getters.ColumnType.USERDEFINED,
        },
      ],
      editModalFormRules: {
        Name: [{ required: true, message: "该项为必填项", trigger: "blur" }],
        NeedIntegral: [
          { required: true, message: "该项为必填项", trigger: "blur" },
        ],
        Stock: [{ required: true, message: "该项为必填项", trigger: "blur" }],
        Status: [
          { required: true, message: "该项为必填项", trigger: "change" },
        ],
      },
      formData: {},
      editorShow: false,
    };
  },
  methods: {
    async ShowEditModal(Id) {
      let { Data } = await this.$Post("/Gift/Get", { Id: Id });
      this.formData = Data || {};
      this.editorShow = true;
    },
    async CreateOrEditForm() {
      this.$refs.editModalForm.validate(async (valid) => {
        if (!valid) return;
        const { Success } = await this.$Post(
          "/Gift/CreateOrEdit",
          this.formData,
        );
        if (Success) {
          this.editorShow = false;
          this.$refs.PaginationTableId.Reload(this.searchForm);
        }
      });
    },
    async ShowDeleteModal(Id) {
      await this.$PostDelete("/Gift/Delete", { Id: Id });
      this.$refs.PaginationTableId.Reload(this.searchForm);
    },
    async ShowBatchDeleteModal() {
      const ids = this.$refs.PaginationTableId.GetSelectionRow().map(
        (x) => x.Id,
      );
      if (ids.length === 0) {
        this.$message.error("你选择需要删除的记录");
        return;
      }
      await this.$PostDelete("/Gift/BatchDelete", { Ids: ids });
      this.$refs.PaginationTableId.Reload(this.searchForm);
    },
    async SearchClick() {
      this.$refs.PaginationTableId.Reload(this.searchForm);
    },
    async ResetClick() {
      this.searchForm = {};
      this.$refs.PaginationTableId.Reload(this.searchForm);
    },
  },
};
</script>

<style scoped>
.text-cut {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>
