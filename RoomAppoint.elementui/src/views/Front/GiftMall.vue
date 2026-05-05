<template>
    <div class="app-container">
        <el-page-header class="card margin-top-lg" @back="$router.go(-1)">
            <template slot="content">
                <span>积分兑换礼品</span>
            </template>
        </el-page-header>

        <div class="card margin-top-lg mall-toolbar">
            <div class="toolbar-left">
                <el-button type="text" @click="$router.push('/Front/GiftRedeemMyList')">查看我的兑换记录</el-button>
            </div>
            <div class="toolbar-right">
                <el-input v-model.trim="searchForm.NameLike" placeholder="搜索礼品名称" clearable size="mini" class="search-input" />
                <el-button type="primary" size="mini" icon="el-icon-search" @click="SearchClick">搜 索</el-button>
                <el-button type="warning" size="mini" icon="el-icon-s-promotion" @click="ResetClick">重 置</el-button>
            </div>
        </div>

        <PaginationTable ref="PaginationTableId" url="/Gift/FrontList" :column="dataColum" :where="where">
            <template v-slot:Operate="scope">
                <el-button type="primary" size="mini" @click="Redeem(scope.row)">立即兑换</el-button>
            </template>
            <template v-slot:Cover="scope">
                <el-image v-if="scope.row.Cover" :src="NormalizeImage(scope.row.Cover)" class="gift-cover" fit="cover">
                    <div slot="error" class="image-slot">
                        <i class="el-icon-picture-outline"></i>
                    </div>
                </el-image>
            </template>
            <template v-slot:Summary="scope">
                <span class="text-cut">{{ scope.row.Summary }}</span>
            </template>
        </PaginationTable>
    </div>
</template>

<script>
import store from '@/store'
import { mapGetters } from 'vuex'
import { ReplaceImageHttp } from '@/utils/comm'

export default {
    name: 'GiftMall',
    computed: {
        ...mapGetters([
            'Token', 'UserInfo', 'RoleType', 'HasUserInfo', 'ColumnType', 'UserId'
        ])
    },
    data() {
        return {
            where: {},
            searchForm: {},
            dataColum: [
                { key: 'Id', hidden: true },
                { key: 'Cover', title: '封面', type: store.getters.ColumnType.USERDEFINED, width: '120px' },
                { key: 'Name', title: '礼品名称', type: store.getters.ColumnType.SHORTTEXT },
                { key: 'NeedIntegral', title: '所需积分', type: store.getters.ColumnType.SHORTTEXT, width: '120px' },
                { key: 'Stock', title: '库存', type: store.getters.ColumnType.SHORTTEXT, width: '100px' },
                { key: 'Summary', title: '简介', type: store.getters.ColumnType.USERDEFINED },
                { title: '操作', width: '160px', key: 'Operate', type: store.getters.ColumnType.USERDEFINED }
            ]
        }
    },
    created() {
        if (!this.Token) {
            this.$router.push('/Login')
        }
    },
    methods: {
        NormalizeImage(value) {
            if (!value) {
                return ''
            }
            let raw = Array.isArray(value) ? value[0] : String(value)
            raw = raw.trim()
            if (raw.startsWith('[') && raw.endsWith(']')) {
                try {
                    const arr = JSON.parse(raw)
                    raw = Array.isArray(arr) ? (arr[0] || '') : raw
                } catch (e) {
                }
            }
            raw = String(raw).split(',')[0].trim().replace(/^['\"]|['\"]$/g, '')
            return ReplaceImageHttp(raw)
        },
        async SearchClick() {
            this.$refs.PaginationTableId.Reload(this.searchForm)
        },
        async ResetClick() {
            this.searchForm = {}
            this.$refs.PaginationTableId.Reload(this.searchForm)
        },
        async Redeem(row) {
            const confirm = await this.$comm.ConfirmMessageBox({ content: `确定要兑换「${row.Name}」吗？` })
            if (!confirm) {
                return
            }
            const { Success } = await this.$Post('/GiftRedeem/Redeem', { Id: row.Id })
            if (Success) {
                this.$message.success('兑换成功')
                this.$refs.PaginationTableId.Reload(this.searchForm)
            }
        }
    }
}
</script>

<style scoped>
.mall-toolbar {
    padding: var(--lib-space-sm) var(--lib-space-md);
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: var(--lib-space-md);
}

.toolbar-right {
    display: flex;
    align-items: center;
    gap: var(--lib-space-sm);
}

.search-input {
    width: 240px;
}

.gift-cover {
    width: 90px;
    height: 60px;
    border-radius: 8px;
}

.text-cut {
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
}

@media (max-width: 768px) {
    .mall-toolbar {
        flex-direction: column;
        align-items: stretch;
    }

    .toolbar-right {
        flex-wrap: wrap;
        justify-content: flex-start;
    }

    .search-input {
        width: 100%;
    }

    .gift-cover {
        width: 64px;
        height: 44px;
    }
}
</style>
