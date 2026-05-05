<template>
    <div class="app-container">
        <el-page-header class="card margin-top-lg" @back="$router.go(-1)">
            <template slot="content">
                <span>我的兑换记录</span>
            </template>
        </el-page-header>

        <PaginationTable ref="PaginationTableId" class="margin-top-lg" url="/GiftRedeem/MyList" :column="dataColum" :where="where">
            <template v-slot:GiftName="scope">
                <span>{{ (scope.row.GiftDto && scope.row.GiftDto.Name) || '-' }}</span>
            </template>
        </PaginationTable>
    </div>
</template>

<script>
import store from '@/store'
import { mapGetters } from 'vuex'

export default {
    name: 'GiftRedeemMyList',
    computed: {
        ...mapGetters([
            'Token', 'UserInfo', 'RoleType', 'HasUserInfo', 'ColumnType', 'UserId'
        ])
    },
    data() {
        return {
            where: {},
            dataColum: [
                { key: 'Id', hidden: true },
                { key: 'GiftName', title: '礼品', type: store.getters.ColumnType.USERDEFINED },
                { key: 'NeedIntegral', title: '标价积分', type: store.getters.ColumnType.SHORTTEXT, width: '100px' },
                { key: 'DeductIntegral', title: '扣减积分', type: store.getters.ColumnType.SHORTTEXT, width: '100px' },
                { key: 'DeductBalance', title: '扣减余额', type: store.getters.ColumnType.SHORTTEXT, width: '100px' },
                { key: 'Title', title: '标题', type: store.getters.ColumnType.SHORTTEXT },
                { key: 'CreationTime', title: '时间', type: store.getters.ColumnType.DATE, width: '160px' }
            ]
        }
    },
    created() {
        if (!this.Token) {
            this.$router.push('/Login')
        }
    }
}
</script>
