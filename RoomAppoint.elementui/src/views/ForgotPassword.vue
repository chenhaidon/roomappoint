<template>
    <div class="auth-page">
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
                    <img class="auth-hero-image" src="https://chd-1314768655.cos-website.ap-shanghai.myqcloud.com/uploads/1778040683933/登录页插图.png">
                </div>
            </div>

            <div class="auth-right">
                <div class="auth-form-wrap">
                    <div class="auth-form-header auth-form-item--enter" style="--delay: 0ms">
                        <h2 class="auth-title">找回你的密码</h2>
                        <p class="auth-subtitle">验证身份后重置密码</p>
                    </div>

                    <el-form class="auth-form" ref="resetForm" :model="formData" label-position="top" :rules="rules">
                        <el-form-item label="账号" prop="UserName" class="auth-form-item--enter" style="--delay: 80ms">
                            <el-input type="text" v-model.trim="formData.UserName" placeholder="请输入注册账号"
                                prefix-icon="el-icon-user" size="large"></el-input>
                        </el-form-item>

                        <el-form-item label="邮箱" prop="Email" class="auth-form-item--enter" style="--delay: 140ms">
                            <el-input v-model.trim="formData.Email" placeholder="请输入注册邮箱"
                                prefix-icon="el-icon-message" size="large"></el-input>
                        </el-form-item>

                        <el-form-item label="手机号码" prop="PhoneNumber" class="auth-form-item--enter" style="--delay: 200ms">
                            <el-input v-model.trim="formData.PhoneNumber" placeholder="请输入注册手机号码"
                                prefix-icon="el-icon-phone" size="large"></el-input>
                        </el-form-item>

                        <el-form-item label="新密码" prop="Password" class="auth-form-item--enter" style="--delay: 260ms">
                            <el-input type="password" v-model.trim="formData.Password" placeholder="请输入新密码"
                                prefix-icon="el-icon-lock" size="large" show-password></el-input>
                        </el-form-item>

                        <el-form-item label="确认密码" prop="ConfirmPassword" class="auth-form-item--enter" style="--delay: 320ms">
                            <el-input type="password" v-model.trim="formData.ConfirmPassword" placeholder="请再次输入新密码"
                                prefix-icon="el-icon-lock" size="large" show-password></el-input>
                        </el-form-item>

                        <el-form-item label="验证码" prop="Code" class="auth-form-item--enter" style="--delay: 380ms">
                            <div class="auth-code-row">
                                <el-input v-model.trim="formData.Code" placeholder="请输入验证码"
                                    prefix-icon="el-icon-key" size="large"></el-input>
                                <div class="auth-code-canvas">
                                    <ValidCode ref="ValidCode"></ValidCode>
                                </div>
                            </div>
                        </el-form-item>

                        <el-form-item class="auth-form-item--enter" style="--delay: 440ms">
                            <el-button class="auth-btn auth-btn--primary" :class="{ 'is-loading': isLoading }" type="primary" size="large" @click="ResetBtn">
                                {{ isLoading ? '重置中' : '重置密码' }}
                            </el-button>
                        </el-form-item>
                    </el-form>

                    <div class="auth-footer-row auth-form-item--enter" style="--delay: 500ms">
                        <span class="auth-footer-text">想起密码了？</span>
                        <RouterLink :to="{ path: '/Login' }">
                            <span class="auth-link">去登录</span>
                        </RouterLink>
                        <span class="auth-footer-divider">|</span>
                        <RouterLink :to="{ path: '/Register' }">
                            <span class="auth-link">去注册</span>
                        </RouterLink>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script>
import ValidCode from '@/components/Code/canvas.vue'
export default {
    components: {
        ValidCode: ValidCode
    },
    data() {
        return {
            brandText: '',
            sloganText: '',
            showSloganCursor: false,
            isLoading: false,
            formData: {
                UserName: '',
                Email: '',
                PhoneNumber: '',
                Password: '',
                ConfirmPassword: '',
                Code: ''
            },
            rules: {
                UserName: [
                    { required: true, message: '请输入账号', trigger: 'blur' },
                ],
                Email: [
                    { required: true, message: '请输入邮箱', trigger: 'blur' },
                    {
                        validator: (rule, value, callback) => {
                            var reg = /^[\w-]+(\.[\w-]+)*@[\w-]+(\.[\w-]+)+$/;
                            if (!value || !reg.test(value)) {
                                callback(new Error('请输入正确的邮箱'));
                            } else {
                                callback();
                            }
                        }, trigger: 'blur'
                    }
                ],
                PhoneNumber: [
                    { required: true, message: '请输入手机号码', trigger: 'blur' },
                    {
                        validator: (rule, value, callback) => {
                            var reg = /^1[34578]\d{9}$/;
                            if (!value || !reg.test(value)) {
                                callback(new Error('请输入正确的手机号'));
                            } else {
                                callback();
                            }
                        }, trigger: 'blur'
                    }
                ],
                Password: [
                    { required: true, message: '请输入新密码', trigger: 'blur' },
                ],
                ConfirmPassword: [
                    { required: true, message: '请再次输入新密码', trigger: 'blur' },
                    {
                        validator: (rule, value, callback) => {
                            if (value !== this.formData.Password) {
                                callback(new Error('两次输入的密码不一致'));
                            } else {
                                callback();
                            }
                        }, trigger: 'blur'
                    }
                ],
                Code: [
                    { required: true, message: '请输入验证码', trigger: 'blur' },
                    {
                        validator: (rule, value, callback) => {
                            let identifyCode = this.$refs.ValidCode.getCode();
                            if (value != identifyCode) {
                                callback(new Error('验证码不正确'));
                            } else {
                                callback();
                            }
                        }, trigger: 'blur'
                    }
                ]
            }
        }
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
            const slogan = '找回密码，重新出发';
            for (let i = 0; i < slogan.length; i++) {
                this.sloganText += slogan[i];
                await this.sleep(100);
            }
            await this.sleep(2000);
            this.showSloganCursor = false;
        },
        ResetBtn() {
            this.$refs.resetForm.validate(async (valid) => {
                if (valid) {
                    this.isLoading = true;
                    try {
                        let res = await this.$Post("/User/ResetPassword", {
                            UserName: this.formData.UserName,
                            Email: this.formData.Email,
                            PhoneNumber: this.formData.PhoneNumber,
                            Password: this.formData.Password
                        });
                        if (res.Success) {
                            this.$message.success("密码重置成功!");
                            this.$router.push({ path: "/Login" });
                        } else {
                            this.$refs.ValidCode.refreshCode();
                        }
                    } finally {
                        this.isLoading = false;
                    }
                } else {
                    this.$message.error("请完善信息");
                    this.$refs.ValidCode.refreshCode();
                    return false;
                }
            });
        }
    }
}
</script>

<style scoped>
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

    min-height: 100vh;
    display: flex;
    align-items: flex-start;
    justify-content: center;
    padding: 24px 16px;
    background: var(--login-bg);
    position: relative;
    overflow: hidden;
    box-sizing: border-box;
}

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

@keyframes fadeUp {
    from { opacity: 0; transform: translateY(24px); }
    to { opacity: 1; transform: none; }
}

.auth-card {
    width: min(960px, 100%);
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

.auth-right {
    flex: 1;
    min-width: 0;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 32px;
    overflow-y: auto;
}

.auth-form-wrap {
    width: 100%;
    max-width: 360px;
}

.auth-form-header {
    margin-bottom: 20px;
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
    margin-top: 4px;
}

.auth-form /deep/ .el-form-item {
    margin-bottom: 18px;
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

.auth-form /deep/ .el-input__inner {
    height: 44px;
    line-height: 44px;
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

.auth-form /deep/ .el-form-item.is-error .el-input__inner {
    animation: shake var(--dur-md);
}

@keyframes shake {
    0%, 100% { transform: translateX(0); }
    25% { transform: translateX(-4px); }
    75% { transform: translateX(4px); }
}

.auth-code-row {
    display: flex;
    align-items: center;
    gap: 12px;
}

.auth-code-row .el-input {
    flex: 1;
}

.auth-code-canvas {
    flex-shrink: 0;
}

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

.auth-footer-row {
    text-align: center;
    margin-top: 16px;
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

@media (max-width: 768px) {
    .auth-page {
        padding: 12px;
    }

    .auth-card {
        flex-direction: column;
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
