<template>
    <div>
        <el-container>
            <el-header class="front-header">
                <div v-if="isMobile" class="mobile-header main-container">
                    <div class="mobile-brand" @click="ToPath('/Front/Home')">
                        <img class="brand-logo" :src="require('@/assets/logo.jpg')">
                        <span class="mobile-title">志高自习室预约</span>
                    </div>
                    <div class="mobile-actions">
                        <el-button type="text" class="mobile-menu-btn" icon="el-icon-menu" @click="drawerVisible = true">菜单</el-button>
                    </div>

                    <el-drawer :visible.sync="drawerVisible" direction="rtl" size="72%" :with-header="false" :destroy-on-close="true" :modal="true" :append-to-body="true" @close="cleanupDrawer">
                        <div class="drawer-body">
                            <div class="drawer-user" v-if="Token">
                                <el-avatar v-if="avatarUrl" :size="36" :src="avatarUrl" class="user-avatar" />
                                <div class="drawer-user-meta">
                                    <div class="drawer-user-name">{{ (UserInfo && UserInfo.UserName) || '' }}</div>
                                    <div class="drawer-user-role">{{ (UserInfo && UserInfo.RoleTypeFormat) || '' }}</div>
                                </div>
                            </div>

                            <div class="drawer-nav">
                                <el-button type="text" class="drawer-link" @click="Go('/Front/AnnouncementList')">公告</el-button>
                                <el-button v-if="Token" type="text" class="drawer-link" @click="Go('/Front/IntegralList')">我的积分</el-button>
                                <el-button v-if="Token" type="text" class="drawer-link" @click="Go('/Front/BalanceRecordList')">我的余额</el-button>
                                <el-button v-if="Token" type="text" class="drawer-link" @click="Go('/Front/GiftMall')">积分兑换</el-button>
                                <el-button v-if="Token" type="text" class="drawer-link" @click="Go('/Front/GiftRedeemMyList')">我的兑换记录</el-button>
                                <el-button v-if="Token" type="text" class="drawer-link" @click="Go('/Front/AppointRecordList')">我的预约记录</el-button>
                                <el-button v-if="Token" type="text" class="drawer-link" @click="Go('/Front/FeedbackList')">意见反馈</el-button>
                                <el-button v-if="Token" type="text" class="drawer-link" @click="Go('/Front/UserPerson')">个人信息</el-button>

                                <el-button v-if="!Token" type="primary" class="drawer-primary" @click="Go('/Login')">登录</el-button>
                                <el-button v-if="!Token" type="default" class="drawer-primary" @click="Go('/Register')">注册</el-button>

                                <el-button v-if="Token" type="danger" plain class="drawer-primary" @click="LoginOut()">退出</el-button>
                            </div>
                        </div>
                    </el-drawer>
                </div>

                <el-menu v-else :default-active="active" class="main-container front-menu" mode="horizontal" text-color="black"
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
                        <el-menu-item @click="ToPath('/Front/BalanceRecordList')">我的余额</el-menu-item>
                        <el-menu-item @click="ToPath('/Front/GiftRedeemMyList')">我的兑换记录</el-menu-item>
                        <el-menu-item @click="ToPath('/Front/AppointRecordList')">我的预约记录</el-menu-item>
                        <el-menu-item @click="ToPath('/Front/FeedbackList')">意见反馈</el-menu-item>
                        <el-menu-item @click="ToUserInfo()">个人信息</el-menu-item>
                        <el-menu-item @click="LoginOut()">退出</el-menu-item>
                    </el-submenu>
                    <el-menu-item class="menu-right menu-action" v-if="Token" @click="ToPath('/Front/GiftMall')">积分兑换</el-menu-item>
                    <el-menu-item class="menu-right menu-action" @click="ToPath('/Front/AnnouncementList')">公告</el-menu-item>
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
        <AiChatBubble />
    </div>
</template>

<script>

import { mapGetters } from 'vuex'
import AiChatBubble from '@/components/Chat/AiChatBubble.vue'
import { ReplaceImageHttp } from '@/utils/comm'
export default {
    components: { AiChatBubble },
    data() {
        return {
            active: 'home',
            isMobile: false,
            drawerVisible: false,

        }
    },
    created() {
        this.UpdateIsMobile();
        window.addEventListener('resize', this.UpdateIsMobile);

    },
    beforeDestroy() {
        window.removeEventListener('resize', this.UpdateIsMobile);
    },
    computed: {
        ...mapGetters(["UserInfo", "Token"]),
        avatarUrl() {
            return this.NormalizeImage(this.UserInfo && this.UserInfo.ImageUrls);
        }
    },
    methods: {
        UpdateIsMobile() {
            this.isMobile = window.matchMedia && window.matchMedia('(max-width: 768px)').matches;
            if (!this.isMobile) {
                this.drawerVisible = false;
            }
        },
        Go(url) {
            this.drawerVisible = false;
            this.ToPath(url);
        },
        cleanupDrawer() {
            this.$nextTick(() => {
                document.querySelectorAll('.el-drawer__wrapper, .v-modal').forEach(el => {
                    el.parentNode && el.parentNode.removeChild(el);
                });
            });
        },
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
            this.drawerVisible = false;
            this.cleanupDrawer();
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
    position: sticky;
    top: 0;
    z-index: 1000;
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

.mobile-header {
    height: 60px;
    display: flex;
    align-items: center;
    justify-content: space-between;
}

.mobile-brand {
    display: flex;
    align-items: center;
    gap: var(--lib-space-sm);
    min-width: 0;
    cursor: pointer;
}

.mobile-title {
    font-weight: 700;
    color: var(--lib-text-primary);
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
}

.mobile-menu-btn {
    padding: 10px 0;
    color: var(--lib-accent);
    font-weight: 600;
}

.drawer-body {
    padding: var(--lib-space-md);
}

.drawer-user {
    display: flex;
    align-items: center;
    gap: var(--lib-space-sm);
    padding-bottom: var(--lib-space-md);
    border-bottom: 1px solid var(--lib-border);
    margin-bottom: var(--lib-space-md);
}

.drawer-user-meta {
    min-width: 0;
}

.drawer-user-name {
    font-weight: 700;
}

.drawer-user-role {
    color: var(--lib-text-secondary);
    font-size: 12px;
}

.drawer-nav {
    display: flex;
    flex-direction: column;
    gap: 8px;
}

.drawer-link {
    text-align: left;
    justify-content: flex-start;
    padding: 10px 0;
    color: var(--lib-text-primary);
}

.drawer-primary {
    width: 100%;
}

@media (max-width: 768px) {
    .brand-logo {
        height: 34px;
    }

    .main-box {
        min-height: calc(100vh - 126px);
    }
}


</style>
