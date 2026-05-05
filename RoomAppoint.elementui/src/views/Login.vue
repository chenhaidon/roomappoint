<template>
    <div class="auth-page" :class="{ 'auth-page--success': isSuccess }">
        <div class="auth-bg-shape auth-bg-shape--1"></div>
        <div class="auth-bg-shape auth-bg-shape--2"></div>

        <div class="auth-card auth-card--enter">
            <div class="auth-left auth-left--enter">
                <div class="auth-left-glow"></div>
                <div class="auth-left-content">
                    <img class="auth-logo" src="@/assets/logo.jpg">
                    <h1 class="auth-brand">
                        <span class="typewriter-text">{{ brandText }}</span><span class="typewriter-cursor">|</span>
                    </h1>
                    <p class="auth-slogan">
                        <span class="typewriter-text">{{ sloganText }}</span><span class="typewriter-cursor" v-show="showSloganCursor">|</span>
                    </p>
                    <img class="auth-hero-image" src="@/assets/登录页插图.png">
                </div>
            </div>

            <div class="auth-right">
                <div class="auth-form-wrap">
                    <div class="auth-form-header auth-form-item--enter" style="--delay: 0ms">
                        <h2 class="auth-title">开始你的专注时刻</h2>
                        <p class="auth-subtitle">把时间交给学习</p>
                    </div>

                    <el-form class="auth-form" ref="loginForm" :model="formData" label-position="top" :rules="rules">
                        <el-form-item label="账号" prop="UserName" class="auth-form-item--enter" style="--delay: 120ms">
                            <el-input type="text" v-model.trim="formData.UserName" placeholder="请输入账号"
                                prefix-icon="el-icon-user" size="large"></el-input>
                        </el-form-item>

                        <el-form-item label="密码" prop="Password" class="auth-form-item--enter" style="--delay: 180ms">
                            <el-input type="password" v-model.trim="formData.Password" placeholder="请输入密码"
                                prefix-icon="el-icon-lock" size="large" show-password></el-input>
                        </el-form-item>

                        <el-form-item label="身份" prop="RoleType" class="auth-form-item--enter" style="--delay: 240ms">
                            <el-radio-group v-model="formData.RoleType" class="auth-role-group">
                                <el-radio-button v-for="item in roleOptions" :key="item.Code" :label="item.Code">
                                    {{ item.Label }}
                                </el-radio-button>
                            </el-radio-group>
                        </el-form-item>

                        <el-form-item prop="SliderVerified" class="auth-form-item--enter" style="--delay: 300ms">
                            <SliderCode ref="SliderCode" @verified="formData.SliderVerified = true"></SliderCode>
                        </el-form-item>

                        <el-form-item class="auth-form-item--enter" style="--delay: 360ms">
                            <el-button class="auth-btn auth-btn--primary" :class="{ 'is-loading': isLoading }" type="primary" size="large" @click="LoginBtn">
                                {{ isLoading ? '登录中' : '进入自习室' }}
                            </el-button>
                        </el-form-item>

                        <el-form-item class="auth-form-item--enter" style="--delay: 420ms">
                            <el-button class="auth-btn auth-btn--ghost" size="large" @click="ToHome">
                                先随便看看
                            </el-button>
                        </el-form-item>
                    </el-form>

                    <div class="auth-footer-row auth-form-item--enter" style="--delay: 480ms">
                        <span class="auth-footer-text">还没有账号？</span>
                        <RouterLink :to="{ path: '/Register' }">
                            <span class="auth-link">立即注册</span>
                        </RouterLink>
                        <span class="auth-footer-divider">|</span>
                        <RouterLink :to="{ path: '/Register' }">
                            <span class="auth-link">忘记密码</span>
                        </RouterLink>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script>
import SliderCode from '@/components/Code/SliderCode.vue';
import store from '@/store';
export default {
    components: {
        SliderCode: SliderCode
    },
    data() {
        return {
            brandText: '',
            sloganText: '',
            showSloganCursor: false,
            isLoading: false,
            isSuccess: false,
            formData: {
                UserName: '',
                Password: '',
                RoleType: "",
                SliderVerified: false
            },
            roleOptions: [],
            rules: {
                UserName: [
                    { required: true, message: '请输入账号', trigger: 'blur' },
                ],
                Password: [
                    { required: true, message: '请输入密码', trigger: 'blur' },
                ],
                RoleType: [
                    { required: true, message: '请选择角色', trigger: 'blur' },
                ],
                SliderVerified: [
                    {
                        validator: (rule, value, callback) => {
                            if (!value) {
                                callback(new Error('请完成滑块验证'));
                            } else {
                                callback();
                            }
                        }, trigger: 'change'
                    }
                ]
            }
        }

    },
    created() {
        this.GetRoleTypeApi();
    },
    mounted() {
        this.startTypewriter();
    },
    methods: {
        sleep(ms) {
            return new Promise(resolve => setTimeout(resolve, ms));
        },
        async startTypewriter() {
            const brand = '专注，从这一刻开始';
            for (let i = 0; i < brand.length; i++) {
                this.brandText += brand[i];
                await this.sleep(150);
            }
            this.showSloganCursor = true;
            await this.sleep(400);
            const slogan = '把时间交给学习';
            for (let i = 0; i < slogan.length; i++) {
                this.sloganText += slogan[i];
                await this.sleep(100);
            }
            await this.sleep(2000);
            this.showSloganCursor = false;
        },
        async GetRoleTypeApi() {
            let { Data: { Items } } = await this.$Post("/Select/RoleType");
            this.roleOptions = Items
        },
        LoginBtn() {
            this.$refs.loginForm.validate(async (valid) => {
                if (valid) {
                    this.isLoading = true;
                    try {
                        let res = await store.dispatch("Login", this.formData);
                        if (res.Success) {
                            this.isSuccess = true;
                            this.$message.success("登录成功!");
                            await this.sleep(400);
                            this.$router.push({ path: "/Admin" });
                        } else {
                            this.$refs.SliderCode.refreshCode();
                        }
                    } finally {
                        this.isLoading = false;
                    }
                } else {
                    this.$message.error("登录验证不通过");
                    this.$refs.SliderCode.refreshCode();
                    return false;
                }
            });
        },
        ToHome() {
            this.$router.push({
                path: "/Front/Home"
            })
        },
    }
}
</script>

<style scoped>
/* 动效参数 */
.auth-page {
    --login-primary: #3A5F73;
    --login-primary-light: #4A7A8C;
    --login-primary-dark: #2A4A5C;
    --login-accent: #E6B980;
    --login-bg: #F5F1E8;
    --login-card: #FFFEF7;
    --login-input-bg: #FAF8F3;
    --ease-out: cubic-bezier(0.22, 1, 0.36, 1);
    --dur-sm: 200ms;
    --dur-md: 320ms;
    --dur-lg: 520ms;

    height: 100vh;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 16px;
    background: var(--login-bg);
    position: relative;
    overflow: hidden;
    box-sizing: border-box;
}

/* 页面登录成功淡出 */
.auth-page--success {
    animation: fadeOut 400ms var(--ease-out) forwards;
}

@keyframes fadeOut {
    to {
        opacity: 0;
        transform: scale(0.98);
    }
}

/* 背景装饰 */
.auth-bg-shape {
    position: absolute;
    border-radius: 50%;
    pointer-events: none;
}

.auth-bg-shape--1 {
    width: 500px;
    height: 500px;
    background: radial-gradient(circle, rgba(58, 95, 115, 0.06) 0%, transparent 70%);
    top: -150px;
    right: -100px;
}

.auth-bg-shape--2 {
    width: 400px;
    height: 400px;
    background: radial-gradient(circle, rgba(230, 185, 128, 0.08) 0%, transparent 70%);
    bottom: -100px;
    left: -100px;
}

/* 容器入场 */
@keyframes fadeUp {
    from { opacity: 0; transform: translateY(24px); }
    to { opacity: 1; transform: none; }
}

.auth-card {
    width: min(960px, 100%);
    max-height: calc(100vh - 32px);
    display: flex;
    background: var(--login-card);
    border-radius: 16px;
    box-shadow: 0 20px 60px rgba(58, 95, 115, 0.15), 0 8px 20px rgba(0, 0, 0, 0.08);
    overflow: hidden;
    position: relative;
    z-index: 1;
}

.auth-card--enter {
    animation: fadeUp var(--dur-md) var(--ease-out);
}

/* 左侧氛围区 40% */
.auth-left {
    width: 40%;
    flex-shrink: 0;
    background: linear-gradient(160deg, var(--login-primary) 0%, var(--login-primary-dark) 100%);
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 32px 28px;
    position: relative;
    overflow: hidden;
}

.auth-left--enter {
    animation: fadeUp var(--dur-lg) var(--ease-out);
}

/* 暖光呼吸 */
.auth-left-glow {
    position: absolute;
    top: -20%;
    left: 50%;
    transform: translateX(-50%);
    width: 320px;
    height: 320px;
    background: radial-gradient(circle, rgba(230, 185, 128, 0.25) 0%, rgba(230, 185, 128, 0.08) 40%, transparent 70%);
    border-radius: 50%;
    pointer-events: none;
    z-index: 0;
    animation: glow 6s ease-in-out infinite;
}

@keyframes glow {
    0%, 100% { opacity: 0.25; transform: translateX(-50%) scale(1); }
    50% { opacity: 0.35; transform: translateX(-50%) scale(1.05); }
}

.auth-left::after {
    content: '';
    position: absolute;
    bottom: -20%;
    right: -15%;
    width: 280px;
    height: 280px;
    background: radial-gradient(circle, rgba(230, 185, 128, 0.12) 0%, transparent 70%);
    border-radius: 50%;
    pointer-events: none;
    animation: glow 6s ease-in-out infinite reverse;
}

.auth-left-content {
    text-align: center;
    color: #fff;
    position: relative;
    z-index: 1;
}

.auth-logo {
    width: 52px;
    height: 52px;
    border-radius: 50%;
    object-fit: cover;
    border: 2px solid rgba(255, 255, 255, 0.2);
    margin-bottom: 16px;
}

.auth-brand {
    font-size: 24px;
    font-weight: 700;
    margin: 0 0 10px;
    letter-spacing: 3px;
    min-height: 36px;
}

.auth-slogan {
    font-size: 13px;
    opacity: 0.6;
    margin: 0 0 24px;
    letter-spacing: 2px;
    min-height: 20px;
    color: rgba(255, 255, 255, 0.8);
}

.typewriter-cursor {
    display: inline-block;
    color: rgba(255, 255, 255, 0.9);
    font-weight: 300;
    animation: cursorBlink 0.8s infinite;
    margin-left: 2px;
}

@keyframes cursorBlink {
    0%, 100% { opacity: 1; }
    50% { opacity: 0; }
}

/* 插画轻浮动 */
.auth-hero-image {
    width: 100%;
    max-width: 260px;
    border-radius: 12px;
    opacity: 0.92;
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.2);
    border: none;
    animation: float 6s ease-in-out infinite;
}

@keyframes float {
    0%, 100% { transform: translateY(0); }
    50% { transform: translateY(-6px); }
}

/* 右侧登录区 60% */
.auth-right {
    flex: 1;
    min-width: 0;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 32px;
}

.auth-form-wrap {
    width: 100%;
    max-width: 360px;
}

.auth-form-header {
    margin-bottom: 24px;
}

.auth-title {
    font-size: 22px;
    font-weight: 700;
    color: var(--login-primary);
    margin: 0 0 6px;
}

.auth-subtitle {
    color: #B0B8C0;
    margin: 0;
    font-size: 13px;
}

.auth-form {
    margin-top: 8px;
}

.auth-form /deep/ .el-form-item {
    margin-bottom: 22px;
}

.auth-form /deep/ .el-form-item__label {
    font-weight: 600;
    color: var(--login-primary);
    padding-bottom: 4px;
    line-height: 28px;
    font-size: 13px;
}

.auth-form /deep/ .el-form-item__content {
    line-height: 36px;
}

/* 表单项分段入场 */
@keyframes itemIn {
    from { opacity: 0; transform: translateY(12px); }
    to { opacity: 1; transform: none; }
}

.auth-form-item--enter {
    opacity: 0;
    transform: translateY(12px);
    animation: itemIn var(--dur-sm) var(--ease-out) forwards;
    animation-delay: var(--delay, 0ms);
}

/* 输入框交互 */
.auth-form /deep/ .el-input__inner {
    height: 48px;
    line-height: 48px;
    background: var(--login-input-bg);
    border-color: #E8E3D9;
    border-radius: 12px;
    font-size: 14px;
    transition: all var(--dur-sm) var(--ease-out);
}

.auth-form /deep/ .el-input__inner:focus {
    border-color: var(--login-primary);
    box-shadow: 0 4px 12px rgba(58, 95, 115, 0.12);
    background: #fff;
    transform: translateY(-1px);
}

.auth-form /deep/ .el-input__prefix {
    color: #B8C0C8;
}

/* 输入框错误抖动 */
.auth-form /deep/ .el-form-item.is-error .el-input__inner {
    animation: shake var(--dur-md);
}

@keyframes shake {
    0%, 100% { transform: translateX(0); }
    25% { transform: translateX(-4px); }
    75% { transform: translateX(4px); }
}

.auth-form /deep/ .el-radio-button__inner {
    padding: 9px 18px;
    font-size: 13px;
    border-radius: 10px;
}

.auth-role-group {
    display: flex;
    gap: 0;
}

.auth-role-group /deep/ .el-radio-button__inner {
    border-color: #E8E3D9;
    color: #8A9BAE;
    font-weight: 500;
    background: var(--login-input-bg);
    transition: all var(--dur-sm) var(--ease-out);
}

.auth-role-group /deep/ .el-radio-button__orig-radio:checked + .el-radio-button__inner {
    background-color: var(--login-primary);
    border-color: var(--login-primary);
    box-shadow: -1px 0 0 0 var(--login-primary);
    color: #fff;
}

/* 主按钮 */
.auth-btn {
    width: 100%;
    font-weight: 700;
    letter-spacing: 3px;
    border-radius: 12px;
    height: 48px;
    font-size: 15px;
    transition: all var(--dur-sm) var(--ease-out);
}

.auth-btn--primary {
    background: linear-gradient(135deg, var(--login-primary) 0%, var(--login-primary-light) 100%);
    border: none;
    box-shadow: 0 4px 16px rgba(58, 95, 115, 0.3);
}

.auth-btn--primary:hover {
    transform: translateY(-2px);
    box-shadow: 0 12px 28px rgba(58, 95, 115, 0.35);
    filter: brightness(1.05);
}

.auth-btn--primary:active {
    transform: translateY(0);
    box-shadow: 0 4px 10px rgba(58, 95, 115, 0.2);
}

/* loading 状态 */
.auth-btn--primary.is-loading {
    pointer-events: none;
    opacity: 0.85;
    letter-spacing: 1px;
}

.auth-btn--primary.is-loading::after {
    content: "";
    width: 16px;
    height: 16px;
    border: 2px solid rgba(255, 255, 255, 0.6);
    border-top-color: #fff;
    border-radius: 50%;
    display: inline-block;
    margin-left: 8px;
    vertical-align: middle;
    animation: spin 0.6s linear infinite;
}

@keyframes spin {
    to { transform: rotate(360deg); }
}

.auth-btn--ghost {
    background: transparent;
    border: none;
    color: #A0AAB4;
    box-shadow: none;
    letter-spacing: 1px;
}

.auth-btn--ghost:hover {
    color: var(--login-primary);
    background: rgba(58, 95, 115, 0.04);
}

/* 底部链接 */
.auth-footer-row {
    text-align: center;
    margin-top: 20px;
    padding-top: 16px;
    border-top: 1px solid #EDE9DF;
}

.auth-footer-text {
    color: #A0AAB4;
    font-size: 13px;
}

.auth-footer-divider {
    color: #E0DCD4;
    margin: 0 8px;
    font-size: 13px;
}

.auth-link {
    color: var(--login-accent);
    font-weight: 600;
    margin-left: 4px;
    font-size: 13px;
    transition: color var(--dur-sm);
}

.auth-link:hover {
    color: #D4A56A;
}

/* 移动端 */
@media (max-width: 768px) {
    .auth-page {
        padding: 12px;
    }

    .auth-card {
        flex-direction: column;
        max-height: none;
    }

    .auth-left {
        width: 100%;
        padding: 24px 16px;
    }

    .auth-logo {
        width: 48px;
        height: 48px;
    }

    .auth-brand {
        font-size: 20px;
    }

    .auth-hero-image {
        max-width: 180px;
    }

    .auth-right {
        padding: 24px 16px;
    }

    .auth-form-wrap {
        max-width: none;
    }
}
</style>
