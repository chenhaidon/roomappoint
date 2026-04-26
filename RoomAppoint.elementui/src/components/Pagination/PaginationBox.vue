<template>
    <div class="pagination-box">
        <div v-if="loading"><el-skeleton :rows="12" animated /></div>
        <div v-else>
            <el-empty v-if="options.length == 0" description="看官，数据正在维护中"></el-empty>
            <div v-else class="box">
                <slot name="content" :data="options"></slot>
            </div>
        </div>
        <div class="custom-pagination">
            <el-pagination background @size-change="handleSizeChange" @current-change="handleCurrentChange"
                :current-page="pagination.page" :page-sizes="pagination.pageSizes" :page-size="pagination.limit"
                layout="total, sizes, prev, pager, next, jumper" :total="pagination.total">
            </el-pagination>
        </div>
    </div>
</template>

<script>

export default {

    props: {



        url: {
            type: String, //默认的请求路径
            default: "",
        },
        where: {
            type: [Number, String, Object],
            default: ""
        },

    },
    watch: {

    },
    data() {
        return {
            loading: false, //是否正在获取数据中
            options: [],
            where_: {},
            pagination: {
                limit: 10,
                page: 1,
                total: 0,
                pageSizes: [10, 20, 50, 100],
            },
            selectOption: {},

        }
    },
    created() {
        this.where_ = this.$props.where;
        this.FetchDataList();
    },
    methods: {

        InitData() {
            this.pagination.limit = 10;
            this.pagination.page = 1;
            this.pagination.total = 0;

            this.where_ = {};
        },
        async Reload(where) {
            this.pagination.page = 1;
            this.pagination.total = 0;
            var searchWhere = {};
            if (where) {
                searchWhere = Object.assign(searchWhere, where);
            }
            this.FetchDataList(searchWhere);
        },
        async FetchDataList(searchWhere) {
            this.loading = true;
            const res = await this.$Post(this.$props.url, {
                Limit: this.pagination.limit,
                Size: this.pagination.limit,
                Page: this.pagination.page,
                ...this.where_,
                ...searchWhere
            });
            this.loading = false;

            const Items = (res && res.Success && res.Data && res.Data.Items) ? res.Data.Items : [];
            const TotalCount = (res && res.Success && res.Data && (res.Data.TotalCount !== undefined && res.Data.TotalCount !== null)) ? res.Data.TotalCount : 0;

            this.options = Items;
            this.pagination.total = TotalCount;
        },

        handleCurrentChange(event) {
            this.pagination.page = event;
            this.FetchDataList();
        },
        handleSizeChange(event) {
            this.pagination.limit = event;
            this.pagination.page = 1;

            this.FetchDataList();
        },

    }
}

</script>
<style scoped>
.pagination-box {
    margin: var(--lib-space-md) 0;
    padding: var(--lib-space-md);
    background: var(--lib-bg-surface);
    border: 1px solid var(--lib-border);
    border-radius: var(--lib-radius-lg);
    box-shadow: var(--lib-shadow-sm);
}

.box {
    width: 100%;
    display: flex;
    flex-wrap: wrap;
    box-sizing: border-box;
    gap: var(--lib-space-sm);
}

.item {
    width: 100%;
    padding: var(--lib-space-xs);
    box-sizing: border-box;
}

.content-cut {
    overflow: hidden;
    text-overflow: ellipsis;
    word-break: break-all;
    display: -webkit-box;
    -webkit-box-orient: vertical;
    -webkit-line-clamp: 4;
    line-height: 1.5;
}

.custom-pagination {
    margin-top: var(--lib-space-lg);
    padding-top: var(--lib-space-sm);
    border-top: 1px solid var(--lib-border);
    display: flex;
    flex-direction: row-reverse;
}

.custom-pagination /deep/ .el-pagination.is-background .el-pager li {
    border-radius: 8px;
}

.custom-pagination /deep/ .el-pagination__total,
.custom-pagination /deep/ .el-pagination__jump {
    color: var(--lib-text-secondary);
}
</style>


