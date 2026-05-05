<template>
    <div class="app-container">
        <el-page-header class="card margin-top-lg balance-header">
            <template slot="content">
                <span class="balance-text">我的余额</span>
                <span class="balance-value">¥ {{ balance }}</span>
            </template>
        </el-page-header>

        <el-alert class="margin-top-lg" title="充值后余额更新可能有延迟，请耐心等待或刷新页面" type="info" show-icon :closable="false"></el-alert>

        <el-card class="box-card margin-top-lg">
            <div slot="header" class="clearfix">
                <el-button type="primary" size="mini" icon="el-icon-search" @click="SearchClick">搜 索</el-button>
                <el-button type="warning" size="mini" @click="ResetClick" icon="el-icon-s-promotion">重 置</el-button>
                <el-button type="success" size="mini" icon="el-icon-money" @click="$router.push('/Front/Recharge')">去充值</el-button>
            </div>
            <div class="tb-body">
                <el-form ref="searchFormRef" :model="searchForm" :inline="true" label-width="100px">
                    <el-form-item label="来源" prop="SourceLike">
                        <el-input v-model.trim="searchForm.SourceLike" placeholder="请输入来源" :clearable="true"></el-input>
                    </el-form-item>
                    <el-form-item label="标题" prop="TitleLike">
                        <el-input v-model.trim="searchForm.TitleLike" placeholder="请输入标题" :clearable="true"></el-input>
                    </el-form-item>
                </el-form>
            </div>
        </el-card>

        <PaginationTable ref="PaginationTableId" url="/Balance/RecordList" :column="dataColum" :where="where">
        </PaginationTable>
    </div>
</template>

<script>
import store from '@/store';
import { mapGetters } from 'vuex';

export default {
    name: "BalanceRecordList",
    computed: {
        ...mapGetters(['Token', 'UserId'])
    },
    data() {
        return {
            balance: '0.00',
            where: {
                UserId: store.getters.UserId
            },
            searchForm: {},
            dataColum: [
                { key: "Id", hidden: true },
                { key: "Title", title: "标题", type: store.getters.ColumnType.SHORTTEXT },
                { key: "Amount", title: "金额(元)", type: store.getters.ColumnType.SHORTTEXT },
                { key: "Source", title: "来源", type: store.getters.ColumnType.SHORTTEXT },
                { key: "RelativeCode", title: "关联号", type: store.getters.ColumnType.SHORTTEXT },
                { key: "CreationTime", title: "时间", type: store.getters.ColumnType.DATETIME, width: '160px' },
            ],
        };
    },
    created() {
        if (!this.Token) {
            this.$router.push('/Login');
            return;
        }
        this.GetBalance();
    },
    methods: {
        async GetBalance() {
            const res = await this.$Post('/Balance/GetMyBalance', {});
            if (res && res.Success) {
                this.balance = res.Data.Balance || '0.00';
            }
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
.balance-header /deep/ .el-page-header__left {
    display: none;
}

.balance-header /deep/ .el-page-header__content {
    margin-left: 0;
    display: inline-flex;
    align-items: center;
    gap: var(--lib-space-xs);
}

.balance-text {
    color: var(--lib-text-primary);
}

.balance-value {
    display: inline-block;
    min-width: 60px;
    padding: 0 10px;
    border-radius: 6px;
    background: #e53935;
    color: #fff;
    line-height: 1.8;
    font-weight: 700;
    text-align: center;
}
</style>
