<template>
    <div class="app-container">
        <el-card class="box-card">
            <div slot="header" class="clearfix" style="display:flex;align-items:center;justify-content:space-between;">
                <span>运维</span>
                <div>
                    <el-button size="mini" type="primary" icon="el-icon-refresh" :loading="loading" @click="refresh">
                        手动刷新
                    </el-button>
                </div>
            </div>

            <el-row :gutter="12">
                <el-col :span="8">
                    <el-card shadow="never">
                        <div class="ops-title">Backend API</div>
                        <div class="ops-status" :class="statusClass(data && data.Backend)">
                            {{ statusText(data && data.Backend) }}
                        </div>
                        <div class="ops-meta" v-if="data && data.Backend && data.Backend.Message">
                            {{ data.Backend.Message }}
                        </div>
                    </el-card>
                </el-col>

                <el-col :span="8">
                    <el-card shadow="never">
                        <div class="ops-title">MySQL</div>
                        <div class="ops-status" :class="statusClass(data && data.Mysql)">
                            {{ statusText(data && data.Mysql) }}
                        </div>
                        <div class="ops-meta" v-if="data && data.Mysql && data.Mysql.Message">
                            {{ data.Mysql.Message }}
                        </div>
                    </el-card>
                </el-col>

                <el-col :span="8">
                    <el-card shadow="never">
                        <div class="ops-title">Nginx</div>
                        <div class="ops-status" :class="statusClass(data && data.Nginx)">
                            {{ statusText(data && data.Nginx) }}
                            <span v-if="data && data.Nginx && data.Nginx.HttpStatus" class="ops-pill">
                                HTTP {{ data.Nginx.HttpStatus }}
                            </span>
                        </div>
                        <div class="ops-meta" v-if="data && data.Nginx && data.Nginx.Message">
                            {{ data.Nginx.Message }}
                        </div>
                    </el-card>
                </el-col>
            </el-row>

            <div class="ops-footer" v-if="data">
                <div>服务端时间：{{ data.Now }}</div>
                <div>运行时长：{{ data.UptimeSeconds }} 秒</div>
                <div>最后刷新：{{ lastRefreshedAt || '-' }}</div>
                <div>自动刷新：每 5 秒</div>
            </div>
        </el-card>
    </div>
</template>

<script>
export default {
    name: "Ops",
    data() {
        return {
            data: null,
            loading: false,
            timer: null,
            lastRefreshedAt: "",
        }
    },
    mounted() {
        this.refresh();
        this.timer = setInterval(() => {
            this.refresh();
        }, 5000);
    },
    beforeDestroy() {
        if (this.timer) {
            clearInterval(this.timer);
            this.timer = null;
        }
    },
    methods: {
        okValue(s) {
            if (!s) return undefined;
            // 后端 JSON 命名策略可能把 ok 序列化为 Ok 或 OK
            return (s.Ok ?? s.OK);
        },
        statusClass(s) {
            const ok = this.okValue(s);
            if (ok === undefined) return "ops-unknown";
            return ok ? "ops-ok" : "ops-bad";
        },
        statusText(s) {
            const ok = this.okValue(s);
            if (ok === undefined) return "未知";
            return ok ? "正常" : "异常";
        },
        async refresh() {
            if (this.loading) return;
            this.loading = true;
            try {
                let res = await this.$Post("/Ops/Status", {});
                if (res && res.Success) {
                    this.data = res.Data;
                    if (process.env.NODE_ENV !== 'production') {
                        console.log('[Ops] status payload:', this.data);
                    }
                    this.lastRefreshedAt = new Date().toLocaleString();
                }
            } finally {
                this.loading = false;
            }
        }
    }
}
</script>

<style scoped>
.ops-title {
    font-weight: 600;
    margin-bottom: 10px;
}

.ops-status {
    font-size: 18px;
    font-weight: 700;
    display: flex;
    align-items: center;
}

.ops-pill {
    margin-left: 10px;
    padding: 2px 8px;
    border-radius: 12px;
    font-size: 12px;
    font-weight: 600;
    background: #f2f6fc;
    color: #606266;
}

.ops-meta {
    margin-top: 8px;
    color: #909399;
    word-break: break-all;
}

.ops-ok {
    color: #67C23A;
}

.ops-bad {
    color: #F56C6C;
}

.ops-unknown {
    color: #909399;
}

.ops-footer {
    margin-top: 16px;
    color: #606266;
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
    row-gap: 6px;
}
</style>
