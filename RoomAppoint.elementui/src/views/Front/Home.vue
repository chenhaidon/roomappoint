<template>
    <div>

        <div class="margin-top-lg card home-carousel-card">
            <el-carousel :interval="5000" arrow="always" height="240px">
                <el-carousel-item v-for="item in BannerList" :key="item.Id">
                    <div class="banner-item-wrap">
                        <img :src="NormalizeImage(item.Cover)" class="banner-image">
                    </div>
                </el-carousel-item>
            </el-carousel>
        </div>

        <div class="card margin-top-lg">
            <div class="item-header notice-header-row">
                <span>最新公告</span>
                <el-button type="text" @click="ToAnnouncementList">查看更多</el-button>
            </div>
            <div class="notice-brief-list" v-if="AnnouncementList.length > 0">
                <div class="notice-brief-item" v-for="item in AnnouncementList" :key="item.Id" @click="ToAnnouncementDetail(item)">
                    <div class="notice-left">
                        <el-tag v-if="item.IsTop == 1" size="mini" type="danger">置顶</el-tag>
                        <span class="notice-brief-title">{{ item.Title }}</span>
                    </div>
                    <div class="notice-brief-time">{{ item.PublishTime || item.CreationTime }}</div>
                </div>
            </div>
            <el-empty v-else description="暂无公告"></el-empty>
        </div>

        <div class="card margin-top-lg">
            <div class="item-header">自习室列表</div>
            <Pagination url="/Room/List">
                <template v-slot:content="{ data }">
                    <div class="room-list">
                        <div class="room-item" v-for="item in data" :key="item.Id" @click="ToRoom(item)">
                            <img class="room-cover" :src="NormalizeImage(item.Cover)">
                            <div class="room-meta">
                                <span class="tit">{{ item.Name }}</span>
                            </div>
                        </div>

                    </div>
                </template>
            </Pagination>
        </div>


    </div>
</template>


<script>
import Pagination from "@/components/Pagination/PaginationBox.vue"
import { ReplaceImageHttp } from "@/utils/comm";
export default {
    components: {
        Pagination
    },
    data() {
        return {
            BannerList: [],
            RoomList: [],
            AnnouncementList: [],
            isMobile: false,
        }
    },
    created() {
        this.UpdateIsMobile();
        window.addEventListener('resize', this.UpdateIsMobile);

        this.BannerListApi();
        this.AnnouncementListApi();
    },
    beforeDestroy() {
        window.removeEventListener('resize', this.UpdateIsMobile);
    },
    methods: {
        UpdateIsMobile() {
            this.isMobile = window.matchMedia && window.matchMedia('(max-width: 768px)').matches;
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
        async BannerListApi() {
            const res = await this.$Post("/Banner/List", {});
            const Items = (res && res.Success && res.Data && res.Data.Items) ? res.Data.Items : [];
            this.BannerList = Items;
        },
        async AnnouncementListApi() {
            const res = await this.$Post("/Announcement/FrontList", { Limit: 5, Page: 1 });
            const Items = (res && res.Success && res.Data && res.Data.Items) ? res.Data.Items : [];
            this.AnnouncementList = Items;
        },
        ToAnnouncementList() {
            this.$router.push({ path: "/Front/AnnouncementList" });
        },
        ToAnnouncementDetail(item) {
            this.$router.push({
                path: "/Front/AnnouncementDetail",
                query: {
                    Id: item.Id
                }
            });
        },
        //跳转到自习室详情
        async ToRoom(item) {
            this.$router.push({
                path: "/Front/Room",
                query: {
                    RoomId: item.Id
                }
            })
        }
    },


}
</script>


<style scoped>
.home-carousel-card {
    padding: var(--lib-space-sm);
    position: sticky;
    top: 0;
    z-index: 10;
    background: #fff;
}

.banner-item-wrap {
    width: 100%;
    height: 100%;
    border-radius: var(--lib-radius-lg);
    overflow: visible;
    border: 1px solid var(--lib-border);
    display: flex;
    align-items: center;
    justify-content: center;
}

.banner-image {
    width: 100%;
    height: 100%;
    object-fit: contain;
}

.room-list {
    width: 100%;
    display: flex;
    flex-wrap: wrap;
    justify-content: center;
    gap: var(--lib-space-lg);
    margin-top: var(--lib-space-md);
}

.room-list .room-item {
    flex: 0 1 280px;
    min-width: 240px;
    max-width: 360px;
    cursor: pointer;
    border-radius: var(--lib-radius-lg);
    border: 1px solid var(--lib-border);
    background: #fff;
    box-shadow: var(--lib-shadow-sm);
    transition: transform .25s ease, box-shadow .25s ease;
    overflow: hidden;
}

.room-list .room-item:hover {
    transform: translateY(-4px);
    box-shadow: var(--lib-shadow-md);
}

.room-list .room-item .room-cover {
    width: 100%;
    height: 220px;
    object-fit: cover;
}

.room-meta {
    padding: var(--lib-space-sm) var(--lib-space-md);
}

.room-list .room-item .tit {
    color: var(--lib-text-primary);
    font-weight: 700;
    width: 100%;
    display: -webkit-box;
    -webkit-box-orient: vertical;
    -webkit-line-clamp: 2;
    overflow: hidden;
    text-align: center;
    text-overflow: ellipsis;
}

.item-header {
    border-left: 4px solid var(--lib-wood);
    padding-left: var(--lib-space-sm);
    color: var(--lib-text-primary);
    font-weight: 700;
    letter-spacing: 1px;
}

.notice-header-row {
    display: flex;
    align-items: center;
    justify-content: space-between;
}

.notice-brief-list {
    margin-top: var(--lib-space-md);
}

.notice-brief-item {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: var(--lib-space-sm);
    padding: 10px 0;
    border-bottom: 1px dashed var(--lib-border);
    cursor: pointer;
}

.notice-left {
    display: flex;
    align-items: center;
    gap: var(--lib-space-xs);
    min-width: 0;
}

.notice-brief-title {
    color: var(--lib-text-primary);
    font-weight: 600;
    display: -webkit-box;
    -webkit-line-clamp: 1;
    -webkit-box-orient: vertical;
    overflow: hidden;
}

.notice-brief-time {
    color: #909399;
    font-size: 12px;
    white-space: nowrap;
}

@media (max-width: 768px) {
    .room-list {
        gap: var(--lib-space-md);
    }

    .room-list .room-item {
        flex: 1 1 100%;
        min-width: 0;
        max-width: 100%;
    }

    .room-list .room-item .room-cover {
        height: 160px;
    }
}
</style>
