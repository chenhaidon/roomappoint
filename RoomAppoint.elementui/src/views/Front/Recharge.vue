<template>
    <div>
        <el-page-header class="card margin-top-lg" @back="goBack" content="余额充值">
        </el-page-header>

        <el-card class="margin-top-lg">
            <div class="recharge-info">
                <div class="current-balance">
                    <span class="label">当前余额：</span>
                    <span class="value">¥ {{ balance }}</span>
                </div>
            </div>

            <el-divider></el-divider>

            <div class="recharge-form">
                <div class="amount-label">充值金额（元）</div>
                <div class="quick-amounts">
                    <el-button v-for="item in quickAmounts" :key="item"
                        :type="amount === item ? 'primary' : 'default'"
                        @click="amount = item" class="amount-btn">
                        {{ item }}元
                    </el-button>
                </div>
                <el-input v-model.number="amount" type="number" placeholder="或输入自定义金额" min="1"
                    class="custom-amount">
                    <template slot="append">元</template>
                </el-input>

                <el-button type="primary" class="submit-btn" :loading="loading" @click="handleRecharge">
                    立即充值
                </el-button>
            </div>
        </el-card>
    </div>
</template>

<script>
import store from '@/store';
import { mapGetters } from 'vuex';

export default {
    name: "Recharge",
    computed: {
        ...mapGetters(['Token'])
    },
    data() {
        return {
            balance: '0.00',
            amount: 50,
            quickAmounts: [10, 20, 50, 100, 200, 500],
            loading: false,
        }
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
        async handleRecharge() {
            if (!this.amount || this.amount < 1) {
                this.$message.warning('充值金额不能小于1元');
                return;
            }
            this.loading = true;
            try {
                const res = await this.$Post('/Pay/CreateOrder', { Amount: this.amount });
                if (res && res.Success) {
                    // 后端返回支付宝签名表单 HTML，直接写入新窗口提交
                    const newWin = window.open('', '_blank');
                    if (newWin) {
                        newWin.document.write(res.Data);
                        newWin.document.close();
                    } else {
                        this.$message.warning('浏览器拦截了弹窗，请允许弹窗后重试');
                    }
                } else {
                    this.$message.error((res && res.Msg) || '创建订单失败');
                }
            } catch (e) {
                this.$message.error('创建订单失败');
            } finally {
                this.loading = false;
            }
        },
        goBack() {
            this.$router.go(-1);
        }
    }
}
</script>

<style scoped>
.recharge-info {
    padding: var(--lib-space-md) 0;
}

.current-balance {
    font-size: 18px;
}

.current-balance .label {
    color: var(--lib-text-secondary);
}

.current-balance .value {
    font-weight: 700;
    font-size: 28px;
    color: var(--lib-primary);
}

.amount-label {
    font-weight: 600;
    margin-bottom: var(--lib-space-sm);
    color: var(--lib-text-primary);
}

.quick-amounts {
    display: flex;
    flex-wrap: wrap;
    gap: 10px;
    margin-bottom: var(--lib-space-md);
}

.amount-btn {
    min-width: 80px;
}

.custom-amount {
    margin-bottom: var(--lib-space-lg);
}

.submit-btn {
    width: 100%;
    height: 44px;
    font-size: 16px;
}
</style>
