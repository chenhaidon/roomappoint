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
          <el-form-item label="用户" prop="UserId">
            <SigleSelect
              url="/User/List"
              :where="{ RoleType: 2 }"
              columnName="Name"
              columnValue="Id"
              :clearable="true"
              v-model="searchForm.UserId"
            />
          </el-form-item>

          <el-form-item label="礼品" prop="GiftId">
            <SigleSelect
              url="/Gift/List"
              columnName="Name"
              columnValue="Id"
              :clearable="true"
              v-model="searchForm.GiftId"
            />
          </el-form-item>
        </el-form>
      </div>
    </el-card>

    <PaginationTable
      ref="PaginationTableId"
      url="/GiftRedeem/List"
      :column="dataColum"
      :where="where"
    />
  </div>
</template>

<script>
import store from "@/store";
import { mapGetters } from "vuex";

export default {
  name: "GiftRedeemList",
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
          key: "GiftDto.Name",
          title: "礼品",
          type: store.getters.ColumnType.SHORTTEXT,
        },
        {
          key: "UserDto.Name",
          title: "用户",
          type: store.getters.ColumnType.SHORTTEXT,
          width: "140px",
        },
        {
          key: "NeedIntegral",
          title: "扣减积分",
          type: store.getters.ColumnType.SHORTTEXT,
          width: "120px",
        },
        {
          key: "Title",
          title: "标题",
          type: store.getters.ColumnType.SHORTTEXT,
        },
        {
          key: "CreationTime",
          title: "时间",
          type: store.getters.ColumnType.DATE,
          width: "160px",
        },
      ],
    };
  },
  methods: {
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
