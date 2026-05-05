<template>
  <div>
    <el-container class="admin-container">
      <el-header class="admin-header-shell">
        <div class="bg-header admin-header">
          <div class="header-brand">
            <img class="brand-logo" :src="require('@/assets/logo2.png')" />
            <span class="brand-name">自习室管理端</span>
          </div>
          <div class="header-user">
            <el-avatar :size="48" :src="avatarUrl">
              <img
                src="https://cube.elemecdn.com/e/fd/0fc7d20532fdaf769a25683617711png.png"
              />
            </el-avatar>
            <div class="user-dropdown-wrap">
              <el-dropdown>
                <span class="el-dropdown-link user-dropdown-link">
                  {{ UserInfo && UserInfo.Name
                  }}<i class="el-icon-arrow-down el-icon--right"></i>
                </span>
                <el-dropdown-menu slot="dropdown">
                  <el-dropdown-item>
                    <RouterLink :to="{ path: '/Admin/UserPerson' }">
                      个人信息</RouterLink
                    >
                  </el-dropdown-item>
                  <el-dropdown-item>
                    <RouterLink :to="{ path: '/Admin/PasswordEdit' }">
                      修改密码</RouterLink
                    >
                  </el-dropdown-item>
                  <el-dropdown-item>
                    <div @click="LoginOut()">退出</div>
                  </el-dropdown-item>
                </el-dropdown-menu>
              </el-dropdown>
            </div>
          </div>
        </div>
      </el-header>

      <el-container>
        <el-aside width="220px" class="admin-aside">
          <el-menu class="menu-list" :router="true" active-text-color="#2E4A62">
            <el-menu-item index="/Admin/Home">
              <i class="el-icon-shujufenxi"></i>
              <span>控制台</span>
            </el-menu-item>
            <el-submenu index="/Admin/UserList">
              <template slot="title">
                <i class="el-icon-s-home"></i>
                <span>用户管理</span>
              </template>
              <el-menu-item index="/Admin/UserList">
                <span>用户信息</span>
              </el-menu-item>
            </el-submenu>
            <el-menu-item index="/Admin/RoomList">
              <i class="el-icon-AIzixishi"></i>
              <span>自习室</span>
            </el-menu-item>
            <el-menu-item index="/Admin/SeatList">
              <i class="el-icon-zuowei"></i>
              <span>座位</span>
            </el-menu-item>
            <el-menu-item index="/Admin/AppointRecordList">
              <i class="el-icon-yuyuejilu"></i>
              <span>预约记录</span>
            </el-menu-item>

            <el-menu-item index="/Admin/BannerList">
              <i class="el-icon-wenanfengmian"></i>
              <span>封面</span>
            </el-menu-item>
            <el-submenu index="/Admin/IntegralList">
              <template slot="title">
                <i class="el-icon-jifen"></i>
                <span>积分</span>
              </template>

              <el-menu-item index="/Admin/IntegralList">
                <span>积分流水</span>
              </el-menu-item>
              <el-menu-item index="/Admin/GiftList">
                <span>礼品管理</span>
              </el-menu-item>
              <el-menu-item index="/Admin/GiftRedeemList">
                <span>兑换记录</span>
              </el-menu-item>
            </el-submenu>

            <el-menu-item index="/Admin/AnnouncementList">
              <i class="el-icon-message-solid"></i>
              <span>公告</span>
            </el-menu-item>

            <el-submenu index="/Admin/AppointRoomAppointStatusData">
              <template slot="title">
                <i class="el-icon-shujutongji"></i>
                <span>数据统计</span>
              </template>
              <el-menu-item index="/Admin/AppointRoomAppointStatusData">
                <span>预约状态分析</span>
              </el-menu-item>
              <el-menu-item index="/Admin/GetIntegralConsumeAndGainChart">
                <span>最近30天积分分析</span>
              </el-menu-item>
              <el-menu-item index="/Admin/GetAppointRoomRealTimeData">
                <span>统计实时在场人数</span>
              </el-menu-item>
            </el-submenu>

            <el-menu-item index="/Admin/Ops">
              <i class="el-icon-setting"></i>
              <span>运维</span>
            </el-menu-item>
            <el-menu-item index="/Admin/AiConfig">
              <i class="el-icon-chat-dot-round"></i>
              <span>AI助手配置</span>
            </el-menu-item>
            <el-menu-item index="/Admin/FeedbackList">
              <i class="el-icon-s-comment"></i>
              <span>反馈管理</span>
            </el-menu-item>
          </el-menu>
        </el-aside>

        <el-main class="admin-main">
          <el-breadcrumb
            separator-class="el-icon-arrow-right"
            class="margin-bottom-lg"
          >
            <el-breadcrumb-item
              v-for="(item, index) in breadList"
              :key="index"
              :to="item.path"
              >{{ item.meta.title }}</el-breadcrumb-item
            >
          </el-breadcrumb>

          <transition>
            <router-view></router-view>
          </transition>
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script>
import { adminRouters } from "@/router/index";
import { mapGetters } from "vuex";
import { ReplaceImageHttp } from "@/utils/comm";
export default {
  name: "Layout",
  data() {
    return {
      routerMenu: [],
      breadList: [],
    };
  },
  computed: {
    ...mapGetters(["UserInfo"]),
    avatarUrl() {
      return this.NormalizeImage(this.UserInfo && this.UserInfo.ImageUrls);
    },
  },
  watch: {
    $route() {
      this.getBreadcrumb();
    },
  },

  created() {
    this.routerMenu = adminRouters;
    this.getBreadcrumb();
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
          raw = Array.isArray(arr) ? arr[0] || "" : raw;
        } catch (e) {}
      }
      raw = String(raw)
        .split(",")[0]
        .trim()
        .replace(/^['\"]|['\"]$/g, "");
      return ReplaceImageHttp(raw);
    },

    async LoginOut() {
      console.log("点击退出");
      await this.$store.dispatch("Logout");
      this.$router.push(`/Login`);
    },
    isHome(route) {
      return route.path === "/Admin";
    },
    getBreadcrumb() {
      let matched = this.$route.matched;
      if (!this.isHome(matched[0])) {
        matched = [{ path: "/Admin", meta: { title: "控制台" } }].concat(
          matched,
        );
      }
      this.breadList = matched;
    },
  },
};
</script>

<style>
.admin-container {
  height: 100vh;
}

.admin-header-shell {
  text-align: center;
  line-height: 60px;
  padding: 0;
}

.admin-header {
  padding: 0 var(--lib-space-lg);
}

.header-brand,
.header-user {
  display: flex;
  align-items: center;
}

.brand-logo {
  height: 30px;
  object-fit: contain;
}

.brand-name {
  margin-left: var(--lib-space-sm);
  letter-spacing: 1px;
  font-weight: 600;
}

.user-dropdown-wrap {
  margin-left: var(--lib-space-sm);
}

.user-dropdown-link {
  color: #fff;
}

.admin-aside {
  background: var(--lib-bg-surface);
  border-right: 1px solid var(--lib-border);
}

.menu-list {
  height: calc(100vh - 60px);
  border-right: none;
  background: transparent;
}

.el-submenu__title,
.el-menu-item {
  text-align: left;
}

.admin-main {
  height: calc(100vh - 60px) !important;
  overflow-y: auto;
  padding: var(--lib-space-lg);
  background: rgba(255, 252, 247, 0.65);
}
</style>
