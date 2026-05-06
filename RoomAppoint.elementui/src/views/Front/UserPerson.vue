<template>
    <div class="person-page">
        <!-- 顶部 -->
        <div class="page-header">
            <div class="header-back" @click="$router.go(-1)">
                <i class="el-icon-arrow-left"></i>
            </div>
            <div class="header-title">个人信息</div>
        </div>

        <!-- 头像区域 -->
        <div class="avatar-section">
            <div class="avatar-wrapper">
                <el-avatar v-if="avatarUrl" :size="72" :src="avatarUrl" class="user-avatar" />
                <div v-else class="avatar-placeholder"><i class="el-icon-user-solid"></i></div>
            </div>
            <div class="avatar-name">{{ formData.Name || formData.UserName || '' }}</div>
            <div class="avatar-role">{{ formData.RoleTypeFormat || '普通用户' }}</div>
        </div>

        <!-- 个人信息表单 -->
        <div class="form-section" v-if="editShow">
            <div class="section-title">基本信息</div>
            <el-form ref="editModalForm" :model="formData" label-position="top" size="small" :rules="rules">
                <el-form-item label="账号" prop="UserName">
                    <el-input v-model="formData.UserName" clearable disabled></el-input>
                </el-form-item>
                <el-form-item label="名称" prop="Name">
                    <el-input v-model="formData.Name" clearable placeholder="请输入名称"></el-input>
                </el-form-item>
                <el-form-item label="邮箱" prop="Email">
                    <el-input v-model="formData.Email" clearable placeholder="请输入邮箱"></el-input>
                </el-form-item>
                <el-form-item label="手机号码" prop="PhoneNumber">
                    <el-input v-model="formData.PhoneNumber" clearable placeholder="请输入手机号码"></el-input>
                </el-form-item>
                <el-form-item label="头像" prop="ImageUrls">
                    <UploadImages v-model="formData.ImageUrls"></UploadImages>
                </el-form-item>
                <el-form-item label="出生年月" prop="Birth">
                    <el-date-picker type="date" value-format="yyyy-MM-dd 00:00:00" placeholder="选择日期"
                        v-model="formData.Birth" clearable style="width: 100%"></el-date-picker>
                </el-form-item>
            </el-form>
            <el-button type="warning" class="save-btn" @click="CreateOrEdit" :loading="saving">
                保存信息
            </el-button>
        </div>

        <!-- 修改密码 -->
        <div class="form-section">
            <div class="section-title">修改密码</div>
            <el-form ref="pwdFormRef" :model="pwdForm" label-position="top" size="small" :rules="pwdRules">
                <el-form-item label="原始密码" prop="OrginPassword">
                    <el-input type="password" v-model.trim="pwdForm.OrginPassword" placeholder="请输入原始密码" show-password></el-input>
                </el-form-item>
                <el-form-item label="新密码" prop="NewPassword">
                    <el-input type="password" v-model.trim="pwdForm.NewPassword" placeholder="请输入新密码" show-password></el-input>
                </el-form-item>
                <el-form-item label="确认密码" prop="TwoPassword">
                    <el-input type="password" v-model.trim="pwdForm.TwoPassword" placeholder="请再次输入新密码" show-password></el-input>
                </el-form-item>
            </el-form>
            <el-button class="pwd-btn" @click="ChangePassword" :loading="changingPwd">
                修改密码
            </el-button>
        </div>
    </div>
</template>

<script>
import store from "@/store/index.js";
import { mapGetters } from "vuex";
import { ReplaceImageHttp } from '@/utils/comm';

export default {
    computed: {
        ...mapGetters(["UserInfo", "UserId"]),
        avatarUrl() {
            return this.NormalizeImage(this.formData && this.formData.ImageUrls);
        }
    },
    data() {
        return {
            editShow: false,
            formData: {},
            pwdForm: {},
            saving: false,
            changingPwd: false,
            rules: {
                UserName: [{ required: true, message: '请输入账号', trigger: 'blur' }],
                Email: [
                    { required: true, message: '请输入邮箱', trigger: 'blur' },
                    {
                        validator: (rule, value, callback) => {
                            if (!value || /^[\w-]+(\.[\w-]+)*@[\w-]+(\.[\w-]+)+$/.test(value)) {
                                callback();
                            } else {
                                callback(new Error('请输入正确邮箱'));
                            }
                        }, trigger: 'blur'
                    }
                ],
                Name: [{ required: true, message: '请输入名称', trigger: 'blur' }],
                PhoneNumber: [
                    { required: true, message: '请输入手机号码', trigger: 'blur' },
                    {
                        validator: (rule, value, callback) => {
                            if (!value || /^1[34578]\d{9}$/.test(value)) {
                                callback();
                            } else {
                                callback(new Error('请输入正确的手机号'));
                            }
                        }, trigger: 'blur'
                    }
                ],
                Birth: [{ required: true, message: '请选择出生年月', trigger: 'blur' }],
                ImageUrls: [{ required: true, message: '请上传头像', trigger: 'blur' }]
            },
            pwdRules: {
                OrginPassword: [{ required: true, message: '请输入原始密码', trigger: 'blur' }],
                NewPassword: [{ required: true, message: '请输入新密码', trigger: 'blur' }],
                TwoPassword: [{ required: true, message: '请再次输入新密码', trigger: 'blur' }]
            }
        };
    },
    created() {
        this.ShowEditModal();
    },
    methods: {
        NormalizeImage(value) {
            if (!value) return '';
            let raw = Array.isArray(value) ? value[0] : String(value);
            raw = raw.trim();
            if (raw.startsWith('[') && raw.endsWith(']')) {
                try { raw = JSON.parse(raw)[0] || raw; } catch (e) {}
            }
            raw = String(raw).split(',')[0].trim().replace(/^['\"]|['\"]$/g, '');
            return ReplaceImageHttp(raw);
        },
        async ShowEditModal() {
            let { Data } = await this.$Post("/User/Get", { Id: this.UserId });
            this.formData = Data;
            this.editShow = true;
        },
        async CreateOrEdit() {
            this.$refs.editModalForm.validate(async (valid) => {
                if (valid) {
                    this.saving = true;
                    let { Success } = await this.$Post("/User/CreateOrEdit", this.formData);
                    this.saving = false;
                    if (Success) {
                        this.$message.success("保存成功");
                        store.dispatch("GetInfo");
                    }
                }
            });
        },
        async ChangePassword() {
            this.$refs.pwdFormRef.validate(async (valid) => {
                if (valid) {
                    if (this.formData.Password !== this.pwdForm.OrginPassword) {
                        this.$message.error("原始密码错误");
                        return;
                    }
                    if (this.pwdForm.NewPassword !== this.pwdForm.TwoPassword) {
                        this.$message.error("确认密码和新密码不一致");
                        return;
                    }
                    if (this.pwdForm.OrginPassword === this.pwdForm.NewPassword) {
                        this.$message.error("新密码不能和原始密码相同");
                        return;
                    }
                    this.changingPwd = true;
                    this.formData.Password = this.pwdForm.TwoPassword;
                    let { Success } = await this.$Post("/User/CreateOrEdit", this.formData);
                    this.changingPwd = false;
                    if (Success) {
                        this.$message.success("密码修改成功，请重新登录");
                        store.dispatch("Logout");
                        this.$router.push("/Login");
                    }
                }
            });
        }
    }
};
</script>

<style scoped>
.person-page {
    max-width: 600px;
    margin: 0 auto;
    min-height: 100vh;
    background: #faf6f2;
    padding-bottom: 32px;
}

/* 顶部 */
.page-header {
    display: flex;
    align-items: center;
    padding: 16px;
    background: #fff;
}
.header-back {
    font-size: 20px;
    padding: 8px;
    cursor: pointer;
    color: #333;
}
.header-title {
    flex: 1;
    font-size: 17px;
    font-weight: 600;
    color: #1a1a1a;
    margin-left: 4px;
}

/* 头像区域 */
.avatar-section {
    text-align: center;
    padding: 28px 16px 20px;
    background: linear-gradient(135deg, #fff8e1, #fff3e0);
    margin: 16px;
    border-radius: 20px;
    box-shadow: 0 4px 16px rgba(255,152,0,0.1);
}
.avatar-wrapper {
    margin-bottom: 12px;
}
.user-avatar {
    border: 3px solid #fff;
    box-shadow: 0 2px 12px rgba(0,0,0,0.1);
}
.avatar-placeholder {
    width: 72px;
    height: 72px;
    border-radius: 50%;
    background: #eee;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    font-size: 32px;
    color: #ddd;
}
.avatar-name {
    font-size: 18px;
    font-weight: 700;
    color: #1a1a1a;
    margin-bottom: 4px;
}
.avatar-role {
    font-size: 13px;
    color: #e65100;
}

/* 表单区域 */
.form-section {
    margin: 0 16px 16px;
    padding: 20px;
    background: #fff;
    border-radius: 16px;
    box-shadow: 0 2px 8px rgba(0,0,0,0.04);
}
.section-title {
    font-size: 15px;
    font-weight: 700;
    color: #1a1a1a;
    margin-bottom: 16px;
    padding-bottom: 12px;
    border-bottom: 1px solid #f0f0f0;
}
.save-btn {
    width: 100%;
    border-radius: 12px;
    padding: 12px;
    font-size: 15px;
    font-weight: 600;
    background: #ff9800;
    border-color: #ff9800;
    margin-top: 8px;
}
.pwd-btn {
    width: 100%;
    border-radius: 12px;
    padding: 12px;
    font-size: 15px;
    font-weight: 600;
    color: #ff9800;
    border-color: #ff9800;
    background: #fff;
    margin-top: 8px;
}

/* 动效 */
@keyframes fadeUp {
    from { opacity: 0; transform: translateY(12px); }
    to { opacity: 1; transform: translateY(0); }
}
.person-page { animation: fadeUp 0.3s ease; }
</style>
