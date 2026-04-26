<template>
    <el-select class="single-select" :disabled="disabled" v-model="selectValue" filterable placeholder="请选择"
        :loading="loading" :clearable="true" :remote="true" @change="Change">
        <el-option v-for="item in options" :key="item.value" :label="item.name" :value="item.value">
            <div class="option-row">
                <span class="option-name">{{ item.name }}</span>
                <span class="option-label">{{ item.label }}</span>
            </div>
        </el-option>
    </el-select>
</template>

<script>
export default {

    props: {

        value: {
            type: [Number, String],
            default: ''
        },
        url: {
            type: String, //默认的请求路径
            default: "",
        },
        columnName: {
            type: String, //需要绑定Name的值
            default: "",
        },
        columnValue: {
            type: String, //需要绑定Value的值
            default: "",
        },
        columnLabel: {
            type: String, //需要绑定Label的值
            default: "",
        },
        where: {},
        filterValue: {
            type: Array, //需要绑定Value的值
            default: () => []
        },
        disabled: {
            type: Boolean,
            default: false
        },


    },
    watch: {
        "value": {
            immediate: true, //该回调将会在侦听开始之后被立即调用
            handler: function (n) {
                if (n?.toString()?.length > 0) {
                    this.selectValue = n.toString();
                }
                else {
                    this.selectValue = n;
                }
            }
        }
    },
    data() {
        return {
            loading: false, //是否正在获取数据中
            options: [],
            where_: {},//默认
            searchWhere: {},//搜索带过来的条件
            selectValue: undefined,

        }
    },
    created() {


        if (this.$props.value?.toString()?.length > 0) {
            this.selectValue = this.$props.value.toString();
        }
        this.where_ = this.$props.where;
        this.FetchDataList();

    },
    methods: {

        async Reload(where) {

            var searchWhere = {};
            if (where) {
                searchWhere = Object.assign(searchWhere, where);
            }
            this.searchWhere = searchWhere;
            this.FetchDataList(searchWhere);
        },
        async FetchDataList(searchWhere = {}) {
            this.loading = true;
            let {
                Data: {
                    Items,

                }
            } = await this.$Post(this.$props.url, {
                Limit: 999,
                ...this.where_,
                ...searchWhere
            });
            this.loading = false;
            let dataList = [];
            Items.forEach((item) => {
                if (this.filterValue.find(x => x == item[`${this.columnValue}`]?.toString()) == null) {
                    dataList.push({
                        name: item[`${this.columnName}`],
                        value: item[`${this.columnValue}`]?.toString(),
                        label: item[`${this.columnLabel}`]?.toString(),
                    });

                }

            });

            if (this.filterValue.filter(x => x.toString() == this.selectValue).length > 0) {
                this.selectValue = undefined;
            }

            this.options = dataList;
        },

        Change(value) {
            this.$emit('input', value);
            this.$emit("Change", value);
        },

    }
}

</script>

<style scoped>
.single-select {
    width: 100%;
}

.single-select /deep/ .el-input__inner {
    border-color: var(--lib-border);
    background: var(--lib-bg-surface);
    color: var(--lib-text-primary);
    border-radius: var(--lib-radius-md);
    transition: border-color .2s ease, box-shadow .2s ease;
}

.single-select /deep/ .el-input__inner:hover {
    border-color: color-mix(in srgb, var(--lib-accent) 35%, var(--lib-border) 65%);
}

.single-select /deep/ .el-input.is-focus .el-input__inner,
.single-select /deep/ .el-input__inner:focus {
    border-color: var(--lib-accent);
    box-shadow: 0 0 0 2px color-mix(in srgb, var(--lib-accent) 18%, transparent 82%);
}

.option-row {
    display: flex;
    justify-content: space-between;
    align-items: center;
    gap: var(--lib-space-sm);
}

.option-name {
    color: var(--lib-text-primary);
}

.option-label {
    color: var(--lib-text-secondary);
    font-size: 13px;
}
</style>
