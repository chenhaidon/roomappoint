<template>
    <div class="auth-page">
        <div class="auth-panel">
            <div class="auth-hero">
                <img class="auth-hero-image" src="@/assets/工作.png">
            </div>
            <div class="auth-form-shell">
                <div class="auth-form-wrap">
                    <h2 class="auth-title">欢迎您来到自习室预约系统</h2>
                    <el-form class="auth-form" ref="loginForm" :model="formData" label-width="70px" label-position="left"
                        :rules="rules">

                        <el-form-item label="账号" prop="UserName">
                            <el-input type="text" v-model.trim="formData.UserName" placeholder="请输入账号">
                            </el-input>
                        </el-form-item>

                        <el-form-item label="密码" prop="Password">
                            <el-input type="password" v-model.trim="formData.Password" placeholder="请输入密码"></el-input>
                        </el-form-item>

                        <el-form-item label="角色" prop="RoleType">
                            <el-radio-group v-model="formData.RoleType">
                                <el-radio v-for="item in roleOptions" :key="item.Code" :label="item.Code">{{
                                    item.Label
                                }}</el-radio>
                            </el-radio-group>
                        </el-form-item>
                        <el-form-item label="验证码" prop="Code">
                            <div class="auth-code-row">
                                <el-input text="Code" v-model.trim="formData.Code" placeholder="请输入验证码"></el-input>
                                <div class="auth-code-canvas">
                                    <ValidCode ref="ValidCode"></ValidCode>
                                </div>
                            </div>
                        </el-form-item>
                        <el-form-item>
                            <el-button class="auth-btn" type="primary" @click="LoginBtn">登 录</el-button>

                        </el-form-item>
                        <el-form-item>
                            <el-button class="auth-btn" type="danger" @click="ToHome">前 台</el-button>

                        </el-form-item>
                    </el-form>
                </div>
                <div class="auth-footer-row">
                    <div>
                        <span>如果没有账号可以</span>
                        <RouterLink :to="{ path: '/Register' }"><span class="auth-link-primary pointer">
                                立即注册
                            </span>
                        </RouterLink>
                    </div>
                    <div><span class="auth-link-secondary pointer">忘记密码</span></div>
                </div>
            </div>
        </div>
    </div>
</template>

<script>
import ValidCode from '@/components/Code/canvas.vue';
import store from '@/store';
export default {
    components: {
        ValidCode: ValidCode
    },
    data() {
        return {
            formData: {
                UserName: '',
                Password: '',
                RoleType: "",
                Code: ""
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

                Code: [
                    { required: true, message: '请输入验证码', trigger: 'blur' },
                    {
                        validator: (rule, value, callback) => {
                            let identifyCode = this.$refs.ValidCode.getCode();

                            if (value != identifyCode) {
                                callback(new Error('请输入正确的验证码'));
                            } else {
                                callback();
                            }
                        }, trigger: 'blur'
                    }
                ]
            }
        }

    },
    created() {
        this.GetRoleTypeApi();
    },
    methods: {
        async GetRoleTypeApi() {
            //就是写请求Select控制器以及RoleType具体方法的前端代码
            let { Data: { Items } } = await this.$Post("/Select/RoleType");

            this.roleOptions = Items

        },
        LoginBtn() {
            this.$refs.loginForm.validate(async (valid) => {
                if (valid) {

                    let res = await store.dispatch("Login", this.formData);
                    if (res.Success) {
                        this.$message.success("登录成功!");
                        this.$router.push({
                            path: "/Admin"
                        })

                    }
                    else {

                        this.$refs.ValidCode.refreshCode();
                    }
                } else {
                    this.$message.error("登录验证不通过")
                    this.$refs.ValidCode.refreshCode();
                    return false;
                }
            });
        },
        //去往前台
        ToHome() {
            this.$router.push({
                path: "/Front/Home"
            })
        },
    }
}
</script>

<style scoped>
.auth-page {
    min-height: 100vh;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: var(--lib-space-xl) var(--lib-space-lg);
    background: radial-gradient(circle at top left, #f1ece2 0%, var(--lib-bg-page) 50%, #ece6db 100%);
}

.auth-panel {
    width: min(1120px, 100%);
    display: flex;
    gap: var(--lib-space-lg);
    background: var(--lib-bg-surface);
    border: 1px solid var(--lib-border);
    border-radius: var(--lib-radius-lg);
    box-shadow: var(--lib-shadow-md);
    padding: var(--lib-space-lg);
}

.auth-hero {
    flex: 1;
    min-width: 0;
}

.auth-hero-image {
    width: 100%;
    height: 100%;
    min-height: 460px;
    object-fit: cover;
    border-radius: 12px;
}

.auth-form-shell {
    flex: 1;
    min-width: 0;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
    gap: var(--lib-space-md);
}

.auth-form-wrap {
    max-width: 500px;
}

.auth-title {
    color: var(--lib-text-primary);
    letter-spacing: 0.5px;
}

.auth-form {
    margin-top: var(--lib-space-lg);
}

.auth-code-row {
    display: flex;
    align-items: center;
}

.auth-code-canvas {
    margin-left: var(--lib-space-sm);
}

.auth-btn {
    width: 100%;
}

.auth-footer-row {
    display: flex;
    justify-content: space-between;
    color: var(--lib-text-secondary);
}

.auth-link-primary {
    padding-left: var(--lib-space-sm);
    color: var(--lib-accent);
    font-weight: 600;
}

.auth-link-secondary {
    color: var(--lib-wood);
}

@media (max-width: 1024px) {
    .auth-panel {
        flex-direction: column;
    }

    .auth-hero-image {
        min-height: 240px;
    }

    .auth-form-wrap {
        max-width: none;
    }
}
</style>