<template>
    <div>

        <div class="margin-top-lg card home-carousel-card">
            <el-carousel :interval="5000" arrow="always" height="400px">
                <el-carousel-item v-for="item in BannerList" :key="item.Id">
                    <div class="banner-item-wrap">
                        <img :src="NormalizeImage(item.Cover)" class="banner-image">
                    </div>
                </el-carousel-item>
            </el-carousel>
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
            RoomList: []
        }
    },
    created() {
        this.BannerListApi();
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
        async BannerListApi() {
            const res = await this.$Post("/Banner/List", {});
            const Items = (res && res.Success && res.Data && res.Data.Items) ? res.Data.Items : [];
            this.BannerList = Items;
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
}

.banner-item-wrap {
    width: 100%;
    height: 100%;
    border-radius: var(--lib-radius-lg);
    overflow: hidden;
    border: 1px solid var(--lib-border);
}

.banner-image {
    width: 100%;
    height: 100%;
    object-fit: cover;
}

.room-list {
    width: 100%;
    display: flex;
    flex-wrap: wrap;
    gap: var(--lib-space-lg);
    margin-top: var(--lib-space-md);
}

.room-list .room-item {
    flex: 1 1 280px;
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
</style>
