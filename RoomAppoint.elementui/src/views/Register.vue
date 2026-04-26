<template>
    <div class="auth-page">
        <div class="auth-panel">
            <div class="auth-hero">
                <img class="auth-hero-image" src="@/assets/工作.png">
            </div>
            <div class="auth-form-shell">
                <div class="auth-form-wrap">
                    <h2 class="auth-title">注册信息填写</h2>
                    <el-form class="auth-form" ref="loginForm" :model="formData" label-width="90px" label-position="left"
                        :rules="rules">

                        <el-form-item label="账号" prop="UserName">
                            <el-input type="text" v-model.trim="formData.UserName" placeholder="请输入账号">
                            </el-input>
                        </el-form-item>

                        <el-form-item label="密码" prop="Password">
                            <el-input type="password" v-model.trim="formData.Password" placeholder="请输入密码"></el-input>
                        </el-form-item>

                        <el-form-item label="邮箱" prop="Email" placeholder="请输入邮箱">
                            <el-input v-model.trim="formData.Email"></el-input>
                        </el-form-item>
                        <el-form-item label="联系方式" prop="PhoneNumber" placeholder="请输入联系方式">
                            <el-input v-model.trim="formData.PhoneNumber"></el-input>
                        </el-form-item>
                        <el-form-item label="姓名" prop="Name" placeholder="请输入姓名">
                            <el-input v-model.trim="formData.Name"></el-input>
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
                            <el-button class="auth-btn" type="primary" @click="RegisterBtn">注 册</el-button>

                        </el-form-item>
                    </el-form>
                </div>
                <div class="auth-footer-row">
                    <div>
                        <span>如果有账号</span>
                        <RouterLink :to="{ path: '/Login' }"><span class="auth-link-primary pointer">去登录</span>
                        </RouterLink>
                    </div>
                    <div><span class="auth-link-secondary pointer">忘记密码</span></div>
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
            formData: {
                UserName: '',
                Password: '',
                RoleType: "2",
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
                "Email": [
                    { required: true, message: '该项为必填项', trigger: 'blur' },
                    {
                        validator: (rule, value, callback) => {
                            var reg = /^[\w-]+(\.[\w-]+)*@[\w-]+(\.[\w-]+)+$/;
                            if (!value || !reg.test(value)) {
                                callback(new Error('请输入正确的邮箱'));
                            }
                            else {
                                callback();
                            }
                        }, trigger: 'blur'
                    },
                ],
                "ImageUrls": [
                    { required: true, message: '该项为必填项', trigger: 'blur' },
                ],
                "Name": [
                    { required: true, message: '该项为必填项', trigger: 'blur' },
                ],

                "PhoneNumber": [
                    { required: true, message: '该项为必填项', trigger: 'blur' },
                    {
                        validator: (rule, value, callback) => {
                            var reg = /^1[34578]\d{9}$/;
                            if (!value || !reg.test(value)) {
                                callback(new Error('请输入正确的手机号'));
                            }
                            else {
                                callback();
                            }
                        }, trigger: 'blur'
                    },
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
            let { Data: { Items } } = await this.$Post("/Select/RoleType");

            this.roleOptions = Items
            console.log(this.roleOptions)
        },
        RegisterBtn() {
            this.$refs.loginForm.validate(async (valid) => {
                if (valid) {

                    let res = await this.$Post("/User/Register", this.formData)
                    console.log(res);
                    if (res.Success) {
                        this.$message.success("注册成功!");
                        this.$router.push({
                            path: "/Login"
                        })
                    }
                    else {

                        this.$refs.ValidCode.refreshCode();
                    }
                } else {
                    this.$message.error("注册验证不通过")
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
