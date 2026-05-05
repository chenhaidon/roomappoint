<template>
    <div>
        <div class="card margin-top-lg">
            <div class="item-header">公告列表</div>
            <Pagination url="/Announcement/FrontList">
                <template v-slot:content="{ data }">
                    <div class="notice-list" v-if="data.length > 0">
                        <div class="notice-item" v-for="item in data" :key="item.Id" @click="ToDetail(item)">
                            <img v-if="item.Cover" class="notice-cover" :src="NormalizeImage(item.Cover)">
                            <div class="notice-main">
                                <div class="notice-title-row">
                                    <span class="notice-title">{{ item.Title }}</span>
                                    <el-tag v-if="item.IsTop == 1" size="mini" type="danger">置顶</el-tag>
                                </div>
                                <div class="notice-summary">{{ item.Summary }}</div>
                                <div class="notice-time">发布时间：{{ item.PublishTime || item.CreationTime }}</div>
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
    name: "AnnouncementList",
    components: {
        Pagination
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
        ToDetail(item) {
            this.$router.push({
                path: "/Front/AnnouncementDetail",
                query: {
                    Id: item.Id
                }
            })
        }
    }
}
</script>

<style scoped>
.notice-list {
    width: 100%;
    display: flex;
    flex-direction: column;
    gap: var(--lib-space-sm);
}

.notice-item {
    display: flex;
    gap: var(--lib-space-sm);
    border: 1px solid var(--lib-border);
    border-radius: var(--lib-radius-md);
    padding: var(--lib-space-sm);
    background: #fff;
    cursor: pointer;
}

.notice-cover {
    width: 180px;
    height: 110px;
    border-radius: 10px;
    object-fit: cover;
}

.notice-main {
    flex: 1;
    min-width: 0;
}

.notice-title-row {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: var(--lib-space-sm);
}

.notice-title {
    font-size: 18px;
    font-weight: 700;
    color: var(--lib-text-primary);
}

.notice-summary {
    margin-top: 8px;
    color: var(--lib-text-secondary);
    line-height: 1.8;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
}

.notice-time {
    margin-top: 8px;
    color: #8c8c8c;
    font-size: 12px;
}

.item-header {
    border-left: 4px solid var(--lib-wood);
    padding-left: var(--lib-space-sm);
    color: var(--lib-text-primary);
    font-weight: 700;
    letter-spacing: 1px;
}
</style>
