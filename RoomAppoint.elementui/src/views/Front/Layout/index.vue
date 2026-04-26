<template>
    <div>
        <el-container>
            <el-header class="front-header">
                <el-menu :default-active="active" class="main-container front-menu" mode="horizontal" text-color="black"
                    active-text-color="#2E4A62">
                    <el-menu-item index="" class="brand-logo-item">
                        <img class="brand-logo" :src="require('@/assets/logo.jpg')">
                    </el-menu-item>

                    <el-menu-item index="/Front/Home" @click="ToPath('/Front/Home')" class="brand-title">志高自习室预约</el-menu-item>

                    <el-menu-item class="menu-right menu-action" v-if="!Token" @click="ToRegister()">注册</el-menu-item>
                    <el-menu-item class="menu-right menu-action" v-if="!Token" @click="ToLogin()">登录</el-menu-item>
                    <el-submenu class="menu-right" v-if="Token" index="user-menu">
                        <template slot="title">
                            <el-avatar v-if="avatarUrl" :size="28" :src="avatarUrl" class="user-avatar" />
                            {{ UserInfo && UserInfo.UserName }}
                        </template>
                        <el-menu-item @click="ToPath('/Front/IntegralList')">我的积分</el-menu-item>
                        <el-menu-item @click="ToPath('/Front/AppointRecordList')">我的预约记录</el-menu-item>
                        <el-menu-item @click="ToUserInfo()">个人信息</el-menu-item>
                        <el-menu-item @click="ToEditPassword()">修改密码</el-menu-item>
                        <el-menu-item @click="LoginOut()">退出</el-menu-item>
                    </el-submenu>
                </el-menu>
            </el-header>
            <el-main class="main-container main-box">
                <router-view></router-view>
            </el-main>
            <div class="footer-gap"></div>
            <el-footer class="front-footer">
                <div class="footer-content">系统归 志高自习室预约所有</div>
            </el-footer>
        </el-container>
    </div>
</template>

<script>

import { mapGetters } from 'vuex'
import { ReplaceImageHttp } from '@/utils/comm'
export default {
    data() {
        return {
            active: 'home',

        }
    },
    created() {

    },
    computed: {
        ...mapGetters(["UserInfo", "Token"]),
        avatarUrl() {
            return this.NormalizeImage(this.UserInfo && this.UserInfo.ImageUrls);
        }
    },
    methods: {
        NormalizeImage(value) {
            if (!value) {
                return "";
            }
            let raw = Array.isArray(value) ? value[0] : String(value);
            raw = raw.trim();
            if (raw.startsWith("[") && raw.endsWith("]")) {
                try {
                    const arr = JSON.parse(raw);
                    raw = Array.isArray(arr) ? (arr[0] || "") : raw;
                } catch (e) {
                }
            }
            raw = String(raw).split(",")[0].trim().replace(/^['\"]|['\"]$/g, "");
            return ReplaceImageHttp(raw);
        },
        //前往登录
        ToLogin() {
            this.$router.push({
                path: "/Login"
            })
        },
        //前往注册
        ToRegister() {
            this.$router.push({
                path: "/Register"
            })
        },
        //退出
        async LoginOut() {
            await this.$store.dispatch('Logout')
            this.$router.push(`/Login`);
        },
        //跳转
        async ToPath(url) {
            this.$router.push({
                path: url
            })
        },
        //跳转到用户信息
        async ToUserInfo() {
            this.$router.push({
                path: "/Front/UserPerson"
            })
        },
        //跳转到修改密码
        async ToEditPassword() {
            this.$router.push({
                path: "/Front/PasswordEdit"
            })
        },
        //返回上一个页面
        goBack() {
            this.$router.go(-1)
        }

    },


}
</script>

<style scoped>
.front-header {
    background: var(--lib-bg-surface);
    border-bottom: 1px solid var(--lib-border);
    box-shadow: var(--lib-shadow-sm);
    padding: 0;
}

.main-container {
    max-width: var(--lib-container);
    width: 100%;
    margin: 0 auto;
    padding: 0 var(--lib-space-md);
}

.front-menu {
    background: transparent;
    border-bottom: none;
}

.brand-logo-item {
    padding: 0 var(--lib-space-sm);
}

.brand-logo {
    height: 46px;
    object-fit: contain;
}

.brand-title {
    font-weight: 700;
    color: var(--lib-text-primary);
}

.menu-right {
    float: right;
}

.menu-action {
    color: var(--lib-text-secondary);
}

.user-avatar {
    margin-right: 8px;
}

.main-box {
    min-height: calc(100vh - 138px);
    padding-top: var(--lib-space-md);
    padding-bottom: var(--lib-space-md);
}

.footer-gap {
    height: var(--lib-space-sm);
}

.front-footer {
    background: var(--lib-accent);
    border-top: 1px solid rgba(255, 255, 255, 0.18);
}

.footer-content {
    text-align: center;
    color: #fff;
    font-weight: 700;
    letter-spacing: 1px;
}

.el-main {
    padding: 0 !important;
}
</style>