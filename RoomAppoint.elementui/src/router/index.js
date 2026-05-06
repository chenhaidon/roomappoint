import Vue from 'vue'
import VueRouter from 'vue-router'
import store from '@/store'
import { Message } from 'element-ui'
import { getToken, setToken, removeToken } from '@/utils/cache'
import NProgress from 'nprogress' // progress bar
import 'nprogress/nprogress.css' // progress bar style

Vue.use(VueRouter)
import AdminLayout from "@/views/Admin/Layout/index.vue"
import FrontLayout from "@/views/Front/Layout/index.vue"

const routes = [
    {
        path: "/", redirect: "/Front",
    },
    {
        path: "/Login", component: () => import("@/views/Login.vue")
    },
    {
        path: "/Register", component: () => import("@/views/Register.vue")
    },
    {
        path: "/ForgotPassword", component: () => import("@/views/ForgotPassword.vue")
    },

]

//前台的路由
export const frontRouters = [
    {
        path: "/Front",
        redirect: "/Front/Home",
        component: FrontLayout,
        meta: {
            title: "前台",
            isAdmin: false,
        },
        children: [
            {
                path: "/Front/Home",
                meta: {
                    isAdmin: false,
                },
                component: () => import("@/views/Front/Home")
            },
            {
                path: "/Front/Room",
                meta: {
                    title: "自习室详情",
                    isAdmin: false,
                },
                component: () => import("@/views/Front/Room")
            },
            {
                path: "/Front/ToOrder",
                meta: {
                    title: "预约下单",
                    isAdmin: false,
                },
                component: () => import("@/views/Front/ToOrder")
            },
            {
                path: "/Front/AnnouncementList",
                meta: {
                    title: "公告",
                    isAdmin: false,
                },
                component: () => import("@/views/Front/AnnouncementList")
            },
            {
                path: "/Front/AnnouncementDetail",
                meta: {
                    title: "公告详情",
                    isAdmin: false,
                },
                component: () => import("@/views/Front/AnnouncementDetail")
            },
            {
                path: "/Front/AppointRecordList",
                meta: {
                    title: "我的预约记录",
                    isAdmin: false,
                },
                component: () => import("@/views/Front/AppointRecordList")
            },
            {
                path: "/Front/IntegralList",
                meta: {
                    title: "我的积分记录",
                    isAdmin: false,
                },
                component: () => import("@/views/Front/IntegralList")
            },
            {
                path: "/Front/GiftMall",
                meta: {
                    title: "积分兑换礼品",
                    isAdmin: false,
                },
                component: () => import("@/views/Front/GiftMall")
            },
            {
                path: "/Front/GiftRedeemMyList",
                meta: {
                    title: "我的兑换记录",
                    isAdmin: false,
                },
                component: () => import("@/views/Front/GiftRedeemMyList")
            },
            {
                path: "/Front/UserPerson",
                meta: {
                    title: "我的个人信息",
                    isAdmin: false,
                },
                component: () => import("@/views/Front/UserPerson")
            },
            {
                path: "/Front/PasswordEdit",
                meta: {
                    title: "修改密码",
                    isAdmin: false,
                },
                component: () => import("@/views/Front/PasswordEdit")
            },
            {
                path: "/Front/FeedbackList",
                meta: {
                    title: "意见反馈",
                    isAdmin: false,
                },
                component: () => import("@/views/Front/FeedbackList")
            },
            {
                path: "/Front/Recharge",
                meta: {
                    title: "余额充值",
                    isAdmin: false,
                },
                component: () => import("@/views/Front/Recharge")
            },
            {
                path: "/Front/BalanceRecordList",
                meta: {
                    title: "余额明细",
                    isAdmin: false,
                },
                component: () => import("@/views/Front/BalanceRecordList")
            }


        ]
    }
]

export const adminRouters = [{
    path: "/Admin",
    redirect: "/Admin/Home",
    component: AdminLayout,
    meta: {
        title: "控制台",
        isAdmin: true,

    },
    children: [
        {
            path: "/Admin/Home",
            meta: {
                isAdmin: true,
            },
            component: () => import("@/views/Admin/Home")
        },
        {
            path: "/Admin/UserList",
            meta: {
                title: "用户信息",
                isAdmin: true,
            },
            component: () => import("@/views/Admin/UserList")
        },
        {
            path: "/Admin/UserPerson",
            meta: {
                title: "个人信息",
                isAdmin: true,
            },
            component: () => import("@/views/Admin/UserPerson")
        },
        {
            path: "/Admin/PasswordEdit",
            meta: {
                title: "修改密码",
                isAdmin: true,
            },
            component: () => import("@/views/Admin/PasswordEdit")
        },

        {
            path: "/Admin/AppointRecordList",
            meta: {
                title: "预约记录",
                isAdmin: true,
            },
            component: () => import("@/views/Admin/AppointRecordList")
        },

        {
            path: "/Admin/BannerList",
            meta: {
                title: "封面",
                isAdmin: true,
            },
            component: () => import("@/views/Admin/BannerList")
        },

        {
            path: "/Admin/IntegralList",
            meta: {
                title: "积分",
                isAdmin: true,
            },
            component: () => import("@/views/Admin/IntegralList")
        },

        {
            path: "/Admin/GiftList",
            meta: {
                title: "礼品",
                isAdmin: true,
            },
            component: () => import("@/views/Admin/GiftList")
        },
        {
            path: "/Admin/GiftRedeemList",
            meta: {
                title: "兑换记录",
                isAdmin: true,
            },
            component: () => import("@/views/Admin/GiftRedeemList")
        },

        {
            path: "/Admin/AnnouncementList",
            meta: {
                title: "公告",
                isAdmin: true,
            },
            component: () => import("@/views/Admin/AnnouncementList")
        },

        {
            path: "/Admin/RoomList",
            meta: {
                title: "自习室",
                isAdmin: true,
            },
            component: () => import("@/views/Admin/RoomList")
        },
        {
            path: "/Admin/SeatList",
            meta: {
                title: "座位",
                isAdmin: true,
            },
            component: () => import("@/views/Admin/SeatList")
        },
        {
            path: "/Admin/AppointRoomAppointStatusData",
            meta: {
                title: "自习室预约统计",
                isAdmin: true,
            },
            component: () => import("@/views/Admin/AppointRoomAppointStatusData")
        },
        {
            path: "/Admin/GetIntegralConsumeAndGainChart",
            meta: {
                title: "最近30天积分的获取和使用",
                isAdmin: true,
            },
            component: () => import("@/views/Admin/GetIntegralConsumeAndGainChart")
        }, {
            path: "/Admin/GetAppointRoomRealTimeData",
            meta: {
                title: "统计每个自习室实时在场的人数",
                isAdmin: true,
            },
            component: () => import("@/views/Admin/GetAppointRoomRealTimeData")
        },
        {
            path: "/Admin/Ops",
            meta: {
                title: "运维",
                isAdmin: true,
            },
            component: () => import("@/views/Admin/Ops")
        },
        {
            path: "/Admin/AiConfig",
            meta: {
                title: "AI助手配置",
                isAdmin: true,
            },
            component: () => import("@/views/Admin/AiConfig")
        },
        {
            path: "/Admin/FeedbackList",
            meta: {
                title: "反馈管理",
                isAdmin: true,
            },
            component: () => import("@/views/Admin/FeedbackList")
        },

    ]

},
]

const router = new VueRouter({
    routes: [...routes, ...frontRouters, ...adminRouters] // (缩写) 相当于 routes: routes
})


// 编写导航路由
router.beforeEach(async (to, from, next) => {

    NProgress.start()
    const hasToken = getToken()
    if (process.env.NODE_ENV !== 'production') {
        console.log(hasToken)
    }
    if (hasToken) {
        if (process.env.NODE_ENV !== 'production') {
            console.log("执行A", store.getters.HasUserInfo)
        }
        if (store.getters.HasUserInfo == false) {
            await store.dispatch('GetInfo');
        }
        //如果去往的页面是后台
        if (to.meta && to.meta.isAdmin) {
            if (store.getters.RoleType == "管理员") {
                next()
            }
            else {
                next({ path: "/" })
            }
        }
        else {
            if (store.getters.RoleType == "管理员") {
                next({ path: "/Admin" })
            }
            else {
                next()
            }
        }

    }
    else {
        if (to.meta && to.meta.isAdmin) {
            next({ path: "/Login" })
        }
        else {
            next()
        }
    }
    NProgress.done()

})
router.afterEach(() => {
    // finish progress bar
    NProgress.done()
})


export default router;